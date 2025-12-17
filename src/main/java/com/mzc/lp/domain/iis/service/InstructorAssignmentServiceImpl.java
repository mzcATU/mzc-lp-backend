package com.mzc.lp.domain.iis.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.iis.constant.AssignmentAction;
import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.dto.request.AssignInstructorRequest;
import com.mzc.lp.domain.iis.dto.request.CancelAssignmentRequest;
import com.mzc.lp.domain.iis.dto.request.ReplaceInstructorRequest;
import com.mzc.lp.domain.iis.dto.request.UpdateRoleRequest;
import com.mzc.lp.domain.iis.dto.response.AssignmentHistoryResponse;
import com.mzc.lp.domain.iis.dto.response.InstructorAssignmentResponse;
import com.mzc.lp.domain.iis.entity.AssignmentHistory;
import com.mzc.lp.domain.iis.entity.InstructorAssignment;
import com.mzc.lp.domain.iis.exception.CannotModifyInactiveAssignmentException;
import com.mzc.lp.domain.iis.exception.InstructorAlreadyAssignedException;
import com.mzc.lp.domain.iis.exception.InstructorAssignmentNotFoundException;
import com.mzc.lp.domain.iis.exception.MainInstructorAlreadyExistsException;
import com.mzc.lp.domain.iis.repository.AssignmentHistoryRepository;
import com.mzc.lp.domain.iis.repository.InstructorAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstructorAssignmentServiceImpl implements InstructorAssignmentService {

    private final InstructorAssignmentRepository assignmentRepository;
    private final AssignmentHistoryRepository historyRepository;

    @Override
    @Transactional
    public InstructorAssignmentResponse assignInstructor(Long timeId, AssignInstructorRequest request, Long operatorId) {
        log.info("Assigning instructor: timeId={}, userId={}, role={}", timeId, request.userId(), request.role());

        Long tenantId = TenantContext.getCurrentTenantId();

        // 중복 배정 체크
        if (assignmentRepository.existsByTimeKeyAndUserKeyAndTenantIdAndStatus(
                timeId, request.userId(), tenantId, AssignmentStatus.ACTIVE)) {
            throw new InstructorAlreadyAssignedException(request.userId(), timeId);
        }

        // MAIN 역할인 경우 기존 MAIN 강사 체크
        if (request.role() == InstructorRole.MAIN) {
            assignmentRepository.findActiveByTimeKeyAndRole(timeId, tenantId, InstructorRole.MAIN)
                    .ifPresent(existing -> {
                        throw new MainInstructorAlreadyExistsException(timeId);
                    });
        }

        // 배정 생성
        InstructorAssignment assignment = InstructorAssignment.create(
                request.userId(),
                timeId,
                request.role(),
                operatorId
        );

        InstructorAssignment saved = assignmentRepository.save(assignment);

        // 이력 저장
        historyRepository.save(AssignmentHistory.ofAssign(saved.getId(), request.role(), operatorId));

        log.info("Instructor assigned: assignmentId={}", saved.getId());
        return InstructorAssignmentResponse.from(saved);
    }

    @Override
    public List<InstructorAssignmentResponse> getInstructorsByTimeId(Long timeId, AssignmentStatus status) {
        log.debug("Getting instructors by timeId: timeId={}, status={}", timeId, status);

        Long tenantId = TenantContext.getCurrentTenantId();

        List<InstructorAssignment> assignments;
        if (status != null) {
            assignments = assignmentRepository.findByTimeKeyAndTenantIdAndStatus(timeId, tenantId, status);
        } else {
            assignments = assignmentRepository.findByTimeKeyAndTenantId(timeId, tenantId);
        }

        return assignments.stream()
                .map(InstructorAssignmentResponse::from)
                .toList();
    }

    @Override
    public InstructorAssignmentResponse getAssignment(Long id) {
        log.debug("Getting assignment: id={}", id);

        InstructorAssignment assignment = findAssignmentById(id);
        return InstructorAssignmentResponse.from(assignment);
    }

    @Override
    public Page<InstructorAssignmentResponse> getAssignmentsByUserId(Long userId, AssignmentStatus status, Pageable pageable) {
        log.debug("Getting assignments by userId: userId={}, status={}", userId, status);

        Long tenantId = TenantContext.getCurrentTenantId();

        if (status != null) {
            return assignmentRepository.findByUserKeyAndTenantIdAndStatus(userId, tenantId, status, pageable)
                    .map(InstructorAssignmentResponse::from);
        }

        return assignmentRepository.findByUserKeyAndTenantId(userId, tenantId, pageable)
                .map(InstructorAssignmentResponse::from);
    }

    @Override
    public List<InstructorAssignmentResponse> getMyAssignments(Long userId) {
        log.debug("Getting my assignments: userId={}", userId);

        Long tenantId = TenantContext.getCurrentTenantId();

        return assignmentRepository.findByUserKeyAndTenantIdAndStatus(userId, tenantId, AssignmentStatus.ACTIVE,
                        Pageable.unpaged())
                .map(InstructorAssignmentResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public InstructorAssignmentResponse updateRole(Long id, UpdateRoleRequest request, Long operatorId) {
        log.info("Updating role: id={}, newRole={}", id, request.role());

        InstructorAssignment assignment = findAssignmentById(id);

        // ACTIVE 상태 체크
        validateActiveStatus(assignment);

        InstructorRole oldRole = assignment.getRole();
        Long tenantId = TenantContext.getCurrentTenantId();

        // MAIN으로 변경 시 기존 MAIN 체크
        if (request.role() == InstructorRole.MAIN && oldRole != InstructorRole.MAIN) {
            assignmentRepository.findActiveByTimeKeyAndRole(assignment.getTimeKey(), tenantId, InstructorRole.MAIN)
                    .ifPresent(existing -> {
                        throw new MainInstructorAlreadyExistsException(assignment.getTimeKey());
                    });
        }

        // 역할 변경
        assignment.updateRole(request.role());

        // 이력 저장
        historyRepository.save(AssignmentHistory.ofRoleChange(
                id, oldRole, request.role(), request.reason(), operatorId));

        log.info("Role updated: id={}, oldRole={}, newRole={}", id, oldRole, request.role());
        return InstructorAssignmentResponse.from(assignment);
    }

    @Override
    @Transactional
    public InstructorAssignmentResponse replaceInstructor(Long id, ReplaceInstructorRequest request, Long operatorId) {
        log.info("Replacing instructor: id={}, newUserId={}", id, request.newUserId());

        InstructorAssignment oldAssignment = findAssignmentById(id);

        // ACTIVE 상태 체크
        validateActiveStatus(oldAssignment);

        Long tenantId = TenantContext.getCurrentTenantId();
        Long timeId = oldAssignment.getTimeKey();

        // 새 강사 중복 배정 체크
        if (assignmentRepository.existsByTimeKeyAndUserKeyAndTenantIdAndStatus(
                timeId, request.newUserId(), tenantId, AssignmentStatus.ACTIVE)) {
            throw new InstructorAlreadyAssignedException(request.newUserId(), timeId);
        }

        // 기존 배정 교체 처리
        oldAssignment.replace();
        historyRepository.save(AssignmentHistory.ofReplace(id, request.reason(), operatorId));

        // 새 배정 생성
        InstructorAssignment newAssignment = InstructorAssignment.create(
                request.newUserId(),
                timeId,
                request.role(),
                operatorId
        );
        InstructorAssignment saved = assignmentRepository.save(newAssignment);
        historyRepository.save(AssignmentHistory.ofAssign(saved.getId(), request.role(), operatorId));

        log.info("Instructor replaced: oldId={}, newId={}", id, saved.getId());
        return InstructorAssignmentResponse.from(saved);
    }

    @Override
    @Transactional
    public void cancelAssignment(Long id, CancelAssignmentRequest request, Long operatorId) {
        log.info("Cancelling assignment: id={}", id);

        InstructorAssignment assignment = findAssignmentById(id);

        // ACTIVE 상태 체크
        validateActiveStatus(assignment);

        // 취소 처리
        assignment.cancel();

        // 이력 저장
        historyRepository.save(AssignmentHistory.ofCancel(id, request.reason(), operatorId));

        log.info("Assignment cancelled: id={}", id);
    }

    // ========== TS 모듈 연동용 메서드 ==========

    @Override
    public boolean existsMainInstructor(Long timeId) {
        log.debug("Checking main instructor exists: timeId={}", timeId);

        Long tenantId = TenantContext.getCurrentTenantId();
        return assignmentRepository.existsActiveMainInstructor(timeId, tenantId);
    }

    @Override
    public Map<Long, List<InstructorAssignmentResponse>> getInstructorsByTimeIds(List<Long> timeIds) {
        log.debug("Getting instructors by timeIds: count={}", timeIds.size());

        if (timeIds == null || timeIds.isEmpty()) {
            return Map.of();
        }

        Long tenantId = TenantContext.getCurrentTenantId();

        List<InstructorAssignment> assignments = assignmentRepository.findActiveByTimeKeyIn(timeIds, tenantId);

        return assignments.stream()
                .collect(Collectors.groupingBy(
                        InstructorAssignment::getTimeKey,
                        Collectors.mapping(
                                InstructorAssignmentResponse::from,
                                Collectors.toList()
                        )
                ));
    }

    // ========== 이력 조회 ==========

    @Override
    public List<AssignmentHistoryResponse> getAssignmentHistories(Long assignmentId, AssignmentAction action) {
        log.debug("Getting assignment histories: assignmentId={}, action={}", assignmentId, action);

        // 배정 존재 여부 확인
        findAssignmentById(assignmentId);

        List<AssignmentHistory> histories;
        if (action != null) {
            histories = historyRepository.findByAssignmentIdAndActionOrderByChangedAtDesc(assignmentId, action);
        } else {
            histories = historyRepository.findByAssignmentIdOrderByChangedAtDesc(assignmentId);
        }

        return histories.stream()
                .map(AssignmentHistoryResponse::from)
                .toList();
    }

    // ========== Private Methods ==========

    private InstructorAssignment findAssignmentById(Long id) {
        Long tenantId = TenantContext.getCurrentTenantId();
        return assignmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new InstructorAssignmentNotFoundException(id));
    }

    private void validateActiveStatus(InstructorAssignment assignment) {
        if (!assignment.isActive()) {
            throw new CannotModifyInactiveAssignmentException(assignment.getId());
        }
    }
}
