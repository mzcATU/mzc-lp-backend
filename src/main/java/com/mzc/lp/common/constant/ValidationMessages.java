package com.mzc.lp.common.constant;

/**
 * 도메인 검증 관련 공통 에러 메시지 상수
 */
public final class ValidationMessages {

    private ValidationMessages() {
        // 인스턴스화 방지
    }

    // Item 관련 메시지
    public static final String MAX_DEPTH_EXCEEDED = "최대 깊이(10단계)를 초과할 수 없습니다";
    public static final String ITEM_NAME_REQUIRED = "항목 이름은 필수입니다";
    public static final String ITEM_NAME_TOO_LONG = "항목 이름은 255자 이하여야 합니다";
    public static final String CANNOT_MOVE_TO_CHILD = "하위 항목으로 이동할 수 없습니다";

    // Relation 관련 메시지
    public static final String FOLDER_CANNOT_BE_IN_LEARNING_ORDER = "폴더는 학습 순서에 포함할 수 없습니다";
    public static final String START_POINT_REQUIRED = "시작점 항목은 필수입니다";
    public static final String TARGET_ITEM_REQUIRED = "대상 항목은 필수입니다";
    public static final String CANNOT_REFERENCE_SELF = "자기 자신을 참조할 수 없습니다";

    // 공통 Title 관련 메시지
    public static final String TITLE_REQUIRED = "제목은 필수입니다";
    public static final String TITLE_TOO_LONG = "제목은 255자 이하여야 합니다";
    public static final String COURSE_TITLE_REQUIRED = "강의 제목은 필수입니다";
    public static final String COURSE_TITLE_TOO_LONG = "강의 제목은 255자 이하여야 합니다";

    // 반려 관련 메시지
    public static final String REJECTION_REASON_REQUIRED = "반려 사유는 필수입니다";
    public static final String REJECTION_REASON_TOO_LONG = "반려 사유는 500자 이하여야 합니다";
}
