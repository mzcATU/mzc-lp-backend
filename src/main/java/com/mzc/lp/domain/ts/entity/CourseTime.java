package com.mzc.lp.domain.ts.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.program.entity.Program;
import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "course_times", indexes = {
        @Index(name = "idx_course_times_status", columnList = "tenant_id, status"),
        @Index(name = "idx_course_times_course", columnList = "tenant_id, cm_course_id"),
        @Index(name = "idx_course_times_program", columnList = "tenant_id, program_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseTime extends TenantEntity {

    // 낙관적 락 (동시 수정 감지)
    @Version
    private Long version;

    // Program 연결 (승인된 프로그램과 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private Program program;

    // CM 연결 (deprecated - Program을 통해 Snapshot으로 연결됨)
    @Deprecated
    @Column(name = "cm_course_id")
    private Long cmCourseId;

    @Deprecated
    @Column(name = "cm_course_version_id")
    private Long cmCourseVersionId;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false, length = 20)
    private DeliveryType deliveryType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CourseTimeStatus status;

    // 모집 기간
    @Column(name = "enroll_start_date", nullable = false)
    private LocalDate enrollStartDate;

    @Column(name = "enroll_end_date", nullable = false)
    private LocalDate enrollEndDate;

    // 학습 기간
    @Column(name = "class_start_date", nullable = false)
    private LocalDate classStartDate;

    @Column(name = "class_end_date", nullable = false)
    private LocalDate classEndDate;

    // 정원 (null = 무제한)
    private Integer capacity;

    // 대기자 수 (null = 대기 불가)
    @Column(name = "max_waiting_count")
    private Integer maxWaitingCount;

    // 현재 등록 인원 (SIS에서 관리하지만 조회 성능을 위해 캐싱)
    @Column(name = "current_enrollment", nullable = false)
    private Integer currentEnrollment;

    @Enumerated(EnumType.STRING)
    @Column(name = "enrollment_method", nullable = false, length = 20)
    private EnrollmentMethod enrollmentMethod;

    // 수료 기준 (0-100%)
    @Column(name = "min_progress_for_completion", nullable = false)
    private Integer minProgressForCompletion;

    // 가격
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "is_free", nullable = false)
    private boolean free;

    // 장소 정보 (OFFLINE/BLENDED일 때 필수)
    @Column(name = "location_info", columnDefinition = "json")
    private String locationInfo;

    // 중간 합류 허용 여부
    @Column(name = "allow_late_enrollment", nullable = false)
    private boolean allowLateEnrollment;

    // 생성자 ID
    @Column(name = "created_by")
    private Long createdBy;

    // 정적 팩토리 메서드
    public static CourseTime cloneFrom(
            CourseTime source,
            String newTitle,
            LocalDate enrollStartDate,
            LocalDate enrollEndDate,
            LocalDate classStartDate,
            LocalDate classEndDate,
            Long createdBy
    ) {
        CourseTime courseTime = new CourseTime();
        // 복제 대상 필드
        courseTime.program = source.program;
        courseTime.deliveryType = source.deliveryType;
        courseTime.capacity = source.capacity;
        courseTime.maxWaitingCount = source.maxWaitingCount;
        courseTime.enrollmentMethod = source.enrollmentMethod;
        courseTime.minProgressForCompletion = source.minProgressForCompletion;
        courseTime.price = source.price;
        courseTime.free = source.free;
        courseTime.locationInfo = source.locationInfo;
        courseTime.allowLateEnrollment = source.allowLateEnrollment;
        courseTime.cmCourseId = source.cmCourseId;
        courseTime.cmCourseVersionId = source.cmCourseVersionId;

        // 새로 지정하는 필드
        courseTime.title = newTitle;
        courseTime.enrollStartDate = enrollStartDate;
        courseTime.enrollEndDate = enrollEndDate;
        courseTime.classStartDate = classStartDate;
        courseTime.classEndDate = classEndDate;
        courseTime.createdBy = createdBy;

        // 고정 값
        courseTime.status = CourseTimeStatus.DRAFT;
        courseTime.currentEnrollment = 0;

        return courseTime;
    }

    public static CourseTime create(
            String title,
            DeliveryType deliveryType,
            LocalDate enrollStartDate,
            LocalDate enrollEndDate,
            LocalDate classStartDate,
            LocalDate classEndDate,
            Integer capacity,
            Integer maxWaitingCount,
            EnrollmentMethod enrollmentMethod,
            Integer minProgressForCompletion,
            BigDecimal price,
            boolean free,
            String locationInfo,
            boolean allowLateEnrollment,
            Long createdBy
    ) {
        CourseTime courseTime = new CourseTime();
        courseTime.title = title;
        courseTime.deliveryType = deliveryType;
        courseTime.status = CourseTimeStatus.DRAFT;
        courseTime.enrollStartDate = enrollStartDate;
        courseTime.enrollEndDate = enrollEndDate;
        courseTime.classStartDate = classStartDate;
        courseTime.classEndDate = classEndDate;
        courseTime.capacity = capacity;
        courseTime.maxWaitingCount = maxWaitingCount;
        courseTime.currentEnrollment = 0;
        courseTime.enrollmentMethod = enrollmentMethod;
        courseTime.minProgressForCompletion = minProgressForCompletion;
        courseTime.price = free ? BigDecimal.ZERO : price;
        courseTime.free = free;
        courseTime.locationInfo = locationInfo;
        courseTime.allowLateEnrollment = allowLateEnrollment;
        courseTime.createdBy = createdBy;
        return courseTime;
    }

    // 비즈니스 메서드

    /**
     * 승인된 Program과 연결
     */
    public void linkProgram(Program program) {
        this.program = program;
    }

    /**
     * @deprecated Program을 통해 Snapshot으로 연결하세요
     */
    @Deprecated
    public void linkCourse(Long cmCourseId, Long cmCourseVersionId) {
        this.cmCourseId = cmCourseId;
        this.cmCourseVersionId = cmCourseVersionId;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updatePeriod(
            LocalDate enrollStartDate,
            LocalDate enrollEndDate,
            LocalDate classStartDate,
            LocalDate classEndDate
    ) {
        this.enrollStartDate = enrollStartDate;
        this.enrollEndDate = enrollEndDate;
        this.classStartDate = classStartDate;
        this.classEndDate = classEndDate;
    }

    public void updateCapacity(Integer capacity, Integer maxWaitingCount) {
        this.capacity = capacity;
        this.maxWaitingCount = maxWaitingCount;
    }

    public void updatePrice(BigDecimal price, boolean free) {
        this.free = free;
        this.price = free ? BigDecimal.ZERO : price;
    }

    public void updateLocationInfo(String locationInfo) {
        this.locationInfo = locationInfo;
    }

    public void updateAllowLateEnrollment(boolean allowLateEnrollment) {
        this.allowLateEnrollment = allowLateEnrollment;
    }

    public void updateMinProgress(Integer minProgressForCompletion) {
        this.minProgressForCompletion = minProgressForCompletion;
    }

    // 상태 전이 메서드

    public void open() {
        this.status = CourseTimeStatus.RECRUITING;
    }

    public void startClass() {
        this.status = CourseTimeStatus.ONGOING;
    }

    public void close() {
        this.status = CourseTimeStatus.CLOSED;
    }

    public void archive() {
        this.status = CourseTimeStatus.ARCHIVED;
    }

    // 정원 관리

    public void incrementEnrollment() {
        this.currentEnrollment++;
    }

    public void decrementEnrollment() {
        if (this.currentEnrollment > 0) {
            this.currentEnrollment--;
        }
    }

    // 검증 메서드

    public boolean isDraft() {
        return this.status == CourseTimeStatus.DRAFT;
    }

    public boolean isRecruiting() {
        return this.status == CourseTimeStatus.RECRUITING;
    }

    public boolean isOngoing() {
        return this.status == CourseTimeStatus.ONGOING;
    }

    public boolean isClosed() {
        return this.status == CourseTimeStatus.CLOSED;
    }

    public boolean isArchived() {
        return this.status == CourseTimeStatus.ARCHIVED;
    }

    public boolean hasUnlimitedCapacity() {
        return this.capacity == null;
    }

    public boolean hasAvailableSeats() {
        return hasUnlimitedCapacity() || this.currentEnrollment < this.capacity;
    }

    public int getAvailableSeats() {
        if (hasUnlimitedCapacity()) {
            return Integer.MAX_VALUE;
        }
        return Math.max(0, this.capacity - this.currentEnrollment);
    }

    public boolean requiresLocationInfo() {
        return this.deliveryType == DeliveryType.OFFLINE
                || this.deliveryType == DeliveryType.BLENDED;
    }

    public boolean canEnroll() {
        if (isRecruiting()) {
            return true;
        }
        if (isOngoing()) {
            return this.allowLateEnrollment;
        }
        return false;
    }
}
