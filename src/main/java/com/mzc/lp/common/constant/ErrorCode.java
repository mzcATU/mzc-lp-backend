package com.mzc.lp.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "Invalid input value"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "Internal server error"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "Entity not found"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "User not found"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "Email already exists"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "U003", "Invalid password format"),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "U004", "Current password is incorrect"),
    USER_ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "U005", "User already withdrawn"),
    ROLE_ALREADY_EXISTS(HttpStatus.CONFLICT, "U006", "Role already exists for this user"),
    COURSE_ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "U007", "Course role not found"),
    INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "U008", "Invalid image format. Only JPG, JPEG, PNG allowed"),
    IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "U009", "Image size exceeds 5MB limit"),
    FILE_STORAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "U010", "Failed to store file"),

    // Course (CM - Course Matrix)
    CM_COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "CM001", "Course not found"),
    CM_COURSE_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CM002", "CourseItem not found"),
    CM_MAX_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "CM003", "Max depth exceeded (10)"),
    CM_CIRCULAR_REFERENCE(HttpStatus.BAD_REQUEST, "CM004", "Circular reference detected"),
    CM_INVALID_PARENT(HttpStatus.BAD_REQUEST, "CM005", "Invalid parent"),
    CM_LEARNING_OBJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "CM008", "LearningObject not found"),

    // CourseTime (TS)
    COURSE_TIME_NOT_FOUND(HttpStatus.NOT_FOUND, "TS001", "CourseTime not found"),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "TS002", "Invalid status transition"),
    CAPACITY_EXCEEDED(HttpStatus.CONFLICT, "TS003", "Capacity exceeded"),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "TS004", "Invalid date range"),
    LOCATION_REQUIRED(HttpStatus.BAD_REQUEST, "TS005", "Location info required for OFFLINE/BLENDED"),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "Unauthorized"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A002", "Access denied"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A003", "Invalid email or password"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "Invalid or expired token");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
