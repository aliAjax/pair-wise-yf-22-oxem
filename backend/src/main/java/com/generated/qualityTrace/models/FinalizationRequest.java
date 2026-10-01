package com.generated.qualityTrace.models;

import com.generated.qualityTrace.constants.FinalizationStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * 完工收尾请求（一次“提交工单号”对应一条）。
 *
 * 幂等/并发语义：
 * - requestId 幂等键：两个人同时提交同一工单，只产生一条 IN_PROGRESS 请求，后到者取回首次结论。
 * - dataVersion：提交时依据的末检/不良数据版本；源数据变化后版本不再匹配，结论标记 SUPERSEDED。
 * - completedBatchCount：逐批写入检查点。写入失败后恢复时，跳过已完成批次，且不再重复写审计。
 */
public class FinalizationRequest {
  private Long id;
  private String requestId;
  private Long workOrderId;
  private String orderNo;
  private String operatorId;
  private FinalizationStatus status;
  private String dataVersion;
  private int totalBatchCount;
  /** 已完成写入的批次数（检查点）。 */
  private int completedBatchCount;
  private int blockerCount;
  private String createdAt;
  private String updatedAt;
  private final List<BatchFinalization> batchResults = new ArrayList<>();

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getRequestId() { return requestId; }
  public void setRequestId(String requestId) { this.requestId = requestId; }
  public Long getWorkOrderId() { return workOrderId; }
  public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
  public String getOrderNo() { return orderNo; }
  public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
  public String getOperatorId() { return operatorId; }
  public void setOperatorId(String operatorId) { this.operatorId = operatorId; }
  public FinalizationStatus getStatus() { return status; }
  public void setStatus(FinalizationStatus status) { this.status = status; }
  public String getDataVersion() { return dataVersion; }
  public void setDataVersion(String dataVersion) { this.dataVersion = dataVersion; }
  public int getTotalBatchCount() { return totalBatchCount; }
  public void setTotalBatchCount(int totalBatchCount) { this.totalBatchCount = totalBatchCount; }
  public int getCompletedBatchCount() { return completedBatchCount; }
  public void setCompletedBatchCount(int completedBatchCount) { this.completedBatchCount = completedBatchCount; }
  public int getBlockerCount() { return blockerCount; }
  public void setBlockerCount(int blockerCount) { this.blockerCount = blockerCount; }
  public String getCreatedAt() { return createdAt; }
  public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
  public String getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
  public List<BatchFinalization> getBatchResults() { return batchResults; }
}
