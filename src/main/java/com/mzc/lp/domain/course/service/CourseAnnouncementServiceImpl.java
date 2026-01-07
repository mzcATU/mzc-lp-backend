package com.mzc.lp.domain.course.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.course.dto.request.CreateAnnouncementRequest;
import com.mzc.lp.domain.course.dto.request.UpdateAnnouncementRequest;
import com.mzc.lp.domain.course.dto.response.AnnouncementListResponse;
import com.mzc.lp.domain.course.dto.response.AnnouncementResponse;
import com.mzc.lp.domain.course.entity.CourseAnnouncement;
import com.mzc.lp.domain.course.exception.AnnouncementNotFoundException;
import com.mzc.lp.domain.course.exception.NotAnnouncementAuthorException;
import com.mzc.lp.domain.course.repository.CourseAnnouncementRepository;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CourseAnnouncementServiceImpl implements CourseAnnouncementService {

    private final CourseAnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AnnouncementResponse createAnnouncement(Long courseId, Long authorId, CreateAnnouncementRequest request) {
        CourseAnnouncement announcement = CourseAnnouncement.createForCourse(
                courseId,
                authorId,
                request.title(),
                request.content(),
                request.isImportant()
        );

        CourseAnnouncement saved = announcementRepository.save(announcement);
        User author = userRepository.findById(authorId).orElse(null);

        return AnnouncementResponse.from(saved, author);
    }

    @Override
    @Transactional
    public AnnouncementResponse createAnnouncementForCourseTime(Long courseId, Long courseTimeId, Long authorId,
                                                                 CreateAnnouncementRequest request) {
        CourseAnnouncement announcement = CourseAnnouncement.createForCourseTime(
                courseId,
                courseTimeId,
                authorId,
                request.title(),
                request.content(),
                request.isImportant()
        );

        CourseAnnouncement saved = announcementRepository.save(announcement);
        User author = userRepository.findById(authorId).orElse(null);

        return AnnouncementResponse.from(saved, author);
    }

    @Override
    public AnnouncementListResponse getAnnouncementsByCourse(Long courseId, int page, int pageSize) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<CourseAnnouncement> announcementPage = announcementRepository.findByCourseId(courseId, tenantId, pageable);

        return toListResponse(announcementPage, page, pageSize);
    }

    @Override
    public AnnouncementListResponse getAnnouncementsByCourseTime(Long courseTimeId, int page, int pageSize) {
        Long tenantId = TenantContext.getCurrentTenantId();
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<CourseAnnouncement> announcementPage = announcementRepository.findByCourseTimeId(courseTimeId, tenantId, pageable);

        return toListResponse(announcementPage, page, pageSize);
    }

    @Override
    @Transactional
    public AnnouncementResponse getAnnouncement(Long announcementId) {
        Long tenantId = TenantContext.getCurrentTenantId();

        CourseAnnouncement announcement = announcementRepository.findByIdAndTenantId(announcementId, tenantId)
                .orElseThrow(() -> new AnnouncementNotFoundException(announcementId));

        // 조회수 증가
        announcementRepository.incrementViewCount(announcementId);
        announcement.incrementViewCount();

        User author = userRepository.findById(announcement.getAuthorId()).orElse(null);

        return AnnouncementResponse.from(announcement, author);
    }

    @Override
    @Transactional
    public AnnouncementResponse updateAnnouncement(Long announcementId, Long userId, UpdateAnnouncementRequest request,
                                                    boolean isAdmin) {
        Long tenantId = TenantContext.getCurrentTenantId();

        CourseAnnouncement announcement = announcementRepository.findByIdAndTenantId(announcementId, tenantId)
                .orElseThrow(() -> new AnnouncementNotFoundException(announcementId));

        // 권한 검증: 작성자이거나 관리자
        if (!isAdmin && !announcement.getAuthorId().equals(userId)) {
            throw new NotAnnouncementAuthorException(announcementId);
        }

        announcement.update(request.title(), request.content(), request.isImportant());

        User author = userRepository.findById(announcement.getAuthorId()).orElse(null);

        return AnnouncementResponse.from(announcement, author);
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Long announcementId, Long userId, boolean isAdmin) {
        Long tenantId = TenantContext.getCurrentTenantId();

        CourseAnnouncement announcement = announcementRepository.findByIdAndTenantId(announcementId, tenantId)
                .orElseThrow(() -> new AnnouncementNotFoundException(announcementId));

        // 권한 검증: 작성자이거나 관리자
        if (!isAdmin && !announcement.getAuthorId().equals(userId)) {
            throw new NotAnnouncementAuthorException(announcementId);
        }

        announcementRepository.delete(announcement);
    }

    private AnnouncementListResponse toListResponse(Page<CourseAnnouncement> announcementPage, int page, int pageSize) {
        List<CourseAnnouncement> announcements = announcementPage.getContent();

        if (announcements.isEmpty()) {
            return AnnouncementListResponse.of(List.of(), 0, page, pageSize, 0);
        }

        // 작성자 벌크 조회
        Set<Long> authorIds = announcements.stream()
                .map(CourseAnnouncement::getAuthorId)
                .collect(Collectors.toSet());
        Map<Long, User> authorMap = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<AnnouncementResponse> responses = announcements.stream()
                .map(a -> AnnouncementResponse.from(a, authorMap.get(a.getAuthorId())))
                .toList();

        return AnnouncementListResponse.of(
                responses,
                announcementPage.getTotalElements(),
                page,
                pageSize,
                announcementPage.getTotalPages()
        );
    }
}
