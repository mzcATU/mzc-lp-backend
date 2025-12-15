package com.mzc.lp.domain.ts.dto.response;

import com.mzc.lp.domain.ts.entity.CourseTime;

import java.math.BigDecimal;

public record PriceResponse(
        Long courseTimeId,
        BigDecimal price,
        boolean free
) {
    public static PriceResponse from(CourseTime courseTime) {
        return new PriceResponse(
                courseTime.getId(),
                courseTime.getPrice(),
                courseTime.isFree()
        );
    }
}
