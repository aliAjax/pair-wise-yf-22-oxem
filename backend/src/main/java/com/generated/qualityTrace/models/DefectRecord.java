package com.generated.qualityTrace.models;

import com.generated.qualityTrace.constants.DefectSeverity;
import com.generated.qualityTrace.constants.DispositionStatus;

/** 不良记录：未关闭的 MAJOR/CRITICAL 会阻断批次放行。 */
public class DefectRecord {
  private Long id;
  private Long batchId;
  private String defectType;
  private Integer defectQty;
  private DefectSeverity severity;
  private String rootCause;
  private DispositionStatus dispositionStatus;

  public DefectRecord() {}

  public DefectRecord(Long id, Long batchId, String defectType, Integer defectQty,
                      DefectSeverity severity, String rootCause,
                      DispositionStatus dispositionStatus) {
    this.id = id;
    this.batchId = batchId;
    this.defectType = defectType;
    this.defectQty = defectQty;
    this.severity = severity;
    this.rootCause = rootCause;
    this.dispositionStatus = dispositionStatus;
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getBatchId() { return batchId; }
  public void setBatchId(Long batchId) { this.batchId = batchId; }
  public String getDefectType() { return defectType; }
  public void setDefectType(String defectType) { this.defectType = defectType; }
  public Integer getDefectQty() { return defectQty; }
  public void setDefectQty(Integer defectQty) { this.defectQty = defectQty; }
  public DefectSeverity getSeverity() { return severity; }
  public void setSeverity(DefectSeverity severity) { this.severity = severity; }
  public String getRootCause() { return rootCause; }
  public void setRootCause(String rootCause) { this.rootCause = rootCause; }
  public DispositionStatus getDispositionStatus() { return dispositionStatus; }
  public void setDispositionStatus(DispositionStatus dispositionStatus) { this.dispositionStatus = dispositionStatus; }

  /** 严重不良：MAJOR / CRITICAL。 */
  public boolean isCritical() {
    return severity == DefectSeverity.MAJOR || severity == DefectSeverity.CRITICAL;
  }

  /** 是否仍未关闭。 */
  public boolean isOpen() {
    return dispositionStatus != DispositionStatus.CLOSED;
  }
}
