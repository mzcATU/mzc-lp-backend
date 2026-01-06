package com.mzc.lp.domain.employee.repository;

import com.mzc.lp.domain.employee.constant.EmployeeStatus;
import com.mzc.lp.domain.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // 테넌트별 전체 조회
    List<Employee> findByTenantIdOrderBySortOrderAsc(Long tenantId);

    // 테넌트별 페이징 조회
    Page<Employee> findByTenantId(Long tenantId, Pageable pageable);

    // 테넌트별 상태 필터 조회
    Page<Employee> findByTenantIdAndStatus(Long tenantId, EmployeeStatus status, Pageable pageable);

    // 부서별 조회
    List<Employee> findByTenantIdAndDepartmentIdOrderBySortOrderAsc(Long tenantId, Long departmentId);

    // 부서별 재직자 수
    long countByTenantIdAndDepartmentIdAndStatus(Long tenantId, Long departmentId, EmployeeStatus status);

    // 단건 조회
    Optional<Employee> findByIdAndTenantId(Long id, Long tenantId);

    // 사번으로 조회
    Optional<Employee> findByTenantIdAndEmployeeNumber(Long tenantId, String employeeNumber);

    // 유저 ID로 조회
    Optional<Employee> findByTenantIdAndUserId(Long tenantId, Long userId);

    // 중복 체크
    boolean existsByTenantIdAndEmployeeNumber(Long tenantId, String employeeNumber);
    boolean existsByTenantIdAndEmployeeNumberAndIdNot(Long tenantId, String employeeNumber, Long id);
    boolean existsByTenantIdAndUserId(Long tenantId, Long userId);
    boolean existsByDepartmentId(Long departmentId);

    // 검색 쿼리
    @Query("SELECT e FROM Employee e JOIN e.user u WHERE e.tenantId = :tenantId " +
           "AND (LOWER(e.employeeNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Employee> searchByKeyword(@Param("tenantId") Long tenantId,
                                   @Param("keyword") String keyword,
                                   Pageable pageable);

    // 복합 검색 쿼리
    @Query("SELECT e FROM Employee e JOIN e.user u WHERE e.tenantId = :tenantId " +
           "AND (:departmentId IS NULL OR e.department.id = :departmentId) " +
           "AND (:status IS NULL OR e.status = :status) " +
           "AND (:keyword IS NULL OR :keyword = '' " +
           "OR LOWER(e.employeeNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Employee> searchWithFilters(@Param("tenantId") Long tenantId,
                                     @Param("departmentId") Long departmentId,
                                     @Param("status") EmployeeStatus status,
                                     @Param("keyword") String keyword,
                                     Pageable pageable);

    // 멤버 풀 조건 검색 쿼리
    @Query("SELECT e FROM Employee e JOIN FETCH e.user u LEFT JOIN FETCH e.department d WHERE e.tenantId = :tenantId " +
           "AND (:hasDepartmentIds = false OR e.department.id IN :departmentIds) " +
           "AND (:hasPositions = false OR e.position IN :positions) " +
           "AND (:hasJobTitles = false OR e.jobTitle IN :jobTitles) " +
           "AND (:hasStatuses = false OR e.status IN :statuses)")
    List<Employee> findByMemberPoolConditions(@Param("tenantId") Long tenantId,
                                               @Param("hasDepartmentIds") boolean hasDepartmentIds,
                                               @Param("departmentIds") List<Long> departmentIds,
                                               @Param("hasPositions") boolean hasPositions,
                                               @Param("positions") List<String> positions,
                                               @Param("hasJobTitles") boolean hasJobTitles,
                                               @Param("jobTitles") List<String> jobTitles,
                                               @Param("hasStatuses") boolean hasStatuses,
                                               @Param("statuses") List<EmployeeStatus> statuses);

    // 멤버 풀 조건 검색 (페이징)
    @Query(value = "SELECT e FROM Employee e JOIN e.user u LEFT JOIN e.department d WHERE e.tenantId = :tenantId " +
           "AND (:hasDepartmentIds = false OR e.department.id IN :departmentIds) " +
           "AND (:hasPositions = false OR e.position IN :positions) " +
           "AND (:hasJobTitles = false OR e.jobTitle IN :jobTitles) " +
           "AND (:hasStatuses = false OR e.status IN :statuses)",
           countQuery = "SELECT COUNT(e) FROM Employee e WHERE e.tenantId = :tenantId " +
           "AND (:hasDepartmentIds = false OR e.department.id IN :departmentIds) " +
           "AND (:hasPositions = false OR e.position IN :positions) " +
           "AND (:hasJobTitles = false OR e.jobTitle IN :jobTitles) " +
           "AND (:hasStatuses = false OR e.status IN :statuses)")
    Page<Employee> findByMemberPoolConditionsWithPage(@Param("tenantId") Long tenantId,
                                                       @Param("hasDepartmentIds") boolean hasDepartmentIds,
                                                       @Param("departmentIds") List<Long> departmentIds,
                                                       @Param("hasPositions") boolean hasPositions,
                                                       @Param("positions") List<String> positions,
                                                       @Param("hasJobTitles") boolean hasJobTitles,
                                                       @Param("jobTitles") List<String> jobTitles,
                                                       @Param("hasStatuses") boolean hasStatuses,
                                                       @Param("statuses") List<EmployeeStatus> statuses,
                                                       Pageable pageable);

    // 멤버 풀 조건 카운트
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.tenantId = :tenantId " +
           "AND (:hasDepartmentIds = false OR e.department.id IN :departmentIds) " +
           "AND (:hasPositions = false OR e.position IN :positions) " +
           "AND (:hasJobTitles = false OR e.jobTitle IN :jobTitles) " +
           "AND (:hasStatuses = false OR e.status IN :statuses)")
    long countByMemberPoolConditions(@Param("tenantId") Long tenantId,
                                      @Param("hasDepartmentIds") boolean hasDepartmentIds,
                                      @Param("departmentIds") List<Long> departmentIds,
                                      @Param("hasPositions") boolean hasPositions,
                                      @Param("positions") List<String> positions,
                                      @Param("hasJobTitles") boolean hasJobTitles,
                                      @Param("jobTitles") List<String> jobTitles,
                                      @Param("hasStatuses") boolean hasStatuses,
                                      @Param("statuses") List<EmployeeStatus> statuses);
}
