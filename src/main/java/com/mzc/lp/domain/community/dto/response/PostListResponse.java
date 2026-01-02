package com.mzc.lp.domain.community.dto.response;

import java.util.List;

public record PostListResponse(
        List<PostResponse> posts,
        Long totalCount,
        Integer page,
        Integer pageSize,
        Integer totalPages
) {
    public static PostListResponse of(List<PostResponse> posts, long totalCount, int page, int pageSize, int totalPages) {
        return new PostListResponse(posts, totalCount, page, pageSize, totalPages);
    }
}
