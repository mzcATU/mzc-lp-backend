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
    CM_SNAPSHOT_NOT_FOUND(HttpStatus.NOT_FOUND, "CM006", "Snapshot not found"),
    CM_SNAPSHOT_STATE_ERROR(HttpStatus.BAD_REQUEST, "CM007", "Invalid snapshot state"),
    CM_LEARNING_OBJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "CM008", "LearningObject not found"),

    // Content (CMS)
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CT001", "Content not found"),
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, "CT002", "Unsupported file type"),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "CT003", "File size exceeded"),
    INVALID_EXTERNAL_URL(HttpStatus.BAD_REQUEST, "CT004", "Invalid external URL"),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CT005", "File upload failed"),
    METADATA_EXTRACTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CT006", "Metadata extraction failed"),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "CT007", "File not found on storage"),
    UNAUTHORIZED_CONTENT_ACCESS(HttpStatus.FORBIDDEN, "CT008", "Not authorized to access this content"),

    // Learning Object (LO)
    LEARNING_OBJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "LO001", "Learning object not found"),
    CONTENT_FOLDER_NOT_FOUND(HttpStatus.NOT_FOUND, "LO002", "Content folder not found"),
    MAX_FOLDER_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "LO003", "Maximum folder depth exceeded"),
    FOLDER_NOT_EMPTY(HttpStatus.BAD_REQUEST, "LO004", "Folder is not empty"),
    DUPLICATE_FOLDER_NAME(HttpStatus.CONFLICT, "LO005", "Folder name already exists in this location"),
    // CourseTime (TS)
    COURSE_TIME_NOT_FOUND(HttpStatus.NOT_FOUND, "TS001", "CourseTime not found"),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "TS002", "Invalid status transition"),
    CAPACITY_EXCEEDED(HttpStatus.CONFLICT, "TS003", "Capacity exceeded"),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "TS004", "Invalid date range"),
    LOCATION_REQUIRED(HttpStatus.BAD_REQUEST, "TS005", "Location info required for OFFLINE/BLENDED"),
    COURSE_TIME_NOT_MODIFIABLE(HttpStatus.BAD_REQUEST, "TS006", "CourseTime is not modifiable in current status"),
    CANNOT_DELETE_MAIN_INSTRUCTOR(HttpStatus.BAD_REQUEST, "TS007", "Cannot delete main instructor while course is ongoing"),
    MAIN_INSTRUCTOR_REQUIRED(HttpStatus.BAD_REQUEST, "TS008", "Main instructor required for opening course time"),

    // Enrollment (SIS)
    ENROLLMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "SIS001", "Enrollment not found"),
    ALREADY_ENROLLED(HttpStatus.CONFLICT, "SIS002", "Already enrolled in this course"),
    CANNOT_CANCEL_COMPLETED(HttpStatus.BAD_REQUEST, "SIS003", "Cannot cancel completed enrollment"),
    ENROLLMENT_PERIOD_CLOSED(HttpStatus.BAD_REQUEST, "SIS004", "Enrollment period is closed"),
    INVALID_PROGRESS_VALUE(HttpStatus.BAD_REQUEST, "SIS005", "Progress value must be between 0 and 100"),
    UNAUTHORIZED_ENROLLMENT_ACCESS(HttpStatus.FORBIDDEN, "SIS006", "Not authorized to access this enrollment"),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "Unauthorized"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A002", "Access denied"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A003", "Invalid email or password"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "Invalid or expired token"),

    // Instructor (IIS)
    INSTRUCTOR_ASSIGNMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "IIS001", "Instructor assignment not found"),
    INSTRUCTOR_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "IIS002", "Instructor already assigned to this course time"),
    MAIN_INSTRUCTOR_ALREADY_EXISTS(HttpStatus.CONFLICT, "IIS003", "Main instructor already exists for this course time"),
    CANNOT_MODIFY_INACTIVE_ASSIGNMENT(HttpStatus.BAD_REQUEST, "IIS004", "Cannot modify inactive assignment"),

    // Tenant (TN)
    TENANT_NOT_FOUND(HttpStatus.NOT_FOUND, "TN001", "Tenant not found"),
    DUPLICATE_TENANT_CODE(HttpStatus.CONFLICT, "TN002", "Tenant code already exists"),
    DUPLICATE_SUBDOMAIN(HttpStatus.CONFLICT, "TN003", "Subdomain already exists"),
    DUPLICATE_CUSTOM_DOMAIN(HttpStatus.CONFLICT, "TN004", "Custom domain already exists"),
    INVALID_TENANT_STATUS(HttpStatus.BAD_REQUEST, "TN005", "Invalid tenant status transition");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
