package com.mzc.lp.domain.course.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.course.dto.request.CreateReviewRequest;
import com.mzc.lp.domain.course.dto.request.UpdateReviewRequest;
import com.mzc.lp.domain.course.dto.response.CourseReviewListResponse;
import com.mzc.lp.domain.course.dto.response.CourseReviewResponse;
import com.mzc.lp.domain.course.dto.response.CourseReviewStatsResponse;
import com.mzc.lp.domain.course.entity.CourseReview;
import com.mzc.lp.domain.course.exception.*;
import com.mzc.lp.domain.course.repository.CourseReviewRepository;
import com.mzc.lp.domain.student.constant.EnrollmentStatus;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.ts.exception.CourseTimeNotFoundException;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CourseReviewServiceImpl implements CourseReviewService {

    private final CourseReviewRepository reviewRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseTimeRepository courseTimeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CourseReviewResponse createReview(Long courseTimeId, Long userId, CreateReviewRequest request) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 1. 차수 존재 확인
        courseTimeRepository.findByIdAndTenantId(courseTimeId, tenantId)
                .orElseThrow(() -> new CourseTimeNotFoundException(courseTimeId));

        // 2. 이미 작성한 리뷰가 있는지 확인
        if (reviewRepository.existsByCourseTimeIdAndUserIdAndTenantId(courseTimeId, userId, tenantId)) {
            throw new CourseReviewAlreadyExistsException(courseTimeId, userId);
        }

        // 3. 진도율 계산 - 해당 차수의 진도율 조회
        Integer completionRate = calculateCompletionRate(courseTimeId, userId, tenantId);

        // 4. 리뷰 생성
        CourseReview review = CourseReview.create(courseTimeId, userId, request.rating(), request.content(), completionRate);
        CourseReview savedReview = reviewRepository.save(review);

        // 5. 사용자 이름 조회 및 응답 생성
        String userName = getUserName(userId);
        return CourseReviewResponse.from(savedReview, userName);
    }

    @Override
    public CourseReviewListResponse getReviews(Long courseTimeId, String sortBy, int page, int pageSize) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 1. 차수 존재 확인
        courseTimeRepository.findByIdAndTenantId(courseTimeId, tenantId)
                .orElseThrow(() -> new CourseTimeNotFoundException(courseTimeId));

        // 2. 정렬 기준 설정
        Sort sort = switch (sortBy != null ? sortBy : "latest") {
            case "rating" -> Sort.by(Sort.Direction.DESC, "rating").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        // 3. 리뷰 목록 조회
        PageRequest pageable = PageRequest.of(page, pageSize, sort);
        Page<CourseReview> reviewPage = reviewRepository.findByCourseTimeIdAndTenantId(courseTimeId, tenantId, pageable);

        // 4. 사용자 이름과 함께 응답 생성
        List<CourseReviewResponse> reviews = reviewPage.getContent().stream()
                .map(review -> {
                    String userName = getUserName(review.getUserId());
                    return CourseReviewResponse.from(review, userName);
                })
                .toList();

        // 5. 리뷰 통계 조회
        Object[] stats = reviewRepository.findReviewStatsForCourseTime(courseTimeId, tenantId);
        Long reviewCount = 0L;
        Double averageRating = null;

        if (stats != null && stats.length >= 2) {
            if (stats[0] instanceof Number count) {
                reviewCount = count.longValue();
            }
            if (stats[1] instanceof Number avg) {
                averageRating = avg.doubleValue();
            }
        }

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
    public CourseReviewStatsResponse getReviewStats(Long courseTimeId) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 차수 존재 확인
        courseTimeRepository.findByIdAndTenantId(courseTimeId, tenantId)
                .orElseThrow(() -> new CourseTimeNotFoundException(courseTimeId));

        Object[] stats = reviewRepository.findReviewStatsForCourseTime(courseTimeId, tenantId);
        Long reviewCount = 0L;
        Double averageRating = null;

        if (stats != null && stats.length >= 2) {
            if (stats[0] instanceof Number count) {
                reviewCount = count.longValue();
            }
            if (stats[1] instanceof Number avg) {
                averageRating = avg.doubleValue();
            }
        }

        return CourseReviewStatsResponse.of(courseTimeId, averageRating, reviewCount);
    }

    @Override
    public CourseReviewResponse getMyReview(Long courseTimeId, Long userId) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 차수 존재 확인
        courseTimeRepository.findByIdAndTenantId(courseTimeId, tenantId)
                .orElseThrow(() -> new CourseTimeNotFoundException(courseTimeId));

        return reviewRepository.findByCourseTimeIdAndUserIdAndTenantId(courseTimeId, userId, tenantId)
                .map(review -> {
                    String userName = getUserName(userId);
                    return CourseReviewResponse.from(review, userName);
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public CourseReviewResponse updateReview(Long courseTimeId, Long reviewId, Long userId, UpdateReviewRequest request) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 1. 리뷰 조회
        CourseReview review = reviewRepository.findByIdAndTenantId(reviewId, tenantId)
                .orElseThrow(() -> new CourseReviewNotFoundException(reviewId));

        // 2. 차수 ID 일치 확인
        if (!review.getCourseTimeId().equals(courseTimeId)) {
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
    public void deleteReview(Long courseTimeId, Long reviewId, Long userId, boolean isAdmin) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 1. 리뷰 조회
        CourseReview review = reviewRepository.findByIdAndTenantId(reviewId, tenantId)
                .orElseThrow(() -> new CourseReviewNotFoundException(reviewId));

        // 2. 차수 ID 일치 확인
        if (!review.getCourseTimeId().equals(courseTimeId)) {
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
     * 해당 차수의 진도율 계산
     */
    private Integer calculateCompletionRate(Long courseTimeId, Long userId, Long tenantId) {
        return enrollmentRepository.findByUserIdAndCourseTimeIdAndTenantId(userId, courseTimeId, tenantId)
                .map(enrollment -> {
                    // 완료 상태면 100%, 아니면 현재 진도율 반환
                    if (enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
                        return 100;
                    }
                    // 진행 중인 경우 Enrollment의 progressPercent 값 사용
                    return enrollment.getProgressPercent();
                })
                .orElse(0);
    }

    /**
     * 사용자 이름 조회
     */
    private String getUserName(Long userId) {
        User user = userRepository.findById(userId)
                .orElse(null);
        return user != null ? user.getName() : "알 수 없음";
    }
}
