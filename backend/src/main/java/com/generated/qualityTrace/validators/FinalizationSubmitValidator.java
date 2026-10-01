package com.generated.qualityTrace.validators;

import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.types.BusinessException;
import com.generated.qualityTrace.types.FinalizationSubmitPayload;

/** 完工收尾入参校验：工单号必填。 */
public final class FinalizationSubmitValidator {

  public static void validate(FinalizationSubmitPayload payload) {
    if (payload == null || payload.orderNo() == null || payload.orderNo().isBlank()) {
      throw new BusinessException(ErrorCodes.FINALIZATION_PARAM_INVALID,
          ErrorMessages.format(ErrorMessages.FINALIZATION_PARAM_INVALID, "缺少 orderNo"));
    }
  }

  private FinalizationSubmitValidator() {}
}
