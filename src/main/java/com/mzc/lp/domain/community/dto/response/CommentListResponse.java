package com.mzc.lp.domain.community.dto.response;

import java.util.List;

public record CommentListResponse(
        List<CommentResponse> comments,
        Long totalCount,
        Integer page,
        Integer pageSize,
        Integer totalPages
) {
    public static CommentListResponse of(List<CommentResponse> comments, long totalCount, int page, int pageSize, int totalPages) {
        return new CommentListResponse(comments, totalCount, page, pageSize, totalPages);
    }
}
