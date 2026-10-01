package com.generated.qualityTrace.constants;

/**
 * 工单完工收尾结论。
 * QUALIFIED   —— 各批次末检合格且无未关闭不良，可完工放行。
 * UNQUALIFIED —— 存在末检不合格或未关闭不良，工单暂停。
 * INCOMPLETE  —— 存在末检缺失/缺检验项的批次（旧数据升级），按未完成处理，工单暂停。
 */
public enum ClosingVerdict {
    QUALIFIED,
    UNQUALIFIED,
    INCOMPLETE
}
