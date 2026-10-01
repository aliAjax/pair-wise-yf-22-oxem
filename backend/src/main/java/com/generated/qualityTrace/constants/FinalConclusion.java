package com.generated.qualityTrace.constants;

/**
 * 单批次末检结论。
 * PASS       —— 末检合格（检验项齐全且全部合格）。
 * FAIL       —— 末检不合格（存在不合格检验项或检验结论为 FAIL）。
 * RECHECK    —— 让步接收/需复检（CONDITIONAL_PASS / RECHECK），不予放行。
 * INCOMPLETE —— 末检缺失或缺检验项（旧数据升级），按未完成处理，不得判合格。
 */
public enum FinalConclusion {
    PASS,
    FAIL,
    RECHECK,
    INCOMPLETE
}
