package com.mzc.lp.domain.employee.service;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.domain.department.entity.Department;
import com.mzc.lp.domain.department.exception.DepartmentNotFoundException;
import com.mzc.lp.domain.department.repository.DepartmentRepository;
import com.mzc.lp.domain.employee.constant.EmployeeStatus;
import com.mzc.lp.domain.employee.dto.request.ChangeEmployeeStatusRequest;
import com.mzc.lp.domain.employee.dto.request.CreateEmployeeRequest;
import com.mzc.lp.domain.employee.dto.request.UpdateEmployeeRequest;
import com.mzc.lp.domain.employee.dto.response.EmployeeResponse;
import com.mzc.lp.domain.employee.entity.Employee;
import com.mzc.lp.domain.employee.exception.EmployeeNotFoundException;
import com.mzc.lp.domain.employee.exception.EmployeeNumberDuplicateException;
import com.mzc.lp.domain.employee.repository.EmployeeRepository;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.exception.UserNotFoundException;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public EmployeeResponse create(Long tenantId, CreateEmployeeRequest request) {
        log.info("Creating employee: tenantId={}, employeeNumber={}, userId={}",
                tenantId, request.employeeNumber(), request.userId());

        // 사번 중복 체크
        if (employeeRepository.existsByTenantIdAndEmployeeNumber(tenantId, request.employeeNumber())) {
            throw new EmployeeNumberDuplicateException(request.employeeNumber());
        }

        // 사용자 중복 등록 체크
        if (employeeRepository.existsByTenantIdAndUserId(tenantId, request.userId())) {
            throw new BusinessException(ErrorCode.EMPLOYEE_USER_ALREADY_EXISTS,
                    "해당 사용자는 이미 임직원으로 등록되어 있습니다");
        }

        // 사용자 조회
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        // 부서 조회 (optional)
        Department department = null;
        if (request.departmentId() != null) {
            department = departmentRepository.findByIdAndTenantId(request.departmentId(), tenantId)
                    .orElseThrow(() -> new DepartmentNotFoundException(request.departmentId()));
        }

        // TenantContext 설정
        TenantContext.setTenantId(tenantId);

        try {
            Employee employee = Employee.create(
                    request.employeeNumber(),
                    user,
                    department,
                    request.position(),
                    request.jobTitle(),
                    request.hireDate()
            );

            if (request.sortOrder() != null) {
                employee.setSortOrder(request.sortOrder());
            }

            Employee saved = employeeRepository.save(employee);
            return EmployeeResponse.from(saved);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long tenantId, Long employeeId, UpdateEmployeeRequest request) {
        log.info("Updating employee: tenantId={}, employeeId={}", tenantId, employeeId);

        Employee employee = employeeRepository.findByIdAndTenantId(employeeId, tenantId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        // 사번 변경 시 중복 체크
        if (request.employeeNumber() != null && !request.employeeNumber().isBlank()) {
            if (employeeRepository.existsByTenantIdAndEmployeeNumberAndIdNot(
                    tenantId, request.employeeNumber(), employeeId)) {
                throw new EmployeeNumberDuplicateException(request.employeeNumber());
            }
            employee.changeEmployeeNumber(request.employeeNumber());
        }

        // 부서 변경
        if (request.departmentId() != null) {
            Department department = departmentRepository.findByIdAndTenantId(request.departmentId(), tenantId)
                    .orElseThrow(() -> new DepartmentNotFoundException(request.departmentId()));
            employee.changeDepartment(department);
        }

        // 기본 정보 업데이트
        employee.update(request.position(), request.jobTitle(), request.hireDate());

        if (request.sortOrder() != null) {
            employee.setSortOrder(request.sortOrder());
        }

        return EmployeeResponse.from(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse changeStatus(Long tenantId, Long employeeId, ChangeEmployeeStatusRequest request) {
        log.info("Changing employee status: tenantId={}, employeeId={}, status={}",
                tenantId, employeeId, request.status());

        Employee employee = employeeRepository.findByIdAndTenantId(employeeId, tenantId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        switch (request.status()) {
            case ACTIVE -> employee.activate();
            case ON_LEAVE -> employee.onLeave();
            case RESIGNED -> employee.resign(request.resignationDate());
        }

        return EmployeeResponse.from(employee);
    }

    @Override
    @Transactional
    public void delete(Long tenantId, Long employeeId) {
        log.info("Deleting employee: tenantId={}, employeeId={}", tenantId, employeeId);

        Employee employee = employeeRepository.findByIdAndTenantId(employeeId, tenantId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        employeeRepository.delete(employee);
    }

    @Override
    public EmployeeResponse getById(Long tenantId, Long employeeId) {
        Employee employee = employeeRepository.findByIdAndTenantId(employeeId, tenantId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        return EmployeeResponse.from(employee);
    }

    @Override
    public EmployeeResponse getByEmployeeNumber(Long tenantId, String employeeNumber) {
        Employee employee = employeeRepository.findByTenantIdAndEmployeeNumber(tenantId, employeeNumber)
                .orElseThrow(EmployeeNotFoundException::new);

        return EmployeeResponse.from(employee);
    }

    @Override
    public Page<EmployeeResponse> getAll(Long tenantId, Pageable pageable) {
        return employeeRepository.findByTenantId(tenantId, pageable)
                .map(EmployeeResponse::from);
    }

    @Override
    public List<EmployeeResponse> getByDepartment(Long tenantId, Long departmentId) {
        return employeeRepository.findByTenantIdAndDepartmentIdOrderBySortOrderAsc(tenantId, departmentId)
                .stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @Override
    public Page<EmployeeResponse> search(Long tenantId, Long departmentId, EmployeeStatus status,
                                          String keyword, Pageable pageable) {
        return employeeRepository.searchWithFilters(tenantId, departmentId, status, keyword, pageable)
                .map(EmployeeResponse::from);
    }
}
