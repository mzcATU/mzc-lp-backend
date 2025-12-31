package com.mzc.lp.domain.program.service;

import com.mzc.lp.domain.program.constant.ProgramStatus;
import com.mzc.lp.domain.program.dto.request.ApproveRequest;
import com.mzc.lp.domain.program.dto.request.CreateProgramRequest;
import com.mzc.lp.domain.program.dto.request.RejectRequest;
import com.mzc.lp.domain.program.dto.request.UpdateProgramRequest;
import com.mzc.lp.domain.program.dto.response.PendingProgramResponse;
import com.mzc.lp.domain.program.dto.response.ProgramDetailResponse;
import com.mzc.lp.domain.program.dto.response.ProgramResponse;
import com.mzc.lp.domain.program.entity.Program;
import com.mzc.lp.domain.program.exception.ProgramNotFoundException;
import com.mzc.lp.domain.program.exception.ProgramOwnershipException;
import com.mzc.lp.domain.program.repository.ProgramRepository;
import com.mzc.lp.domain.snapshot.entity.CourseSnapshot;
import com.mzc.lp.domain.snapshot.exception.SnapshotNotFoundException;
import com.mzc.lp.domain.snapshot.repository.CourseSnapshotRepository;
import com.mzc.lp.domain.user.constant.CourseRole;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.entity.UserCourseRole;
import com.mzc.lp.domain.user.exception.UserNotFoundException;
import com.mzc.lp.domain.user.repository.UserCourseRoleRepository;
import com.mzc.lp.domain.user.repository.UserRepository;
import com.mzc.lp.common.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgramServiceImpl implements ProgramService {

    private final ProgramRepository programRepository;
    private final CourseSnapshotRepository snapshotRepository;
    private final UserRepository userRepository;
    private final UserCourseRoleRepository userCourseRoleRepository;

    @Override
    @Transactional
    public ProgramResponse createProgram(CreateProgramRequest request, Long creatorId) {
        log.info("Creating program: title={}, creatorId={}", request.title(), creatorId);

        Program program = Program.create(
                request.title(),
                request.description(),
                request.thumbnailUrl(),
                request.level(),
                request.type(),
                request.estimatedHours(),
                creatorId
        );

        // Snapshot 연결 (선택)
        if (request.snapshotId() != null) {
            CourseSnapshot snapshot = snapshotRepository.findByIdAndTenantId(request.snapshotId(), TenantContext.getCurrentTenantId())
                    .orElseThrow(() -> new SnapshotNotFoundException(request.snapshotId()));
            program.linkSnapshot(snapshot);
        }

        Program savedProgram = programRepository.save(program);
        log.info("Program created: id={}", savedProgram.getId());

        return ProgramResponse.from(savedProgram);
    }

    @Override
    public ProgramDetailResponse getProgram(Long programId) {
        log.debug("Getting program: id={}", programId);

        Program program = programRepository.findByIdWithSnapshot(programId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new ProgramNotFoundException(programId));

        // User 정보 조회
        Set<Long> userIds = new HashSet<>();
        userIds.add(program.getCreatedBy());
        if (program.getApprovedBy() != null) {
            userIds.add(program.getApprovedBy());
        }

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        User creator = userMap.get(program.getCreatedBy());
        User approver = program.getApprovedBy() != null ? userMap.get(program.getApprovedBy()) : null;

        // OWNER 정보 조회
        User owner = findOwnerByProgramId(programId);

        return ProgramDetailResponse.from(
                program,
                creator != null ? creator.getName() : null,
                approver != null ? approver.getName() : null,
                owner != null ? owner.getId() : null,
                owner != null ? owner.getName() : null,
                owner != null ? owner.getEmail() : null
        );
    }

    @Override
    public Page<ProgramResponse> getPrograms(ProgramStatus status, Long createdBy, Pageable pageable) {
        log.debug("Getting programs: status={}, createdBy={}", status, createdBy);

        Page<Program> programs;

        if (status != null && createdBy != null) {
            programs = programRepository.findByTenantIdAndStatusAndCreatedBy(TenantContext.getCurrentTenantId(), status, createdBy, pageable);
        } else if (status != null) {
            programs = programRepository.findByTenantIdAndStatus(TenantContext.getCurrentTenantId(), status, pageable);
        } else if (createdBy != null) {
            programs = programRepository.findByTenantIdAndCreatedBy(TenantContext.getCurrentTenantId(), createdBy, pageable);
        } else {
            programs = programRepository.findByTenantId(TenantContext.getCurrentTenantId(), pageable);
        }

        // N+1 방지: 일괄 조회
        List<Program> programList = programs.getContent();

        // 1. createdBy userIds 수집
        Set<Long> creatorIds = programList.stream()
                .map(Program::getCreatedBy)
                .collect(Collectors.toSet());

        // 2. User 일괄 조회
        Map<Long, User> userMap = userRepository.findAllById(creatorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 3. OWNER 일괄 조회 (programIds)
        List<Long> programIds = programList.stream()
                .map(Program::getId)
                .toList();
        Map<Long, User> ownerMap = findOwnersByProgramIds(programIds);

        return programs.map(program -> {
            User creator = userMap.get(program.getCreatedBy());
            User owner = ownerMap.get(program.getId());

            return ProgramResponse.from(
                    program,
                    creator != null ? creator.getName() : null,
                    owner != null ? owner.getId() : null,
                    owner != null ? owner.getName() : null,
                    owner != null ? owner.getEmail() : null
            );
        });
    }

    private User findOwnerByProgramId(Long programId) {
        List<UserCourseRole> ownerRoles = userCourseRoleRepository.findByCourseIdAndRole(programId, CourseRole.OWNER);
        if (ownerRoles.isEmpty()) {
            return null;
        }
        return ownerRoles.get(0).getUser();
    }

    private Map<Long, User> findOwnersByProgramIds(List<Long> programIds) {
        if (programIds.isEmpty()) {
            return Map.of();
        }

        // 각 프로그램의 OWNER 역할 조회 (null 안전하게 처리)
        Map<Long, User> result = new java.util.HashMap<>();
        for (Long programId : programIds) {
            List<UserCourseRole> ownerRoles = userCourseRoleRepository.findByCourseIdAndRole(programId, CourseRole.OWNER);
            if (!ownerRoles.isEmpty()) {
                result.put(programId, ownerRoles.get(0).getUser());
            }
        }
        return result;
    }

    @Override
    @Transactional
    public ProgramResponse updateProgram(Long programId, UpdateProgramRequest request) {
        log.info("Updating program: id={}", programId);

        Program program = programRepository.findByIdAndTenantId(programId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new ProgramNotFoundException(programId));

        program.update(
                request.title(),
                request.description(),
                request.thumbnailUrl(),
                request.level(),
                request.type(),
                request.estimatedHours()
        );

        log.info("Program updated: id={}", programId);
        return ProgramResponse.from(program);
    }

    @Override
    @Transactional
    public void deleteProgram(Long programId, Long currentUserId, boolean isTenantAdmin) {
        log.info("Deleting program: id={}, currentUserId={}, isTenantAdmin={}", programId, currentUserId, isTenantAdmin);

        Program program = programRepository.findByIdAndTenantId(programId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new ProgramNotFoundException(programId));

        // 소유권 검증: 본인이 생성한 프로그램 또는 TENANT_ADMIN만 삭제 가능
        if (!isTenantAdmin && !currentUserId.equals(program.getCreatedBy())) {
            throw new ProgramOwnershipException("본인이 생성한 프로그램만 삭제할 수 있습니다");
        }

        // DRAFT 상태에서만 삭제 가능
        if (!program.isDraft()) {
            program.close();
            log.info("Program closed instead of deleted: id={}", programId);
        } else {
            programRepository.delete(program);
            log.info("Program deleted: id={}", programId);
        }
    }

    @Override
    @Transactional
    public ProgramResponse submitProgram(Long programId) {
        log.info("Submitting program: id={}", programId);

        Program program = programRepository.findByIdAndTenantId(programId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new ProgramNotFoundException(programId));

        program.submit();

        log.info("Program submitted: id={}, status={}", programId, program.getStatus());
        return ProgramResponse.from(program);
    }

    @Override
    public Page<PendingProgramResponse> getPendingPrograms(Pageable pageable) {
        log.debug("Getting pending programs");

        Page<Program> pendingPrograms = programRepository.findPendingPrograms(TenantContext.getCurrentTenantId(), pageable);

        // N+1 방지: User 일괄 조회
        List<Program> programList = pendingPrograms.getContent();
        Set<Long> creatorIds = programList.stream()
                .map(Program::getCreatedBy)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userRepository.findAllById(creatorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return pendingPrograms.map(program -> {
            User creator = userMap.get(program.getCreatedBy());
            return PendingProgramResponse.from(
                    program,
                    creator != null ? creator.getName() : null
            );
        });
    }

    @Override
    @Transactional
    public ProgramDetailResponse approveProgram(Long programId, ApproveRequest request, Long operatorId) {
        log.info("Approving program: id={}, operatorId={}", programId, operatorId);

        Program program = programRepository.findByIdWithSnapshot(programId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new ProgramNotFoundException(programId));

        program.approve(operatorId, request != null ? request.comment() : null);

        // B2C: Program 생성자에게 OWNER 역할 자동 부여
        assignOwnerRole(program);

        log.info("Program approved: id={}, approvedBy={}", programId, operatorId);
        return ProgramDetailResponse.from(program);
    }

    private void assignOwnerRole(Program program) {
        Long creatorId = program.getCreatedBy();
        Long programId = program.getId();

        // 이미 OWNER 역할이 있는지 확인
        if (userCourseRoleRepository.existsByUserIdAndCourseIdAndRole(creatorId, programId, CourseRole.OWNER)) {
            log.debug("OWNER role already exists: userId={}, programId={}", creatorId, programId);
            return;
        }

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new UserNotFoundException(creatorId));

        UserCourseRole ownerRole = UserCourseRole.createOwner(creator, programId);
        userCourseRoleRepository.save(ownerRole);

        log.info("OWNER role assigned: userId={}, programId={}", creatorId, programId);
    }

    @Override
    @Transactional
    public ProgramDetailResponse rejectProgram(Long programId, RejectRequest request, Long operatorId) {
        log.info("Rejecting program: id={}, operatorId={}", programId, operatorId);

        Program program = programRepository.findByIdWithSnapshot(programId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new ProgramNotFoundException(programId));

        program.reject(operatorId, request.reason());

        log.info("Program rejected: id={}, reason={}", programId, request.reason());
        return ProgramDetailResponse.from(program);
    }

    @Override
    @Transactional
    public ProgramResponse closeProgram(Long programId) {
        log.info("Closing program: id={}", programId);

        Program program = programRepository.findByIdAndTenantId(programId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new ProgramNotFoundException(programId));

        program.close();

        log.info("Program closed: id={}", programId);
        return ProgramResponse.from(program);
    }

    @Override
    @Transactional
    public ProgramResponse linkSnapshot(Long programId, Long snapshotId) {
        log.info("Linking snapshot to program: programId={}, snapshotId={}", programId, snapshotId);

        Program program = programRepository.findByIdAndTenantId(programId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new ProgramNotFoundException(programId));

        CourseSnapshot snapshot = snapshotRepository.findByIdAndTenantId(snapshotId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new SnapshotNotFoundException(snapshotId));

        program.linkSnapshot(snapshot);

        log.info("Snapshot linked to program: programId={}, snapshotId={}", programId, snapshotId);
        return ProgramResponse.from(program);
    }
}
