package com.mzc.lp.domain.roadmap.dto.response;

import com.mzc.lp.domain.program.entity.Program;
import com.mzc.lp.domain.roadmap.entity.RoadmapProgram;

public record RoadmapProgramDto(
        Long id,
        String title,
        String category,
        String duration,
        Integer order
) {
    public static RoadmapProgramDto from(RoadmapProgram roadmapProgram) {
        Program program = roadmapProgram.getProgram();

        // duration 계산 (estimatedHours를 "N시간" 형식으로 변환)
        String duration = program.getEstimatedHours() != null
            ? program.getEstimatedHours() + "시간"
            : "";

        // category는 program의 type을 사용 (또는 별도 카테고리 필드가 있다면 그것 사용)
        String category = program.getType() != null
            ? program.getType().name()
            : "";

        return new RoadmapProgramDto(
                program.getId(),
                program.getTitle(),
                category,
                duration,
                roadmapProgram.getOrderIndex()
        );
    }
}
