package com.mzc.lp.domain.employee.service;

import com.mzc.lp.domain.employee.constant.EmployeeStatus;
import com.mzc.lp.domain.employee.dto.request.ChangeEmployeeStatusRequest;
import com.mzc.lp.domain.employee.dto.request.CreateEmployeeRequest;
import com.mzc.lp.domain.employee.dto.request.CreateLmsAccountRequest;
import com.mzc.lp.domain.employee.dto.request.UpdateEmployeeRequest;
import com.mzc.lp.domain.employee.dto.response.CreateLmsAccountResponse;
import com.mzc.lp.domain.employee.dto.response.EmployeeLmsAccountResponse;
import com.mzc.lp.domain.employee.dto.response.EmployeeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {

    EmployeeResponse create(Long tenantId, CreateEmployeeRequest request);

    EmployeeResponse update(Long tenantId, Long employeeId, UpdateEmployeeRequest request);

    EmployeeResponse changeStatus(Long tenantId, Long employeeId, ChangeEmployeeStatusRequest request);

    void delete(Long tenantId, Long employeeId);

    EmployeeResponse getById(Long tenantId, Long employeeId);

    EmployeeResponse getByEmployeeNumber(Long tenantId, String employeeNumber);

    Page<EmployeeResponse> getAll(Long tenantId, Pageable pageable);

    List<EmployeeResponse> getByDepartment(Long tenantId, Long departmentId);

    Page<EmployeeResponse> search(Long tenantId, Long departmentId, EmployeeStatus status, String keyword, Pageable pageable);

    // LMS 계정 연동 API
    EmployeeLmsAccountResponse getLmsAccount(Long tenantId, Long employeeId);

    CreateLmsAccountResponse createLmsAccount(Long tenantId, Long employeeId, CreateLmsAccountRequest request);

    boolean hasLmsAccount(Long tenantId, Long employeeId);
}
