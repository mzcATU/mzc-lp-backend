package com.mzc.lp.domain.roadmap.entity;

import com.mzc.lp.common.entity.BaseEntity;
import com.mzc.lp.domain.program.entity.Program;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로드맵-프로그램 연결 엔티티
 * 로드맵에 포함된 프로그램과 순서 정보를 관리
 */
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    // ===== 정적 팩토리 메서드 =====

    /**
     * 로드맵-프로그램 연결 생성
     */
    public static RoadmapProgram create(Roadmap roadmap, Program program, Integer orderIndex) {
        RoadmapProgram roadmapProgram = new RoadmapProgram();
        roadmapProgram.roadmap = roadmap;
        roadmapProgram.program = program;
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
