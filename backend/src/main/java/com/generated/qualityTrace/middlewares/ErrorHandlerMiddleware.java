package com.generated.qualityTrace.middlewares;

import com.generated.qualityTrace.services.FinalizationWriteException;
import com.generated.qualityTrace.types.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全局异常处理（service/controller 已各自包装业务异常，这里只做统一出口）。
 * - BusinessException：4xx + 错误码
 * - FinalizationWriteException：500，提示可凭同 requestId 重新提交恢复
 */
@RestControllerAdvice
public class ErrorHandlerMiddleware {

  private static final Logger log = LoggerFactory.getLogger(ErrorHandlerMiddleware.class);

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
    log.warn("business error: code={}, message={}", ex.getCode(), ex.getMessage());
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("error", ex.getCode());
    body.put("message", ex.getMessage());
    HttpStatus status =
        switch (ex.getCode()) {
          case "WORK_ORDER_NOT_FOUND", "BATCH_NOT_FOUND" -> HttpStatus.NOT_FOUND;
          case "RBAC_DENIED" -> HttpStatus.FORBIDDEN;
          case "AUTH_REQUIRED" -> HttpStatus.UNAUTHORIZED;
          case "FINALIZATION_STALE" -> HttpStatus.CONFLICT;
          default -> HttpStatus.BAD_REQUEST;
        };
    return ResponseEntity.status(status).body(body);
  }

  @ExceptionHandler(FinalizationWriteException.class)
  public ResponseEntity<Map<String, Object>> handleWrite(FinalizationWriteException ex) {
    log.error("finalization write failure: {}", ex.getMessage());
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("error", "FINALIZATION_WRITE_FAILED");
    body.put("message", ex.getMessage());
    body.put("recoverable", true);
    body.put("hint", "使用相同 requestId 重新提交，将从未完成批次恢复且不重复写审计");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegal(IllegalArgumentException ex) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("error", "PARAM_INVALID");
    body.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }
}
