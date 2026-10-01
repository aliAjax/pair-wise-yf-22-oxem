package com.generated.qualityTrace.models;

import com.generated.qualityTrace.constants.ItemResultStatus;

/** 检验项结果：决定 QualityInspection 结论。末检缺少检验项时按未完成处理。 */
public class InspectionItemResult {
  private Long id;
  private Long inspectionId;
  private String itemCode;
  private String itemName;
  private String measuredValue;
  private Double limitMin;
  private Double limitMax;
  private ItemResultStatus itemStatus;

  public InspectionItemResult() {}

  public InspectionItemResult(Long id, Long inspectionId, String itemCode, String itemName,
                              String measuredValue, Double limitMin, Double limitMax,
                              ItemResultStatus itemStatus) {
    this.id = id;
    this.inspectionId = inspectionId;
    this.itemCode = itemCode;
    this.itemName = itemName;
    this.measuredValue = measuredValue;
    this.limitMin = limitMin;
    this.limitMax = limitMax;
    this.itemStatus = itemStatus;
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getInspectionId() { return inspectionId; }
  public void setInspectionId(Long inspectionId) { this.inspectionId = inspectionId; }
  public String getItemCode() { return itemCode; }
  public void setItemCode(String itemCode) { this.itemCode = itemCode; }
  public String getItemName() { return itemName; }
  public void setItemName(String itemName) { this.itemName = itemName; }
  public String getMeasuredValue() { return measuredValue; }
  public void setMeasuredValue(String measuredValue) { this.measuredValue = measuredValue; }
  public Double getLimitMin() { return limitMin; }
  public void setLimitMin(Double limitMin) { this.limitMin = limitMin; }
  public Double getLimitMax() { return limitMax; }
  public void setLimitMax(Double limitMax) { this.limitMax = limitMax; }
  public ItemResultStatus getItemStatus() { return itemStatus; }
  public void setItemStatus(ItemResultStatus itemStatus) { this.itemStatus = itemStatus; }
}
