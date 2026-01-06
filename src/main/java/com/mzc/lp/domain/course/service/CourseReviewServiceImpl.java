package com.mzc.lp.domain.course.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.course.dto.request.CreateReviewRequest;
import com.mzc.lp.domain.course.dto.request.UpdateReviewRequest;
import com.mzc.lp.domain.course.dto.response.CourseReviewListResponse;
import com.mzc.lp.domain.course.dto.response.CourseReviewResponse;
import com.mzc.lp.domain.course.dto.response.CourseReviewStatsResponse;
import com.mzc.lp.domain.course.entity.CourseReview;
import com.mzc.lp.domain.course.exception.*;
import com.mzc.lp.domain.course.repository.CourseRepository;
import com.mzc.lp.domain.course.repository.CourseReviewRepository;
import com.mzc.lp.domain.student.constant.EnrollmentStatus;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.exception.UserNotFoundException;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CourseReviewServiceImpl implements CourseReviewService {

    private final CourseReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseTimeRepository courseTimeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CourseReviewResponse createReview(Long courseId, Long userId, CreateReviewRequest request) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 1. 코스 존재 확인
        courseRepository.findByIdAndTenantId(courseId, tenantId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        // 2. 이미 작성한 리뷰가 있는지 확인
        if (reviewRepository.existsByCourseIdAndUserIdAndTenantId(courseId, userId, tenantId)) {
            throw new CourseReviewAlreadyExistsException(courseId, userId);
        }

        // 3. 수강 완료 여부 확인 - 해당 코스의 어느 차수라도 완료했으면 OK
        boolean hasCompletedCourse = hasCompletedAnyCourseTime(courseId, userId, tenantId);
        if (!hasCompletedCourse) {
            throw new CourseNotCompletedException(courseId);
        }

        // 4. 리뷰 생성
        CourseReview review = CourseReview.create(courseId, userId, request.rating(), request.content());
        CourseReview savedReview = reviewRepository.save(review);

        // 5. 사용자 이름 조회 및 응답 생성
        String userName = getUserName(userId);
        return CourseReviewResponse.from(savedReview, userName);
    }

    @Override
    public CourseReviewListResponse getReviews(Long courseId, String sortBy, int page, int pageSize) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 1. 코스 존재 확인
        courseRepository.findByIdAndTenantId(courseId, tenantId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        // 2. 정렬 기준 설정
        Sort sort = switch (sortBy != null ? sortBy : "latest") {
            case "rating" -> Sort.by(Sort.Direction.DESC, "rating").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        // 3. 리뷰 목록 조회
        PageRequest pageable = PageRequest.of(page, pageSize, sort);
        Page<CourseReview> reviewPage = reviewRepository.findByCourseIdAndTenantId(courseId, tenantId, pageable);

        // 4. 사용자 이름과 함께 응답 생성
        List<CourseReviewResponse> reviews = reviewPage.getContent().stream()
                .map(review -> {
                    String userName = getUserName(review.getUserId());
                    return CourseReviewResponse.from(review, userName);
                })
                .collect(Collectors.toList());

        // 5. 리뷰 통계 조회
        Object[] stats = reviewRepository.findReviewStatsForCourse(courseId, tenantId);
        Long reviewCount = stats != null && stats[0] != null ? ((Number) stats[0]).longValue() : 0L;
        Double averageRating = stats != null && stats[1] != null ? ((Number) stats[1]).doubleValue() : null;

        return CourseReviewListResponse.of(
                reviews,
                reviewPage.getTotalPages(),
                reviewPage.getTotalElements(),
                page,
                pageSize,
                averageRating,
                reviewCount
        );
    }

    @Override
    public CourseReviewStatsResponse getReviewStats(Long courseId) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 코스 존재 확인
        courseRepository.findByIdAndTenantId(courseId, tenantId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        Object[] stats = reviewRepository.findReviewStatsForCourse(courseId, tenantId);
        Long reviewCount = stats != null && stats[0] != null ? ((Number) stats[0]).longValue() : 0L;
        Double averageRating = stats != null && stats[1] != null ? ((Number) stats[1]).doubleValue() : null;

        return CourseReviewStatsResponse.of(courseId, averageRating, reviewCount);
    }

    @Override
    public CourseReviewResponse getMyReview(Long courseId, Long userId) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 코스 존재 확인
        courseRepository.findByIdAndTenantId(courseId, tenantId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        return reviewRepository.findByCourseIdAndUserIdAndTenantId(courseId, userId, tenantId)
                .map(review -> {
                    String userName = getUserName(userId);
                    return CourseReviewResponse.from(review, userName);
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public CourseReviewResponse updateReview(Long courseId, Long reviewId, Long userId, UpdateReviewRequest request) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 1. 리뷰 조회
        CourseReview review = reviewRepository.findByIdAndTenantId(reviewId, tenantId)
                .orElseThrow(() -> new CourseReviewNotFoundException(reviewId));

        // 2. 코스 ID 일치 확인
        if (!review.getCourseId().equals(courseId)) {
            throw new CourseReviewNotFoundException(reviewId);
        }

        // 3. 본인 확인
        if (!review.isOwner(userId)) {
            throw new NotReviewOwnerException(reviewId, userId);
        }

        // 4. 리뷰 수정
        review.update(request.rating(), request.content());

        // 5. 응답 생성
        String userName = getUserName(userId);
        return CourseReviewResponse.from(review, userName);
    }

    @Override
    @Transactional
    public void deleteReview(Long courseId, Long reviewId, Long userId, boolean isAdmin) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 1. 리뷰 조회
        CourseReview review = reviewRepository.findByIdAndTenantId(reviewId, tenantId)
                .orElseThrow(() -> new CourseReviewNotFoundException(reviewId));

        // 2. 코스 ID 일치 확인
        if (!review.getCourseId().equals(courseId)) {
            throw new CourseReviewNotFoundException(reviewId);
        }

        // 3. 권한 확인 (본인 또는 관리자)
        if (!isAdmin && !review.isOwner(userId)) {
            throw new NotReviewOwnerException(reviewId, userId);
        }

        // 4. 리뷰 삭제
        reviewRepository.delete(review);
    }

    /**
     * 해당 코스의 어느 차수라도 완료했는지 확인
     */
    private boolean hasCompletedAnyCourseTime(Long courseId, Long userId, Long tenantId) {
        // 1. 해당 코스의 모든 차수 ID 조회
        List<Long> courseTimeIds = courseTimeRepository.findByCmCourseIdAndTenantId(courseId, tenantId)
                .stream()
                .map(ct -> ct.getId())
                .toList();

        if (courseTimeIds.isEmpty()) {
            return false;
        }

        // 2. 해당 차수들 중 COMPLETED 상태인 수강 신청이 있는지 확인
        return courseTimeIds.stream()
                .anyMatch(courseTimeId ->
                        enrollmentRepository.findByUserIdAndCourseTimeIdAndTenantId(userId, courseTimeId, tenantId)
                                .map(enrollment -> enrollment.getStatus() == EnrollmentStatus.COMPLETED)
                                .orElse(false)
                );
    }

    /**
     * 사용자 이름 조회
     */
    private String getUserName(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return user.getName();
    }
}
