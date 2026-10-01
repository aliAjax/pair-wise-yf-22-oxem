package com.generated.qualityTrace.validators;

import org.springframework.stereotype.Component;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.exceptions.ClosingException;

/**
 * 完工收尾入参校验。
 */
@Component
public class WorkOrderClosingValidator {

    public String validateWorkOrderNo(String workOrderNo) {
        if (workOrderNo == null || workOrderNo.trim().isEmpty()) {
            throw new ClosingException(ErrorCodes.CLOSING_INVALID_WORK_ORDER_NO,
                    ErrorMessages.CLOSING_INVALID_WORK_ORDER_NO);
        }
        return workOrderNo.trim();
    }
}
