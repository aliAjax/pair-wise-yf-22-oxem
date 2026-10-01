package com.generated.qualityTrace.routes;

/** 工单相关路由。完工收尾挂在工单资源下：/api/work-orders/finalizations。 */
public final class WorkOrderRoutes {
  public static final String PATH = "/api/work-order";
  public static final String FINALIZATIONS_PATH = "/api/work-orders/finalizations";
  public static final String FINALIZATION_BY_ID_PATH = FINALIZATIONS_PATH + "/{requestId}";

  private WorkOrderRoutes() {}
}
