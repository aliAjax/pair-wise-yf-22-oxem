package com.generated.qualityTrace.constructors;

import com.generated.qualityTrace.models.InspectionItemResult;

import java.util.LinkedHashMap;
import java.util.Map;

public final class InspectionItemResultDtoFactory {

  public static Map<String, Object> create() {
    return Map.of("id", 1, "itemCode", "", "itemStatus", "PENDING");
  }

  public static Map<String, Object> toView(InspectionItemResult i) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", i.getId());
    dto.put("inspectionId", i.getInspectionId());
    dto.put("itemCode", i.getItemCode());
    dto.put("itemName", i.getItemName());
    dto.put("measuredValue", i.getMeasuredValue());
    dto.put("limitMin", i.getLimitMin());
    dto.put("limitMax", i.getLimitMax());
    dto.put("itemStatus", i.getItemStatus() == null ? null : i.getItemStatus().name());
    return dto;
  }

  private InspectionItemResultDtoFactory() {}
}
