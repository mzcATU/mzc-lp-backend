package com.mzc.lp.domain.course.entity;

import com.mzc.lp.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 코스 공지사항 엔티티
 * 강사/운영자가 특정 코스 수강생에게 공지사항을 전달
 */
@Entity
@Table(name = "course_announcements",
        indexes = {
                @Index(name = "idx_announcement_course", columnList = "course_id"),
                @Index(name = "idx_announcement_course_time", columnList = "course_time_id"),
                @Index(name = "idx_announcement_author", columnList = "author_id"),
                @Index(name = "idx_announcement_created", columnList = "created_at")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseAnnouncement extends TenantEntity {

    /**
     * 코스 ID (코스 전체 공지)
     */
    @Column(name = "course_id")
    private Long courseId;

    /**
     * 차수 ID (차수별 공지, null이면 코스 전체 공지)
     */
    @Column(name = "course_time_id")
    private Long courseTimeId;

    /**
     * 작성자 ID
     */
    @Column(name = "author_id", nullable = false)
    private Long authorId;

    /**
     * 공지 제목
     */
    @Column(nullable = false, length = 255)
    private String title;

    /**
     * 공지 내용
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 중요 공지 여부 (상단 고정)
     */
    @Column(name = "is_important", nullable = false)
    private Boolean isImportant = false;

    /**
     * 조회수
     */
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    /**
     * 코스 전체 공지 생성
     */
    public static CourseAnnouncement createForCourse(Long courseId, Long authorId, String title, String content, Boolean isImportant) {
        CourseAnnouncement announcement = new CourseAnnouncement();
        announcement.courseId = courseId;
        announcement.courseTimeId = null;
        announcement.authorId = authorId;
        announcement.title = title;
        announcement.content = content;
        announcement.isImportant = isImportant != null ? isImportant : false;
        announcement.viewCount = 0;
        return announcement;
    }

    /**
     * 차수별 공지 생성
     */
    public static CourseAnnouncement createForCourseTime(Long courseId, Long courseTimeId, Long authorId,
                                                          String title, String content, Boolean isImportant) {
        CourseAnnouncement announcement = createForCourse(courseId, authorId, title, content, isImportant);
        announcement.courseTimeId = courseTimeId;
        return announcement;
    }

    /**
     * 공지사항 수정
     */
    public void update(String title, String content, Boolean isImportant) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
        if (isImportant != null) {
            this.isImportant = isImportant;
        }
    }

    /**
     * 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 중요 공지로 설정
     */
    public void markAsImportant() {
        this.isImportant = true;
    }

    /**
     * 일반 공지로 변경
     */
    public void unmarkAsImportant() {
        this.isImportant = false;
    }
}
