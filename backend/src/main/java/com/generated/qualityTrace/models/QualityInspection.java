package com.generated.qualityTrace.models;

import com.generated.qualityTrace.constants.InspectionResultStatus;
import com.generated.qualityTrace.constants.InspectionType;

/** 质量检验：首检/巡检/末检。完工收尾只取 inspectionType=FINAL 的最新一条。 */
public class QualityInspection {
  private Long id;
  private Long batchId;
  private String inspectorId;
  private InspectionType inspectionType;
  private String standardVersion;
  private InspectionResultStatus resultStatus;
  private String inspectedAt;
  /** 该标准版本要求的检验项数量；旧数据可能为 null（按缺失处理，不能当合格）。 */
  private Integer requiredItemCount;

  public QualityInspection() {}

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getBatchId() { return batchId; }
  public void setBatchId(Long batchId) { this.batchId = batchId; }
  public String getInspectorId() { return inspectorId; }
  public void setInspectorId(String inspectorId) { this.inspectorId = inspectorId; }
  public InspectionType getInspectionType() { return inspectionType; }
  public void setInspectionType(InspectionType inspectionType) { this.inspectionType = inspectionType; }
  public String getStandardVersion() { return standardVersion; }
  public void setStandardVersion(String standardVersion) { this.standardVersion = standardVersion; }
  public InspectionResultStatus getResultStatus() { return resultStatus; }
  public void setResultStatus(InspectionResultStatus resultStatus) { this.resultStatus = resultStatus; }
  public String getInspectedAt() { return inspectedAt; }
  public void setInspectedAt(String inspectedAt) { this.inspectedAt = inspectedAt; }
  public Integer getRequiredItemCount() { return requiredItemCount; }
  public void setRequiredItemCount(Integer requiredItemCount) { this.requiredItemCount = requiredItemCount; }
}
