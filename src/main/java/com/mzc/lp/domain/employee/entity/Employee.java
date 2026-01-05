package com.mzc.lp.domain.employee.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.department.entity.Department;
import com.mzc.lp.domain.employee.constant.EmployeeStatus;
import com.mzc.lp.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "employees", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id", "employee_number"}),
        @UniqueConstraint(columnNames = {"tenant_id", "user_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Employee extends TenantEntity {

    @Column(name = "employee_number", nullable = false, length = 50)
    private String employeeNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(length = 100)
    private String position;

    @Column(length = 100)
    private String jobTitle;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "resignation_date")
    private LocalDate resignationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployeeStatus status;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // 정적 팩토리 메서드
    public static Employee create(String employeeNumber, User user) {
        Employee employee = new Employee();
        employee.employeeNumber = employeeNumber;
        employee.user = user;
        employee.status = EmployeeStatus.ACTIVE;
        return employee;
    }

    public static Employee create(String employeeNumber, User user, Department department,
                                   String position, String jobTitle, LocalDate hireDate) {
        Employee employee = create(employeeNumber, user);
        employee.department = department;
        employee.position = position;
        employee.jobTitle = jobTitle;
        employee.hireDate = hireDate;
        return employee;
    }

    // 비즈니스 메서드
    public void update(String position, String jobTitle, LocalDate hireDate) {
        this.position = position;
        this.jobTitle = jobTitle;
        this.hireDate = hireDate;
    }

    public void changeDepartment(Department department) {
        this.department = department;
    }

    public void changeEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    public void activate() {
        this.status = EmployeeStatus.ACTIVE;
        this.resignationDate = null;
    }

    public void onLeave() {
        this.status = EmployeeStatus.ON_LEAVE;
    }

    public void resign(LocalDate resignationDate) {
        this.status = EmployeeStatus.RESIGNED;
        this.resignationDate = resignationDate != null ? resignationDate : LocalDate.now();
    }
}
