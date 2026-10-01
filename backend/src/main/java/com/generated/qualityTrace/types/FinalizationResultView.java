package com.generated.qualityTrace.types;

import java.util.List;
import java.util.Map;

/**
 * 完工收尾响应：整体结论 + 逐批说明。
 *
 * @param decision     FINISHED（放行）/ PAUSED（有问题留在暂停）/ SUPERSEDED（旧结论失效）
 * @param workOrderStatus 决策后工单状态
 * @param duplicate    是否为并发重复请求取回的首次结论
 * @param resumed      是否为写入失败后从未完成批次恢复
 * @param stale        旧结论是否已被末检/不良变化作废
 * @param dataVersion  本次结论依据的数据版本
 * @param batches      逐批结论与中文说明
 */
public record FinalizationResultView(
    String requestId,
    String orderNo,
    String decision,
    String workOrderStatus,
    boolean duplicate,
    boolean resumed,
    boolean stale,
    String dataVersion,
    int totalBatches,
    int blockerCount,
    List<Map<String, Object>> batches) {}
