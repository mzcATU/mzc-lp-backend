package com.mzc.lp.domain.cart.dto.response;

import com.mzc.lp.domain.cart.entity.CartItem;
import com.mzc.lp.domain.course.entity.Course;
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
    public static CartItemResponse from(CartItem cartItem, CourseTime courseTime, Course course) {
        return new CartItemResponse(
                cartItem.getId(),
                courseTime.getId(),
                courseTime.getTitle(),
                course != null ? course.getThumbnailUrl() : null,
                course != null && course.getLevel() != null ? course.getLevel().name() : null,
                course != null ? course.getEstimatedHours() : null,
                courseTime.isFree(),
                courseTime.getPrice() != null ? courseTime.getPrice().toString() : null,
                cartItem.getAddedAt()
        );
    }
}
