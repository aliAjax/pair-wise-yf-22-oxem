package com.generated.qualityTrace.constructors;

import com.generated.qualityTrace.constants.StatusLabels;
import com.generated.qualityTrace.models.DefectRecord;

import java.util.LinkedHashMap;
import java.util.Map;

public final class DefectRecordDtoFactory {

  public static Map<String, Object> create() {
    return Map.of("id", 1, "severity", "MINOR", "dispositionStatus", "OPEN");
  }

  public static Map<String, Object> toView(DefectRecord d) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", d.getId());
    dto.put("batchId", d.getBatchId());
    dto.put("defectType", d.getDefectType());
    dto.put("defectQty", d.getDefectQty());
    dto.put("severity", d.getSeverity() == null ? null : d.getSeverity().name());
    dto.put("severityText", d.getSeverity() == null ? null : StatusLabels.severity(d.getSeverity()));
    dto.put("rootCause", d.getRootCause());
    dto.put("dispositionStatus",
        d.getDispositionStatus() == null ? null : d.getDispositionStatus().name());
    return dto;
  }

  private DefectRecordDtoFactory() {}
}
