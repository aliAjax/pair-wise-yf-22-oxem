package com.generated.qualityTrace.constructors;

import com.generated.qualityTrace.constants.BatchFinalStatus;
import com.generated.qualityTrace.constants.FinalizationStatus;
import com.generated.qualityTrace.constants.StatusLabels;
import com.generated.qualityTrace.models.BatchFinalization;
import com.generated.qualityTrace.models.FinalizationRequest;
import com.generated.qualityTrace.types.FinalizationResultView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 完工收尾响应 DTO 构造器（整体 + 逐批说明），禁止在 controller 散写结构。 */
public final class FinalizationDtoFactory {

  public static Map<String, Object> batchView(BatchFinalization b) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("batchId", b.getBatchId());
    dto.put("batchNo", b.getBatchNo());
    dto.put("batchIndex", b.getBatchIndex());
    dto.put("conclusion", b.getConclusion() == null ? null : b.getConclusion().name());
    dto.put("conclusionText",
        b.getConclusion() == null ? null : StatusLabels.batchFinal(b.getConclusion()));
    dto.put("blocking", b.isBlocking());
    dto.put("blockerCount", b.getBlockerCount());
    dto.put("finalInspectionStatus", b.getFinalInspectionStatus());
    dto.put("recordedItemCount", b.getRecordedItemCount());
    dto.put("requiredItemCount", b.getRequiredItemCount());
    dto.put("legacyMissingStandard", b.isLegacyMissingStandard());
    dto.put("openCriticalDefects", b.getOpenCriticalDefects());
    dto.put("openMinorDefects", b.getOpenMinorDefects());
    dto.put("explanation", b.getExplanation());
    return dto;
  }

  public static FinalizationResultView resultView(
      FinalizationRequest request,
      List<BatchFinalization> batches,
      boolean duplicate,
      boolean resumed,
      boolean stale) {
    List<Map<String, Object>> batchViews = batches.stream().map(FinalizationDtoFactory::batchView).toList();
    int blockingBatchCount = (int) batches.stream().filter(BatchFinalization::isBlocking).count();
    boolean blocking =
        request.getStatus() == FinalizationStatus.PAUSED
            || request.getStatus() == FinalizationStatus.SUPERSEDED
            || blockingBatchCount > 0;
    String decision =
        request.getStatus() == FinalizationStatus.SUPERSEDED
            ? FinalizationStatus.SUPERSEDED.name()
            : blocking ? FinalizationStatus.PAUSED.name() : FinalizationStatus.FINISHED.name();
    String workOrderStatus = blocking ? "PAUSED" : "FINISHED";
    return new FinalizationResultView(
        request.getRequestId(),
        request.getOrderNo(),
        decision,
        workOrderStatus,
        duplicate,
        resumed,
        stale,
        request.getDataVersion(),
        batches.size(),
        blockingBatchCount,
        batchViews);
  }

  public static Map<String, Object> requestView(FinalizationRequest r) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("requestId", r.getRequestId());
    dto.put("orderNo", r.getOrderNo());
    dto.put("operatorId", r.getOperatorId());
    dto.put("status", r.getStatus() == null ? null : r.getStatus().name());
    dto.put("statusText", r.getStatus() == null ? null : StatusLabels.finalization(r.getStatus()));
    dto.put("dataVersion", r.getDataVersion());
    dto.put("totalBatchCount", r.getTotalBatchCount());
    dto.put("completedBatchCount", r.getCompletedBatchCount());
    dto.put("blockerCount", r.getBlockerCount());
    return dto;
  }

  private FinalizationDtoFactory() {}
}
