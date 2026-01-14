package com.mzc.lp.domain.roadmap.entity;

import com.mzc.lp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로드맵-프로그램 연결 엔티티
 * 로드맵에 포함된 프로그램과 순서 정보를 관리
 *
 * @deprecated 로드맵 기능은 Phase 3에서 일시적으로 비활성화됨.
 *             Program 엔티티 제거로 인해 향후 Course 기반으로 재설계 필요.
 */
@Deprecated
@Entity
@Table(
    name = "roadmap_programs",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_roadmap_program",
            columnNames = {"roadmap_id", "program_id"}
        )
    },
    indexes = {
        @Index(name = "idx_roadmap_program_roadmap", columnList = "roadmap_id, order_index"),
        @Index(name = "idx_roadmap_program_program", columnList = "program_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoadmapProgram extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private Roadmap roadmap;

    /**
     * @deprecated Program 엔티티 제거됨. 향후 Course ID로 대체 예정.
     */
    @Deprecated
    @Column(name = "program_id", nullable = false)
    private Long programId;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    // ===== 정적 팩토리 메서드 =====

    /**
     * 로드맵-프로그램 연결 생성
     *
     * @deprecated 로드맵 기능 비활성화됨
     */
    @Deprecated
    public static RoadmapProgram create(Roadmap roadmap, Long programId, Integer orderIndex) {
        RoadmapProgram roadmapProgram = new RoadmapProgram();
        roadmapProgram.roadmap = roadmap;
        roadmapProgram.programId = programId;
        roadmapProgram.orderIndex = orderIndex;
        return roadmapProgram;
    }

    // ===== 비즈니스 메서드 =====

    /**
     * 순서 변경
     */
    public void updateOrder(Integer newOrderIndex) {
        this.orderIndex = newOrderIndex;
    }
}
