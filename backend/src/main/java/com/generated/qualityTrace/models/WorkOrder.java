package com.generated.qualityTrace.models;

import com.generated.qualityTrace.constants.WorkOrderStatus;

/** 生产工单。贯穿 model/repository/service/controller/route/构造器/日志。 */
public class WorkOrder {
  private Long id;
  private String orderNo;
  private String productCode;
  private String productName;
  private Integer plannedQty;
  private String lineCode;
  private String startAt;
  private WorkOrderStatus status;

  public WorkOrder() {}

  public WorkOrder(Long id, String orderNo, String productCode, String productName,
                   Integer plannedQty, String lineCode, String startAt, WorkOrderStatus status) {
    this.id = id;
    this.orderNo = orderNo;
    this.productCode = productCode;
    this.productName = productName;
    this.plannedQty = plannedQty;
    this.lineCode = lineCode;
    this.startAt = startAt;
    this.status = status;
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getOrderNo() { return orderNo; }
  public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
  public String getProductCode() { return productCode; }
  public void setProductCode(String productCode) { this.productCode = productCode; }
  public String getProductName() { return productName; }
  public void setProductName(String productName) { this.productName = productName; }
  public Integer getPlannedQty() { return plannedQty; }
  public void setPlannedQty(Integer plannedQty) { this.plannedQty = plannedQty; }
  public String getLineCode() { return lineCode; }
  public void setLineCode(String lineCode) { this.lineCode = lineCode; }
  public String getStartAt() { return startAt; }
  public void setStartAt(String startAt) { this.startAt = startAt; }
  public WorkOrderStatus getStatus() { return status; }
  public void setStatus(WorkOrderStatus status) { this.status = status; }
}
