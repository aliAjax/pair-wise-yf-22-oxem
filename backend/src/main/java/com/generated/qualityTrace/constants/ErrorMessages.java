package com.generated.qualityTrace.constants;

public final class ErrorMessages {
    public static final String AUTH_REQUIRED = "missing token";
    public static final String RBAC_DENIED = "role denied";

    /** 完工收尾相关错误消息 */
    public static final String CLOSING_WORK_ORDER_NOT_FOUND = "工单不存在，无法收尾";
    public static final String CLOSING_BATCHES_NOT_FOUND = "工单下无批次，无法收尾";
    public static final String CLOSING_WRITE_FAILED = "收尾写入失败，存在未完成批次，可提交恢复";
    public static final String CLOSING_RECOVERY_NOT_FOUND = "无待恢复的收尾结论";
    public static final String CLOSING_ALREADY_COMPLETE = "收尾结论已完成，无需恢复";
    public static final String CLOSING_INVALID_WORK_ORDER_NO = "工单号不能为空";

    /** 批次末检/不良原因模板 */
    public static final String CLOSING_BATCH_NO_FINAL = "批次 %s 无末检记录";
    public static final String CLOSING_BATCH_FINAL_NO_ITEMS = "批次 %s 末检缺检验项，按未完成处理";
    public static final String CLOSING_BATCH_FINAL_FAIL = "批次 %s 末检不合格（%s）";
    public static final String CLOSING_BATCH_FINAL_RECHECK = "批次 %s 末检需复检/让步接收，未放行";
    public static final String CLOSING_BATCH_OPEN_DEFECT = "批次 %s 存在未关闭不良：%s（%s，%s）";

    private ErrorMessages() {}
}
