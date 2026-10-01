package com.generated.qualityTrace.constants;

/**
 * 检验类型：首检 / 巡检 / 末检（批次最终检验）。
 * 出现位置：constants、types、models、repositories、services、validators、logTemplates、展示 DTO。
 */
public enum InspectionType {
  FIRST,
  PATROL,
  /** 末检（批次最终检验），完工收尾只看这一类型。 */
  FINAL
}
