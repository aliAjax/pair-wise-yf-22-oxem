package com.generated.qualityTrace.validators;

import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.constants.InspectionType;
import com.generated.qualityTrace.models.QualityInspection;
import com.generated.qualityTrace.types.BusinessException;

/** 检验提交校验：末检必须带批次与结论。 */
public final class QualityInspectionValidator {

  public static void validateFinal(QualityInspection inspection) {
    if (inspection == null || inspection.getBatchId() == null) {
      throw new BusinessException(ErrorCodes.FINALIZATION_PARAM_INVALID,
          ErrorMessages.format(ErrorMessages.FINALIZATION_PARAM_INVALID, "末检缺少 batchId"));
    }
    if (inspection.getResultStatus() == null) {
      throw new BusinessException(ErrorCodes.FINALIZATION_PARAM_INVALID,
          ErrorMessages.format(ErrorMessages.FINALIZATION_PARAM_INVALID, "末检缺少 resultStatus"));
    }
    inspection.setInspectionType(InspectionType.FINAL);
  }

  private QualityInspectionValidator() {}
}
