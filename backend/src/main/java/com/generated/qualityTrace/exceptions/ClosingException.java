package com.generated.qualityTrace.exceptions;

/**
 * 完工收尾业务异常。携带错误码与错误消息，由 controller 抛出、
 * ErrorHandlerMiddleware 统一包装为响应。
 */
public class ClosingException extends RuntimeException {

    private final String code;

    public ClosingException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
