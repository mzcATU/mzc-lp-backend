package com.mzc.lp.domain.student.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BulkEnrollmentResponse {

    private List<EnrollmentResult> results;
    private int successCount;
    private int failureCount;

    @Getter
    @Builder
    public static class EnrollmentResult {
        private Long courseTimeId;
        private boolean success;
        private Long enrollmentId;
        private String errorMessage;

        public static EnrollmentResult success(Long courseTimeId, Long enrollmentId) {
            return EnrollmentResult.builder()
                    .courseTimeId(courseTimeId)
                    .success(true)
                    .enrollmentId(enrollmentId)
                    .build();
        }

        public static EnrollmentResult failure(Long courseTimeId, String errorMessage) {
            return EnrollmentResult.builder()
                    .courseTimeId(courseTimeId)
                    .success(false)
                    .errorMessage(errorMessage)
                    .build();
        }
    }

    public static BulkEnrollmentResponse of(List<EnrollmentResult> results) {
        int successCount = (int) results.stream().filter(EnrollmentResult::isSuccess).count();
        int failureCount = results.size() - successCount;

        return BulkEnrollmentResponse.builder()
                .results(results)
                .successCount(successCount)
                .failureCount(failureCount)
                .build();
    }
}
