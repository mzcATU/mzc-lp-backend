package com.mzc.lp.domain.roadmap.service;

import com.mzc.lp.domain.program.entity.Program;
import com.mzc.lp.domain.program.exception.ProgramNotFoundException;
import com.mzc.lp.domain.program.repository.ProgramRepository;
import com.mzc.lp.domain.roadmap.constant.RoadmapStatus;
import com.mzc.lp.domain.roadmap.dto.request.CreateRoadmapRequest;
import com.mzc.lp.domain.roadmap.dto.request.SaveDraftRequest;
import com.mzc.lp.domain.roadmap.dto.request.UpdateRoadmapRequest;
import com.mzc.lp.domain.roadmap.dto.response.RoadmapDetailResponse;
import com.mzc.lp.domain.roadmap.dto.response.RoadmapResponse;
import com.mzc.lp.domain.roadmap.dto.response.RoadmapStatisticsResponse;
import com.mzc.lp.domain.roadmap.entity.Roadmap;
import com.mzc.lp.domain.roadmap.entity.RoadmapProgram;
import com.mzc.lp.domain.roadmap.exception.DuplicateProgramInRoadmapException;
import com.mzc.lp.domain.roadmap.exception.InvalidProgramException;
import com.mzc.lp.domain.roadmap.repository.RoadmapProgramRepository;
import com.mzc.lp.domain.roadmap.repository.RoadmapRepository;
import com.mzc.lp.domain.user.constant.CourseRole;
import com.mzc.lp.domain.user.repository.UserCourseRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoadmapServiceImpl implements RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapProgramRepository roadmapProgramRepository;
    private final ProgramRepository programRepository;
    private final UserCourseRoleRepository userCourseRoleRepository;

    /**
     * 로드맵 생성
     */
    @Override
    @Transactional
    public RoadmapResponse createRoadmap(CreateRoadmapRequest request, Long authorId) {
        // 1. 프로그램 ID 중복 체크
        validateNoDuplicatePrograms(request.programIds());

        // 2. 모든 프로그램이 존재하고 권한이 있는지 검증
        validateProgramsAndPermissions(request.programIds(), authorId);

        // 3. 로드맵 생성
        Roadmap roadmap = Roadmap.create(
                request.title(),
                request.description(),
                authorId,
                request.status() != null ? request.status() : RoadmapStatus.DRAFT
        );
        roadmapRepository.save(roadmap);

        // 4. 로드맵-프로그램 연결 생성
        saveRoadmapPrograms(roadmap, request.programIds());

        // 5. 응답 생성
        int courseCount = request.programIds().size();
        return RoadmapResponse.from(roadmap, courseCount);
    }

    /**
     * 로드맵 상세 조회
     */
    @Override
    public RoadmapDetailResponse getRoadmap(Long roadmapId, Long currentUserId) {
        // 1. 로드맵 조회
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new com.mzc.lp.domain.roadmap.exception.RoadmapNotFoundException(roadmapId));

        // 2. 권한 확인: DRAFT 상태는 작성자만 조회 가능
        if (roadmap.isDraft() && !roadmap.isOwnedBy(currentUserId)) {
            throw new com.mzc.lp.domain.roadmap.exception.RoadmapOwnershipException(roadmapId);
        }

        // 3. 로드맵에 포함된 프로그램 목록 조회
        List<RoadmapProgram> roadmapPrograms = roadmapProgramRepository
                .findByRoadmapIdOrderByOrderIndexAsc(roadmapId);

        List<com.mzc.lp.domain.roadmap.dto.response.RoadmapProgramDto> programDtos = roadmapPrograms.stream()
                .map(com.mzc.lp.domain.roadmap.dto.response.RoadmapProgramDto::from)
                .toList();

        return RoadmapDetailResponse.from(roadmap, programDtos);
    }

    /**
     * 내 로드맵 목록 조회 (필터링, 정렬 지원)
     */
    @Override
    public Page<RoadmapResponse> getMyRoadmaps(Long authorId, RoadmapStatus status, String sortBy, Pageable pageable) {
        Page<Roadmap> roadmaps;

        // sortBy에 따라 다른 Repository 메서드 호출
        if (sortBy == null || "updatedAt".equals(sortBy)) {
            // 최신순 (기본값)
            roadmaps = status != null
                    ? roadmapRepository.findByAuthorIdAndStatusOrderByUpdatedAtDesc(authorId, status, pageable)
                    : roadmapRepository.findByAuthorIdOrderByUpdatedAtDesc(authorId, pageable);
        } else if ("enrolledStudents".equals(sortBy)) {
            // 수강생순
            roadmaps = status != null
                    ? roadmapRepository.findByAuthorIdAndStatusOrderByEnrolledStudentsDesc(authorId, status, pageable)
                    : roadmapRepository.findByAuthorIdOrderByEnrolledStudentsDesc(authorId, pageable);
        } else if ("title".equals(sortBy)) {
            // 제목순
            roadmaps = status != null
                    ? roadmapRepository.findByAuthorIdAndStatusOrderByTitleAsc(authorId, status, pageable)
                    : roadmapRepository.findByAuthorIdOrderByTitleAsc(authorId, pageable);
        } else {
            // 기본값: 최신순
            roadmaps = status != null
                    ? roadmapRepository.findByAuthorIdAndStatusOrderByUpdatedAtDesc(authorId, status, pageable)
                    : roadmapRepository.findByAuthorIdOrderByUpdatedAtDesc(authorId, pageable);
        }

        // 각 로드맵의 프로그램 개수 조회 (N+1 방지를 위해 일괄 조회)
        List<Long> roadmapIds = roadmaps.getContent().stream()
                .map(Roadmap::getId)
                .toList();

        // 모든 로드맵의 프로그램 개수를 한 번에 조회
        return roadmaps.map(roadmap -> {
            int courseCount = roadmapProgramRepository.countByRoadmapId(roadmap.getId());
            return RoadmapResponse.from(roadmap, courseCount);
        });
    }

    /**
     * 로드맵 수정 (전체 검증)
     */
    @Override
    @Transactional
    public RoadmapResponse updateRoadmap(Long roadmapId, UpdateRoadmapRequest request, Long currentUserId) {
        // 1. 로드맵 조회
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new com.mzc.lp.domain.roadmap.exception.RoadmapNotFoundException(roadmapId));

        // 2. 권한 확인: 작성자만 수정 가능
        if (!roadmap.isOwnedBy(currentUserId)) {
            throw new com.mzc.lp.domain.roadmap.exception.RoadmapOwnershipException(roadmapId);
        }

        // 3. 수정 가능 상태 확인 (DRAFT만 수정 가능)
        if (!roadmap.isModifiable()) {
            throw new com.mzc.lp.domain.roadmap.exception.RoadmapNotModifiableException(roadmap.getStatus());
        }

        // 4. 기본 정보 업데이트
        roadmap.update(request.title(), request.description(), request.status());

        // 5. programIds가 제공된 경우 프로그램 목록 업데이트
        if (request.programIds() != null && !request.programIds().isEmpty()) {
            // 중복 검증
            validateNoDuplicatePrograms(request.programIds());

            // 권한 검증
            validateProgramsAndPermissions(request.programIds(), currentUserId);

            // 기존 프로그램 삭제
            roadmapProgramRepository.deleteByRoadmapId(roadmapId);

            // 새 프로그램 추가
            saveRoadmapPrograms(roadmap, request.programIds());
        }

        // 6. 응답 생성
        int courseCount = roadmapProgramRepository.countByRoadmapId(roadmapId);
        return RoadmapResponse.from(roadmap, courseCount);
    }

    /**
     * 로드맵 임시저장 (최소 검증)
     */
    @Override
    @Transactional
    public RoadmapResponse saveDraft(Long roadmapId, SaveDraftRequest request, Long currentUserId) {
        // 1. 로드맵 조회
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new com.mzc.lp.domain.roadmap.exception.RoadmapNotFoundException(roadmapId));

        // 2. 권한 확인: 작성자만 수정 가능
        if (!roadmap.isOwnedBy(currentUserId)) {
            throw new com.mzc.lp.domain.roadmap.exception.RoadmapOwnershipException(roadmapId);
        }

        // 3. 수정 가능 상태 확인 (DRAFT만 수정 가능)
        if (!roadmap.isModifiable()) {
            throw new com.mzc.lp.domain.roadmap.exception.RoadmapNotModifiableException(roadmap.getStatus());
        }

        // 4. 기본 정보 업데이트 (최소 검증)
        roadmap.updateBasicInfo(request.title(), request.description());

        // 5. programIds가 제공된 경우에만 업데이트
        if (request.programIds() != null) {
            // 기존 프로그램 삭제
            roadmapProgramRepository.deleteByRoadmapId(roadmapId);

            // 새 프로그램 추가 (빈 배열이면 아무것도 안 함)
            if (!request.programIds().isEmpty()) {
                // 중복 검증
                validateNoDuplicatePrograms(request.programIds());

                // 권한 검증 및 저장
                validateProgramsAndPermissions(request.programIds(), currentUserId);
                saveRoadmapPrograms(roadmap, request.programIds());
            }
        }

        // 6. 응답 생성
        int courseCount = roadmapProgramRepository.countByRoadmapId(roadmapId);
        return RoadmapResponse.from(roadmap, courseCount);
    }

    /**
     * 로드맵 삭제
     */
    @Override
    @Transactional
    public void deleteRoadmap(Long roadmapId, Long currentUserId) {
        // 1. 로드맵 조회
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new com.mzc.lp.domain.roadmap.exception.RoadmapNotFoundException(roadmapId));

        // 2. 권한 확인: 작성자만 삭제 가능
        if (!roadmap.isOwnedBy(currentUserId)) {
            throw new com.mzc.lp.domain.roadmap.exception.RoadmapOwnershipException(roadmapId);
        }

        // 3. 수강생이 있는 경우 삭제 불가 (Phase 2에서 활성화)
        // if (roadmap.getEnrolledStudents() > 0) {
        //     throw new RoadmapHasEnrollmentsException(roadmapId);
        // }

        // 4. 로드맵 삭제 (RoadmapProgram은 CASCADE로 자동 삭제)
        roadmapRepository.delete(roadmap);
    }

    /**
     * 로드맵 복제
     */
    @Override
    @Transactional
    public RoadmapResponse duplicateRoadmap(Long roadmapId, Long currentUserId) {
        // 1. 원본 로드맵 조회
        Roadmap originalRoadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new com.mzc.lp.domain.roadmap.exception.RoadmapNotFoundException(roadmapId));

        // 2. 원본 로드맵의 프로그램 목록 조회
        List<RoadmapProgram> originalPrograms = roadmapProgramRepository
                .findByRoadmapIdOrderByOrderIndexAsc(roadmapId);

        // 3. 새 로드맵 생성 (복사본)
        Roadmap newRoadmap = Roadmap.duplicate(originalRoadmap, currentUserId);
        roadmapRepository.save(newRoadmap);

        // 4. 프로그램 목록 복사
        for (RoadmapProgram originalProgram : originalPrograms) {
            RoadmapProgram newProgram = RoadmapProgram.create(
                    newRoadmap,
                    originalProgram.getProgram(),
                    originalProgram.getOrderIndex()
            );
            roadmapProgramRepository.save(newProgram);
        }

        // 5. 응답 생성
        int courseCount = originalPrograms.size();
        return RoadmapResponse.from(newRoadmap, courseCount);
    }

    /**
     * 통계 조회
     */
    @Override
    public RoadmapStatisticsResponse getStatistics(Long authorId) {
        // 1. 전체 로드맵 개수
        long totalRoadmaps = roadmapRepository.countByAuthorId(authorId);

        // 2. 총 수강생 수
        long totalEnrollments = roadmapRepository.sumEnrolledStudentsByAuthorId(authorId);

        // 3. 평균 강의 수
        Double averageCourseCount = roadmapRepository.getAverageCourseCountByAuthorId(authorId);

        return RoadmapStatisticsResponse.of(totalRoadmaps, totalEnrollments, averageCourseCount);
    }

    // ===== Private Helper Methods =====

    /**
     * 프로그램 ID 중복 검증
     */
    private void validateNoDuplicatePrograms(List<Long> programIds) {
        Set<Long> uniqueIds = new HashSet<>(programIds);
        if (uniqueIds.size() != programIds.size()) {
            throw new DuplicateProgramInRoadmapException(null);
        }
    }

    /**
     * 프로그램 존재 여부 및 권한 검증
     */
    private void validateProgramsAndPermissions(List<Long> programIds, Long userId) {
        for (Long programId : programIds) {
            // 1. 프로그램 존재 확인
            Program program = programRepository.findById(programId)
                    .orElseThrow(() -> new ProgramNotFoundException(programId));

            // 2. 권한 확인: DESIGNER(테넌트 레벨) 또는 해당 프로그램의 OWNER
            boolean isDesigner = userCourseRoleRepository
                    .existsByUserIdAndCourseIdIsNullAndRole(userId, CourseRole.DESIGNER);

            boolean isOwner = userCourseRoleRepository
                    .existsByUserIdAndCourseIdAndRole(userId, programId, CourseRole.OWNER);

            if (!isDesigner && !isOwner) {
                throw new InvalidProgramException(programId);
            }
        }
    }

    /**
     * 로드맵-프로그램 연결 저장
     */
    private void saveRoadmapPrograms(Roadmap roadmap, List<Long> programIds) {
        for (int i = 0; i < programIds.size(); i++) {
            Long programId = programIds.get(i);
            Program program = programRepository.findById(programId)
                    .orElseThrow(() -> new ProgramNotFoundException(programId));

            RoadmapProgram roadmapProgram = RoadmapProgram.create(roadmap, program, i);
            roadmapProgramRepository.save(roadmapProgram);
        }
    }
}
