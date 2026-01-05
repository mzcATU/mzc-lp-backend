package com.mzc.lp.domain.department.service;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.domain.department.dto.request.CreateDepartmentRequest;
import com.mzc.lp.domain.department.dto.request.UpdateDepartmentRequest;
import com.mzc.lp.domain.department.dto.response.DepartmentResponse;
import com.mzc.lp.domain.department.entity.Department;
import com.mzc.lp.domain.department.exception.DepartmentCodeDuplicateException;
import com.mzc.lp.domain.department.exception.DepartmentNotFoundException;
import com.mzc.lp.domain.department.repository.DepartmentRepository;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DepartmentResponse create(Long tenantId, CreateDepartmentRequest request) {
        log.info("Creating department: tenantId={}, name={}, code={}", tenantId, request.name(), request.code());

        // 코드 중복 체크
        if (departmentRepository.existsByTenantIdAndCode(tenantId, request.code().toUpperCase())) {
            throw new DepartmentCodeDuplicateException(request.code());
        }

        // TenantContext 설정
        TenantContext.setTenantId(tenantId);

        try {
            Department department = Department.create(
                    request.name(),
                    request.code(),
                    request.description()
            );

            // 상위 부서 설정
            if (request.parentId() != null) {
                Department parent = departmentRepository.findByIdAndTenantId(request.parentId(), tenantId)
                        .orElseThrow(() -> new DepartmentNotFoundException(request.parentId()));
                department.setParent(parent);
            }

            // 매니저 설정
            if (request.managerId() != null) {
                department.setManager(request.managerId());
            }

            // 정렬 순서 설정
            if (request.sortOrder() != null) {
                department.setSortOrder(request.sortOrder());
            }

            Department saved = departmentRepository.save(department);

            String managerName = getManagerName(saved.getManagerId());
            return DepartmentResponse.from(saved, managerName, 0);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    @Transactional
    public DepartmentResponse update(Long tenantId, Long departmentId, UpdateDepartmentRequest request) {
        log.info("Updating department: tenantId={}, departmentId={}", tenantId, departmentId);

        Department department = departmentRepository.findByIdAndTenantId(departmentId, tenantId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));

        // 코드 중복 체크 (자기 자신 제외)
        if (request.code() != null && !request.code().isBlank()) {
            if (departmentRepository.existsByTenantIdAndCodeAndIdNot(tenantId, request.code().toUpperCase(), departmentId)) {
                throw new DepartmentCodeDuplicateException(request.code());
            }
        }

        // 기본 정보 업데이트
        department.update(request.name(), request.code(), request.description());

        // 상위 부서 변경
        if (request.parentId() != null) {
            // 자기 자신을 상위로 설정할 수 없음
            if (request.parentId().equals(departmentId)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "자기 자신을 상위 부서로 설정할 수 없습니다");
            }
            Department parent = departmentRepository.findByIdAndTenantId(request.parentId(), tenantId)
                    .orElseThrow(() -> new DepartmentNotFoundException(request.parentId()));
            department.setParent(parent);
        }

        // 매니저 변경
        if (request.managerId() != null) {
            department.setManager(request.managerId());
        }

        // 정렬 순서 변경
        if (request.sortOrder() != null) {
            department.setSortOrder(request.sortOrder());
        }

        String managerName = getManagerName(department.getManagerId());
        return DepartmentResponse.from(department, managerName, 0);
    }

    @Override
    @Transactional
    public void delete(Long tenantId, Long departmentId) {
        log.info("Deleting department: tenantId={}, departmentId={}", tenantId, departmentId);

        Department department = departmentRepository.findByIdAndTenantId(departmentId, tenantId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));

        // 하위 부서 존재 여부 확인
        if (departmentRepository.existsByParentId(departmentId)) {
            throw new BusinessException(ErrorCode.DEPARTMENT_HAS_CHILDREN, "하위 부서가 존재하여 삭제할 수 없습니다");
        }

        // TODO: 소속 멤버 존재 여부 확인 (Employee 도메인 구현 후 추가)

        departmentRepository.delete(department);
    }

    @Override
    public DepartmentResponse getById(Long tenantId, Long departmentId) {
        Department department = departmentRepository.findByIdAndTenantId(departmentId, tenantId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));

        String managerName = getManagerName(department.getManagerId());
        return DepartmentResponse.fromWithChildren(department, managerName, 0);
    }

    @Override
    public List<DepartmentResponse> getAll(Long tenantId) {
        List<Department> departments = departmentRepository.findByTenantIdOrderBySortOrderAsc(tenantId);
        return departments.stream()
                .map(d -> DepartmentResponse.from(d, getManagerName(d.getManagerId()), 0))
                .toList();
    }

    @Override
    public List<DepartmentResponse> getRootDepartments(Long tenantId) {
        List<Department> rootDepartments = departmentRepository.findByTenantIdAndParentIsNullOrderBySortOrderAsc(tenantId);
        return rootDepartments.stream()
                .map(d -> DepartmentResponse.fromWithChildren(d, getManagerName(d.getManagerId()), 0))
                .toList();
    }

    @Override
    public List<DepartmentResponse> search(Long tenantId, String keyword) {
        List<Department> departments = departmentRepository.searchByKeyword(tenantId, keyword);
        return departments.stream()
                .map(d -> DepartmentResponse.from(d, getManagerName(d.getManagerId()), 0))
                .toList();
    }

    @Override
    public List<DepartmentResponse> getActiveDepartments(Long tenantId) {
        List<Department> departments = departmentRepository.findByTenantIdAndIsActiveTrueOrderBySortOrderAsc(tenantId);
        return departments.stream()
                .map(d -> DepartmentResponse.from(d, getManagerName(d.getManagerId()), 0))
                .toList();
    }

    private String getManagerName(Long managerId) {
        if (managerId == null) {
            return null;
        }
        return userRepository.findById(managerId)
                .map(User::getName)
                .orElse(null);
    }
}
