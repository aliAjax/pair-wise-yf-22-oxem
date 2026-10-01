package com.generated.qualityTrace.repositories;

import com.generated.qualityTrace.constants.InspectionType;
import com.generated.qualityTrace.models.QualityInspection;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class QualityInspectionRepository {

  private final Map<Long, QualityInspection> store = new ConcurrentHashMap<>();
  private final AtomicLong idSeq = new AtomicLong(100);
  private final AtomicLong versionSeq = new AtomicLong(0);
  /** 检验记录变更版本：任何末检的新增/修改都会自增，用于收尾结论失效判定。 */
  private volatile long changeVersion = versionSeq.incrementAndGet();

  public QualityInspectionRepository() {
    seed();
  }

  private void seed() {
    // 工单1 批次1：末检合格
    saveSeed(new QualityInspection() {{
        setId(1L); setBatchId(1L); setInspectorId("u-qc-01");
        setInspectionType(InspectionType.FINAL); setStandardVersion("STD-2026-A");
        setResultStatus(com.generated.qualityTrace.constants.InspectionResultStatus.PASS);
        setInspectedAt("2026-09-20T17:00:00"); setRequiredItemCount(3);
    }});
    // 工单1 批次2：末检不合格
    saveSeed(new QualityInspection() {{
        setId(2L); setBatchId(2L); setInspectorId("u-qc-01");
        setInspectionType(InspectionType.FINAL); setStandardVersion("STD-2026-A");
        setResultStatus(com.generated.qualityTrace.constants.InspectionResultStatus.FAIL);
        setInspectedAt("2026-09-20T21:00:00"); setRequiredItemCount(3);
    }});
    // 工单2 批次3：末检要求复检
    saveSeed(new QualityInspection() {{
        setId(3L); setBatchId(3L); setInspectorId("u-qc-02");
        setInspectionType(InspectionType.FINAL); setStandardVersion("STD-2026-B");
        setResultStatus(com.generated.qualityTrace.constants.InspectionResultStatus.RECHECK);
        setInspectedAt("2026-09-21T16:00:00"); setRequiredItemCount(2);
    }});
    // 工单3 批次4：旧数据——末检被标 PASS，但 required_item_count 缺失，按未完成处理
    saveSeed(new QualityInspection() {{
        setId(4L); setBatchId(4L); setInspectorId("u-qc-02");
        setInspectionType(InspectionType.FINAL); setStandardVersion("STD-2025-OLD");
        setResultStatus(com.generated.qualityTrace.constants.InspectionResultStatus.PASS);
        setInspectedAt("2026-09-21T14:30:00"); setRequiredItemCount(null);
    }});
  }

  private void saveSeed(QualityInspection inspection) {
    store.put(inspection.getId(), inspection);
  }

  public List<QualityInspection> findEntities() {
    return store.values().stream().sorted(Comparator.comparing(QualityInspection::getId)).toList();
  }

  /** 取批次最新一条末检（按 inspectedAt / id 取最大）。 */
  public Optional<QualityInspection> findLatestFinalByBatchId(Long batchId) {
    return store.values().stream()
        .filter(i -> i.getBatchId().equals(batchId) && i.getInspectionType() == InspectionType.FINAL)
        .max(Comparator.comparing(QualityInspection::getInspectedAt, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(QualityInspection::getId));
  }

  public Optional<QualityInspection> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  public List<Map<String, Object>> findAll() {
    return findEntities().stream().map(QualityInspectionRepository::toMap).toList();
  }

  private static Map<String, Object> toMap(QualityInspection i) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", i.getId());
    m.put("batchId", i.getBatchId());
    m.put("inspectionType", i.getInspectionType() == null ? null : i.getInspectionType().name());
    m.put("resultStatus", i.getResultStatus() == null ? null : i.getResultStatus().name());
    return m;
  }

  public QualityInspection save(QualityInspection inspection) {
    if (inspection.getId() == null) {
      inspection.setId(idSeq.incrementAndGet());
    }
    store.put(inspection.getId(), inspection);
    if (inspection.getInspectionType() == InspectionType.FINAL) {
      changeVersion = versionSeq.incrementAndGet();
    }
    return inspection;
  }

  public long currentChangeVersion() {
    return changeVersion;
  }
}
