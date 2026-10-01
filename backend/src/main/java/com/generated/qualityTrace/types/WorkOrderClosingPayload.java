package com.generated.qualityTrace.types;

/**
 * 完工收尾请求载荷。收尾以工单号为唯一入口，故载荷仅携带工单号。
 */
public record WorkOrderClosingPayload(String workOrderNo) {}
