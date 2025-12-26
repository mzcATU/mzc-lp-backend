package com.mzc.lp.domain.category.repository;

import com.mzc.lp.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByIdAndTenantId(Long id, Long tenantId);

    List<Category> findByTenantIdOrderBySortOrderAsc(Long tenantId);

    List<Category> findByTenantIdAndActiveOrderBySortOrderAsc(Long tenantId, Boolean active);

    boolean existsByCodeAndTenantId(String code, Long tenantId);

    boolean existsByCodeAndTenantIdAndIdNot(String code, Long tenantId, Long id);

    boolean existsByIdAndTenantId(Long id, Long tenantId);
}
