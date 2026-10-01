package com.generated.qualityTrace.models;

/** 审计日志：写操作必落。idempotencyKey 唯一，保证重试不再写审计。 */
public class AuditLog {
  private Long id;
  private String actor;
  private String action;
  private String targetType;
  private String targetId;
  private String detail;
  /** 幂等键，同一请求同一动作只会落一条。 */
  private String idempotencyKey;
  private String createdAt;

  public AuditLog() {}

  public AuditLog(String actor, String action, String targetType, String targetId,
                  String detail, String idempotencyKey, String createdAt) {
    this.actor = actor;
    this.action = action;
    this.targetType = targetType;
    this.targetId = targetId;
    this.detail = detail;
    this.idempotencyKey = idempotencyKey;
    this.createdAt = createdAt;
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getActor() { return actor; }
  public void setActor(String actor) { this.actor = actor; }
  public String getAction() { return action; }
  public void setAction(String action) { this.action = action; }
  public String getTargetType() { return targetType; }
  public void setTargetType(String targetType) { this.targetType = targetType; }
  public String getTargetId() { return targetId; }
  public void setTargetId(String targetId) { this.targetId = targetId; }
  public String getDetail() { return detail; }
  public void setDetail(String detail) { this.detail = detail; }
  public String getIdempotencyKey() { return idempotencyKey; }
  public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
  public String getCreatedAt() { return createdAt; }
  public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
