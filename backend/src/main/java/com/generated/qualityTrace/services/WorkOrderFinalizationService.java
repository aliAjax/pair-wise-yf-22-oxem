package com.generated.qualityTrace.services;

import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.constants.FinalizationStatus;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.constants.WorkOrderStatus;
import com.generated.qualityTrace.models.BatchFinalization;
import com.generated.qualityTrace.models.FinalizationRequest;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.models.QualityInspection;
import com.generated.qualityTrace.repositories.BatchFinalizationRepository;
import com.generated.qualityTrace.repositories.FinalizationRequestRepository;
import com.generated.qualityTrace.repositories.WorkOrderRepository;
import com.generated.qualityTrace.types.BusinessException;
import com.generated.qualityTrace.types.FinalizationResultView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工单完工收尾主编排。
 *
 * 语义：
 * 1. 提交工单号后汇总各批次的末检结论和未关闭不良；有问题工单留在 PAUSED，并逐批说明；全部通过才 FINISHED。
 * 2. 每次结论带数据版本；末检/检验项/不良一变，旧结论置 SUPERSEDED；重新提交按最新记录重算。
 * 3. 同一工单并发提交按工单加锁串行化，只产生一份结果，重复请求取回首次结论。
 * 4. 逐批写检查点；写入失败后凭同 requestId 从未完成批次恢复，恢复不重复写审计。
 */
@Service
public class WorkOrderFinalizationService {

  private static final Logger log = LoggerFactory.getLogger(WorkOrderFinalizationService.class);

  private final WorkOrderRepository workOrderRepository;
  private final FinalizationRequestRepository requestRepository;
  private final BatchFinalizationRepository batchRepository;
  private final BatchDataVersionService dataVersionService;
  private final BatchFinalizationEvaluator evaluator;
  private final AuditLogService auditLogService;
  private final FinalizationFailureGate failureGate;

  /** 按工单号分段加锁：不同工单不互斥，同一工单的同时提交串行化。 */
  private final Map<String, Object> orderLocks = new ConcurrentHashMap<>();

  public WorkOrderFinalizationService(WorkOrderRepository workOrderRepository,
                                      FinalizationRequestRepository requestRepository,
                                      BatchFinalizationRepository batchRepository,
                                      BatchDataVersionService dataVersionService,
                                      BatchFinalizationEvaluator evaluator,
                                      AuditLogService auditLogService,
                                      FinalizationFailureGate failureGate) {
    this.workOrderRepository = workOrderRepository;
    this.requestRepository = requestRepository;
    this.batchRepository = batchRepository;
    this.dataVersionService = dataVersionService;
    this.evaluator = evaluator;
    this.auditLogService = auditLogService;
    this.failureGate = failureGate;
  }

  public FinalizationResultView submit(String orderNo, String operatorId, String clientRequestId) {
    if (orderNo == null || orderNo.isBlank()) {
      throw new BusinessException(ErrorCodes.FINALIZATION_PARAM_INVALID,
          ErrorMessages.format(ErrorMessages.FINALIZATION_PARAM_INVALID, "orderNo 为空"));
    }
    var workOrder = workOrderRepository.findByOrderNo(orderNo)
        .orElseThrow(() -> new BusinessException(ErrorCodes.WORK_ORDER_NOT_FOUND,
            ErrorMessages.format(ErrorMessages.WORK_ORDER_NOT_FOUND, orderNo)));
    if (workOrder.getStatus() == WorkOrderStatus.FINISHED) {
      throw new BusinessException(ErrorCodes.WORK_ORDER_ALREADY_FINISHED,
          ErrorMessages.format(ErrorMessages.WORK_ORDER_ALREADY_FINISHED, orderNo));
    }
    if (workOrder.getStatus() != WorkOrderStatus.PAUSED && workOrder.getStatus() != WorkOrderStatus.RUNNING) {
      throw new BusinessException(ErrorCodes.WORK_ORDER_NOT_PAUSABLE,
          ErrorMessages.format(ErrorMessages.WORK_ORDER_NOT_PAUSABLE, workOrder.getStatus()));
    }

    String operator = (operatorId == null || operatorId.isBlank()) ? "line-supervisor" : operatorId;
    String currentVersion = dataVersionService.versionForWorkOrder(workOrder.getId());

    Object lock = orderLocks.computeIfAbsent(orderNo, k -> new Object());
    synchronized (lock) {
      Optional<FinalizationRequest> latest =
          requestRepository.findLatestByWorkOrderId(workOrder.getId());

      if (latest.isPresent()) {
        FinalizationRequest prior = latest.get();
        // 重复提交取回首次结论：仍有效（版本未变）的 FINISHED/PAUSED 直接返回
        if (prior.getStatus() != FinalizationStatus.SUPERSEDED
            && prior.getStatus() != FinalizationStatus.FAILED
            && currentVersion.equals(prior.getDataVersion())) {
          List<BatchFinalization> batches = batchRepository.findByRequestId(prior.getRequestId());
          return toView(prior, batches, true, false, false);
        }
        // 写入失败且数据未变：从未完成批次恢复，重试不再写审计
        if (prior.getStatus() == FinalizationStatus.FAILED
            && currentVersion.equals(prior.getDataVersion())) {
          return resume(prior, currentVersion);
        }
      }

      // 旧结论已失效（或无历史）：按最新记录重新计算，生成新的一份结果
      return computeFresh(workOrder.getId(), orderNo, operator, clientRequestId, currentVersion);
    }
  }

