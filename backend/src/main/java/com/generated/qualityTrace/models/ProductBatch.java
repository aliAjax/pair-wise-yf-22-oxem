package com.generated.qualityTrace.models;

/** 产品批次：关联末检与不良记录，是完工收尾逐批判定的主体。 */
public class ProductBatch {
  private Long id;
  private String batchNo;
  private Long workOrderId;
  private Integer quantity;
  private String materialLotNo;
  private String producedAt;
  private String batchStatus;

  public ProductBatch() {}

  public ProductBatch(Long id, String batchNo, Long workOrderId, Integer quantity,
                      String materialLotNo, String producedAt, String batchStatus) {
    this.id = id;
    this.batchNo = batchNo;
    this.workOrderId = workOrderId;
    this.quantity = quantity;
    this.materialLotNo = materialLotNo;
    this.producedAt = producedAt;
    this.batchStatus = batchStatus;
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getBatchNo() { return batchNo; }
  public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
  public Long getWorkOrderId() { return workOrderId; }
  public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
  public Integer getQuantity() { return quantity; }
  public void setQuantity(Integer quantity) { this.quantity = quantity; }
  public String getMaterialLotNo() { return materialLotNo; }
  public void setMaterialLotNo(String materialLotNo) { this.materialLotNo = materialLotNo; }
  public String getProducedAt() { return producedAt; }
  public void setProducedAt(String producedAt) { this.producedAt = producedAt; }
  public String getBatchStatus() { return batchStatus; }
  public void setBatchStatus(String batchStatus) { this.batchStatus = batchStatus; }
}
