package com.generated.qualityTrace.types;

import com.generated.qualityTrace.constants.ErrorCodes;

/** 业务异常：service/controller 分别包装，错误码集中于 {@link ErrorCodes}。 */
public class BusinessException extends RuntimeException {
  private final String code;

  public BusinessException(String code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
