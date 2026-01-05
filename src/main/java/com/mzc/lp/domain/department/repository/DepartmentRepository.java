package com.mzc.lp.domain.department.repository;

import com.mzc.lp.domain.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // 테넌트별 부서 목록 조회
    List<Department> findByTenantIdOrderBySortOrderAsc(Long tenantId);

    // 테넌트별 최상위 부서 목록 조회
    List<Department> findByTenantIdAndParentIsNullOrderBySortOrderAsc(Long tenantId);

    // 테넌트별 활성 부서 목록 조회
    List<Department> findByTenantIdAndIsActiveTrueOrderBySortOrderAsc(Long tenantId);

    // 테넌트별 부서 코드로 조회
    Optional<Department> findByTenantIdAndCode(Long tenantId, String code);

    // 테넌트별 부서 ID로 조회
    Optional<Department> findByIdAndTenantId(Long id, Long tenantId);

    // 부서 코드 중복 체크
    boolean existsByTenantIdAndCode(Long tenantId, String code);

    // 부서 코드 중복 체크 (자기 자신 제외)
    boolean existsByTenantIdAndCodeAndIdNot(Long tenantId, String code, Long id);

    // 하위 부서 조회
    List<Department> findByParentIdOrderBySortOrderAsc(Long parentId);

    // 하위 부서 존재 여부 확인
    boolean existsByParentId(Long parentId);

    // 부서 검색 (이름 또는 코드)
    @Query("SELECT d FROM Department d WHERE d.tenantId = :tenantId " +
           "AND (LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Department> searchByKeyword(@Param("tenantId") Long tenantId, @Param("keyword") String keyword);

    // 매니저별 부서 조회
    List<Department> findByTenantIdAndManagerId(Long tenantId, Long managerId);
}
