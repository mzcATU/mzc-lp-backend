package com.mzc.lp.domain.ts.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 차수 생성/수정 검증 규칙
 */
@Getter
@RequiredArgsConstructor
public enum ValidationRule {

    // DeliveryType 제약 (R10-R19)
    R10("R10", "constraint.deliveryType.locationRequired", true),
    R11("R11", "constraint.deliveryType.online.relative.recommended", false),
    R12("R12", "constraint.deliveryType.offline.fixed.recommended", false),
    R13("R13", "constraint.deliveryType.blended.fixed.recommended", false),
    R14("R14", "constraint.deliveryType.live.fixed.required", true),
    R15("R15", "constraint.deliveryType.online.unlimited.noCapacity", false),

    // EnrollmentMethod 제약 (R50-R59)
    R50("R50", "constraint.enrollmentMethod.inviteOnly.noCapacity", false),
    R51("R51", "constraint.enrollmentMethod.approval.maxWaiting", false),
    R52("R52", "constraint.enrollmentMethod.firstCome.waitingList.recommended", false),
    R53("R53", "constraint.enrollmentMethod.approval.noWaitingList", true),

    // 일관성 제약 (R60-R69)
    R61("R61", "constraint.consistency.fixed.classEndDateRequired", true),
    R62("R62", "constraint.consistency.relative.durationDaysRequired", true),
    R63("R63", "constraint.consistency.unlimited.noClassEndDate", true),
    R64("R64", "constraint.consistency.enrollEndBeforeClassStart", true),
    R65("R65", "constraint.consistency.classEndAfterClassStart", true),

    // Course 연동 제약 (R70-R79)
    R70("R70", "constraint.course.deliveryTypeMismatch", false);

    private final String code;
    private final String messageCode;
    private final boolean blocking;

    public boolean isWarning() {
        return !blocking;
    }
}
