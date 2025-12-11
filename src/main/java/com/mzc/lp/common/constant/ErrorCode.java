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

    // Course
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "CR001", "Course not found"),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "Unauthorized"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A002", "Access denied"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A003", "Invalid email or password"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "Invalid or expired token");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
