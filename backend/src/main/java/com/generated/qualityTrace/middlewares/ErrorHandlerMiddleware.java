package com.generated.qualityTrace.middlewares;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.exceptions.ClosingException;

/**
 * 全局异常处理中间件。
 *
 * service/controller 分别包装异常后抛出，这里统一映射为响应：
 * ClosingException 按其错误码映射状态码，其余异常兜底 500。
 */
@RestControllerAdvice
public class ErrorHandlerMiddleware {

    @ExceptionHandler(ClosingException.class)
    public ResponseEntity<Map<String, Object>> handleClosing(ClosingException ex) {
        HttpStatus status = mapStatus(ex.getCode());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", ex.getCode());
        body.put("message", ex.getMessage());
        body.put("status", status.value());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArg(IllegalArgumentException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", ErrorCodes.CLOSING_INVALID_WORK_ORDER_NO);
        body.put("message", ex.getMessage());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", ErrorCodes.CLOSING_WRITE_FAILED);
        body.put("message", ex.getMessage());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private HttpStatus mapStatus(String code) {
        if (ErrorCodes.CLOSING_WORK_ORDER_NOT_FOUND.equals(code)
                || ErrorCodes.CLOSING_RECOVERY_NOT_FOUND.equals(code)) {
            return HttpStatus.NOT_FOUND;
        }
        if (ErrorCodes.CLOSING_INVALID_WORK_ORDER_NO.equals(code)) {
            return HttpStatus.BAD_REQUEST;
        }
        if (ErrorCodes.CLOSING_WRITE_FAILED.equals(code)) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}
