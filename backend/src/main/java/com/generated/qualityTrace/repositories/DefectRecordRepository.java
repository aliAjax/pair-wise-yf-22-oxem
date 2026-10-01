package com.generated.qualityTrace.repositories;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class DefectRecordRepository {

    /** 不良记录。收尾只统计未关闭（disposition_status != CLOSED）的不良。 */
    private final ConcurrentHashMap<String, Map<String, Object>> store = new ConcurrentHashMap<>();

    public DefectRecordRepository() {
        // B-004 有一条未关闭的严重不良；B-003 的不良已关闭
        seed("D-001", "B-004", "划伤", "5", "CRITICAL", "模具磨损", "OPEN");
        seed("D-002", "B-003", "磕碰", "2", "MAJOR", "操作不当", "CLOSED");
    }

    private void seed(String defectNo, String batchNo, String defectType, String defectQty,
                      String severity, String rootCause, String dispositionStatus) {
        Map<String, Object> m = new HashMap<>();
        m.put("defectNo", defectNo);
        m.put("batchNo", batchNo);
        m.put("defectType", defectType);
        m.put("defectQty", defectQty);
        m.put("severity", severity);
        m.put("rootCause", rootCause);
        m.put("dispositionStatus", dispositionStatus);
        store.put(defectNo, m);
    }

    public List<Map<String, Object>> findAll() {
        return List.of(Map.of("id", 1, "name", "不良记录", "status", "READY"));
    }

    /** 按批次查询全部不良，按不良号排序。 */
    public List<Map<String, Object>> findByBatchNo(String batchNo) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> m : store.values()) {
            if (batchNo.equals(m.get("batchNo"))) {
                list.add(m);
            }
        }
        list.sort(Comparator.comparing(m -> (String) m.get("defectNo")));
        return list;
    }

    /** 按批次查询未关闭不良（disposition_status != CLOSED）。 */
    public List<Map<String, Object>> findUnclosedByBatchNo(String batchNo) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> m : store.values()) {
            if (batchNo.equals(m.get("batchNo")) && !"CLOSED".equals(m.get("dispositionStatus"))) {
                list.add(m);
            }
        }
        list.sort(Comparator.comparing(m -> (String) m.get("defectNo")));
        return list;
    }

    public Optional<Map<String, Object>> findByDefectNo(String defectNo) {
        return Optional.ofNullable(store.get(defectNo));
    }

    /** 关闭不良（处置状态置为 CLOSED）。返回是否命中。 */
    public boolean closeDefect(String defectNo) {
        Map<String, Object> m = store.get(defectNo);
        if (m == null) {
            return false;
        }
        m.put("dispositionStatus", "CLOSED");
        return true;
    }
}
