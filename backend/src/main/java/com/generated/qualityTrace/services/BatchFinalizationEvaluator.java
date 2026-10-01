package com.generated.qualityTrace.services;

import com.generated.qualityTrace.constants.BatchFinalStatus;
import com.generated.qualityTrace.constants.DefectSeverity;
import com.generated.qualityTrace.constants.FinalizationMessages;
import com.generated.qualityTrace.constants.InspectionResultStatus;
import com.generated.qualityTrace.constants.StatusLabels;
import com.generated.qualityTrace.models.BatchFinalization;
import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.models.InspectionItemResult;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.models.QualityInspection;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 单批次完工判定：汇总“末检结论”和“未关闭不良”。
 *
 * 放行条件（全部满足才 PASS）：
 * 1. 存在末检记录；
 * 2. 末检结论为 PASS（CONDITIONAL_PASS 需质量经理单独让步，收尾不当合格；FAIL/RECHECK 阻断）；
 * 3. 末检检验项齐全：requiredItemCount 为空（旧数据升级）或已录项数少于要求项数，按未完成处理，不能当合格；
 * 4. 没有未关闭的 MAJOR/CRITICAL 严重不良。
 * MINOR 未关闭仅提示，不阻断完工。
 */
@Service
public class BatchFinalizationEvaluator {

  /** 旧数据升级：没有 required_item_count 时至少要有检验项行；末检标 PASS 但 0 项 => 未完成。 */
  static final int LEGACY_MIN_ITEM_COUNT = 1;

  public BatchFinalization evaluate(ProductBatch batch, QualityInspection finalInspection,
                                    List<InspectionItemResult> items,
                                    List<DefectRecord> defects, int batchIndex,
                                    String requestId) {
    BatchFinalization result = new BatchFinalization();
    result.setRequestId(requestId);
    result.setBatchId(batch.getId());
    result.setBatchNo(batch.getBatchNo());
    result.setBatchIndex(batchIndex);
    result.setFinalInspectionStatus(null);
    result.setRequiredItemCount(0);
    result.setRecordedItemCount(items == null ? 0 : items.size());

    int openCritical = 0;
    int openMinor = 0;
    StringBuilder criticalKinds = new StringBuilder();
    for (DefectRecord d : defects) {
      if (!d.isOpen()) {
        continue;
      }
      if (d.isCritical()) {
        openCritical++;
        if (criticalKinds.length() > 0) {
          criticalKinds.append('/');
        }
        criticalKinds.append(StatusLabels.severity(d.getSeverity()));
      } else if (d.getSeverity() == DefectSeverity.MINOR) {
        openMinor++;
      }
    }
    result.setOpenCriticalDefects(openCritical);
    result.setOpenMinorDefects(openMinor);

    BatchFinalStatus inspectionStatus = evaluateInspection(batch, finalInspection, items, result);
    boolean criticalBlocking = openCritical > 0;

    BatchFinalStatus conclusion;
    if (inspectionStatus != BatchFinalStatus.PASS && criticalBlocking) {
      // 末检问题与严重不良同时存在：逐批结论以末检问题为主，严重不良条数保留在明细字段中
      conclusion = inspectionStatus;
    } else if (inspectionStatus != BatchFinalStatus.PASS) {
      conclusion = inspectionStatus;
    } else if (criticalBlocking) {
      conclusion = BatchFinalStatus.CRITICAL_OPEN;
    } else {
      conclusion = BatchFinalStatus.PASS;
    }
    // 每批阻断数按 0/1 计（是否放行），问题明细看 finalInspectionStatus / openCriticalDefects
    result.setConclusion(conclusion);
    result.setBlockerCount(conclusion == BatchFinalStatus.PASS ? 0 : 1);
    result.setExplanation(buildExplanation(batch, result, conclusion, criticalKinds.toString()));
    return result;
  }

  private BatchFinalStatus evaluateInspection(ProductBatch batch, QualityInspection fin,
                                              List<InspectionItemResult> items,
                                              BatchFinalization result) {
    if (fin == null) {
      return BatchFinalStatus.FINAL_MISSING;
    }
    InspectionResultStatus rs = fin.getResultStatus();
    result.setFinalInspectionStatus(rs == null ? null : rs.name());

    int recorded = items == null ? 0 : items.size();
    result.setRecordedItemCount(recorded);
    Integer required = fin.getRequiredItemCount();
    boolean legacyMissingStandard = required == null;
    result.setLegacyMissingStandard(legacyMissingStandard);
    int requiredCount = legacyMissingStandard ? LEGACY_MIN_ITEM_COUNT : required;
    result.setRequiredItemCount(requiredCount);

    // 旧数据升级：缺检验项的批次按未完成处理，即使末检被标成 PASS 也不能当合格
    if (recorded < requiredCount) {
      return BatchFinalStatus.ITEMS_INCOMPLETE;
    }
    if (rs == InspectionResultStatus.PASS) {
      return BatchFinalStatus.PASS;
    }
    if (rs == InspectionResultStatus.FAIL) {
      return BatchFinalStatus.FINAL_FAIL;
    }
    // RECHECK / CONDITIONAL_PASS / null：收尾时均视为未定论，按复检处理逐批说明
    return BatchFinalStatus.FINAL_RECHECK;
  }

  private String buildExplanation(ProductBatch batch, BatchFinalization result,
                                  BatchFinalStatus conclusion, String criticalKinds) {
    String text =
        switch (conclusion) {
          case PASS -> FinalizationMessages.msg(BatchFinalStatus.PASS, batch.getBatchNo());
          case FINAL_FAIL ->
              FinalizationMessages.msg(BatchFinalStatus.FINAL_FAIL, batch.getBatchNo());
          case FINAL_RECHECK ->
              FinalizationMessages.msg(BatchFinalStatus.FINAL_RECHECK, batch.getBatchNo());
          case FINAL_MISSING ->
              FinalizationMessages.msg(BatchFinalStatus.FINAL_MISSING, batch.getBatchNo());
          case ITEMS_INCOMPLETE ->
              FinalizationMessages.msg(BatchFinalStatus.ITEMS_INCOMPLETE, batch.getBatchNo(),
                  result.getRecordedItemCount(),
                  result.getRequiredItemCount() == 0
                      ? LEGACY_MIN_ITEM_COUNT
                      : result.getRequiredItemCount());
          case CRITICAL_OPEN ->
              FinalizationMessages.msg(BatchFinalStatus.CRITICAL_OPEN, batch.getBatchNo(),
                  result.getOpenCriticalDefects(), criticalKinds);
        };
    if (result.getOpenCriticalDefects() > 0 && conclusion != BatchFinalStatus.CRITICAL_OPEN) {
      text += "；" + FinalizationMessages.msg(BatchFinalStatus.CRITICAL_OPEN,
          batch.getBatchNo(), result.getOpenCriticalDefects(), criticalKinds);
    }
    if (result.getOpenMinorDefects() > 0) {
      text += "；" + FinalizationMessages.minorHint(result.getOpenMinorDefects());
    }
    return text;
  }
}
