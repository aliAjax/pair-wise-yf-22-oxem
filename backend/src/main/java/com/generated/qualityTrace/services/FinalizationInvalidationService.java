package com.generated.qualityTrace.services;

import com.generated.qualityTrace.constants.FinalizationStatus;
import com.generated.qualityTrace.constants.WorkOrderStatus;
import com.generated.qualityTrace.models.FinalizationRequest;
import com.generated.qualityTrace.repositories.FinalizationRequestRepository;
import com.generated.qualityTrace.repositories.ProductBatchRepository;
import com.generated.qualityTrace.repositories.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 末检/检验项/不良发生变化后，让受影响工单的旧收尾结论失效。
 *
 * “末检或不良一变，旧的收尾结论就失效”：
 * - 已 FINISHED/PAUSED 的结论置 SUPERSEDED；
 * - 曾放行（FINISHED）的工单若结论失效，退回 PAUSED，等待重新提交按最新记录重算；
 * - 写一条 FINALIZATION_INVALIDATED 审计。
 */
@Service
public class FinalizationInvalidationService {

  private static final Logger log = LoggerFactory.getLogger(FinalizationInvalidationService.class);

  private final ProductBatchRepository batchRepository;
  private final FinalizationRequestRepository requestRepository;
  private final WorkOrderRepository workOrderRepository;
  private final BatchDataVersionService dataVersionService;
  private final AuditLogService auditLogService;

  public FinalizationInvalidationService(ProductBatchRepository batchRepository,
                                         FinalizationRequestRepository requestRepository,
                                         WorkOrderRepository workOrderRepository,
                                         BatchDataVersionService dataVersionService,
                                         AuditLogService auditLogService) {
    this.batchRepository = batchRepository;
    this.requestRepository = requestRepository;
    this.workOrderRepository = workOrderRepository;
    this.dataVersionService = dataVersionService;
    this.auditLogService = auditLogService;
  }

  /** 某批次相关质量数据变化，失效其工单上仍有效的收尾结论。 */
  public void invalidateByBatch(Long batchId) {
    batchRepository.findById(batchId).ifPresent(batch -> invalidateWorkOrder(batch.getWorkOrderId()));
  }

  public void invalidateWorkOrder(Long workOrderId) {
    String newVersion = dataVersionService.versionForWorkOrder(workOrderId);
    List<FinalizationRequest> stale =
        requestRepository.markStaleByWorkOrder(workOrderId, newVersion);
    for (FinalizationRequest r : stale) {
      log.info("finalization invalidated: workOrder={}, request={}, oldVersion={}, newVersion={}",
          r.getOrderNo(), r.getRequestId(), r.getDataVersion(), newVersion);
      auditLogService.recordInvalidation(r.getRequestId(), r.getOrderNo(),
          r.getDataVersion(), newVersion, r.getOperatorId());
      // 曾被放行的工单，结论失效后退回暂停，避免“带着已失效的合格结论流出”
      workOrderRepository.findById(workOrderId).ifPresent(wo -> {
        if (wo.getStatus() == WorkOrderStatus.FINISHED) {
          workOrderRepository.updateStatus(workOrderId, WorkOrderStatus.PAUSED);
        }
      });
    }
  }
}
