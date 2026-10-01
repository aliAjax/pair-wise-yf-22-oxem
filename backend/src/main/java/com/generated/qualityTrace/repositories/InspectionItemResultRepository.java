package com.generated.qualityTrace.repositories;

import com.generated.qualityTrace.constants.ItemResultStatus;
import com.generated.qualityTrace.models.InspectionItemResult;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InspectionItemResultRepository {

  private final Map<Long, InspectionItemResult> store = new ConcurrentHashMap<>();
  private final AtomicLong idSeq = new AtomicLong(100);
  private final AtomicLong versionSeq = new AtomicLong(0);
  /** 检验项变更版本：末检检验项一录/一改，旧收尾结论即失效。 */
  private volatile long changeVersion = versionSeq.incrementAndGet();

  public InspectionItemResultRepository() {
    seed();
  }

  private void seed() {
    // 批次1 末检(id=1)：3 项齐全且 OK
    put(new InspectionItemResult(1L, 1L, "DIM-A", "总长", "99.98", 99.90, 100.10, ItemResultStatus.OK));
    put(new InspectionItemResult(2L, 1L, "DIM-B", "外径", "25.01", 24.95, 25.05, ItemResultStatus.OK));
    put(new InspectionItemResult(3L, 1L, "SURF", "粗糙度", "Ra1.6", null, null, ItemResultStatus.OK));
    // 批次2 末检(id=2)：3 项齐全但有 NG（与 FAIL 结论一致）
    put(new InspectionItemResult(4L, 2L, "DIM-A", "总长", "100.40", 99.90, 100.10, ItemResultStatus.NG));
    put(new InspectionItemResult(5L, 2L, "DIM-B", "外径", "25.00", 24.95, 25.05, ItemResultStatus.OK));
    put(new InspectionItemResult(6L, 2L, "SURF", "粗糙度", "Ra1.5", null, null, ItemResultStatus.OK));
    // 批次3 末检(id=3)：2 项齐全，结论 RECHECK
    put(new InspectionItemResult(7L, 3L, "DIA", "孔径", "12.02", 11.98, 12.05, ItemResultStatus.OK));
    put(new InspectionItemResult(8L, 3L, "RUNOUT", "跳动", "0.09", 0.0, 0.08, ItemResultStatus.NG));
    // 批次4 末检(id=4)：旧数据升级后没有任何检验项行 —— 收尾时按未完成处理，不能当合格
  }

  private void put(InspectionItemResult item) {
    store.put(item.getId(), item);
  }

  public List<InspectionItemResult> findEntities() {
    return store.values().stream().sorted(Comparator.comparing(InspectionItemResult::getId)).toList();
  }

  public List<InspectionItemResult> findByInspectionId(Long inspectionId) {
    return findEntities().stream()
        .filter(i -> i.getInspectionId().equals(inspectionId))
        .sorted(Comparator.comparing(InspectionItemResult::getId))
        .toList();
  }

  public List<Map<String, Object>> findAll() {
    return findEntities().stream().map(InspectionItemResultRepository::toMap).toList();
  }

  private static Map<String, Object> toMap(InspectionItemResult i) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", i.getId());
    m.put("inspectionId", i.getInspectionId());
    m.put("itemCode", i.getItemCode());
    m.put("itemStatus", i.getItemStatus() == null ? null : i.getItemStatus().name());
    return m;
  }

  public InspectionItemResult save(InspectionItemResult item) {
    if (item.getId() == null) {
      item.setId(idSeq.incrementAndGet());
    }
    store.put(item.getId(), item);
    changeVersion = versionSeq.incrementAndGet();
    return item;
  }

  public long currentChangeVersion() {
    return changeVersion;
  }
}
