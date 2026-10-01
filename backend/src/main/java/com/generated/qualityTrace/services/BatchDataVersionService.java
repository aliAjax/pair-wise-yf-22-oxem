package com.generated.qualityTrace.services;

import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.models.InspectionItemResult;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.models.QualityInspection;
import com.generated.qualityTrace.repositories.DefectRecordRepository;
import com.generated.qualityTrace.repositories.InspectionItemResultRepository;
import com.generated.qualityTrace.repositories.ProductBatchRepository;
import com.generated.qualityTrace.repositories.QualityInspectionRepository;
import com.generated.qualityTrace.utils.FinalizationVersionCalculator;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 工单质量数据快照与版本。
 * 版本覆盖：各批次最新末检（结论/时间/要求项数）、末检检验项、不良记录（等级/处置/数量）。
 * 末检或不良一变版本即变。
 */
@Service
public class BatchDataVersionService {

  private final ProductBatchRepository batchRepository;
  private final QualityInspectionRepository inspectionRepository;
  private final InspectionItemResultRepository itemRepository;
  private final DefectRecordRepository defectRepository;

  public BatchDataVersionService(ProductBatchRepository batchRepository,
                                 QualityInspectionRepository inspectionRepository,
                                 InspectionItemResultRepository itemRepository,
                                 DefectRecordRepository defectRepository) {
    this.batchRepository = batchRepository;
    this.inspectionRepository = inspectionRepository;
    this.itemRepository = itemRepository;
    this.defectRepository = defectRepository;
  }

  public List<ProductBatch> batchesOfWorkOrder(Long workOrderId) {
    return batchRepository.findByWorkOrderId(workOrderId);
  }

  public QualityInspection latestFinal(Long batchId) {
    return inspectionRepository.findLatestFinalByBatchId(batchId).orElse(null);
  }

  public List<InspectionItemResult> itemsOfLatestFinal(QualityInspection finalInspection) {
    return finalInspection == null ? List.of()
        : itemRepository.findByInspectionId(finalInspection.getId());
  }

  public List<DefectRecord> defectsOfBatch(Long batchId) {
    return defectRepository.findByBatchId(batchId);
  }

  /** 计算工单当前质量数据版本（内容哈希）。 */
  public String versionForWorkOrder(Long workOrderId) {
    List<ProductBatch> batches = batchesOfWorkOrder(workOrderId);
    return FinalizationVersionCalculator.version(
        batches,
        this::latestFinal,
        finId -> itemRepository.findByInspectionId(finId),
        this::defectsOfBatch);
  }
}
