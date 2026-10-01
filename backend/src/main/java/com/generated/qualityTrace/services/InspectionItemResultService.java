package com.generated.qualityTrace.services;

import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.constructors.InspectionItemResultDtoFactory;
import com.generated.qualityTrace.models.InspectionItemResult;
import com.generated.qualityTrace.models.QualityInspection;
import com.generated.qualityTrace.repositories.InspectionItemResultRepository;
import com.generated.qualityTrace.repositories.QualityInspectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 检验项结果服务。末检检验项一录入/修改，版本变化，旧收尾结论失效
 * （旧数据升级场景：补录缺失检验项后需重新提交收尾）。
 */
@Service
public class InspectionItemResultService {

  private final InspectionItemResultRepository repo;
  private final QualityInspectionRepository inspectionRepository;
  private final FinalizationInvalidationService invalidationService;
  private final AuditLogService auditLogService;

  public InspectionItemResultService(InspectionItemResultRepository repo,
                                     QualityInspectionRepository inspectionRepository,
                                     FinalizationInvalidationService invalidationService,
                                     AuditLogService auditLogService) {
    this.repo = repo;
    this.inspectionRepository = inspectionRepository;
    this.invalidationService = invalidationService;
    this.auditLogService = auditLogService;
  }

  public List<Map<String, Object>> list() {
    return repo.findAll();
  }

  public Map<String, Object> record(InspectionItemResult item) {
    InspectionItemResult saved = repo.save(item);
    auditLogService.record("qc", LogTemplates.CREATE, "INSPECTION_ITEM_RESULT",
        String.valueOf(saved.getId()),
        "record item inspection=" + saved.getInspectionId() + ", code=" + saved.getItemCode()
            + ", status=" + saved.getItemStatus(),
        "item-create:" + saved.getId());
    inspectionRepository.findById(saved.getInspectionId())
        .map(QualityInspection::getBatchId)
        .ifPresent(invalidationService::invalidateByBatch);
    return InspectionItemResultDtoFactory.toView(saved);
  }
}
