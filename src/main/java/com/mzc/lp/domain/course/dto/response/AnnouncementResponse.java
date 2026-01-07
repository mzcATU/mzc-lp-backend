package com.mzc.lp.domain.course.dto.response;

import com.mzc.lp.domain.course.entity.CourseAnnouncement;
import com.mzc.lp.domain.user.entity.User;

import java.time.Instant;

/**
 * 공지사항 응답 DTO
 */
public record AnnouncementResponse(
        Long id,
        Long courseId,
        Long courseTimeId,
        String title,
        String content,
        Boolean isImportant,
        Integer viewCount,
        AuthorInfo author,
        Instant createdAt,
        Instant updatedAt
) {
    public static AnnouncementResponse from(CourseAnnouncement announcement, User author) {
        return new AnnouncementResponse(
                announcement.getId(),
                announcement.getCourseId(),
                announcement.getCourseTimeId(),
                announcement.getTitle(),
                announcement.getContent(),
                announcement.getIsImportant(),
                announcement.getViewCount(),
                author != null ? AuthorInfo.from(author) : null,
                announcement.getCreatedAt(),
                announcement.getUpdatedAt()
        );
    }

    public record AuthorInfo(
            Long id,
            String name,
            String profileImageUrl
    ) {
        public static AuthorInfo from(User user) {
            return new AuthorInfo(
                    user.getId(),
                    user.getName(),
                    user.getProfileImageUrl()
            );
        }
    }
}
