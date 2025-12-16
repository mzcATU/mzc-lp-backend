package com.mzc.lp.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Tenant 정보를 찾을 수 없을 때 발생하는 예외
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class TenantNotFoundException extends RuntimeException {

    public TenantNotFoundException(String message) {
        super(message);
    }

    public TenantNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
