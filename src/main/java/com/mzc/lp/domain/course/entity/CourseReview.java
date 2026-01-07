package com.mzc.lp.domain.course.entity;

import com.mzc.lp.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cm_course_reviews",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_course_review",
                columnNames = {"tenant_id", "course_id", "user_id"}
        ),
        indexes = {
                @Index(name = "idx_course_review_course", columnList = "tenant_id, course_id"),
                @Index(name = "idx_course_review_user", columnList = "tenant_id, user_id"),
                @Index(name = "idx_course_review_rating", columnList = "tenant_id, rating")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseReview extends TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "completion_rate", nullable = false)
    private Integer completionRate;  // 0-100 (%)

    // ===== 정적 팩토리 메서드 =====
    public static CourseReview create(Long courseId, Long userId, Integer rating, String content, Integer completionRate) {
        CourseReview review = new CourseReview();
        review.courseId = courseId;
        review.userId = userId;
        review.validateAndSetRating(rating);
        review.content = content;
        review.validateAndSetCompletionRate(completionRate);
        return review;
    }

    // ===== 비즈니스 메서드 =====
    public void update(Integer rating, String content) {
        validateAndSetRating(rating);
        this.content = content;
    }

    public void updateRating(Integer rating) {
        validateAndSetRating(rating);
    }

    public void updateContent(String content) {
        this.content = content;
    }

    // ===== Private 검증 메서드 =====
    private void validateAndSetRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.rating = rating;
    }

    private void validateAndSetCompletionRate(Integer completionRate) {
        if (completionRate == null || completionRate < 0 || completionRate > 100) {
            throw new IllegalArgumentException("Completion rate must be between 0 and 100");
        }
        this.completionRate = completionRate;
    }

    // ===== 검증 메서드 =====
    public boolean isOwner(Long userId) {
        return this.userId.equals(userId);
    }
}
