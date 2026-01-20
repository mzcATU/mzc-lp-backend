package com.mzc.lp.domain.ts.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.snapshot.entity.CourseSnapshot;
import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.DurationType;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import com.mzc.lp.domain.ts.exception.InvalidStatusTransitionException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "course_times", indexes = {
        @Index(name = "idx_course_times_status", columnList = "tenant_id, status"),
        @Index(name = "idx_course_times_course", columnList = "tenant_id, course_id"),
        @Index(name = "idx_course_times_snapshot", columnList = "tenant_id, snapshot_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseTime extends TenantEntity {

    // 낙관적 락 (동시 수정 감지)
    @Version
    private Long version;

    // Course 직접 연결 (등록된 강의)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    // Snapshot 직접 연결 (차수별 독립 스냅샷)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id")
    private CourseSnapshot snapshot;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

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

    @Column(name = "class_end_date")
    private LocalDate classEndDate;

    // 학습 기간 유형
    @Enumerated(EnumType.STRING)
    @Column(name = "duration_type", nullable = false, length = 20)
    private DurationType durationType;

    // 학습 일수 (FIXED: 자동 계산, RELATIVE: 필수, UNLIMITED: null)
    @Column(name = "duration_days")
    private Integer durationDays;

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

    // 정기 수업 일정 (FIXED + OFFLINE/BLENDED/LIVE에서 선택 사항)
    @Embedded
    private RecurringSchedule recurringSchedule;

    // 생성자 ID
    @Column(name = "created_by")
    private Long createdBy;

    // 정적 팩토리 메서드
    public static CourseTime cloneFrom(
            CourseTime source,
            String newTitle,
            String newDescription,
            LocalDate enrollStartDate,
            LocalDate enrollEndDate,
            LocalDate classStartDate,
            LocalDate classEndDate,
            // 운영 설정 (선택 - null이면 원본 복사)
            Integer capacity,
            BigDecimal price,
            Boolean isFree,
            String locationInfo,
            Boolean copyRecurringSchedule,
            Long createdBy
    ) {
        CourseTime courseTime = new CourseTime();

        // 원본 그대로 복사 (프론트에서 수정 불가)
        courseTime.course = source.course;
        courseTime.deliveryType = source.deliveryType;
        courseTime.durationType = source.durationType;
        courseTime.maxWaitingCount = source.maxWaitingCount;
        courseTime.enrollmentMethod = source.enrollmentMethod;
        courseTime.minProgressForCompletion = source.minProgressForCompletion;
        courseTime.allowLateEnrollment = source.allowLateEnrollment;

        // 운영 설정 - 요청값 있으면 사용, 없으면 원본 복사
        courseTime.capacity = capacity != null ? capacity : source.capacity;
        courseTime.locationInfo = locationInfo != null ? locationInfo : source.locationInfo;

        // 가격 설정 - isFree 처리
        if (isFree != null) {
            courseTime.free = isFree;
            courseTime.price = isFree ? BigDecimal.ZERO : (price != null ? price : source.price);
        } else {
            courseTime.free = source.free;
            courseTime.price = price != null ? price : source.price;
        }

        // 정기 일정 - 복사 여부에 따라
        courseTime.recurringSchedule = Boolean.TRUE.equals(copyRecurringSchedule)
                ? source.recurringSchedule
                : null;

        // 새로 지정하는 필드
        courseTime.title = newTitle;
        courseTime.description = newDescription;
        courseTime.enrollStartDate = enrollStartDate;
        courseTime.enrollEndDate = enrollEndDate;
        courseTime.classStartDate = classStartDate;
        courseTime.classEndDate = classEndDate;
        courseTime.createdBy = createdBy;

        // durationDays 계산 (FIXED 타입인 경우)
        if (courseTime.durationType == DurationType.FIXED && classEndDate != null) {
            courseTime.durationDays = (int) java.time.temporal.ChronoUnit.DAYS.between(classStartDate, classEndDate) + 1;
        } else {
            courseTime.durationDays = source.durationDays;
        }

        // 상태 자동 결정: 모집 시작일이 오늘이거나 이미 지남 → RECRUITING, 미래 → DRAFT
        courseTime.status = !enrollStartDate.isAfter(LocalDate.now())
                ? CourseTimeStatus.RECRUITING
                : CourseTimeStatus.DRAFT;
        courseTime.currentEnrollment = 0;

        return courseTime;
    }

    public static CourseTime create(
            String title,
            String description,
            DeliveryType deliveryType,
            DurationType durationType,
            LocalDate enrollStartDate,
            LocalDate enrollEndDate,
            LocalDate classStartDate,
            LocalDate classEndDate,
            Integer durationDays,
            Integer capacity,
            Integer maxWaitingCount,
            EnrollmentMethod enrollmentMethod,
            Integer minProgressForCompletion,
            BigDecimal price,
            boolean free,
            String locationInfo,
            boolean allowLateEnrollment,
            RecurringSchedule recurringSchedule,
            Long createdBy
    ) {
        CourseTime courseTime = new CourseTime();
        courseTime.title = title;
        courseTime.description = description;
        courseTime.deliveryType = deliveryType;
        courseTime.durationType = durationType;
        // 상태 자동 결정: 모집 시작일이 오늘이거나 이미 지남 → RECRUITING, 미래 → DRAFT
        courseTime.status = !enrollStartDate.isAfter(LocalDate.now())
                ? CourseTimeStatus.RECRUITING
                : CourseTimeStatus.DRAFT;
        courseTime.enrollStartDate = enrollStartDate;
        courseTime.enrollEndDate = enrollEndDate;
        courseTime.classStartDate = classStartDate;
        courseTime.classEndDate = classEndDate;

        // durationDays 계산: FIXED는 자동 계산, RELATIVE는 입력값 사용, UNLIMITED는 null
        if (durationType == DurationType.FIXED && classEndDate != null) {
            courseTime.durationDays = (int) java.time.temporal.ChronoUnit.DAYS.between(classStartDate, classEndDate) + 1;
        } else {
            courseTime.durationDays = durationDays;
        }

        courseTime.capacity = capacity;
        courseTime.maxWaitingCount = maxWaitingCount;
        courseTime.currentEnrollment = 0;
        courseTime.enrollmentMethod = enrollmentMethod;
        courseTime.minProgressForCompletion = minProgressForCompletion;
        courseTime.price = free ? BigDecimal.ZERO : price;
        courseTime.free = free;
        courseTime.locationInfo = locationInfo;
        courseTime.allowLateEnrollment = allowLateEnrollment;
        courseTime.recurringSchedule = recurringSchedule;
        courseTime.createdBy = createdBy;
        return courseTime;
    }

    // 비즈니스 메서드

    /**
     * Course와 Snapshot 직접 연결
     */
    public void linkCourseAndSnapshot(Course course, CourseSnapshot snapshot) {
        this.course = course;
        this.snapshot = snapshot;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateDescription(String description) {
        this.description = description;
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

        // FIXED 타입이면 durationDays 자동 계산
        if (this.durationType == DurationType.FIXED && classEndDate != null && classStartDate != null) {
            this.durationDays = (int) java.time.temporal.ChronoUnit.DAYS.between(classStartDate, classEndDate) + 1;
        }
    }

    public void updateDurationType(DurationType durationType, Integer durationDays) {
        this.durationType = durationType;

        if (durationType == DurationType.FIXED && this.classEndDate != null && this.classStartDate != null) {
            // FIXED: 자동 계산
            this.durationDays = (int) java.time.temporal.ChronoUnit.DAYS.between(this.classStartDate, this.classEndDate) + 1;
        } else if (durationType == DurationType.UNLIMITED) {
            // UNLIMITED: null
            this.durationDays = null;
            this.classEndDate = null;
        } else {
            // RELATIVE: 입력값 사용
            this.durationDays = durationDays;
            this.classEndDate = null;
        }
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

    public void updateRecurringSchedule(RecurringSchedule recurringSchedule) {
        this.recurringSchedule = recurringSchedule;
    }

    public void clearRecurringSchedule() {
        this.recurringSchedule = null;
    }

    // 상태 전이 메서드

    /**
     * DRAFT → RECRUITING 상태 전이
     * @throws InvalidStatusTransitionException DRAFT 상태가 아닌 경우
     */
    public void open() {
        if (this.status != CourseTimeStatus.DRAFT) {
            throw new InvalidStatusTransitionException(this.status, CourseTimeStatus.RECRUITING);
        }
        this.status = CourseTimeStatus.RECRUITING;
    }

    /**
     * RECRUITING → ONGOING 상태 전이
     * @throws InvalidStatusTransitionException RECRUITING 상태가 아닌 경우
     */
    public void startClass() {
        if (this.status != CourseTimeStatus.RECRUITING) {
            throw new InvalidStatusTransitionException(this.status, CourseTimeStatus.ONGOING);
        }
        this.status = CourseTimeStatus.ONGOING;
    }

    /**
     * ONGOING → CLOSED 상태 전이
     * @throws InvalidStatusTransitionException ONGOING 상태가 아닌 경우
     */
    public void close() {
        if (this.status != CourseTimeStatus.ONGOING) {
            throw new InvalidStatusTransitionException(this.status, CourseTimeStatus.CLOSED);
        }
        this.status = CourseTimeStatus.CLOSED;
    }

    /**
     * CLOSED → ARCHIVED 상태 전이
     * @throws InvalidStatusTransitionException CLOSED 상태가 아닌 경우
     */
    public void archive() {
        if (this.status != CourseTimeStatus.CLOSED) {
            throw new InvalidStatusTransitionException(this.status, CourseTimeStatus.ARCHIVED);
        }
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
