package com.mzc.lp.domain.program.service;

import com.mzc.lp.domain.program.constant.ProgramStatus;
import com.mzc.lp.domain.program.dto.request.ApproveRequest;
import com.mzc.lp.domain.program.dto.request.CreateProgramRequest;
import com.mzc.lp.domain.program.dto.request.RejectRequest;
import com.mzc.lp.domain.program.dto.request.UpdateProgramRequest;
import com.mzc.lp.domain.program.dto.response.PendingProgramResponse;
import com.mzc.lp.domain.program.dto.response.ProgramDetailResponse;
import com.mzc.lp.domain.program.dto.response.ProgramResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProgramService {

    // CRUD
    ProgramResponse createProgram(CreateProgramRequest request, Long creatorId);

    ProgramDetailResponse getProgram(Long programId);

    Page<ProgramResponse> getPrograms(ProgramStatus status, Long createdBy, Pageable pageable);

    ProgramResponse updateProgram(Long programId, UpdateProgramRequest request);

    void deleteProgram(Long programId, Long currentUserId, boolean isTenantAdmin);

    // 워크플로우
    ProgramResponse submitProgram(Long programId);

    Page<PendingProgramResponse> getPendingPrograms(Pageable pageable);

    ProgramDetailResponse approveProgram(Long programId, ApproveRequest request, Long operatorId);

    ProgramDetailResponse rejectProgram(Long programId, RejectRequest request, Long operatorId);

    ProgramResponse closeProgram(Long programId);

    // Snapshot 연결
    ProgramResponse linkSnapshot(Long programId, Long snapshotId);

    // 내 프로그램 조회 (로드맵 생성용)
    Page<ProgramResponse> getMyPrograms(Long userId, String search, Pageable pageable);
}
