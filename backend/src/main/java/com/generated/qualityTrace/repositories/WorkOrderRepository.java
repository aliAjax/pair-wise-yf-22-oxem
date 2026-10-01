package com.generated.qualityTrace.repositories;

import com.generated.qualityTrace.constants.WorkOrderStatus;
import com.generated.qualityTrace.models.WorkOrder;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** 工单数据访问。保留 findAll 投影供旧列表接口使用。 */
@Repository
public class WorkOrderRepository {

  private final Map<Long, WorkOrder> store = new ConcurrentHashMap<>();
  private final AtomicLong idSeq = new AtomicLong(100);

  public WorkOrderRepository() {
    seed();
  }

  private void seed() {
    store.put(1L, new WorkOrder(1L, "WO-20260920-01", "P-1001", "减速电机", 200, "L1",
        "2026-09-20T08:00:00", WorkOrderStatus.RUNNING));
    store.put(2L, new WorkOrder(2L, "WO-20260920-02", "P-1002", "传动轴", 120, "L2",
        "2026-09-21T08:00:00", WorkOrderStatus.PAUSED));
    // 旧数据升级场景：3 号工单的末检在检验项缺失的情况下被标成 PASS
    store.put(3L, new WorkOrder(3L, "WO-20260921-03", "P-1003", "法兰盘", 60, "L1",
        "2026-09-21T09:00:00", WorkOrderStatus.PAUSED));
  }

  public List<WorkOrder> findEntities() {
    return store.values().stream().sorted(java.util.Comparator.comparing(WorkOrder::getId)).toList();
  }

  /** 旧列表接口投影，字段命名沿用 ld-614 文档。 */
  public List<Map<String, Object>> findAll() {
    return findEntities().stream().map(WorkOrderRepository::toMap).toList();
  }

  private static Map<String, Object> toMap(WorkOrder w) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", w.getId());
    m.put("orderNo", w.getOrderNo());
    m.put("productCode", w.getProductCode());
    m.put("status", w.getStatus() == null ? null : w.getStatus().name());
    return m;
  }

  public Optional<WorkOrder> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  public Optional<WorkOrder> findByOrderNo(String orderNo) {
    return store.values().stream().filter(w -> w.getOrderNo().equals(orderNo)).findFirst();
  }

  public WorkOrder save(WorkOrder workOrder) {
    if (workOrder.getId() == null) {
      workOrder.setId(idSeq.incrementAndGet());
    }
    store.put(workOrder.getId(), workOrder);
    return workOrder;
  }

  public void updateStatus(Long id, WorkOrderStatus status) {
    WorkOrder w = store.get(id);
    if (w != null) {
      w.setStatus(status);
    }
  }
}
