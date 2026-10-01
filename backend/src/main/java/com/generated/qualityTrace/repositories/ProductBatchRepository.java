package com.generated.qualityTrace.repositories;

import com.generated.qualityTrace.models.ProductBatch;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ProductBatchRepository {

  private final Map<Long, ProductBatch> store = new ConcurrentHashMap<>();
  private final AtomicLong idSeq = new AtomicLong(100);

  public ProductBatchRepository() {
    seed();
  }

  private void seed() {
    store.put(1L, new ProductBatch(1L, "B-0920-01-A", 1L, 100, "MAT-L-77",
        "2026-09-20T16:00:00", "PRODUCED"));
    store.put(2L, new ProductBatch(2L, "B-0920-01-B", 1L, 100, "MAT-L-78",
        "2026-09-20T20:00:00", "PRODUCED"));
    store.put(3L, new ProductBatch(3L, "B-0921-02-A", 2L, 120, "MAT-L-79",
        "2026-09-21T15:00:00", "PRODUCED"));
    store.put(4L, new ProductBatch(4L, "B-0921-03-A", 3L, 60, "MAT-L-80",
        "2026-09-21T14:00:00", "PRODUCED"));
  }

  public List<ProductBatch> findEntities() {
    return store.values().stream().sorted(java.util.Comparator.comparing(ProductBatch::getId)).toList();
  }

  public List<ProductBatch> findByWorkOrderId(Long workOrderId) {
    return findEntities().stream().filter(b -> b.getWorkOrderId().equals(workOrderId)).toList();
  }

  public Optional<ProductBatch> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  public List<Map<String, Object>> findAll() {
    return findEntities().stream().map(ProductBatchRepository::toMap).toList();
  }

  private static Map<String, Object> toMap(ProductBatch b) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", b.getId());
    m.put("batchNo", b.getBatchNo());
    m.put("workOrderId", b.getWorkOrderId());
    m.put("status", b.getBatchStatus());
    return m;
  }

  public ProductBatch save(ProductBatch batch) {
    if (batch.getId() == null) {
      batch.setId(idSeq.incrementAndGet());
    }
    store.put(batch.getId(), batch);
    return batch;
  }
}
