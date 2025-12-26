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
import com.mzc.lp.common.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgramServiceImpl implements ProgramService {

    private final ProgramRepository programRepository;
    private final CourseSnapshotRepository snapshotRepository;

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

        return ProgramDetailResponse.from(program);
    }

    @Override
    public Page<ProgramResponse> getPrograms(ProgramStatus status, Long creatorId, Pageable pageable) {
        log.debug("Getting programs: status={}, creatorId={}", status, creatorId);

        Page<Program> programs;

        if (status != null && creatorId != null) {
            programs = programRepository.findByTenantIdAndStatusAndCreatorId(TenantContext.getCurrentTenantId(), status, creatorId, pageable);
        } else if (status != null) {
            programs = programRepository.findByTenantIdAndStatus(TenantContext.getCurrentTenantId(), status, pageable);
        } else if (creatorId != null) {
            programs = programRepository.findByTenantIdAndCreatorId(TenantContext.getCurrentTenantId(), creatorId, pageable);
        } else {
            programs = programRepository.findByTenantId(TenantContext.getCurrentTenantId(), pageable);
        }

        return programs.map(ProgramResponse::from);
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
        if (!isTenantAdmin && !currentUserId.equals(program.getCreatorId())) {
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
        return pendingPrograms.map(PendingProgramResponse::from);
    }

    @Override
    @Transactional
    public ProgramDetailResponse approveProgram(Long programId, ApproveRequest request, Long operatorId) {
        log.info("Approving program: id={}, operatorId={}", programId, operatorId);

        Program program = programRepository.findByIdWithSnapshot(programId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new ProgramNotFoundException(programId));

        program.approve(operatorId, request != null ? request.comment() : null);

        log.info("Program approved: id={}, approvedBy={}", programId, operatorId);
        return ProgramDetailResponse.from(program);
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
