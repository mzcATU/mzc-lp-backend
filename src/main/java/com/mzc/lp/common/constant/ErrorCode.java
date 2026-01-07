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
    CM_SNAPSHOT_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CM009", "SnapshotItem not found"),
    CM_UNAUTHORIZED_COURSE_ACCESS(HttpStatus.FORBIDDEN, "CM010", "Not authorized to access this course"),
    CM_REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "CM011", "Course review not found"),
    CM_REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "CM012", "Review already exists for this course"),
    CM_NOT_COMPLETED_COURSE(HttpStatus.BAD_REQUEST, "CM013", "Cannot write review for incomplete course"),
    CM_NOT_REVIEW_OWNER(HttpStatus.FORBIDDEN, "CM014", "Not authorized to modify this review"),

    // Content (CMS)
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CT001", "Content not found"),
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, "CT002", "Unsupported file type"),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "CT003", "File size exceeded"),
    INVALID_EXTERNAL_URL(HttpStatus.BAD_REQUEST, "CT004", "Invalid external URL"),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CT005", "File upload failed"),
    METADATA_EXTRACTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CT006", "Metadata extraction failed"),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "CT007", "File not found on storage"),
    UNAUTHORIZED_CONTENT_ACCESS(HttpStatus.FORBIDDEN, "CT008", "Not authorized to access this content"),
    CONTENT_VERSION_NOT_FOUND(HttpStatus.NOT_FOUND, "CT009", "Content version not found"),
    CONTENT_IN_USE(HttpStatus.CONFLICT, "CT010", "Content is in use by learning objects and cannot be modified"),
    CONTENT_DOWNLOAD_NOT_ALLOWED(HttpStatus.FORBIDDEN, "CT011", "Download is not allowed for this content"),

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
    UNAUTHORIZED_COURSE_TIME_ACCESS(HttpStatus.FORBIDDEN, "TS009", "Not authorized to access this course time"),
    COURSE_TIME_NOT_AVAILABLE(HttpStatus.NOT_FOUND, "TS010", "CourseTime is not available for public access"),

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
    UNAUTHORIZED_ASSIGNMENT_ACCESS(HttpStatus.FORBIDDEN, "IIS005", "Not authorized to access this assignment"),
    INSTRUCTOR_SCHEDULE_CONFLICT(HttpStatus.CONFLICT, "IIS006", "Instructor has schedule conflict with another course time"),

    // Tenant (TN)
    TENANT_NOT_FOUND(HttpStatus.NOT_FOUND, "TN001", "Tenant not found"),
    DUPLICATE_TENANT_CODE(HttpStatus.CONFLICT, "TN002", "Tenant code already exists"),
    DUPLICATE_SUBDOMAIN(HttpStatus.CONFLICT, "TN003", "Subdomain already exists"),
    DUPLICATE_CUSTOM_DOMAIN(HttpStatus.CONFLICT, "TN004", "Custom domain already exists"),
    INVALID_TENANT_STATUS(HttpStatus.BAD_REQUEST, "TN005", "Invalid tenant status transition"),

    // Program (PG)
    PROGRAM_NOT_FOUND(HttpStatus.NOT_FOUND, "PG001", "Program not found"),
    INVALID_PROGRAM_STATUS(HttpStatus.BAD_REQUEST, "PG002", "Invalid program status transition"),
    PROGRAM_NOT_MODIFIABLE(HttpStatus.BAD_REQUEST, "PG003", "Program is not modifiable in current status"),
    PROGRAM_NOT_APPROVED(HttpStatus.BAD_REQUEST, "PG004", "Program is not approved"),
    REJECTION_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "PG005", "Rejection reason is required"),
    UNAUTHORIZED_PROGRAM_ACCESS(HttpStatus.FORBIDDEN, "PG006", "Not authorized to access this program"),

    // Category (CAT)
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CAT001", "Category not found"),
    DUPLICATE_CATEGORY_CODE(HttpStatus.CONFLICT, "CAT002", "Category code already exists"),

    // User Group (UG)
    USER_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "UG001", "User group not found"),
    DUPLICATE_USER_GROUP_NAME(HttpStatus.CONFLICT, "UG002", "User group name already exists"),

    // Notice (NT)
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NT001", "Notice not found"),

    // Wishlist (WL)
    WISHLIST_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "WL001", "Wishlist item not found"),
    ALREADY_IN_WISHLIST(HttpStatus.CONFLICT, "WL002", "Course is already in wishlist"),

    // Cart (CART)
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CART001", "Cart item not found"),
    ALREADY_IN_CART(HttpStatus.CONFLICT, "CART002", "Course is already in cart"),

    // Community (CM - Community)
    COMMUNITY_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "CMT001", "Community post not found"),
    COMMUNITY_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CMT002", "Community comment not found"),
    COMMUNITY_ALREADY_LIKED(HttpStatus.CONFLICT, "CMT003", "Already liked"),
    COMMUNITY_NOT_POST_AUTHOR(HttpStatus.FORBIDDEN, "CMT004", "Not authorized to modify this post"),
    COMMUNITY_NOT_COMMENT_AUTHOR(HttpStatus.FORBIDDEN, "CMT005", "Not authorized to modify this comment"),
    COMMUNITY_NOT_ENROLLED(HttpStatus.FORBIDDEN, "CMT006", "Not enrolled in this course"),

    // Notification (NF)
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NF001", "Notification not found"),

    // Certificate (CERT)
    CERTIFICATE_NOT_FOUND(HttpStatus.NOT_FOUND, "CERT001", "Certificate not found"),
    CERTIFICATE_ALREADY_ISSUED(HttpStatus.CONFLICT, "CERT002", "Certificate already issued for this enrollment"),
    CERTIFICATE_REVOKED(HttpStatus.BAD_REQUEST, "CERT003", "Certificate has been revoked"),
    CERTIFICATE_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "CERT004", "Certificate template not found"),
    CERTIFICATE_TEMPLATE_CODE_DUPLICATE(HttpStatus.CONFLICT, "CERT005", "Certificate template code already exists"),

    // Department (DEPT)
    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "DEPT001", "Department not found"),
    DEPARTMENT_CODE_DUPLICATE(HttpStatus.CONFLICT, "DEPT002", "Department code already exists"),
    DEPARTMENT_HAS_CHILDREN(HttpStatus.BAD_REQUEST, "DEPT003", "Cannot delete department with sub-departments"),
    DEPARTMENT_HAS_MEMBERS(HttpStatus.BAD_REQUEST, "DEPT004", "Cannot delete department with members"),

    // Employee (EMP)
    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "EMP001", "Employee not found"),
    EMPLOYEE_NUMBER_DUPLICATE(HttpStatus.CONFLICT, "EMP002", "Employee number already exists"),
    EMPLOYEE_USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "EMP003", "User is already registered as employee"),

    // Banner (BNR)
    BANNER_NOT_FOUND(HttpStatus.NOT_FOUND, "BNR001", "Banner not found"),

    // Auto Enrollment Rule (AER)
    AUTO_ENROLLMENT_RULE_NOT_FOUND(HttpStatus.NOT_FOUND, "AER001", "Auto enrollment rule not found"),

    // Member Pool (MP)
    MEMBER_POOL_NOT_FOUND(HttpStatus.NOT_FOUND, "MP001", "Member pool not found"),

    // Roadmap (RM)
    ROADMAP_NOT_FOUND(HttpStatus.NOT_FOUND, "RM001", "Roadmap not found"),
    ROADMAP_NOT_MODIFIABLE(HttpStatus.BAD_REQUEST, "RM002", "Roadmap is not modifiable in current status"),
    UNAUTHORIZED_ROADMAP_ACCESS(HttpStatus.FORBIDDEN, "RM003", "Not authorized to access this roadmap"),
    INVALID_PROGRAM_FOR_ROADMAP(HttpStatus.BAD_REQUEST, "RM004", "Invalid program for roadmap"),
    DUPLICATE_PROGRAM_IN_ROADMAP(HttpStatus.CONFLICT, "RM005", "Program already exists in roadmap"),
    ROADMAP_HAS_ENROLLMENTS(HttpStatus.BAD_REQUEST, "RM006", "Cannot delete roadmap with enrollments");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
