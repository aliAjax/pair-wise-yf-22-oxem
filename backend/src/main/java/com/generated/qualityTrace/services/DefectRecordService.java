package com.generated.qualityTrace.services;

import com.generated.qualityTrace.constants.DispositionStatus;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.constructors.DefectRecordDtoFactory;
import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.repositories.DefectRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 不良记录服务。登记不良或处置/关闭不良都会改变批次质量数据版本，
 * 必须作废旧收尾结论（“末检或不良一变，旧的收尾结论就失效”）。
 */
@Service
public class DefectRecordService {

  private final DefectRecordRepository repo;
  private final FinalizationInvalidationService invalidationService;
  private final AuditLogService auditLogService;

  public DefectRecordService(DefectRecordRepository repo,
                             FinalizationInvalidationService invalidationService,
                             AuditLogService auditLogService) {
    this.repo = repo;
    this.invalidationService = invalidationService;
    this.auditLogService = auditLogService;
  }

  public List<Map<String, Object>> list() {
    return repo.findAll();
  }

  /** 登记不良：反向影响批次放行，旧收尾结论立即失效。 */
  public Map<String, Object> register(DefectRecord defect) {
    if (defect.getDispositionStatus() == null) {
      defect.setDispositionStatus(DispositionStatus.OPEN);
    }
    DefectRecord saved = repo.save(defect);
    auditLogService.record("line", LogTemplates.CREATE, "DEFECT_RECORD",
        String.valueOf(saved.getId()),
        "register defect batch=" + saved.getBatchId() + ", severity=" + saved.getSeverity()
            + ", qty=" + saved.getDefectQty(),
        "defect-create:" + saved.getId());
    invalidationService.invalidateByBatch(saved.getBatchId());
    return DefectRecordDtoFactory.toView(saved);
  }

  /** 处置不良（OPEN/IN_PROGRESS/CLOSED）。关闭严重不良后需重新提交收尾。 */
  public Map<String, Object> dispose(Long defectId, DispositionStatus status) {
    DefectRecord defect = repo.findEntities().stream()
        .filter(d -> d.getId().equals(defectId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("defect not found: " + defectId));
    defect.setDispositionStatus(status);
    DefectRecord saved = repo.save(defect);
    auditLogService.record("qm", LogTemplates.STATUS, "DEFECT_RECORD",
        String.valueOf(saved.getId()),
        "dispose defect id=" + saved.getId() + ", status=" + status,
        "defect-dispose:" + saved.getId() + ":" + status.name());
    invalidationService.invalidateByBatch(saved.getBatchId());
    return DefectRecordDtoFactory.toView(saved);
  }
}
