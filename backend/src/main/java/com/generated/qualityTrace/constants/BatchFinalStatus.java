package com.generated.qualityTrace.constants;

/**
 * 批次收尾结论（逐批说明使用）。
 *
 * PASS              末检通过且没有未关闭严重不良，可以放行
 * FINAL_FAIL        末检结论不合格
 * FINAL_RECHECK     末检结论为复检，尚未定论
 * FINAL_MISSING     该批次没有末检记录
 * ITEMS_INCOMPLETE  末检存在但检验项缺失/未录入完（旧数据升级场景），按未完成处理，不能当合格
 * CRITICAL_OPEN     存在未关闭的严重（MAJOR/CRITICAL）不良
 * 出现位置：constants、services、constructors、utils/formatters、errorMessages、日志模板、README。
 */
public enum BatchFinalStatus {
  PASS,
  FINAL_FAIL,
  FINAL_RECHECK,
  FINAL_MISSING,
  ITEMS_INCOMPLETE,
  CRITICAL_OPEN
}
