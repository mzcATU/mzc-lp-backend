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
import com.mzc.lp.domain.employee.dto.request.CreateLmsAccountRequest;
import com.mzc.lp.domain.employee.dto.request.UpdateEmployeeRequest;
import com.mzc.lp.domain.employee.dto.response.CreateLmsAccountResponse;
import com.mzc.lp.domain.employee.dto.response.EmployeeLmsAccountResponse;
import com.mzc.lp.domain.employee.dto.response.EmployeeResponse;
import com.mzc.lp.domain.employee.entity.Employee;
import com.mzc.lp.domain.employee.exception.EmployeeNotFoundException;
import com.mzc.lp.domain.employee.exception.EmployeeNumberDuplicateException;
import com.mzc.lp.domain.employee.repository.EmployeeRepository;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.exception.UserNotFoundException;
import com.mzc.lp.domain.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;

import java.security.SecureRandom;
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
    private final PasswordEncoder passwordEncoder;

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

    // ========== LMS 계정 연동 API ==========

    @Override
    public EmployeeLmsAccountResponse getLmsAccount(Long tenantId, Long employeeId) {
        log.info("Getting LMS account for employee: tenantId={}, employeeId={}", tenantId, employeeId);

        Employee employee = employeeRepository.findByIdAndTenantId(employeeId, tenantId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        if (employee.getUser() != null) {
            return EmployeeLmsAccountResponse.withAccount(employee);
        } else {
            return EmployeeLmsAccountResponse.withoutAccount(
                    employee.getId(),
                    employee.getEmployeeNumber(),
                    null
            );
        }
    }

    @Override
    @Transactional
    public CreateLmsAccountResponse createLmsAccount(Long tenantId, Long employeeId, CreateLmsAccountRequest request) {
        log.info("Creating LMS account for employee: tenantId={}, employeeId={}", tenantId, employeeId);

        Employee employee = employeeRepository.findByIdAndTenantId(employeeId, tenantId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        // 이미 User가 연결되어 있는지 체크
        if (employee.getUser() != null) {
            throw new BusinessException(ErrorCode.EMPLOYEE_USER_ALREADY_EXISTS,
                    "해당 임직원은 이미 LMS 계정이 연결되어 있습니다");
        }

        // 비밀번호 결정
        String rawPassword;
        if (Boolean.TRUE.equals(request.generatePassword())) {
            rawPassword = generateRandomPassword();
        } else if (request.password() != null && !request.password().isBlank()) {
            rawPassword = request.password();
        } else {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호가 지정되지 않았습니다");
        }

        // 이메일은 사번@회사도메인 형태로 생성 (임시)
        String email = employee.getEmployeeNumber() + "@company.com";

        // 이메일 중복 체크
        if (userRepository.existsByTenantIdAndEmail(tenantId, email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL, "이미 사용 중인 이메일입니다: " + email);
        }

        // TenantContext 설정
        TenantContext.setTenantId(tenantId);

        try {
            String encodedPassword = passwordEncoder.encode(rawPassword);
            User user = User.create(email, employee.getEmployeeNumber(), encodedPassword);

            if (request.role() != null) {
                user.updateRole(request.role());
            }

            User savedUser = userRepository.save(user);

            log.info("LMS account created for employee: employeeId={}, userId={}", employeeId, savedUser.getId());

            return CreateLmsAccountResponse.from(savedUser, employee, rawPassword);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public boolean hasLmsAccount(Long tenantId, Long employeeId) {
        Employee employee = employeeRepository.findByIdAndTenantId(employeeId, tenantId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        return employee.getUser() != null;
    }

    /**
     * 랜덤 비밀번호 생성 (12자리, 대소문자+숫자+특수문자 포함)
     */
    private String generateRandomPassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*";
        String allChars = upper + lower + digits + special;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(12);

        // 최소 1개씩 포함
        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));

        // 나머지는 랜덤
        for (int i = 4; i < 12; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // 셔플
        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }
}
