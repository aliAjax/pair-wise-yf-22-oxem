package com.generated.qualityTrace.repositories;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class WorkOrderRepository {

    /** 工单号 -> 工单记录。收尾只读取工单号与状态。 */
    private final ConcurrentHashMap<String, Map<String, Object>> store = new ConcurrentHashMap<>();

    public WorkOrderRepository() {
        seed("WO-2026-0001", "P-1001", "轴承座", "1000", "L-01", "RUNNING");
        seed("WO-2026-0002", "P-1002", "法兰盘", "500", "L-02", "RUNNING");
        seed("WO-2026-0003", "P-1003", "端盖", "800", "L-01", "RUNNING");
    }

    private void seed(String orderNo, String productCode, String productName, String plannedQty,
                      String lineCode, String status) {
        Map<String, Object> m = new HashMap<>();
        m.put("orderNo", orderNo);
        m.put("productCode", productCode);
        m.put("productName", productName);
        m.put("plannedQty", plannedQty);
        m.put("lineCode", lineCode);
        m.put("status", status);
        store.put(orderNo, m);
    }

    public List<Map<String, Object>> findAll() {
        return List.of(Map.of("id", 1, "name", "生产工单", "status", "READY"));
    }

    public Optional<Map<String, Object>> findByOrderNo(String orderNo) {
        return Optional.ofNullable(store.get(orderNo));
    }

    public boolean existsByOrderNo(String orderNo) {
        return store.containsKey(orderNo);
    }

    /** 完工收尾时回写工单状态（PAUSED / FINISHED）。 */
    public void updateStatus(String orderNo, String status) {
        Map<String, Object> m = store.get(orderNo);
        if (m != null) {
            m.put("status", status);
        }
    }

    public String statusOf(String orderNo) {
        Map<String, Object> m = store.get(orderNo);
        return m == null ? null : (String) m.get("status");
    }
}
