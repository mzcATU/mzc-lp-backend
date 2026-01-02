package com.mzc.lp.domain.cart.dto.response;

import com.mzc.lp.domain.cart.entity.CartItem;
import com.mzc.lp.domain.course.entity.Course;

import java.time.Instant;

public record CartItemResponse(
        Long cartItemId,
        Long courseId,
        String courseTitle,
        String courseDescription,
        String thumbnailUrl,
        String level,
        String type,
        Integer estimatedHours,
        Instant addedAt
) {
    public static CartItemResponse from(CartItem cartItem, Course course) {
        return new CartItemResponse(
                cartItem.getId(),
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getThumbnailUrl(),
                course.getLevel() != null ? course.getLevel().name() : null,
                course.getType() != null ? course.getType().name() : null,
                course.getEstimatedHours(),
                cartItem.getAddedAt()
        );
    }
}
