package com.generated.qualityTrace.validators;

import com.generated.qualityTrace.constants.DefectSeverity;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.types.BusinessException;

/** 不良登记校验。 */
public final class DefectRecordValidator {

  public static void validate(DefectRecord defect) {
    if (defect == null || defect.getBatchId() == null) {
      throw new BusinessException(ErrorCodes.FINALIZATION_PARAM_INVALID,
          ErrorMessages.format(ErrorMessages.FINALIZATION_PARAM_INVALID, "不良缺少 batchId"));
    }
    if (defect.getSeverity() == null) {
      defect.setSeverity(DefectSeverity.MINOR);
    }
  }

  private DefectRecordValidator() {}
}
