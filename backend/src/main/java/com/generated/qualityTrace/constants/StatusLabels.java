package com.generated.qualityTrace.constants;

import java.util.Locale;

/**
 * 状态文案/风险等级等展示文本。被 Formatters、DTO 工厂、控制器展示共同引用，
 * 新增枚举值时必须同步这里（牵一发动全身）。
 */
public final class StatusLabels {
  public static String workOrder(WorkOrderStatus status) {
    return switch (status) {
      case PLANNED -> "待开工";
      case RUNNING -> "生产中";
      case PAUSED -> "已暂停";
      case FINISHED -> "已完工";
      case CANCELLED -> "已取消";
    };
  }

  public static String inspection(InspectionResultStatus status) {
    return switch (status) {
      case PASS -> "合格";
      case FAIL -> "不合格";
      case CONDITIONAL_PASS -> "让步接收";
      case RECHECK -> "待复检";
    };
  }

  public static String batchFinal(BatchFinalStatus status) {
    return switch (status) {
      case PASS -> "通过";
      case FINAL_FAIL -> "末检不合格";
      case FINAL_RECHECK -> "末检待复检";
      case FINAL_MISSING -> "缺末检";
      case ITEMS_INCOMPLETE -> "检验项缺失";
      case CRITICAL_OPEN -> "严重不良未关闭";
    };
  }

  public static String finalization(FinalizationStatus status) {
    return switch (status) {
      case IN_PROGRESS -> "收尾中";
      case FINISHED -> "收尾通过";
      case PAUSED -> "留在暂停";
      case SUPERSEDED -> "结论已失效";
      case FAILED -> "写入失败";
    };
  }

  public static String severity(DefectSeverity severity) {
    return switch (severity) {
      case MINOR -> "一般";
      case MAJOR -> "主要";
      case CRITICAL -> "严重";
    };
  }

  public static String riskLevel(DefectSeverity severity) {
    return severity.name().toLowerCase(Locale.ROOT);
  }

  private StatusLabels() {}
}
