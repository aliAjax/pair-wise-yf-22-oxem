package com.generated.qualityTrace.services;

import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.models.AuditLog;
import com.generated.qualityTrace.repositories.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * 审计服务：所有写操作落审计。
 * 关键：写入按 idempotencyKey 幂等——收尾请求重试/从检查点恢复时，
 * FINALIZE_SUBMIT 等动作已写过就不再写第二遍（“重试不再写审计”）。
 */
@Service
public class AuditLogService {

  private final AuditLogRepository repository;

  public AuditLogService(AuditLogRepository repository) {
    this.repository = repository;
  }

  /** 幂等记录；key 已存在时返回旧记录，不产生新行。 */
  public AuditLog record(String actor, String action, String targetType, String targetId,
                         String detail, String idempotencyKey) {
    AuditLog log = new AuditLog(actor, action, targetType, targetId, detail,
        idempotencyKey, Instant.now().toString());
    return repository.saveIdempotent(log);
  }

  /** 收尾提交动作：同一 requestId 只记一次。 */
  public void recordFinalizationSubmit(String requestId, String orderNo, String operatorId,
                                       String dataVersion) {
    record(operatorId, LogTemplates.FINALIZE_SUBMIT, "WORK_ORDER", orderNo,
        String.format("workOrder=%s, request=%s, operator=%s, version=%s",
            orderNo, requestId, operatorId, dataVersion),
        "fin-submit:" + requestId);
  }

  /** 逐批结论：同一 requestId + batchIndex 只记一次，恢复时不重复。 */
  public void recordBatchConclusion(String requestId, String orderNo, String batchNo,
                                    String conclusion, int batchIndex, String operatorId) {
    record(operatorId, LogTemplates.FINALIZE_BATCH, "PRODUCT_BATCH", batchNo,
        String.format("workOrder=%s, batch=%s, conclusion=%s", orderNo, batchNo, conclusion),
        "fin-batch:" + requestId + ":" + batchIndex);
  }

  public void recordComplete(String requestId, String orderNo, String result, int blockers,
                             String operatorId) {
    record(operatorId, LogTemplates.FINALIZE_COMPLETE, "WORK_ORDER", orderNo,
        String.format("workOrder=%s, result=%s, blockers=%d, request=%s",
            orderNo, result, blockers, requestId),
        "fin-complete:" + requestId);
  }

  public void recordInvalidation(String requestId, String orderNo, String oldVersion,
                                 String newVersion, String operatorId) {
    record(operatorId, LogTemplates.FINALIZATION_INVALIDATED, "FINALIZATION_REQUEST", requestId,
        String.format("workOrder=%s, request=%s, oldVersion=%s, newVersion=%s",
            orderNo, requestId, oldVersion, newVersion),
        "fin-invalid:" + requestId + ":" + newVersion);
  }

  public List<AuditLog> list() {
    return repository.findAll();
  }
}
