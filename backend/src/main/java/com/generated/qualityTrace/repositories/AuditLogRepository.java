package com.generated.qualityTrace.repositories;

import com.generated.qualityTrace.models.AuditLog;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 审计日志仓储。idempotencyKey 唯一：同一收尾请求重试/恢复时，
 * 已写过的审计动作不会重复落库。
 */
@Repository
public class AuditLogRepository {

  private final ConcurrentHashMap<Long, AuditLog> store = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, AuditLog> byIdempotencyKey = new ConcurrentHashMap<>();
  private final AtomicLong idSeq = new AtomicLong(1);

  /**
   * 幂等写入：key 已存在时直接返回旧记录（不新增）。
   */
  public AuditLog saveIdempotent(AuditLog log) {
    if (log.getIdempotencyKey() != null) {
      return byIdempotencyKey.computeIfAbsent(
          log.getIdempotencyKey(),
          k -> {
            log.setId(idSeq.getAndIncrement());
            store.put(log.getId(), log);
            return log;
          });
    }
    log.setId(idSeq.getAndIncrement());
    store.put(log.getId(), log);
    return log;
  }

  public Optional<AuditLog> findByIdempotencyKey(String key) {
    return Optional.ofNullable(byIdempotencyKey.get(key));
  }

  public List<AuditLog> findAll() {
    return store.values().stream().sorted(Comparator.comparing(AuditLog::getId)).toList();
  }
}
