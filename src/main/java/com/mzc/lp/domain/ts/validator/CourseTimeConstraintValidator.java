package com.mzc.lp.domain.ts.validator;

import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.ts.constant.*;
import com.mzc.lp.domain.ts.dto.request.CreateCourseTimeRequest;
import com.mzc.lp.domain.ts.dto.request.UpdateCourseTimeRequest;
import com.mzc.lp.domain.ts.dto.response.*;
import com.mzc.lp.domain.ts.entity.CourseTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

/**
 * CourseTime 제약 조건 검증기
 */
@Component
@RequiredArgsConstructor
public class CourseTimeConstraintValidator {

    /**
     * 차수 생성 요청 검증
     */
    public CourseTimeValidationResult validate(CreateCourseTimeRequest request, Course course) {
        CourseTimeValidationResult.Builder builder = CourseTimeValidationResult.builder();

        // 일관성 제약 검증
        validateConsistencyConstraints(builder, request.durationType(), request.classStartDate(),
                request.classEndDate(), request.durationDays(), request.enrollEndDate());

        // DeliveryType 제약 검증
        validateDeliveryTypeConstraints(builder, request.deliveryType(), request.durationType(),
                request.locationInfo(), request.capacity());

        // EnrollmentMethod 제약 검증
        validateEnrollmentMethodConstraints(builder, request.enrollmentMethod(),
                request.maxWaitingCount(), request.capacity());

        // Course 연동 제약 검증
        validateCourseConstraints(builder, request.deliveryType(), course);

        // QualityRating 평가
        evaluateQualityRating(builder, request.deliveryType(), request.durationType(),
                request.enrollmentMethod(), request.capacity());

        return builder.build();
    }

    /**
     * 차수 수정 요청 검증
     */
    public CourseTimeValidationResult validate(UpdateCourseTimeRequest request, CourseTime existing, Course course) {
        CourseTimeValidationResult.Builder builder = CourseTimeValidationResult.builder();

        DurationType durationType = request.durationType() != null ? request.durationType() : existing.getDurationType();
        DeliveryType deliveryType = request.deliveryType() != null ? request.deliveryType() : existing.getDeliveryType();
        EnrollmentMethod enrollmentMethod = request.enrollmentMethod() != null ? request.enrollmentMethod() : existing.getEnrollmentMethod();
        LocalDate classStartDate = request.classStartDate() != null ? request.classStartDate() : existing.getClassStartDate();
        LocalDate classEndDate = request.classEndDate() != null ? request.classEndDate() : existing.getClassEndDate();
        LocalDate enrollEndDate = request.enrollEndDate() != null ? request.enrollEndDate() : existing.getEnrollEndDate();
        Integer durationDays = request.durationDays() != null ? request.durationDays() : existing.getDurationDays();
        Integer capacity = request.capacity() != null ? request.capacity() : existing.getCapacity();
        Integer maxWaitingCount = request.maxWaitingCount() != null ? request.maxWaitingCount() : existing.getMaxWaitingCount();
        String locationInfo = request.locationInfo() != null ? request.locationInfo() : existing.getLocationInfo();

        // 일관성 제약 검증
        validateConsistencyConstraints(builder, durationType, classStartDate, classEndDate, durationDays, enrollEndDate);

        // DeliveryType 제약 검증
        validateDeliveryTypeConstraints(builder, deliveryType, durationType, locationInfo, capacity);

        // EnrollmentMethod 제약 검증
        validateEnrollmentMethodConstraints(builder, enrollmentMethod, maxWaitingCount, capacity);

        // Course 연동 제약 검증
        validateCourseConstraints(builder, deliveryType, course);

        // QualityRating 평가
        evaluateQualityRating(builder, deliveryType, durationType, enrollmentMethod, capacity);

        return builder.build();
    }

