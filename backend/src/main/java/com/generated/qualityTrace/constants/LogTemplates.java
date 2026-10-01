package com.generated.qualityTrace.constants;

/**
 * 日志模板集中存放。每个写操作都要落审计日志。
 * 字段变更时必须同步改模板与调用处（WorkOrderFinalizationService / AuditLogService）。
 */
public final class LogTemplates {
  public static final String CREATE = "create";
  public static final String UPDATE = "update";
  public static final String STATUS = "status";
  public static final String EXPORT = "export";

  /** 完工收尾：首次提交（每个 requestId 只允许写一次，重试不再写审计）。 */
  public static final String FINALIZE_SUBMIT =
      "finalization submit: workOrder=%s, request=%s, operator=%s, version=%s";
  /** 完工收尾：逐批汇总结果。 */
  public static final String FINALIZE_BATCH =
      "finalization batch: workOrder=%s, batch=%s, conclusion=%s";
  /** 完工收尾：最终落定（FINISHED / PAUSED）。 */
  public static final String FINALIZE_COMPLETE =
      "finalization complete: workOrder=%s, result=%s, blockers=%d, request=%s";
  /** 末检或不良变化导致旧收尾结论失效。 */
  public static final String FINALIZATION_INVALIDATED =
      "finalization invalidated: workOrder={}, request={}, oldVersion={}, newVersion={}";
  /** 写入失败后按检查点恢复，跳过已完成批次（不再写 FINALIZE_SUBMIT 审计）。 */
  public static final String FINALIZATION_RESUME =
      "finalization resume: workOrder={}, request={}, fromBatchIndex={}";

  private LogTemplates() {}
}
