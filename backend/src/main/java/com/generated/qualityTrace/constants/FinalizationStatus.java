package com.generated.qualityTrace.constants;

/**
 * 工单完工收尾请求的生命周期状态。
 *
 * IN_PROGRESS 首次提交后正在逐批汇总/写入
 * FINISHED    所有批次满足放行条件，工单已完工
 * PAUSED      有批次存在问题，工单留在暂停（不会被放走）
 * SUPERSEDED  所依据的末检/不良数据已变化，旧结论失效；重新提交必须按最新记录重算
 * FAILED      写入中断，可凭检查点恢复
 * 出现位置：constants、models、services、constructors、errorMessages、logTemplates、README。
 */
public enum FinalizationStatus {
  IN_PROGRESS,
  FINISHED,
  PAUSED,
  SUPERSEDED,
  FAILED
}
