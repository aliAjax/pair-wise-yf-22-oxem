package com.generated.qualityTrace.constants;

import java.util.Locale;

/** 错误消息模板，与 {@link ErrorCodes} 一一对应；收尾逐批说明文案集中在 {@link FinalizationMessages}。 */
public final class ErrorMessages {
  public static final String AUTH_REQUIRED = "missing token";
  public static final String RBAC_DENIED = "role denied";

  public static final String WORK_ORDER_NOT_FOUND = "工单不存在: %s";
  public static final String WORK_ORDER_NOT_PAUSABLE = "工单当前状态 %s 不允许提交完工收尾";
  public static final String WORK_ORDER_ALREADY_FINISHED = "工单 %s 已完工，不能重复收尾";
  public static final String BATCH_NOT_FOUND = "批次不存在: %s";
  public static final String FINALIZATION_PARAM_INVALID = "完工收尾入参非法: %s";
  public static final String FINALIZATION_STALE = "收尾结论所依据的末检/不良数据已变化，旧结论失效，请重新提交";
  public static final String FINALIZATION_WRITE_FAILED = "收尾结果写入中断，批次 %s 未完成，请重新提交以恢复";

  public static String format(String template, Object... args) {
    return String.format(Locale.ROOT, template, args);
  }

  private ErrorMessages() {}
}
