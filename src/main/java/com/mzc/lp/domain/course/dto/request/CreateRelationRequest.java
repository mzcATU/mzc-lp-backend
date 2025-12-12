package com.mzc.lp.domain.course.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateRelationRequest(
        @NotEmpty(message = "학습 순서 목록은 비어있을 수 없습니다")
        @Valid
        List<RelationItem> relations
) {
    public record RelationItem(
            Long fromItemId,

            @NotNull(message = "대상 항목 ID는 필수입니다")
            Long toItemId
    ) {
    }
}
