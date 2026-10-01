package com.generated.qualityTrace.repositories;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InspectionItemResultRepository {

    /** 检验项记录。旧数据升级后，末检缺检验项的批次按未完成处理。 */
    private final ConcurrentHashMap<String, Map<String, Object>> store = new ConcurrentHashMap<>();

    public InspectionItemResultRepository() {
        // I-001 / I-002 / I-004 检验项齐全且合格；I-003 有一项不合格；I-005 无检验项
        seed("IR-001", "I-001", "DIM-01", "内径", "50.02", "49.95", "50.05", "PASS");
        seed("IR-002", "I-002", "DIM-01", "内径", "50.01", "49.95", "50.05", "PASS");
        seed("IR-003", "I-003", "DIM-01", "内径", "50.12", "49.95", "50.05", "FAIL");
        seed("IR-004", "I-004", "DIM-01", "内径", "50.00", "49.95", "50.05", "PASS");
    }

    private void seed(String itemNo, String inspectionNo, String itemCode, String itemName,
                      String measuredValue, String limitMin, String limitMax, String itemStatus) {
        Map<String, Object> m = new HashMap<>();
        m.put("itemNo", itemNo);
        m.put("inspectionNo", inspectionNo);
        m.put("itemCode", itemCode);
        m.put("itemName", itemName);
        m.put("measuredValue", measuredValue);
        m.put("limitMin", limitMin);
        m.put("limitMax", limitMax);
        m.put("itemStatus", itemStatus);
        store.put(itemNo, m);
    }

    public List<Map<String, Object>> findAll() {
        return List.of(Map.of("id", 1, "name", "检验项结果", "status", "READY"));
    }

    /** 按检验单查询检验项，按项号排序。 */
    public List<Map<String, Object>> findByInspectionNo(String inspectionNo) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> m : store.values()) {
            if (inspectionNo.equals(m.get("inspectionNo"))) {
                list.add(m);
            }
        }
        list.sort(Comparator.comparing(m -> (String) m.get("itemNo")));
        return list;
    }
}
