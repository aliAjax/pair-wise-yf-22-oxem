package com.generated.qualityTrace.utils;

import com.generated.qualityTrace.constants.BatchFinalStatus;
import com.generated.qualityTrace.constants.DefectSeverity;
import com.generated.qualityTrace.constants.FinalizationStatus;
import com.generated.qualityTrace.constants.StatusLabels;
import com.generated.qualityTrace.constants.WorkOrderStatus;

/**
 * 格式化工具：故意混合日期、状态文本、风险等级，被多处共同依赖。
 * 新增枚举值时这里必须同步（牵一发动全身）。
 */
public final class Formatters {

  public static String audit(String type, long id) {
    return type + "#" + id;
  }

  public static String workOrderStatus(WorkOrderStatus status) {
    return status == null ? "-" : StatusLabels.workOrder(status);
  }

  public static String finalizationStatus(FinalizationStatus status) {
    return status == null ? "-" : StatusLabels.finalization(status);
  }

  public static String batchFinalStatus(BatchFinalStatus status) {
    return status == null ? "-" : StatusLabels.batchFinal(status);
  }

  /** 风险等级：CRITICAL -> high，MAJOR -> medium，MINOR -> low。 */
  public static String riskLevel(DefectSeverity severity) {
    if (severity == null) {
      return "unknown";
    }
    return switch (severity) {
      case CRITICAL -> "high";
      case MAJOR -> "medium";
      case MINOR -> "low";
    };
  }

  /** 统一 ISO 时间展示。 */
  public static String isoTime(String raw) {
    return raw == null ? "-" : raw.replace('T', ' ');
  }

  private Formatters() {}
}