  private FinalizationResultView computeFresh(Long workOrderId, String orderNo, String operator,
                                              String clientRequestId, String currentVersion) {
    String requestId = (clientRequestId == null || clientRequestId.isBlank())
        ? "fin-" + Instant.now().toEpochMilli() + "-" + workOrderId
        : clientRequestId;
    if (requestRepository.findByRequestId(requestId).isPresent()) {
      FinalizationRequest existing = requestRepository.findByRequestId(requestId).orElseThrow();
      List<BatchFinalization> batches = batchRepository.findByRequestId(requestId);
      return toView(existing, batches, true, false,
          existing.getStatus() == FinalizationStatus.SUPERSEDED);
    }

    FinalizationRequest request = new FinalizationRequest();
    request.setRequestId(requestId);
    request.setWorkOrderId(workOrderId);
    request.setOrderNo(orderNo);
    request.setOperatorId(operator);
    request.setStatus(FinalizationStatus.IN_PROGRESS);
    request.setDataVersion(currentVersion);
    List<ProductBatch> batches = dataVersionService.batchesOfWorkOrder(workOrderId);
    request.setTotalBatchCount(batches.size());
    request.setCompletedBatchCount(0);
    request.setCreatedAt(Instant.now().toString());
    requestRepository.save(request);

    // 提交审计只写一次
    auditLogService.recordFinalizationSubmit(requestId, orderNo, operator, currentVersion);

    return runBatchLoop(request, batches, operator, false);
  }

  private FinalizationResultView resume(FinalizationRequest request, String currentVersion) {
    log.info(LogTemplates.FINALIZATION_RESUME, request.getOrderNo(), request.getRequestId(),
        request.getCompletedBatchCount());    request.setStatus(FinalizationStatus.IN_PROGRESS);
    request.setUpdatedAt(Instant.now().toString());
    requestRepository.save(request);
    List<ProductBatch> batches = dataVersionService.batchesOfWorkOrder(request.getWorkOrderId());
    // 恢复路径不调用 recordFinalizationSubmit：提交/逐批/完成审计均靠幂等键跳过已写部分
    return runBatchLoop(request, batches, request.getOperatorId(), true);
  }

  private FinalizationResultView runBatchLoop(FinalizationRequest request,
                                              List<ProductBatch> orderedBatches,
                                              String operator, boolean resumed) {
    String orderNo = request.getOrderNo();
    List<BatchFinalization> results = new ArrayList<>();
    try {
      for (int i = 0; i < orderedBatches.size(); i++) {
        ProductBatch batch = orderedBatches.get(i);

        // 检查点：该批次已完成则直接取回，绝不重复写入/重复审计
        Optional<BatchFinalization> done =
            batchRepository.findByRequestId(request.getRequestId()).stream()
                .filter(b -> b.getBatchId().equals(batch.getId()))
                .findFirst();
        if (done.isPresent()) {
          results.add(done.get());
          continue;
        }

        // 未完成批次：在写入前可能注入失败，模拟“写入失败后恢复”
        failureGate.checkpointBeforeWrite(orderNo, i);

        QualityInspection fin = dataVersionService.latestFinal(batch.getId());
        var items = dataVersionService.itemsOfLatestFinal(fin);
        var defects = dataVersionService.defectsOfBatch(batch.getId());
        BatchFinalization bf = evaluator.evaluate(batch, fin, items, defects, i,
            request.getRequestId());

        // 逻辑写入（检查点）+ 审计作为该批次的收尾动作；审计幂等键保证恢复不重复
        batchRepository.saveCheckpoint(bf);
        auditLogService.recordBatchConclusion(request.getRequestId(), orderNo, bf.getBatchNo(),
            bf.getConclusion().name(), i, operator);

        request.setCompletedBatchCount(i + 1);
        request.setUpdatedAt(Instant.now().toString());
        requestRepository.save(request);
        results.add(bf);
      }
      return finalize(request, results, operator, resumed);
    } catch (FinalizationWriteException e) {
      log.warn("finalization write failed, request={} will resume from checkpoint: {}",
          request.getRequestId(), e.getMessage());
      request.setStatus(FinalizationStatus.FAILED);
      request.setUpdatedAt(Instant.now().toString());
      requestRepository.save(request);
      throw e;
    }
  }

  private FinalizationResultView finalize(FinalizationRequest request,
                                          List<BatchFinalization> results,
                                          String operator, boolean resumed) {
    int blockers = (int) results.stream().filter(BatchFinalization::isBlocking).count();
    request.setBlockerCount(blockers);

    FinalizationStatus status;
    WorkOrderStatus orderStatus;
    if (blockers > 0) {
      // 有问题：留在暂停，逐批说明已在 explanation 中
      status = FinalizationStatus.PAUSED;
      orderStatus = WorkOrderStatus.PAUSED;
    } else {
      status = FinalizationStatus.FINISHED;
      orderStatus = WorkOrderStatus.FINISHED;
    }
    request.setStatus(status);
    request.setUpdatedAt(Instant.now().toString());
    requestRepository.save(request);
    workOrderRepository.updateStatus(request.getWorkOrderId(), orderStatus);

    auditLogService.recordComplete(request.getRequestId(), request.getOrderNo(),
        status.name(), blockers, operator);
    return toView(request, results, false, resumed, false);
  }

  private FinalizationResultView toView(FinalizationRequest request,
                                        List<BatchFinalization> batches,
                                        boolean duplicate, boolean resumed, boolean stale) {
    return com.generated.qualityTrace.constructors.FinalizationDtoFactory
        .resultView(request, batches, duplicate, resumed, stale);
  }

  public Optional<FinalizationRequest> findRequest(String requestId) {
    return requestRepository.findByRequestId(requestId);
  }

  public List<BatchFinalization> batchResults(String requestId) {
    return batchRepository.findByRequestId(requestId);
  }
}