    /**
     * 일관성 제약 검증 (R61-R65)
     */
    private void validateConsistencyConstraints(
            CourseTimeValidationResult.Builder builder,
            DurationType durationType,
            LocalDate classStartDate,
            LocalDate classEndDate,
            Integer durationDays,
            LocalDate enrollEndDate
    ) {
        // R61: FIXED 타입은 classEndDate 필수
        if (durationType == DurationType.FIXED && classEndDate == null) {
            builder.addError(ValidationError.clientValidatable(
                    ValidationRule.R61.getCode(),
                    "classEndDate",
                    I18nMessage.of(ValidationRule.R61.getMessageCode())
            ));
        }

        // R62: RELATIVE 타입은 durationDays 필수
        if (durationType == DurationType.RELATIVE && (durationDays == null || durationDays <= 0)) {
            builder.addError(ValidationError.clientValidatable(
                    ValidationRule.R62.getCode(),
                    "durationDays",
                    I18nMessage.of(ValidationRule.R62.getMessageCode())
            ));
        }

        // R63: UNLIMITED 타입은 classEndDate가 null이어야 함
        if (durationType == DurationType.UNLIMITED && classEndDate != null) {
            builder.addError(ValidationError.clientValidatable(
                    ValidationRule.R63.getCode(),
                    "classEndDate",
                    I18nMessage.of(ValidationRule.R63.getMessageCode())
            ));
        }

        // R64: 모집 종료일은 학습 시작일 이전이어야 함
        if (enrollEndDate != null && classStartDate != null && enrollEndDate.isAfter(classStartDate)) {
            builder.addError(ValidationError.clientValidatable(
                    ValidationRule.R64.getCode(),
                    "enrollEndDate",
                    I18nMessage.of(ValidationRule.R64.getMessageCode(),
                            Map.of("enrollEndDate", enrollEndDate.toString(), "classStartDate", classStartDate.toString()))
            ));
        }

        // R65: 학습 종료일은 학습 시작일 이후여야 함
        if (classEndDate != null && classStartDate != null && classEndDate.isBefore(classStartDate)) {
            builder.addError(ValidationError.clientValidatable(
                    ValidationRule.R65.getCode(),
                    "classEndDate",
                    I18nMessage.of(ValidationRule.R65.getMessageCode(),
                            Map.of("classEndDate", classEndDate.toString(), "classStartDate", classStartDate.toString()))
            ));
        }
    }

    /**
     * DeliveryType 제약 검증 (R10-R15)
     */
    private void validateDeliveryTypeConstraints(
            CourseTimeValidationResult.Builder builder,
            DeliveryType deliveryType,
            DurationType durationType,
            String locationInfo,
            Integer capacity
    ) {
        // R10: OFFLINE/BLENDED는 locationInfo 필수
        if ((deliveryType == DeliveryType.OFFLINE || deliveryType == DeliveryType.BLENDED)
                && (locationInfo == null || locationInfo.isBlank())) {
            builder.addError(ValidationError.serverOnly(
                    ValidationRule.R10.getCode(),
                    "locationInfo",
                    I18nMessage.of(ValidationRule.R10.getMessageCode(),
                            Map.of("deliveryType", deliveryType.name()))
            ));
        }

        // R14: LIVE는 FIXED 타입 필수
        if (deliveryType == DeliveryType.LIVE && durationType != DurationType.FIXED) {
            builder.addError(ValidationError.serverOnly(
                    ValidationRule.R14.getCode(),
                    "durationType",
                    I18nMessage.of(ValidationRule.R14.getMessageCode())
            ));
        }

        // R15: ONLINE + UNLIMITED + 정원 없음은 경고
        if (deliveryType == DeliveryType.ONLINE && durationType == DurationType.UNLIMITED && capacity == null) {
            builder.addWarning(ValidationWarning.common(
                    ValidationRule.R15.getCode(),
                    "capacity",
                    I18nMessage.of(ValidationRule.R15.getMessageCode())
            ));
        }
    }

    /**
     * EnrollmentMethod 제약 검증 (R50-R53)
     */
    private void validateEnrollmentMethodConstraints(
            CourseTimeValidationResult.Builder builder,
            EnrollmentMethod enrollmentMethod,
            Integer maxWaitingCount,
            Integer capacity
    ) {
        // R53: APPROVAL + 대기자 > 0 은 불가 (비즈니스 정책)
        if (enrollmentMethod == EnrollmentMethod.APPROVAL && maxWaitingCount != null && maxWaitingCount > 0) {
            builder.addError(ValidationError.serverOnly(
                    ValidationRule.R53.getCode(),
                    "maxWaitingCount",
                    I18nMessage.of(ValidationRule.R53.getMessageCode())
            ));
        }

        // R50: INVITE_ONLY는 정원 설정 권장
        if (enrollmentMethod == EnrollmentMethod.INVITE_ONLY && capacity == null) {
            builder.addWarning(ValidationWarning.caution(
                    ValidationRule.R50.getCode(),
                    "capacity",
                    I18nMessage.of(ValidationRule.R50.getMessageCode())
            ));
        }

        // R52: FIRST_COME + 정원 있으면 대기자 설정 권장
        if (enrollmentMethod == EnrollmentMethod.FIRST_COME && capacity != null
                && (maxWaitingCount == null || maxWaitingCount == 0)) {
            builder.addWarning(ValidationWarning.common(
                    ValidationRule.R52.getCode(),
                    "maxWaitingCount",
                    I18nMessage.of(ValidationRule.R52.getMessageCode())
            ));
        }
    }

