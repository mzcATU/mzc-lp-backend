package com.mzc.lp.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mzc.lp.common.constant.ErrorCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        ErrorInfo error
) {
    public record ErrorInfo(
            String code,
            String message
    ) {
        public static ErrorInfo of(ErrorCode errorCode) {
            return new ErrorInfo(errorCode.getCode(), errorCode.getMessage());
        }

        public static ErrorInfo of(ErrorCode errorCode, String message) {
            return new ErrorInfo(errorCode.getCode(), message);
        }
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, ErrorInfo.of(errorCode));
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, null, ErrorInfo.of(errorCode, message));
    }
}
