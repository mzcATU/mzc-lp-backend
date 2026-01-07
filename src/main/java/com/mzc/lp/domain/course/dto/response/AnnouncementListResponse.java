package com.mzc.lp.domain.course.dto.response;

import java.util.List;

/**
 * 공지사항 목록 응답 DTO
 */
public record AnnouncementListResponse(
        List<AnnouncementResponse> announcements,
        long totalCount,
        int page,
        int pageSize,
        int totalPages
) {
    public static AnnouncementListResponse of(List<AnnouncementResponse> announcements, long totalCount,
                                               int page, int pageSize, int totalPages) {
        return new AnnouncementListResponse(announcements, totalCount, page, pageSize, totalPages);
    }
}
