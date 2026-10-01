package com.generated.qualityTrace.services;

import com.generated.qualityTrace.constructors.ProductBatchDtoFactory;
import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.models.InspectionItemResult;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.models.QualityInspection;
import com.generated.qualityTrace.repositories.ProductBatchRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductBatchService {

  private final ProductBatchRepository repo;
  private final BatchDataVersionService dataVersionService;

  public ProductBatchService(ProductBatchRepository repo,
                             BatchDataVersionService dataVersionService) {
    this.repo = repo;
    this.dataVersionService = dataVersionService;
  }

  public List<Map<String, Object>> list() {
    return repo.findEntities().stream().map(ProductBatchDtoFactory::toView).toList();
  }

  /** GET /api/trace/{batchNo}：批次全链路追溯树（批次 -> 末检/检验项/不良）。 */
  public Map<String, Object> trace(String batchNo) {
    ProductBatch batch = repo.findEntities().stream()
        .filter(b -> b.getBatchNo().equals(batchNo))
        .findFirst()
        .orElse(null);
    if (batch == null) {
      return Map.of("found", false, "batchNo", batchNo);
    }
    QualityInspection fin = dataVersionService.latestFinal(batch.getId());
    List<InspectionItemResult> items = dataVersionService.itemsOfLatestFinal(fin);
    List<DefectRecord> defects = dataVersionService.defectsOfBatch(batch.getId());

    Map<String, Object> root = new LinkedHashMap<>();
    root.put("found", true);
    root.put("batch", ProductBatchDtoFactory.toView(batch));
    root.put("finalInspection",
        fin == null ? null
            : com.generated.qualityTrace.constructors.QualityInspectionDtoFactory.toView(fin));
    root.put("inspectionItems",
        items.stream().map(com.generated.qualityTrace.constructors.InspectionItemResultDtoFactory::toView).toList());
    root.put("defects",
        defects.stream().map(com.generated.qualityTrace.constructors.DefectRecordDtoFactory::toView).toList());
    root.put("dataVersion",
        com.generated.qualityTrace.utils.FinalizationVersionCalculator.version(
            List.of(batch),
            id -> fin,
            id -> items,
            id -> defects));
    return root;
  }
}
