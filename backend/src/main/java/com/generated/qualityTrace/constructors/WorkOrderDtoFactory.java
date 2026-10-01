package com.generated.qualityTrace.constructors;

import com.generated.qualityTrace.constants.StatusLabels;
import com.generated.qualityTrace.constants.WorkOrderStatus;
import com.generated.qualityTrace.models.WorkOrder;

import java.util.LinkedHashMap;
import java.util.Map;

/** 工单响应 DTO 构造器：controller/service 不得散写默认结构。 */
public final class WorkOrderDtoFactory {

  public static Map<String, Object> create() {
    return Map.of("id", 1, "orderNo", "", "status", WorkOrderStatus.PLANNED.name());
  }

  public static Map<String, Object> toView(WorkOrder w) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", w.getId());
    dto.put("orderNo", w.getOrderNo());
    dto.put("productCode", w.getProductCode());
    dto.put("productName", w.getProductName());
    dto.put("plannedQty", w.getPlannedQty());
    dto.put("lineCode", w.getLineCode());
    dto.put("status", w.getStatus() == null ? null : w.getStatus().name());
    dto.put("statusText", w.getStatus() == null ? null : StatusLabels.workOrder(w.getStatus()));
    return dto;
  }

  private WorkOrderDtoFactory() {}
}
