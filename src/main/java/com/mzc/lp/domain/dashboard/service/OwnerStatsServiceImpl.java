package com.mzc.lp.domain.dashboard.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.dto.stats.ProgramStatsProjection;
import com.mzc.lp.common.dto.stats.StatusCountProjection;
import com.mzc.lp.domain.dashboard.dto.response.OwnerStatsResponse;
import com.mzc.lp.domain.program.repository.ProgramRepository;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerStatsServiceImpl implements OwnerStatsService {

    private final ProgramRepository programRepository;
    private final CourseTimeRepository courseTimeRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public OwnerStatsResponse getMyStats(Long userId) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 소유 프로그램 ID 목록 조회
        List<Long> programIds = programRepository.findIdsByCreatedByAndTenantId(userId, tenantId);

        if (programIds.isEmpty()) {
            log.debug("내 강의 통계 조회 - 사용자 ID: {}, 소유 프로그램 없음", userId);
            return createEmptyResponse();
        }

        // 차수 ID 목록 조회
        List<Long> courseTimeIds = courseTimeRepository.findIdsByProgramIdInAndTenantId(programIds, tenantId);

        // 전체 개요
        long totalPrograms = programIds.size();
        long totalCourseTimes = courseTimeIds.size();
        long totalStudents = courseTimeIds.isEmpty() ? 0L :
                enrollmentRepository.countByCourseTimeIdInAndTenantId(courseTimeIds, tenantId);

        // 수강 통계
        long totalEnrollments = totalStudents;
        List<StatusCountProjection> enrollmentStatusProjections = courseTimeIds.isEmpty() ?
                Collections.emptyList() :
                enrollmentRepository.countByCourseTimeIdInGroupByStatus(courseTimeIds, tenantId);
        Double completionRate = courseTimeIds.isEmpty() ? null :
                enrollmentRepository.getCompletionRateByCourseTimeIds(courseTimeIds, tenantId);

        // 프로그램별 통계
        List<ProgramStatsProjection> programStatsProjections =
                programRepository.findProgramStatsByOwner(userId, tenantId);
        List<OwnerStatsResponse.ProgramStat> programStats = programStatsProjections.stream()
                .map(p -> OwnerStatsResponse.ProgramStat.of(
                        p.getProgramId(),
                        p.getTitle(),
                        p.getCourseTimeCount(),
                        p.getTotalStudents(),
                        p.getCompletionRate()
                ))
                .toList();

        log.debug("내 강의 통계 조회 - 사용자 ID: {}, 전체 프로그램: {}, 전체 차수: {}, 전체 수강생: {}",
                userId, totalPrograms, totalCourseTimes, totalStudents);

        return OwnerStatsResponse.of(
                totalPrograms,
                totalCourseTimes,
                totalStudents,
                totalEnrollments,
                enrollmentStatusProjections,
                completionRate,
                programStats
        );
    }

    private OwnerStatsResponse createEmptyResponse() {
        return OwnerStatsResponse.of(
                0L,
                0L,
                0L,
                0L,
                Collections.emptyList(),
                null,
                Collections.emptyList()
        );
    }
}
