package com.generated.qualityTrace.repositories;

import com.generated.qualityTrace.constants.DefectSeverity;
import com.generated.qualityTrace.constants.DispositionStatus;
import com.generated.qualityTrace.models.DefectRecord;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class DefectRecordRepository {

  private final Map<Long, DefectRecord> store = new ConcurrentHashMap<>();
  private final AtomicLong idSeq = new AtomicLong(100);
  private final AtomicLong versionSeq = new AtomicLong(0);
  /** 不良记录变更版本：不良一登记/处置/关闭，旧收尾结论即失效。 */
  private volatile long changeVersion = versionSeq.incrementAndGet();

  public DefectRecordRepository() {
    seed();
  }

  private void seed() {
    // 批次1：严重不良已关闭 —— 不阻断
    put(new DefectRecord(1L, 1L, "划伤", 2, DefectSeverity.MAJOR, "周转碰撞",
        DispositionStatus.CLOSED));
    // 批次1：未关闭一般不良 —— 仅提示
    put(new DefectRecord(2L, 1L, "毛刺", 1, DefectSeverity.MINOR, "刀具磨损",
        DispositionStatus.IN_PROGRESS));
    // 批次2：严重不良未关闭 —— 阻断
    put(new DefectRecord(3L, 2L, "尺寸超差", 6, DefectSeverity.CRITICAL, "夹具偏移",
        DispositionStatus.OPEN));
    // 批次3：无不良；批次4（旧数据）：无不良
  }

  private void put(DefectRecord defect) {
    store.put(defect.getId(), defect);
  }

  public List<DefectRecord> findEntities() {
    return store.values().stream().sorted(Comparator.comparing(DefectRecord::getId)).toList();
  }

  public List<DefectRecord> findByBatchId(Long batchId) {
    return findEntities().stream().filter(d -> d.getBatchId().equals(batchId)).toList();
  }

  public List<Map<String, Object>> findAll() {
    return findEntities().stream().map(DefectRecordRepository::toMap).toList();
  }

  private static Map<String, Object> toMap(DefectRecord d) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", d.getId());
    m.put("batchId", d.getBatchId());
    m.put("defectType", d.getDefectType());
    m.put("severity", d.getSeverity() == null ? null : d.getSeverity().name());
    m.put("dispositionStatus", d.getDispositionStatus() == null ? null : d.getDispositionStatus().name());
    return m;
  }

  public DefectRecord save(DefectRecord defect) {
    if (defect.getId() == null) {
      defect.setId(idSeq.incrementAndGet());
    }
    store.put(defect.getId(), defect);
    changeVersion = versionSeq.incrementAndGet();
    return defect;
  }

  public long currentChangeVersion() {
    return changeVersion;
  }
}
