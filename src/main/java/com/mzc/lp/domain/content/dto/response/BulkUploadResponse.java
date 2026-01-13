package com.mzc.lp.domain.content.dto.response;

import java.util.List;

/**
 * 일괄 업로드 응답 DTO
 */
public record BulkUploadResponse(
        int totalCount,
        int successCount,
        int failCount,
        List<ContentResponse> successItems,
        List<FailedItem> failedItems
) {
    public record FailedItem(
            String fileName,
            String errorMessage
    ) {}

    public static BulkUploadResponse of(
            List<ContentResponse> successItems,
            List<FailedItem> failedItems
    ) {
        return new BulkUploadResponse(
                successItems.size() + failedItems.size(),
                successItems.size(),
                failedItems.size(),
                successItems,
                failedItems
        );
    }
}
