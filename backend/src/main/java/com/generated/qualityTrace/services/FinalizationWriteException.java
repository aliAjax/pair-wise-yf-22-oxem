package com.generated.qualityTrace.services;

/** 收尾结果逐批写入中断（模拟或真实 IO 失败）。触发检查点恢复，不重复写审计。 */
public class FinalizationWriteException extends RuntimeException {
  public FinalizationWriteException(String message) {
    super(message);
  }

  public FinalizationWriteException(String message, Throwable cause) {
    super(message, cause);
  }
}
