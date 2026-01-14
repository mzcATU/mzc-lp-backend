package com.mzc.lp.domain.tu.dto.response;

import com.mzc.lp.domain.course.entity.Course;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

/**
 * TU 강의 탐색용 응답 DTO
 */
public record CourseExploreItemResponse(
        Long id,
        String title,
        String instructor,
        BigDecimal originalPrice,
        BigDecimal price,
        String image,
        int discount,
        double rating,
        int reviewCount,
        int studentCount,
        int totalHours,
        String category,
        String level,
        boolean isNew,
        boolean isBestseller
) {
    /**
     * Course 엔티티로부터 CourseExploreItemResponse 생성
     */
    public static CourseExploreItemResponse from(Course course, String instructorName, int studentCount) {
        BigDecimal price = BigDecimal.valueOf(89000); // 기본 가격 (CourseTime에서 가져와야 함)
        BigDecimal originalPrice = BigDecimal.valueOf(120000);

        return new CourseExploreItemResponse(
                course.getId(),
                course.getTitle(),
                instructorName != null ? instructorName : "강사 미정",
                originalPrice,
                price,
                course.getThumbnailUrl() != null ? course.getThumbnailUrl() : "https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=400&h=250&fit=crop",
                calculateDiscount(originalPrice, price),
                4.5, // 기본 평점
                0,   // 기본 리뷰 수
                studentCount,
                course.getEstimatedHours() != null ? course.getEstimatedHours() : 10,
                "dev", // 기본 카테고리
                course.getLevel() != null ? course.getLevel().name().toLowerCase() : "beginner",
                isNewCourse(course),
                false
        );
    }

    /**
     * Course + CourseTime 정보를 조합하여 생성
     */
    public static CourseExploreItemResponse from(
            Course course,
            String instructorName,
            BigDecimal price,
            BigDecimal originalPrice,
            int studentCount,
            double rating,
            int reviewCount,
            boolean isBestseller
    ) {
        return new CourseExploreItemResponse(
                course.getId(),
                course.getTitle(),
                instructorName != null ? instructorName : "강사 미정",
                originalPrice,
                price,
                course.getThumbnailUrl() != null ? course.getThumbnailUrl() : "https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=400&h=250&fit=crop",
                calculateDiscount(originalPrice, price),
                rating,
                reviewCount,
                studentCount,
                course.getEstimatedHours() != null ? course.getEstimatedHours() : 10,
                "dev",
                course.getLevel() != null ? course.getLevel().name().toLowerCase() : "beginner",
                isNewCourse(course),
                isBestseller
        );
    }

    private static int calculateDiscount(BigDecimal originalPrice, BigDecimal price) {
        if (originalPrice == null || price == null || originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return originalPrice.subtract(price)
                .multiply(BigDecimal.valueOf(100))
                .divide(originalPrice, 0, java.math.RoundingMode.HALF_UP)
                .intValue();
    }

    private static boolean isNewCourse(Course course) {
        if (course.getCreatedAt() == null) {
            return false;
        }
        // 30일 이내 생성된 강의는 NEW
        return course.getCreatedAt().isAfter(Instant.now().minus(Duration.ofDays(30)));
    }
}
