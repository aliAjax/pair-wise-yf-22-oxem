package com.generated.qualityTrace.utils;

import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.models.InspectionItemResult;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.models.QualityInspection;

import java.util.List;

/**
 * 收尾数据版本计算。
 *
 * 版本由工单下全部批次的“末检结论 + 检验项 + 不良记录”内容决定：
 * 末检一改、检验项一录、不良一登记/处置/关闭，版本即变，
 * 旧的收尾结论对不上版本就作废（SUPERSEDED），重新提交按最新记录重算。
 */
public final class FinalizationVersionCalculator {

  private FinalizationVersionCalculator() {}

  public static String version(
      List<ProductBatch> batches,
      java.util.function.Function<Long, QualityInspection> latestFinal,
      java.util.function.Function<Long, List<InspectionItemResult>> itemsOf,
      java.util.function.Function<Long, List<DefectRecord>> defectsOf) {
    StringBuilder sb = new StringBuilder("v1|");
    for (ProductBatch b : batches) {
      sb.append(b.getId()).append(':');
      QualityInspection fin = latestFinal.apply(b.getId());
      if (fin == null) {
        sb.append("none;");
      } else {
        sb.append(fin.getId())
            .append('/')
            .append(fin.getResultStatus())
            .append('/')
            .append(fin.getInspectedAt())
            .append('/')
            .append(fin.getRequiredItemCount())
            .append(';');
        for (InspectionItemResult item : itemsOf.apply(fin.getId())) {
          sb.append(item.getItemCode())
              .append('=')
              .append(item.getItemStatus())
              .append('@')
              .append(item.getMeasuredValue())
              .append(',');
        }
        sb.append(';');
      }
      for (DefectRecord d : defectsOf.apply(b.getId())) {
        sb.append(d.getId())
            .append('=')
            .append(d.getSeverity())
            .append('/')
            .append(d.getDispositionStatus())
            .append('/')
            .append(d.getDefectQty())
            .append(',');
      }
      sb.append('|');
    }
    return Integer.toHexString(sb.toString().hashCode());
  }
}
