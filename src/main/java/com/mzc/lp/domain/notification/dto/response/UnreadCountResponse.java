package com.mzc.lp.domain.notification.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UnreadCountResponse {
    private long count;

    public static UnreadCountResponse of(long count) {
        return UnreadCountResponse.builder()
                .count(count)
                .build();
    }
}
