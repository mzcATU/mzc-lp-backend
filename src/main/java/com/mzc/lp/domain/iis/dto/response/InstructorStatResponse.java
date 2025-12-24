package com.mzc.lp.domain.iis.dto.response;

public record InstructorStatResponse(
        Long userId,
        String userName,
        Long totalCount,
        Long mainCount,
        Long subCount
) {
    public static InstructorStatResponse of(Long userId, String userName, Long totalCount, Long mainCount, Long subCount) {
        return new InstructorStatResponse(userId, userName, totalCount, mainCount, subCount);
    }
}
