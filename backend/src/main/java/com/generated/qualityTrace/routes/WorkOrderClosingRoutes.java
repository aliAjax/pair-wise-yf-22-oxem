package com.generated.qualityTrace.routes;

/**
 * 工单完工收尾路由常量。
 */
public final class WorkOrderClosingRoutes {

    public static final String PATH = "/api/work-order";
    public static final String CLOSING = "/{orderNo}/closing";
    public static final String CLOSING_INVALIDATE = "/{orderNo}/closing/invalidate";
    public static final String CLOSING_RECOVER = "/{orderNo}/closing/recover";
    public static final String DEFECT_CLOSE = "/{orderNo}/defects/{defectNo}/close";

    private WorkOrderClosingRoutes() {}
}
