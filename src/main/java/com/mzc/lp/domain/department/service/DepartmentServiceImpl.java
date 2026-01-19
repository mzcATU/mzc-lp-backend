package com.mzc.lp.domain.department.service;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.domain.department.dto.request.CreateDepartmentRequest;
import com.mzc.lp.domain.department.dto.request.UpdateDepartmentRequest;
import com.mzc.lp.domain.department.dto.response.DepartmentMemberResponse;
import com.mzc.lp.domain.department.dto.response.DepartmentResponse;
import com.mzc.lp.domain.department.entity.Department;
import com.mzc.lp.domain.department.exception.DepartmentCodeDuplicateException;
import com.mzc.lp.domain.department.exception.DepartmentNotFoundException;
import com.mzc.lp.domain.department.repository.DepartmentRepository;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.exception.UserNotFoundException;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    /**
     * 테넌트별 부서명->인원수 맵 조회
     */
    private Map<String, Integer> getDepartmentMemberCounts(Long tenantId) {
        Map<String, Integer> countMap = new HashMap<>();
        List<Object[]> results = userRepository.countByTenantIdGroupByDepartment(tenantId);
        for (Object[] row : results) {
            String deptName = (String) row[0];
            Long count = (Long) row[1];
            countMap.put(deptName, count.intValue());
        }
        return countMap;
    }

    /**
     * 특정 부서의 인원수 조회 (하위 부서 포함)
     */
    private int getMemberCountWithChildren(Department department, Map<String, Integer> countMap) {
        int count = countMap.getOrDefault(department.getName(), 0);
        for (Department child : department.getChildren()) {
            count += getMemberCountWithChildren(child, countMap);
        }
        return count;
    }

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

            Map<String, Integer> countMap = getDepartmentMemberCounts(tenantId);
            int memberCount = getMemberCountWithChildren(saved, countMap);
            String managerName = getManagerName(saved.getManagerId());
            return DepartmentResponse.from(saved, managerName, memberCount);
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

        Map<String, Integer> countMap = getDepartmentMemberCounts(tenantId);
        int memberCount = getMemberCountWithChildren(department, countMap);
        String managerName = getManagerName(department.getManagerId());
        return DepartmentResponse.from(department, managerName, memberCount);
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

        Map<String, Integer> countMap = getDepartmentMemberCounts(tenantId);
        String managerName = getManagerName(department.getManagerId());
        return DepartmentResponse.fromWithChildren(department, managerName,
                d -> getMemberCountWithChildren(d, countMap));
    }

    @Override
    public List<DepartmentResponse> getAll(Long tenantId) {
        List<Department> departments = departmentRepository.findByTenantIdOrderBySortOrderAsc(tenantId);
        Map<String, Integer> countMap = getDepartmentMemberCounts(tenantId);
        return departments.stream()
                .map(d -> DepartmentResponse.from(d, getManagerName(d.getManagerId()),
                        getMemberCountWithChildren(d, countMap)))
                .toList();
    }

    @Override
    public List<DepartmentResponse> getRootDepartments(Long tenantId) {
        List<Department> rootDepartments = departmentRepository.findByTenantIdAndParentIsNullOrderBySortOrderAsc(tenantId);
        Map<String, Integer> countMap = getDepartmentMemberCounts(tenantId);
        return rootDepartments.stream()
                .map(d -> DepartmentResponse.fromWithChildren(d, getManagerName(d.getManagerId()),
                        dept -> getMemberCountWithChildren(dept, countMap)))
                .toList();
    }

    @Override
    public List<DepartmentResponse> search(Long tenantId, String keyword) {
        List<Department> departments = departmentRepository.searchByKeyword(tenantId, keyword);
        Map<String, Integer> countMap = getDepartmentMemberCounts(tenantId);
        return departments.stream()
                .map(d -> DepartmentResponse.from(d, getManagerName(d.getManagerId()),
                        getMemberCountWithChildren(d, countMap)))
                .toList();
    }

    @Override
    public List<DepartmentResponse> getActiveDepartments(Long tenantId) {
        List<Department> departments = departmentRepository.findByTenantIdAndIsActiveTrueOrderBySortOrderAsc(tenantId);
        Map<String, Integer> countMap = getDepartmentMemberCounts(tenantId);
        return departments.stream()
                .map(d -> DepartmentResponse.from(d, getManagerName(d.getManagerId()),
                        getMemberCountWithChildren(d, countMap)))
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

    @Override
    public List<DepartmentMemberResponse> getMembersByDepartmentId(Long tenantId, Long departmentId) {
        Department department = departmentRepository.findByIdAndTenantId(departmentId, tenantId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));

        List<User> members = userRepository.findByTenantIdAndDepartment(tenantId, department.getName());
        return members.stream()
                .map(DepartmentMemberResponse::from)
                .toList();
    }

    @Override
    public List<DepartmentMemberResponse> getAvailableMembersForDepartment(Long tenantId, Long departmentId) {
        Department department = departmentRepository.findByIdAndTenantId(departmentId, tenantId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));

        List<User> availableUsers = userRepository.findByTenantIdAndDepartmentNot(tenantId, department.getName());
        return availableUsers.stream()
                .map(DepartmentMemberResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void addMemberToDepartment(Long tenantId, Long departmentId, Long userId) {
        Department department = departmentRepository.findByIdAndTenantId(departmentId, tenantId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 테넌트 검증
        if (!user.getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "해당 사용자에 대한 접근 권한이 없습니다");
        }

        user.updateDepartment(department.getName());
        log.info("Added user to department: userId={}, departmentId={}, departmentName={}",
                userId, departmentId, department.getName());
    }
}
