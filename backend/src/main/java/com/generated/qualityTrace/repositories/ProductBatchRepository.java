package com.generated.qualityTrace.repositories;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class ProductBatchRepository {

    /** 批次号 -> 批次记录。收尾按工单号聚合其下所有批次。 */
    private final ConcurrentHashMap<String, Map<String, Object>> store = new ConcurrentHashMap<>();

    public ProductBatchRepository() {
        // WO-2026-0001：两个批次均正常
        seed("B-001", "WO-2026-0001", "500", "LOT-A1", "PRODUCED");
        seed("B-002", "WO-2026-0001", "500", "LOT-A2", "PRODUCED");
        // WO-2026-0002：B-003 末检不合格，B-004 有未关闭严重不良
        seed("B-003", "WO-2026-0002", "300", "LOT-B1", "PRODUCED");
        seed("B-004", "WO-2026-0002", "200", "LOT-B2", "PRODUCED");
        // WO-2026-0003：旧数据升级，B-005 末检缺检验项，B-006 无末检
        seed("B-005", "WO-2026-0003", "400", "LOT-C1", "PRODUCED");
        seed("B-006", "WO-2026-0003", "400", "LOT-C2", "PRODUCED");
    }

    private void seed(String batchNo, String workOrderNo, String quantity, String materialLotNo,
                     String batchStatus) {
        Map<String, Object> m = new HashMap<>();
        m.put("batchNo", batchNo);
        m.put("workOrderNo", workOrderNo);
        m.put("quantity", quantity);
        m.put("materialLotNo", materialLotNo);
        m.put("batchStatus", batchStatus);
        store.put(batchNo, m);
    }

    public List<Map<String, Object>> findAll() {
        return List.of(Map.of("id", 1, "name", "产品批次", "status", "READY"));
    }

    /** 按工单号查询批次，按批次号排序，保证收尾结论稳定。 */
    public List<Map<String, Object>> findByWorkOrderNo(String workOrderNo) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> m : store.values()) {
            if (workOrderNo.equals(m.get("workOrderNo"))) {
                list.add(m);
            }
        }
        list.sort(Comparator.comparing(m -> (String) m.get("batchNo")));
        return list;
    }

    public Optional<Map<String, Object>> findByBatchNo(String batchNo) {
        return Optional.ofNullable(store.get(batchNo));
    }
}
