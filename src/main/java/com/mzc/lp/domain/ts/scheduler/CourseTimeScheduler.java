package com.mzc.lp.domain.ts.scheduler;

import com.mzc.lp.domain.notification.constant.NotificationType;
import com.mzc.lp.domain.notification.service.NotificationService;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseTimeScheduler {

    private final CourseTimeRepository courseTimeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationService notificationService;

    /**
     * RECRUITING → ONGOING 상태 전환 배치
     * classStartDate가 도래한 차수를 ONGOING으로 전환
     * 매일 자정(00:00)에 실행
     */
    @Scheduled(cron = "${scheduler.course-time.start-class:0 0 0 * * *}")
    @Transactional
    public void startCourseTimes() {
        LocalDate today = LocalDate.now();
        log.info("[Batch] Starting course times: date={}", today);

        List<CourseTime> courseTimesToStart = courseTimeRepository
                .findByStatusAndClassStartDateLessThanEqual(CourseTimeStatus.RECRUITING, today);

        if (courseTimesToStart.isEmpty()) {
            log.info("[Batch] No course times to start");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (CourseTime courseTime : courseTimesToStart) {
            try {
                courseTime.startClass();
                successCount++;
                log.info("[Batch] Course time started: id={}, title={}",
                        courseTime.getId(), courseTime.getTitle());

                // COURSE 알림 발송: 강의 시작 알림
                sendCourseStartNotifications(courseTime);
            } catch (Exception e) {
                failCount++;
                log.error("[Batch] Failed to start course time: id={}, error={}",
                        courseTime.getId(), e.getMessage());
            }
        }

        log.info("[Batch] Start course times completed: total={}, success={}, fail={}",
                courseTimesToStart.size(), successCount, failCount);
    }

    /**
     * 강의 시작 알림 발송 (COURSE 타입)
     * 해당 차수에 등록된 모든 수강생에게 알림 발송
     */
    private void sendCourseStartNotifications(CourseTime courseTime) {
        try {
            List<Long> userIds = enrollmentRepository.findUserIdsByCourseTimeId(courseTime.getId());
            log.info("[Batch] Sending course start notification to {} users for courseTime: {}",
                    userIds.size(), courseTime.getId());

            String title = "강의 시작 안내";
            String message = String.format("[%s] 강의가 시작되었습니다.", courseTime.getTitle());
            String link = "/my-courses/" + courseTime.getId();

            for (Long userId : userIds) {
                try {
                    notificationService.createNotification(
                            userId,
                            NotificationType.COURSE,
                            title,
                            message,
                            link,
                            courseTime.getId(),
                            "COURSE_TIME",
                            null,
                            null
                    );
                } catch (Exception e) {
                    log.warn("[Batch] Failed to send course start notification to user {}: {}",
                            userId, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[Batch] Failed to send course start notifications for courseTime {}: {}",
                    courseTime.getId(), e.getMessage());
        }
    }

    /**
     * ONGOING → CLOSED 상태 전환 배치
     * classEndDate가 경과한 차수를 CLOSED로 전환
     * 매일 자정(00:00)에 실행
     */
    @Scheduled(cron = "${scheduler.course-time.close-class:0 0 0 * * *}")
    @Transactional
    public void closeCourseTimes() {
        LocalDate today = LocalDate.now();
        log.info("[Batch] Closing course times: date={}", today);

        List<CourseTime> courseTimesToClose = courseTimeRepository
                .findByStatusAndClassEndDateLessThan(CourseTimeStatus.ONGOING, today);

        if (courseTimesToClose.isEmpty()) {
            log.info("[Batch] No course times to close");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (CourseTime courseTime : courseTimesToClose) {
            try {
                courseTime.close();
                successCount++;
                log.info("[Batch] Course time closed: id={}, title={}",
                        courseTime.getId(), courseTime.getTitle());

                // COURSE 알림 발송: 강의 종료 알림
                sendCourseEndNotifications(courseTime);
            } catch (Exception e) {
                failCount++;
                log.error("[Batch] Failed to close course time: id={}, error={}",
                        courseTime.getId(), e.getMessage());
            }
        }

        log.info("[Batch] Close course times completed: total={}, success={}, fail={}",
                courseTimesToClose.size(), successCount, failCount);
    }

    /**
     * 강의 종료 알림 발송 (COURSE 타입)
     * 해당 차수에 등록된 모든 수강생에게 알림 발송
     */
    private void sendCourseEndNotifications(CourseTime courseTime) {
        try {
            List<Long> userIds = enrollmentRepository.findUserIdsByCourseTimeId(courseTime.getId());
            log.info("[Batch] Sending course end notification to {} users for courseTime: {}",
                    userIds.size(), courseTime.getId());

            String title = "강의 종료 안내";
            String message = String.format("[%s] 강의가 종료되었습니다.", courseTime.getTitle());
            String link = "/my-courses/" + courseTime.getId();

            for (Long userId : userIds) {
                try {
                    notificationService.createNotification(
                            userId,
                            NotificationType.COURSE,
                            title,
                            message,
                            link,
                            courseTime.getId(),
                            "COURSE_TIME",
                            null,
                            null
                    );
                } catch (Exception e) {
                    log.warn("[Batch] Failed to send course end notification to user {}: {}",
                            userId, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[Batch] Failed to send course end notifications for courseTime {}: {}",
                    courseTime.getId(), e.getMessage());
        }
    }
}
