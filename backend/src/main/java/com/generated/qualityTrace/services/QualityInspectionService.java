package com.generated.qualityTrace.services;

import com.generated.qualityTrace.constants.InspectionType;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.constructors.QualityInspectionDtoFactory;
import com.generated.qualityTrace.models.QualityInspection;
import com.generated.qualityTrace.repositories.QualityInspectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 质量检验服务。提交末检会改变批次质量数据版本，
 * 必须经 {@link FinalizationInvalidationService} 作废旧收尾结论。
 */
@Service
public class QualityInspectionService {

  private final QualityInspectionRepository repo;
  private final FinalizationInvalidationService invalidationService;
  private final AuditLogService auditLogService;

  public QualityInspectionService(QualityInspectionRepository repo,
                                  FinalizationInvalidationService invalidationService,
                                  AuditLogService auditLogService) {
    this.repo = repo;
    this.invalidationService = invalidationService;
    this.auditLogService = auditLogService;
  }

  public List<Map<String, Object>> list() {
    return repo.findAll();
  }

  /** 提交（含重录）末检：落库后旧收尾结论立即失效。 */
  public Map<String, Object> submitFinal(QualityInspection inspection) {
    inspection.setInspectionType(InspectionType.FINAL);
    QualityInspection saved = repo.save(inspection);
    auditLogService.record(inspection.getInspectorId(), LogTemplates.CREATE,
        "QUALITY_INSPECTION", String.valueOf(saved.getId()),
        "submit final inspection batch=" + saved.getBatchId()
            + ", result=" + saved.getResultStatus(),
        "inspection-create:" + saved.getId());
    invalidationService.invalidateByBatch(saved.getBatchId());
    return QualityInspectionDtoFactory.toView(saved);
  }
}
