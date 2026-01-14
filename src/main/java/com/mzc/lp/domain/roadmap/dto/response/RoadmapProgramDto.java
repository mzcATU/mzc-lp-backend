package com.mzc.lp.domain.roadmap.dto.response;

import com.mzc.lp.domain.roadmap.entity.RoadmapProgram;

/**
 * 로드맵 프로그램 DTO
 *
 * @deprecated 로드맵 기능은 Phase 3에서 일시적으로 비활성화됨.
 *             Program 엔티티 제거로 인해 향후 Course 기반으로 재설계 필요.
 */
@Deprecated
public record RoadmapProgramDto(
        Long id,
        String title,
        String category,
        String duration,
        Integer order
) {
    /**
     * @deprecated 로드맵 기능 비활성화됨
     */
    @Deprecated
    public static RoadmapProgramDto from(RoadmapProgram roadmapProgram) {
        // Program 엔티티 제거로 인해 더미 데이터 반환
        // 향후 Course 기반으로 재설계 시 수정 필요
        return new RoadmapProgramDto(
                roadmapProgram.getProgramId(),
                "비활성화된 프로그램",
                "",
                "",
                roadmapProgram.getOrderIndex()
        );
    }
}
