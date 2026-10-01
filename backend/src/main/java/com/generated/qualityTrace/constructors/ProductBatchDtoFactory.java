package com.generated.qualityTrace.constructors;

import com.generated.qualityTrace.models.ProductBatch;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ProductBatchDtoFactory {

  public static Map<String, Object> create() {
    return Map.of("id", 1, "batchNo", "");
  }

  public static Map<String, Object> toView(ProductBatch b) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", b.getId());
    dto.put("batchNo", b.getBatchNo());
    dto.put("workOrderId", b.getWorkOrderId());
    dto.put("quantity", b.getQuantity());
    dto.put("materialLotNo", b.getMaterialLotNo());
    dto.put("batchStatus", b.getBatchStatus());
    return dto;
  }

  private ProductBatchDtoFactory() {}
}
