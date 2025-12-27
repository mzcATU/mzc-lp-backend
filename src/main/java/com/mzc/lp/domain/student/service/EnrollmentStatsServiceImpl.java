package com.mzc.lp.domain.student.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.student.constant.EnrollmentStatus;
import com.mzc.lp.common.dto.stats.TypeCountProjection;
import com.mzc.lp.domain.student.dto.response.CourseTimeEnrollmentStatsResponse;
import com.mzc.lp.domain.student.dto.response.MyLearningStatsResponse;
import com.mzc.lp.domain.student.dto.response.UserEnrollmentStatsResponse;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.ts.exception.CourseTimeNotFoundException;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import com.mzc.lp.domain.user.exception.UserNotFoundException;
import com.mzc.lp.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentStatsServiceImpl implements EnrollmentStatsService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseTimeRepository courseTimeRepository;
    private final UserRepository userRepository;

    @Override
    public CourseTimeEnrollmentStatsResponse getCourseTimeStats(Long courseTimeId) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 차수 존재 확인
        courseTimeRepository.findByIdAndTenantId(courseTimeId, tenantId)
                .orElseThrow(() -> new CourseTimeNotFoundException(courseTimeId));

        // 전체 수강생 수
        long totalEnrollments = enrollmentRepository.countByCourseTimeIdAndTenantId(courseTimeId, tenantId);

        // 상태별 카운트
        long enrolledCount = enrollmentRepository.countByCourseTimeIdAndStatusAndTenantId(
                courseTimeId, EnrollmentStatus.ENROLLED, tenantId);
        long completedCount = enrollmentRepository.countByCourseTimeIdAndStatusAndTenantId(
                courseTimeId, EnrollmentStatus.COMPLETED, tenantId);
        long droppedCount = enrollmentRepository.countByCourseTimeIdAndStatusAndTenantId(
                courseTimeId, EnrollmentStatus.DROPPED, tenantId);
        long failedCount = enrollmentRepository.countByCourseTimeIdAndStatusAndTenantId(
                courseTimeId, EnrollmentStatus.FAILED, tenantId);

        // 평균 진도율
        Double averageProgress = enrollmentRepository.findAverageProgressByCourseTimeId(courseTimeId, tenantId);

        log.debug("차수별 통계 조회 - 차수 ID: {}, 전체: {}, 수강중: {}, 수료: {}",
                courseTimeId, totalEnrollments, enrolledCount, completedCount);

        return CourseTimeEnrollmentStatsResponse.of(
                courseTimeId,
                totalEnrollments,
                enrolledCount,
                completedCount,
                droppedCount,
                failedCount,
                averageProgress
        );
    }

    @Override
    public UserEnrollmentStatsResponse getUserStats(Long userId) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 사용자 존재 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 전체 수강 횟수
        long totalEnrollments = enrollmentRepository.countByUserIdAndTenantId(userId, tenantId);

        // 상태별 카운트
        long completedCount = enrollmentRepository.countByUserIdAndStatusAndTenantId(
                userId, EnrollmentStatus.COMPLETED, tenantId);
        long inProgressCount = enrollmentRepository.countByUserIdAndStatusAndTenantId(
                userId, EnrollmentStatus.ENROLLED, tenantId);
        long droppedCount = enrollmentRepository.countByUserIdAndStatusAndTenantId(
                userId, EnrollmentStatus.DROPPED, tenantId);
        long failedCount = enrollmentRepository.countByUserIdAndStatusAndTenantId(
                userId, EnrollmentStatus.FAILED, tenantId);

        // 평균 점수 (수료한 과정만)
        Double averageScore = enrollmentRepository.findAverageScoreByUserId(userId, tenantId);

        // 평균 진도율
        Double averageProgress = enrollmentRepository.findAverageProgressByUserId(userId, tenantId);

        log.debug("사용자별 통계 조회 - 사용자 ID: {}, 전체: {}, 수료: {}, 수료율: {}%",
                userId, totalEnrollments, completedCount,
                totalEnrollments > 0 ? (completedCount * 100.0 / totalEnrollments) : 0);

        return UserEnrollmentStatsResponse.of(
                userId,
                totalEnrollments,
                completedCount,
                inProgressCount,
                droppedCount,
                failedCount,
                averageScore,
                averageProgress
        );
    }

    @Override
    public MyLearningStatsResponse getMyLearningStats(Long userId) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 전체 수강 횟수
        long totalCourses = enrollmentRepository.countByUserIdAndTenantId(userId, tenantId);

        // 상태별 카운트
        long completedCount = enrollmentRepository.countByUserIdAndStatusAndTenantId(
                userId, EnrollmentStatus.COMPLETED, tenantId);
        long inProgressCount = enrollmentRepository.countByUserIdAndStatusAndTenantId(
                userId, EnrollmentStatus.ENROLLED, tenantId);
        long droppedCount = enrollmentRepository.countByUserIdAndStatusAndTenantId(
                userId, EnrollmentStatus.DROPPED, tenantId);
        long failedCount = enrollmentRepository.countByUserIdAndStatusAndTenantId(
                userId, EnrollmentStatus.FAILED, tenantId);

        // 수강 유형별 카운트 (voluntary/mandatory)
        List<TypeCountProjection> typeCounts = enrollmentRepository.countByUserIdGroupByType(userId, tenantId);
        Map<String, Long> typeCountMap = typeCounts.stream()
                .collect(Collectors.toMap(TypeCountProjection::getType, TypeCountProjection::getCount));

        long voluntaryCount = typeCountMap.getOrDefault("VOLUNTARY", 0L);
        long mandatoryCount = typeCountMap.getOrDefault("MANDATORY", 0L);

        // 평균 진도율
        Double averageProgress = enrollmentRepository.findAverageProgressByUserId(userId, tenantId);

        // 평균 점수 (수료한 과정만)
        Double averageScore = enrollmentRepository.findAverageScoreByUserId(userId, tenantId);

        log.debug("내 학습 통계 조회 - 사용자 ID: {}, 전체: {}, 수료: {}, 수료율: {}%",
                userId, totalCourses, completedCount,
                totalCourses > 0 ? (completedCount * 100.0 / totalCourses) : 0);

        return MyLearningStatsResponse.of(
                totalCourses,
                inProgressCount,
                completedCount,
                droppedCount,
                failedCount,
                voluntaryCount,
                mandatoryCount,
                averageProgress,
                averageScore
        );
    }
}
