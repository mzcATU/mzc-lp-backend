package com.mzc.lp.domain.roadmap.entity;

import com.mzc.lp.common.constant.ValidationMessages;
import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.roadmap.constant.RoadmapStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로드맵 엔티티
 * 여러 강의(Program)를 묶어 학습 경로를 제공
 */
@Entity
@Table(
    name = "roadmaps",
    indexes = {
        @Index(name = "idx_roadmap_author_status", columnList = "author_id, status"),
        @Index(name = "idx_roadmap_tenant_status", columnList = "tenant_id, status"),
        @Index(name = "idx_roadmap_updated_at", columnList = "updated_at")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Roadmap extends TenantEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoadmapStatus status;

    @Column(name = "enrolled_students", nullable = false)
    private Integer enrolledStudents = 0;

    // ===== 정적 팩토리 메서드 =====

    /**
     * 로드맵 생성
     */
    public static Roadmap create(String title, String description, Long authorId, RoadmapStatus status) {
        Roadmap roadmap = new Roadmap();
        roadmap.validateTitle(title);
        roadmap.title = title;
        roadmap.description = description;
        roadmap.authorId = authorId;
        roadmap.status = status != null ? status : RoadmapStatus.DRAFT;
        roadmap.enrolledStudents = 0;
        return roadmap;
    }

    /**
     * 로드맵 복제
     */
    public static Roadmap duplicate(Roadmap original, Long newAuthorId) {
        String duplicateTitle = generateDuplicateTitle(original.title);
        Roadmap roadmap = new Roadmap();
        roadmap.title = duplicateTitle;
        roadmap.description = original.description;
        roadmap.authorId = newAuthorId;
        roadmap.status = RoadmapStatus.DRAFT;
        roadmap.enrolledStudents = 0;
        return roadmap;
    }

    // ===== 비즈니스 메서드 =====

    /**
     * 기본 정보 업데이트 (임시저장용)
     */
    public void updateBasicInfo(String title, String description) {
        if (title != null) {
            validateTitle(title);
            this.title = title;
        }
        this.description = description;
    }

    /**
     * 전체 정보 업데이트 (일반 수정용)
     */
    public void update(String title, String description, RoadmapStatus status) {
        if (title != null) {
            validateTitle(title);
            this.title = title;
        }
        this.description = description;
        if (status != null) {
            this.status = status;
        }
    }

    /**
     * 공개 상태로 변경
     */
    public void publish() {
        this.status = RoadmapStatus.PUBLISHED;
    }

    /**
     * 작성 중 상태로 변경
     */
    public void unpublish() {
        this.status = RoadmapStatus.DRAFT;
    }

    // ===== 상태 확인 메서드 =====

    public boolean isDraft() {
        return this.status == RoadmapStatus.DRAFT;
    }

    public boolean isPublished() {
        return this.status == RoadmapStatus.PUBLISHED;
    }

    public boolean isModifiable() {
        return this.status == RoadmapStatus.DRAFT;
    }

    public boolean isOwnedBy(Long userId) {
        return this.authorId.equals(userId);
    }

    // ===== Private 검증 메서드 =====

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(ValidationMessages.TITLE_REQUIRED);
        }
        if (title.length() > 255) {
            throw new IllegalArgumentException(ValidationMessages.TITLE_TOO_LONG);
        }
    }

    private static String generateDuplicateTitle(String originalTitle) {
        String suffix = " (복사본)";
        // 중복 방지: "(복사본)"이 이미 있으면 번호 추가
        if (originalTitle.endsWith(suffix)) {
            return originalTitle + " 2";
        }
        // 제목 길이 제한 체크 (255자)
        if (originalTitle.length() + suffix.length() > 255) {
            int maxLength = 255 - suffix.length();
            return originalTitle.substring(0, maxLength) + suffix;
        }
        return originalTitle + suffix;
    }
}
