package com.mzc.lp.domain.course.service;

import com.mzc.lp.domain.course.dto.request.CreateAnnouncementRequest;
import com.mzc.lp.domain.course.dto.request.UpdateAnnouncementRequest;
import com.mzc.lp.domain.course.dto.response.AnnouncementListResponse;
import com.mzc.lp.domain.course.dto.response.AnnouncementResponse;

public interface CourseAnnouncementService {

    /**
     * 코스 공지 작성
     */
    AnnouncementResponse createAnnouncement(Long courseId, Long authorId, CreateAnnouncementRequest request);

    /**
     * 차수별 공지 작성
     */
    AnnouncementResponse createAnnouncementForCourseTime(Long courseId, Long courseTimeId, Long authorId,
                                                          CreateAnnouncementRequest request);

    /**
     * 코스 공지 목록 조회
     */
    AnnouncementListResponse getAnnouncementsByCourse(Long courseId, int page, int pageSize);

    /**
     * 차수별 공지 목록 조회
     */
    AnnouncementListResponse getAnnouncementsByCourseTime(Long courseTimeId, int page, int pageSize);

    /**
     * 공지 상세 조회
     */
    AnnouncementResponse getAnnouncement(Long announcementId);

    /**
     * 공지 수정
     */
    AnnouncementResponse updateAnnouncement(Long announcementId, Long userId, UpdateAnnouncementRequest request, boolean isAdmin);

    /**
     * 공지 삭제
     */
    void deleteAnnouncement(Long announcementId, Long userId, boolean isAdmin);
}
