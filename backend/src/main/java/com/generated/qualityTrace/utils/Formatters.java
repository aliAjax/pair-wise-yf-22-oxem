package com.generated.qualityTrace.utils;

public final class Formatters {

    private Formatters() {}

    public static String audit(String type, long id) {
        return type + "#" + id;
    }

    /** 收尾结论文案。 */
    public static String closingVerdict(String verdict) {
        if (verdict == null) {
            return "未收尾";
        }
        switch (verdict) {
            case "QUALIFIED":
                return "合格，可完工放行";
            case "UNQUALIFIED":
                return "不合格，工单暂停";
            case "INCOMPLETE":
                return "未完成，工单暂停";
            default:
                return verdict;
        }
    }

    /** 批次末检结论文案。 */
    public static String finalConclusion(String conclusion) {
        if (conclusion == null) {
            return "无末检";
        }
        switch (conclusion) {
            case "PASS":
                return "末检合格";
            case "FAIL":
                return "末检不合格";
            case "RECHECK":
                return "需复检/让步接收";
            case "INCOMPLETE":
                return "末检未完成（缺检验项）";
            default:
                return conclusion;
        }
    }

    /** 不良处置状态文案。 */
    public static String defectDisposition(String disposition) {
        if (disposition == null) {
            return "未知";
        }
        switch (disposition) {
            case "OPEN":
                return "未关闭";
            case "CLOSED":
                return "已关闭";
            case "REWORK":
                return "返工中";
            case "SCRAPPED":
                return "已报废";
            default:
                return disposition;
        }
    }

    /** 工单状态文案。 */
    public static String workOrderStatus(String status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case "PLANNED":
                return "已计划";
            case "RUNNING":
                return "生产中";
            case "PAUSED":
                return "已暂停";
            case "FINISHED":
                return "已完工";
            case "CANCELLED":
                return "已取消";
            default:
                return status;
        }
    }
}
