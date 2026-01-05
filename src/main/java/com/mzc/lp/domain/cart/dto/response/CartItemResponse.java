package com.mzc.lp.domain.cart.dto.response;

import com.mzc.lp.domain.cart.entity.CartItem;
import com.mzc.lp.domain.program.entity.Program;
import com.mzc.lp.domain.ts.entity.CourseTime;

import java.time.Instant;

public record CartItemResponse(
        Long cartItemId,
        Long courseTimeId,
        String courseTimeTitle,
        String thumbnailUrl,
        String level,
        Integer estimatedHours,
        Boolean isFree,
        String price,
        Instant addedAt
) {
    public static CartItemResponse from(CartItem cartItem, CourseTime courseTime, Program program) {
        return new CartItemResponse(
                cartItem.getId(),
                courseTime.getId(),
                courseTime.getTitle(),
                program != null ? program.getThumbnailUrl() : null,
                program != null && program.getLevel() != null ? program.getLevel().name() : null,
                program != null ? program.getEstimatedHours() : null,
                courseTime.isFree(),
                courseTime.getPrice() != null ? courseTime.getPrice().toString() : null,
                cartItem.getAddedAt()
        );
    }
}
