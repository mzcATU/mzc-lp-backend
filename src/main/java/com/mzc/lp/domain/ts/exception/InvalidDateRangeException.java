package com.mzc.lp.domain.ts.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

import java.time.LocalDate;

public class InvalidDateRangeException extends BusinessException {

    public InvalidDateRangeException() {
        super(ErrorCode.INVALID_DATE_RANGE);
    }

    public InvalidDateRangeException(String message) {
        super(ErrorCode.INVALID_DATE_RANGE, message);
    }

    public InvalidDateRangeException(LocalDate startDate, LocalDate endDate) {
        super(ErrorCode.INVALID_DATE_RANGE,
                String.format("Invalid date range: start=%s, end=%s", startDate, endDate));
    }
}
