package com.generated.qualityTrace.controllers;

import com.generated.qualityTrace.models.AuditLog;
import com.generated.qualityTrace.services.AuditLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 审计查询：验证“重试不再写审计”等幂等行为。 */
@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

  private final AuditLogService service;

  public AuditLogController(AuditLogService service) {
    this.service = service;
  }

  @GetMapping
  public List<Map<String, Object>> list() {
    return service.list().stream().map(AuditLogController::toView).toList();
  }

  private static Map<String, Object> toView(AuditLog log) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", log.getId());
    dto.put("actor", log.getActor());
    dto.put("action", log.getAction());
    dto.put("targetType", log.getTargetType());
    dto.put("targetId", log.getTargetId());
    dto.put("detail", log.getDetail());
    dto.put("idempotencyKey", log.getIdempotencyKey());
    dto.put("createdAt", log.getCreatedAt());
    return dto;
  }
}
