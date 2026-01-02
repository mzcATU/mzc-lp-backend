package com.mzc.lp.domain.ts.dto.response;

import com.mzc.lp.domain.category.entity.Category;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.program.constant.ProgramLevel;
import com.mzc.lp.domain.program.constant.ProgramType;
import com.mzc.lp.domain.program.entity.Program;

public record ProgramSummaryResponse(
        Long id,
        String title,
        String description,
        String thumbnailUrl,
        ProgramLevel level,
        ProgramType type,
        Integer estimatedHours,
        Long categoryId,
        String categoryName
) {
    public static ProgramSummaryResponse from(Program program) {
        if (program == null) {
            return null;
        }
        Long categoryId = extractCategoryId(program);
        return new ProgramSummaryResponse(
                program.getId(),
                program.getTitle(),
                program.getDescription(),
                program.getThumbnailUrl(),
                program.getLevel(),
                program.getType(),
                program.getEstimatedHours(),
                categoryId,
                null
        );
    }

    /**
     * 목록용 요약 응답 (description 제외)
     */
    public static ProgramSummaryResponse forList(Program program) {
        if (program == null) {
            return null;
        }
        Long categoryId = extractCategoryId(program);
        return new ProgramSummaryResponse(
                program.getId(),
                program.getTitle(),
                null,
                program.getThumbnailUrl(),
                program.getLevel(),
                program.getType(),
                program.getEstimatedHours(),
                categoryId,
                null
        );
    }

    /**
     * 목록용 요약 응답 (카테고리 이름 포함)
     */
    public static ProgramSummaryResponse forListWithCategory(Program program, Category category) {
        if (program == null) {
            return null;
        }
        Long categoryId = extractCategoryId(program);
        return new ProgramSummaryResponse(
                program.getId(),
                program.getTitle(),
                null,
                program.getThumbnailUrl(),
                program.getLevel(),
                program.getType(),
                program.getEstimatedHours(),
                categoryId,
                category != null ? category.getName() : null
        );
    }

    private static Long extractCategoryId(Program program) {
        if (program.getSnapshot() != null
                && program.getSnapshot().getSourceCourse() != null) {
            Course course = program.getSnapshot().getSourceCourse();
            return course.getCategoryId();
        }
        return null;
    }
}
