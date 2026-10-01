package com.generated.qualityTrace.constants;

/** 错误码集中定义，service 与 controller 分别包装异常，禁止在单一位置吞掉。 */
public final class ErrorCodes {
  public static final String AUTH_REQUIRED = "AUTH_REQUIRED";
  public static final String RBAC_DENIED = "RBAC_DENIED";

  public static final String WORK_ORDER_NOT_FOUND = "WORK_ORDER_NOT_FOUND";
  public static final String WORK_ORDER_NOT_PAUSABLE = "WORK_ORDER_NOT_PAUSABLE";
  public static final String WORK_ORDER_ALREADY_FINISHED = "WORK_ORDER_ALREADY_FINISHED";
  public static final String BATCH_NOT_FOUND = "BATCH_NOT_FOUND";
  public static final String FINALIZATION_PARAM_INVALID = "FINALIZATION_PARAM_INVALID";
  public static final String FINALIZATION_STALE = "FINALIZATION_STALE";
  public static final String FINALIZATION_WRITE_FAILED = "FINALIZATION_WRITE_FAILED";

  private ErrorCodes() {}
}
