package com.mzc.lp.domain.iis.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.domain.iis.dto.response.ScheduleConflictResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class InstructorScheduleConflictException extends BusinessException {

    private final List<ScheduleConflictResponse> conflicts;

    public InstructorScheduleConflictException(Long userId, List<ScheduleConflictResponse> conflicts) {
        super(ErrorCode.INSTRUCTOR_SCHEDULE_CONFLICT,
                "Instructor has " + conflicts.size() + " schedule conflict(s): userId=" + userId);
        this.conflicts = conflicts;
    }
}
