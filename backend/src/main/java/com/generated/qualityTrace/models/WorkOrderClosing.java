package com.generated.qualityTrace.models;

import java.util.List;
import java.util.Map;

/**
 * 工单完工收尾结论。
 *
 * 由工单号聚合各批次末检结论与未关闭不良得出。末检或不良记录一旦变化，
 * 旧结论即失效（valid=false），重新提交时按最新记录重算。
 *
 * 写入失败时以 PENDING 状态保留已完成批次，重试从未完成批次恢复，
 * 已写审计不再重复写入（auditWritten 标记）。
 */
public class WorkOrderClosing {
    public String id;
    public String workOrderNo;
    /** ClosingVerdict：QUALIFIED / UNQUALIFIED / INCOMPLETE */
    public String verdict;
    /** 工单是否需暂停（verdict 非 QUALIFIED 时为 true） */
    public boolean paused;
    public int batchCount;
    public int qualifiedBatchCount;
    /** 逐批收尾结论：batchNo / finalConclusion / unclosedDefects / reasons / qualified */
    public List<Map<String, Object>> batchSummaries;
    /** 最新记录指纹，用于判断结论是否仍有效 */
    public String fingerprint;
    /** 结论是否仍有效（末检/不良变化后置为 false） */
    public boolean valid;
    /** 审计是否已写入（恢复时据此跳过重写审计） */
    public boolean auditWritten;
    /** PENDING / COMPLETE：写入失败时为 PENDING，恢复后为 COMPLETE */
    public String status;
    public String createdAt;
    public String updatedAt;
}
