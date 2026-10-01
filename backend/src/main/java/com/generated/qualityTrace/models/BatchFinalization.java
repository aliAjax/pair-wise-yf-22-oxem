package com.generated.qualityTrace.models;

import com.generated.qualityTrace.constants.BatchFinalStatus;

/** 单个批次的收尾结论与逐批说明（有问题就留在暂停，并逐批说明原因）。 */
public class BatchFinalization {
  private Long id;
  private String requestId;
  private Long batchId;
  private String batchNo;
  private BatchFinalStatus conclusion;
  /** 阻断放行的硬问题数量（末检问题 + 未关闭严重不良）。 */
  private int blockerCount;
  /** 未关闭严重不良条数（CRITICAL_OPEN 说明用）。 */
  private int openCriticalDefects;
  /** 未关闭一般不良条数（提示，不阻断）。 */
  private int openMinorDefects;
  private String finalInspectionStatus;
  private int recordedItemCount;
  private int requiredItemCount;
  /** 旧数据升级：末检没有 required_item_count 标准，按最低检验项门槛补齐判定。 */
  private boolean legacyMissingStandard;
  /** 面向车间主管的逐批中文说明。 */
  private String explanation;
  /** 批次序号（与检查点配合，恢复时决定从哪里继续）。 */
  private int batchIndex;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getRequestId() { return requestId; }
  public void setRequestId(String requestId) { this.requestId = requestId; }
  public Long getBatchId() { return batchId; }
  public void setBatchId(Long batchId) { this.batchId = batchId; }
  public String getBatchNo() { return batchNo; }
  public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
  public BatchFinalStatus getConclusion() { return conclusion; }
  public void setConclusion(BatchFinalStatus conclusion) { this.conclusion = conclusion; }
  public int getBlockerCount() { return blockerCount; }
  public void setBlockerCount(int blockerCount) { this.blockerCount = blockerCount; }
  public int getOpenCriticalDefects() { return openCriticalDefects; }
  public void setOpenCriticalDefects(int openCriticalDefects) { this.openCriticalDefects = openCriticalDefects; }
  public int getOpenMinorDefects() { return openMinorDefects; }
  public void setOpenMinorDefects(int openMinorDefects) { this.openMinorDefects = openMinorDefects; }
  public String getFinalInspectionStatus() { return finalInspectionStatus; }
  public void setFinalInspectionStatus(String finalInspectionStatus) { this.finalInspectionStatus = finalInspectionStatus; }
  public int getRecordedItemCount() { return recordedItemCount; }
  public void setRecordedItemCount(int recordedItemCount) { this.recordedItemCount = recordedItemCount; }
  public int getRequiredItemCount() { return requiredItemCount; }
  public void setRequiredItemCount(int requiredItemCount) { this.requiredItemCount = requiredItemCount; }
  public boolean isLegacyMissingStandard() { return legacyMissingStandard; }
  public void setLegacyMissingStandard(boolean legacyMissingStandard) { this.legacyMissingStandard = legacyMissingStandard; }
  public String getExplanation() { return explanation; }
  public void setExplanation(String explanation) { this.explanation = explanation; }
  public int getBatchIndex() { return batchIndex; }
  public void setBatchIndex(int batchIndex) { this.batchIndex = batchIndex; }

  public boolean isBlocking() {
    return conclusion != BatchFinalStatus.PASS;
  }
}
