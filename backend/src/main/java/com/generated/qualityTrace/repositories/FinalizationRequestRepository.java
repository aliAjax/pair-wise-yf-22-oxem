package com.generated.qualityTrace.repositories;

import com.generated.qualityTrace.constants.FinalizationStatus;
import com.generated.qualityTrace.models.FinalizationRequest;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 完工收尾请求仓储。
 * 同工单并发去重由 WorkOrderFinalizationService 的按工单锁保证：
 * 同时提交只落一份请求，后到者取回首次结论；写入失败后同 requestId 恢复。
 */
@Repository
public class FinalizationRequestRepository {

  private final Map<Long, FinalizationRequest> store = new ConcurrentHashMap<>();
  private final Map<String, FinalizationRequest> byRequestId = new ConcurrentHashMap<>();
  private final AtomicLong idSeq = new AtomicLong(1);

  public Optional<FinalizationRequest> findByRequestId(String requestId) {
    return Optional.ofNullable(byRequestId.get(requestId));
  }

  public List<FinalizationRequest> findByWorkOrderId(Long workOrderId) {
    return store.values().stream()
        .filter(r -> r.getWorkOrderId().equals(workOrderId))
        .sorted(Comparator.comparing(FinalizationRequest::getId))
        .toList();
  }

  /** 最近一次收尾请求（含已失效/失败的），用于判定重复提交与恢复。 */
  public Optional<FinalizationRequest> findLatestByWorkOrderId(Long workOrderId) {
    return findByWorkOrderId(workOrderId).stream().max(Comparator.comparing(FinalizationRequest::getId));
  }

  public FinalizationRequest save(FinalizationRequest request) {
    if (request.getId() == null) {
      request.setId(idSeq.getAndIncrement());
    }
    store.put(request.getId(), request);
    byRequestId.put(request.getRequestId(), request);
    return request;
  }

  /**
   * 数据变化后，把工单下仍有效的旧结论标记为失效（SUPERSEDED）。
   * 进行中或已失效的不动。返回被失效的请求。
   */
  public List<FinalizationRequest> markStaleByWorkOrder(Long workOrderId, String currentDataVersion) {
    return store.values().stream()
        .filter(r -> r.getWorkOrderId().equals(workOrderId)
            && r.getStatus() != FinalizationStatus.SUPERSEDED
            && r.getStatus() != FinalizationStatus.IN_PROGRESS
            && !java.util.Objects.equals(currentDataVersion, r.getDataVersion()))
        .peek(r -> r.setStatus(FinalizationStatus.SUPERSEDED))
        .toList();
  }
}
