package com.mzc.lp.domain.notification.repository;

import com.mzc.lp.domain.notification.constant.NotificationCategory;
import com.mzc.lp.domain.notification.constant.NotificationTrigger;
import com.mzc.lp.domain.notification.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    /**
     * 테넌트별 전체 템플릿 조회
     */
    List<NotificationTemplate> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    /**
     * 테넌트별 카테고리별 템플릿 조회
     */
    List<NotificationTemplate> findByTenantIdAndCategoryOrderByCreatedAtDesc(Long tenantId, NotificationCategory category);

    /**
     * 테넌트별 활성 템플릿 조회
     */
    List<NotificationTemplate> findByTenantIdAndIsActiveTrueOrderByCreatedAtDesc(Long tenantId);

    /**
     * 테넌트별 트리거 타입으로 템플릿 조회
     */
    Optional<NotificationTemplate> findByTenantIdAndTriggerType(Long tenantId, NotificationTrigger triggerType);

    /**
     * 테넌트별 활성화된 트리거 타입 템플릿 조회
     */
    Optional<NotificationTemplate> findByTenantIdAndTriggerTypeAndIsActiveTrue(Long tenantId, NotificationTrigger triggerType);

    /**
     * 테넌트별 트리거 타입 존재 여부 확인
     */
    boolean existsByTenantIdAndTriggerType(Long tenantId, NotificationTrigger triggerType);

    /**
     * 테넌트별 템플릿 개수
     */
    long countByTenantId(Long tenantId);

    /**
     * 테넌트별 카테고리별 템플릿 개수
     */
    long countByTenantIdAndCategory(Long tenantId, NotificationCategory category);

    /**
     * 키워드 검색 (이름, 제목 템플릿)
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE t.tenantId = :tenantId " +
            "AND (LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.titleTemplate) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<NotificationTemplate> searchByKeyword(@Param("tenantId") Long tenantId, @Param("keyword") String keyword);
}
