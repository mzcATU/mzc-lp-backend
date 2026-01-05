package com.mzc.lp.domain.memberpool.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.employee.constant.EmployeeStatus;
import com.mzc.lp.domain.employee.entity.Employee;
import com.mzc.lp.domain.employee.repository.EmployeeRepository;
import com.mzc.lp.domain.memberpool.dto.request.CreateMemberPoolRequest;
import com.mzc.lp.domain.memberpool.dto.request.MemberPoolConditionDto;
import com.mzc.lp.domain.memberpool.dto.request.UpdateMemberPoolRequest;
import com.mzc.lp.domain.memberpool.dto.response.MemberPoolMemberResponse;
import com.mzc.lp.domain.memberpool.dto.response.MemberPoolResponse;
import com.mzc.lp.domain.memberpool.entity.MemberPool;
import com.mzc.lp.domain.memberpool.exception.MemberPoolNotFoundException;
import com.mzc.lp.domain.memberpool.repository.MemberPoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberPoolServiceImpl implements MemberPoolService {

    private final MemberPoolRepository memberPoolRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public MemberPoolResponse create(Long tenantId, CreateMemberPoolRequest request) {
        MemberPoolConditionDto conditions = request.conditions();

        MemberPool pool = MemberPool.create(
                request.name(),
                request.description(),
                conditions.departments(),
                conditions.positions(),
                conditions.jobTitles(),
                conditions.employeeStatuses()
        );

        TenantContext.setTenantId(tenantId);
        pool.setSortOrder(request.sortOrder());
        MemberPool saved = memberPoolRepository.save(pool);

        long memberCount = countMembers(tenantId, conditions);
        return MemberPoolResponse.from(saved, memberCount);
    }

    @Override
    @Transactional
    public MemberPoolResponse update(Long tenantId, Long poolId, UpdateMemberPoolRequest request) {
        MemberPool pool = memberPoolRepository.findByIdAndTenantId(poolId, tenantId)
                .orElseThrow(() -> new MemberPoolNotFoundException(poolId));

        MemberPoolConditionDto conditions = request.conditions();
        if (conditions != null) {
            pool.update(
                    request.name(),
                    request.description(),
                    conditions.departments(),
                    conditions.positions(),
                    conditions.jobTitles(),
                    conditions.employeeStatuses()
            );
        } else {
            pool.update(
                    request.name(),
                    request.description(),
                    pool.getCondition() != null ? pool.getCondition().getDepartmentIdList() : List.of(),
                    pool.getCondition() != null ? pool.getCondition().getPositionList() : List.of(),
                    pool.getCondition() != null ? pool.getCondition().getJobTitleList() : List.of(),
                    pool.getCondition() != null ? pool.getCondition().getEmployeeStatusList() : List.of()
            );
        }

        if (request.sortOrder() != null) {
            pool.setSortOrder(request.sortOrder());
        }

        MemberPoolConditionDto currentConditions = conditions != null ? conditions :
                new MemberPoolConditionDto(
                        pool.getCondition() != null ? pool.getCondition().getDepartmentIdList() : List.of(),
                        pool.getCondition() != null ? pool.getCondition().getPositionList() : List.of(),
                        pool.getCondition() != null ? pool.getCondition().getJobTitleList() : List.of(),
                        pool.getCondition() != null ? pool.getCondition().getEmployeeStatusList() : List.of()
                );

        long memberCount = countMembers(tenantId, currentConditions);
        return MemberPoolResponse.from(pool, memberCount);
    }

    @Override
    @Transactional
    public void delete(Long tenantId, Long poolId) {
        MemberPool pool = memberPoolRepository.findByIdAndTenantId(poolId, tenantId)
                .orElseThrow(() -> new MemberPoolNotFoundException(poolId));
        memberPoolRepository.delete(pool);
    }

    @Override
    public MemberPoolResponse getById(Long tenantId, Long poolId) {
        MemberPool pool = memberPoolRepository.findByIdAndTenantId(poolId, tenantId)
                .orElseThrow(() -> new MemberPoolNotFoundException(poolId));

        MemberPoolConditionDto conditions = new MemberPoolConditionDto(
                pool.getCondition() != null ? pool.getCondition().getDepartmentIdList() : List.of(),
                pool.getCondition() != null ? pool.getCondition().getPositionList() : List.of(),
                pool.getCondition() != null ? pool.getCondition().getJobTitleList() : List.of(),
                pool.getCondition() != null ? pool.getCondition().getEmployeeStatusList() : List.of()
        );

        long memberCount = countMembers(tenantId, conditions);
        return MemberPoolResponse.from(pool, memberCount);
    }

    @Override
    public List<MemberPoolResponse> getAll(Long tenantId) {
        return memberPoolRepository.findByTenantIdOrderBySortOrderAsc(tenantId).stream()
                .map(pool -> {
                    MemberPoolConditionDto conditions = new MemberPoolConditionDto(
                            pool.getCondition() != null ? pool.getCondition().getDepartmentIdList() : List.of(),
                            pool.getCondition() != null ? pool.getCondition().getPositionList() : List.of(),
                            pool.getCondition() != null ? pool.getCondition().getJobTitleList() : List.of(),
                            pool.getCondition() != null ? pool.getCondition().getEmployeeStatusList() : List.of()
                    );
                    long memberCount = countMembers(tenantId, conditions);
                    return MemberPoolResponse.from(pool, memberCount);
                })
                .toList();
    }

    @Override
    public List<MemberPoolResponse> getActivePoolls(Long tenantId) {
        return memberPoolRepository.findByTenantIdAndIsActiveTrueOrderBySortOrderAsc(tenantId).stream()
                .map(pool -> {
                    MemberPoolConditionDto conditions = new MemberPoolConditionDto(
                            pool.getCondition() != null ? pool.getCondition().getDepartmentIdList() : List.of(),
                            pool.getCondition() != null ? pool.getCondition().getPositionList() : List.of(),
                            pool.getCondition() != null ? pool.getCondition().getJobTitleList() : List.of(),
                            pool.getCondition() != null ? pool.getCondition().getEmployeeStatusList() : List.of()
                    );
                    long memberCount = countMembers(tenantId, conditions);
                    return MemberPoolResponse.from(pool, memberCount);
                })
                .toList();
    }

    @Override
    public Page<MemberPoolMemberResponse> getMembers(Long tenantId, Long poolId, Pageable pageable) {
        MemberPool pool = memberPoolRepository.findByIdAndTenantId(poolId, tenantId)
                .orElseThrow(() -> new MemberPoolNotFoundException(poolId));

        List<Long> departmentIds = pool.getCondition() != null ? pool.getCondition().getDepartmentIdList() : List.of();
        List<String> positions = pool.getCondition() != null ? pool.getCondition().getPositionList() : List.of();
        List<String> jobTitles = pool.getCondition() != null ? pool.getCondition().getJobTitleList() : List.of();
        List<EmployeeStatus> statuses = convertToStatuses(
                pool.getCondition() != null ? pool.getCondition().getEmployeeStatusList() : List.of()
        );

        Page<Employee> employees = employeeRepository.findByMemberPoolConditionsWithPage(
                tenantId,
                !departmentIds.isEmpty(), departmentIds.isEmpty() ? List.of(-1L) : departmentIds,
                !positions.isEmpty(), positions.isEmpty() ? List.of("") : positions,
                !jobTitles.isEmpty(), jobTitles.isEmpty() ? List.of("") : jobTitles,
                !statuses.isEmpty(), statuses.isEmpty() ? List.of(EmployeeStatus.ACTIVE) : statuses,
                pageable
        );

        return employees.map(MemberPoolMemberResponse::from);
    }

    @Override
    public Page<MemberPoolMemberResponse> previewMembers(Long tenantId, MemberPoolConditionDto conditions, Pageable pageable) {
        List<EmployeeStatus> statuses = convertToStatuses(conditions.employeeStatuses());
        List<Long> departmentIds = conditions.departments();
        List<String> positions = conditions.positions();
        List<String> jobTitles = conditions.jobTitles();

        Page<Employee> employees = employeeRepository.findByMemberPoolConditionsWithPage(
                tenantId,
                !departmentIds.isEmpty(), departmentIds.isEmpty() ? List.of(-1L) : departmentIds,
                !positions.isEmpty(), positions.isEmpty() ? List.of("") : positions,
                !jobTitles.isEmpty(), jobTitles.isEmpty() ? List.of("") : jobTitles,
                !statuses.isEmpty(), statuses.isEmpty() ? List.of(EmployeeStatus.ACTIVE) : statuses,
                pageable
        );

        return employees.map(MemberPoolMemberResponse::from);
    }

    @Override
    @Transactional
    public MemberPoolResponse activate(Long tenantId, Long poolId) {
        MemberPool pool = memberPoolRepository.findByIdAndTenantId(poolId, tenantId)
                .orElseThrow(() -> new MemberPoolNotFoundException(poolId));
        pool.activate();

        MemberPoolConditionDto conditions = new MemberPoolConditionDto(
                pool.getCondition() != null ? pool.getCondition().getDepartmentIdList() : List.of(),
                pool.getCondition() != null ? pool.getCondition().getPositionList() : List.of(),
                pool.getCondition() != null ? pool.getCondition().getJobTitleList() : List.of(),
                pool.getCondition() != null ? pool.getCondition().getEmployeeStatusList() : List.of()
        );

        long memberCount = countMembers(tenantId, conditions);
        return MemberPoolResponse.from(pool, memberCount);
    }

    @Override
    @Transactional
    public MemberPoolResponse deactivate(Long tenantId, Long poolId) {
        MemberPool pool = memberPoolRepository.findByIdAndTenantId(poolId, tenantId)
                .orElseThrow(() -> new MemberPoolNotFoundException(poolId));
        pool.deactivate();

        MemberPoolConditionDto conditions = new MemberPoolConditionDto(
                pool.getCondition() != null ? pool.getCondition().getDepartmentIdList() : List.of(),
                pool.getCondition() != null ? pool.getCondition().getPositionList() : List.of(),
                pool.getCondition() != null ? pool.getCondition().getJobTitleList() : List.of(),
                pool.getCondition() != null ? pool.getCondition().getEmployeeStatusList() : List.of()
        );

        long memberCount = countMembers(tenantId, conditions);
        return MemberPoolResponse.from(pool, memberCount);
    }

    private long countMembers(Long tenantId, MemberPoolConditionDto conditions) {
        List<EmployeeStatus> statuses = convertToStatuses(conditions.employeeStatuses());
        List<Long> departmentIds = conditions.departments();
        List<String> positions = conditions.positions();
        List<String> jobTitles = conditions.jobTitles();

        return employeeRepository.countByMemberPoolConditions(
                tenantId,
                !departmentIds.isEmpty(), departmentIds.isEmpty() ? List.of(-1L) : departmentIds,
                !positions.isEmpty(), positions.isEmpty() ? List.of("") : positions,
                !jobTitles.isEmpty(), jobTitles.isEmpty() ? List.of("") : jobTitles,
                !statuses.isEmpty(), statuses.isEmpty() ? List.of(EmployeeStatus.ACTIVE) : statuses
        );
    }

    private List<EmployeeStatus> convertToStatuses(List<String> statusStrings) {
        if (statusStrings == null || statusStrings.isEmpty()) {
            return List.of();
        }
        return statusStrings.stream()
                .map(EmployeeStatus::valueOf)
                .toList();
    }
}