    /**
     * Course 연동 제약 검증 (R70)
     */
    private void validateCourseConstraints(
            CourseTimeValidationResult.Builder builder,
            DeliveryType deliveryType,
            Course course
    ) {
        if (course == null || course.getType() == null) {
            return;
        }

        DeliveryType expectedDeliveryType = mapCourseTypeToDeliveryType(course.getType());
        if (expectedDeliveryType != null && deliveryType != expectedDeliveryType) {
            builder.addWarning(ValidationWarning.common(
                    ValidationRule.R70.getCode(),
                    "deliveryType",
                    I18nMessage.of(ValidationRule.R70.getMessageCode(),
                            Map.of("courseType", course.getType().name(),
                                    "expectedDeliveryType", expectedDeliveryType.name(),
                                    "actualDeliveryType", deliveryType.name()))
            ));
        }
    }

    /**
     * QualityRating 평가
     */
    private void evaluateQualityRating(
            CourseTimeValidationResult.Builder builder,
            DeliveryType deliveryType,
            DurationType durationType,
            EnrollmentMethod enrollmentMethod,
            Integer capacity
    ) {
        QualityRating rating = QualityRating.BEST;

        // DeliveryType별 권장 DurationType 평가
        rating = evaluateDeliveryTypeDurationCombination(rating, deliveryType, durationType);

        // EnrollmentMethod + Capacity 평가
        rating = evaluateEnrollmentMethodCapacityCombination(rating, enrollmentMethod, capacity);

        builder.setQualityRating(rating);
    }

    private QualityRating evaluateDeliveryTypeDurationCombination(
            QualityRating current,
            DeliveryType deliveryType,
            DurationType durationType
    ) {
        // ONLINE + RELATIVE = BEST
        if (deliveryType == DeliveryType.ONLINE && durationType == DurationType.RELATIVE) {
            return current;
        }

        // ONLINE + UNLIMITED = GOOD
        if (deliveryType == DeliveryType.ONLINE && durationType == DurationType.UNLIMITED) {
            return maxRating(current, QualityRating.GOOD);
        }

        // OFFLINE/BLENDED/LIVE + FIXED = BEST
        if ((deliveryType == DeliveryType.OFFLINE || deliveryType == DeliveryType.BLENDED || deliveryType == DeliveryType.LIVE)
                && durationType == DurationType.FIXED) {
            return current;
        }

        // ONLINE + FIXED = COMMON (비권장이지만 허용)
        if (deliveryType == DeliveryType.ONLINE && durationType == DurationType.FIXED) {
            return maxRating(current, QualityRating.COMMON);
        }

        // OFFLINE/BLENDED + RELATIVE/UNLIMITED = CAUTION
        if ((deliveryType == DeliveryType.OFFLINE || deliveryType == DeliveryType.BLENDED)
                && (durationType == DurationType.RELATIVE || durationType == DurationType.UNLIMITED)) {
            return maxRating(current, QualityRating.CAUTION);
        }

        return current;
    }

    private QualityRating evaluateEnrollmentMethodCapacityCombination(
            QualityRating current,
            EnrollmentMethod enrollmentMethod,
            Integer capacity
    ) {
        // APPROVAL + 정원 없음 = COMMON (경고만)
        if (enrollmentMethod == EnrollmentMethod.APPROVAL && capacity == null) {
            return maxRating(current, QualityRating.COMMON);
        }

        return current;
    }

    private QualityRating maxRating(QualityRating a, QualityRating b) {
        return a.ordinal() > b.ordinal() ? a : b;
    }

    private DeliveryType mapCourseTypeToDeliveryType(com.mzc.lp.domain.course.constant.CourseType courseType) {
        return switch (courseType) {
            case ONLINE -> DeliveryType.ONLINE;
            case OFFLINE -> DeliveryType.OFFLINE;
            case BLENDED -> DeliveryType.BLENDED;
        };
    }
}
