package com.generated.qualityTrace.constructors;

import com.generated.qualityTrace.constants.StatusLabels;
import com.generated.qualityTrace.models.QualityInspection;

import java.util.LinkedHashMap;
import java.util.Map;

public final class QualityInspectionDtoFactory {

  public static Map<String, Object> create() {
    return Map.of("id", 1, "inspectionType", "FINAL", "resultStatus", "PENDING");
  }

  public static Map<String, Object> toView(QualityInspection i) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", i.getId());
    dto.put("batchId", i.getBatchId());
    dto.put("inspectorId", i.getInspectorId());
    dto.put("inspectionType", i.getInspectionType() == null ? null : i.getInspectionType().name());
    dto.put("standardVersion", i.getStandardVersion());
    dto.put("resultStatus", i.getResultStatus() == null ? null : i.getResultStatus().name());
    dto.put("resultStatusText",
        i.getResultStatus() == null ? null : StatusLabels.inspection(i.getResultStatus()));
    dto.put("requiredItemCount", i.getRequiredItemCount());
    dto.put("inspectedAt", i.getInspectedAt());
    return dto;
  }

  private QualityInspectionDtoFactory() {}
}
