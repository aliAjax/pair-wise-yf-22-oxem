package com.generated.qualityTrace.repositories;

import com.generated.qualityTrace.models.BatchFinalization;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 逐批收尾结论仓储。每个 (requestId, batchIndex) 唯一，构成恢复检查点。
 * 写入失败后重试：已存在的批次直接跳过，从未完成批次继续，不重复落审计。
 */
@Repository
public class BatchFinalizationRepository {

  private record SlotKey(String requestId, int batchIndex) {}

  private final ConcurrentHashMap<SlotKey, BatchFinalization> store = new ConcurrentHashMap<>();
  private final AtomicLong idSeq = new AtomicLong(1);

  /**
   * 检查点写入：已完成的批次结论已存在时直接返回旧记录，不再执行任何副作用。
   * 返回值的 existed 标记用于跳过重复审计。
   */
  public SaveResult saveCheckpoint(BatchFinalization result) {
    SlotKey key = new SlotKey(result.getRequestId(), result.getBatchIndex());
    boolean[] existed = {false};
    BatchFinalization saved =
        store.compute(
            key,
            (k, old) -> {
              if (old != null) {
                existed[0] = true;
                return old;
              }
              if (result.getId() == null) {
                result.setId(idSeq.getAndIncrement());
              }
              return result;
            });
    return new SaveResult(saved, existed[0]);
  }

  public List<BatchFinalization> findByRequestId(String requestId) {
    return store.values().stream()
        .filter(b -> b.getRequestId().equals(requestId))
        .sorted(Comparator.comparingInt(BatchFinalization::getBatchIndex))
        .toList();
  }

  public int countByRequestId(String requestId) {
    return (int) store.keySet().stream().filter(k -> k.requestId().equals(requestId)).count();
  }

  /** 某请求下指定批次是否已有检查点。 */
  public boolean exists(String requestId, Long batchId) {
    return store.values().stream()
        .anyMatch(b -> b.getRequestId().equals(requestId) && b.getBatchId().equals(batchId));
  }

  public record SaveResult(BatchFinalization value, boolean existed) {}
}
