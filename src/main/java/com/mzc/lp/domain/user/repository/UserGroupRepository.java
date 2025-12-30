package com.mzc.lp.domain.user.repository;

import com.mzc.lp.domain.user.entity.UserGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {

    @Query("SELECT g FROM UserGroup g WHERE g.tenantId = :tenantId")
    List<UserGroup> findAllByTenantId(@Param("tenantId") Long tenantId);

    @Query("SELECT g FROM UserGroup g WHERE g.tenantId = :tenantId")
    Page<UserGroup> findAllByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT g FROM UserGroup g WHERE g.tenantId = :tenantId AND g.isActive = :isActive")
    List<UserGroup> findAllByTenantIdAndIsActive(@Param("tenantId") Long tenantId, @Param("isActive") Boolean isActive);

    @Query("SELECT g FROM UserGroup g WHERE g.tenantId = :tenantId AND g.id = :id")
    Optional<UserGroup> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Query("SELECT g FROM UserGroup g WHERE g.tenantId = :tenantId AND g.name = :name")
    Optional<UserGroup> findByTenantIdAndName(@Param("tenantId") Long tenantId, @Param("name") String name);

    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM UserGroup g WHERE g.tenantId = :tenantId AND g.name = :name")
    boolean existsByTenantIdAndName(@Param("tenantId") Long tenantId, @Param("name") String name);

    @Query("SELECT g FROM UserGroup g WHERE g.tenantId = :tenantId AND (LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(g.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<UserGroup> searchByKeyword(@Param("tenantId") Long tenantId, @Param("keyword") String keyword, Pageable pageable);
}
