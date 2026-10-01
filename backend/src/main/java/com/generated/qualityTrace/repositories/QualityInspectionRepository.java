package com.generated.qualityTrace.repositories;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class QualityInspectionRepository {

    /** 检验单号 -> 检验记录。收尾只取末检（FINAL）结论。 */
    private final ConcurrentHashMap<String, Map<String, Object>> store = new ConcurrentHashMap<>();

    public QualityInspectionRepository() {
        // WO-2026-0001：两个批次末检均 PASS
        seed("I-001", "B-001", "U-01", "FINAL", "V1", "PASS", "2026-09-01");
        seed("I-002", "B-002", "U-01", "FINAL", "V1", "PASS", "2026-09-01");
        // WO-2026-0002：B-003 末检 FAIL，B-004 末检 PASS
        seed("I-003", "B-003", "U-02", "FINAL", "V1", "FAIL", "2026-09-02");
        seed("I-004", "B-004", "U-02", "FINAL", "V1", "PASS", "2026-09-02");
        // WO-2026-0003：B-005 有末检记录但缺检验项（旧数据升级），B-006 无末检
        seed("I-005", "B-005", "U-03", "FINAL", "V1", "PASS", "2026-09-03");
    }

    private void seed(String inspectionNo, String batchNo, String inspectorId, String inspectionType,
                      String standardVersion, String resultStatus, String inspectedAt) {
        Map<String, Object> m = new HashMap<>();
        m.put("inspectionNo", inspectionNo);
        m.put("batchNo", batchNo);
        m.put("inspectorId", inspectorId);
        m.put("inspectionType", inspectionType);
        m.put("standardVersion", standardVersion);
        m.put("resultStatus", resultStatus);
        m.put("inspectedAt", inspectedAt);
        store.put(inspectionNo, m);
    }

    public List<Map<String, Object>> findAll() {
        return List.of(Map.of("id", 1, "name", "质量检验", "status", "READY"));
    }

    /** 按批次查询全部检验记录。 */
    public List<Map<String, Object>> findByBatchNo(String batchNo) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> m : store.values()) {
            if (batchNo.equals(m.get("batchNo"))) {
                list.add(m);
            }
        }
        list.sort(Comparator.comparing(m -> (String) m.get("inspectionNo")));
        return list;
    }

    /** 按批次查询末检（FINAL）记录，无则返回空。 */
    public Optional<Map<String, Object>> findFinalByBatchNo(String batchNo) {
        for (Map<String, Object> m : store.values()) {
            if (batchNo.equals(m.get("batchNo")) && "FINAL".equals(m.get("inspectionType"))) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    public Optional<Map<String, Object>> findByInspectionNo(String inspectionNo) {
        return Optional.ofNullable(store.get(inspectionNo));
    }
}
