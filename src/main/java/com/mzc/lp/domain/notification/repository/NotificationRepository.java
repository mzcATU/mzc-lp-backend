package com.mzc.lp.domain.notification.repository;

import com.mzc.lp.domain.notification.constant.NotificationType;
import com.mzc.lp.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 사용자별 알림 목록 조회 (페이징)
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 사용자별 + 타입별 알림 목록 조회
    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, NotificationType type, Pageable pageable);

    // 사용자별 + 읽음 상태별 알림 목록 조회
    Page<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead, Pageable pageable);

    // 사용자별 + 타입 + 읽음 상태
    Page<Notification> findByUserIdAndTypeAndIsReadOrderByCreatedAtDesc(
            Long userId, NotificationType type, Boolean isRead, Pageable pageable);

    // 읽지 않은 알림 개수
    long countByUserIdAndIsRead(Long userId, Boolean isRead);

    // 특정 알림 조회
    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    // 모든 알림 읽음 처리
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);

    // 읽은 알림 삭제
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.userId = :userId AND n.isRead = true")
    int deleteReadByUserId(@Param("userId") Long userId);

    // 사용자의 모든 알림 삭제
    void deleteByUserId(Long userId);

    // 특정 reference에 대한 알림 존재 여부 (중복 알림 방지용)
    boolean existsByUserIdAndReferenceIdAndReferenceTypeAndActorId(
            Long userId, Long referenceId, String referenceType, Long actorId);
}
