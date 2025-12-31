package com.mzc.lp.domain.wishlist.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
public class WishlistCheckResponse {

    private Map<Long, Boolean> wishlistStatus;

    public static WishlistCheckResponse of(List<Long> requestedCourseIds, List<Long> wishlistedCourseIds) {
        Map<Long, Boolean> statusMap = requestedCourseIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        wishlistedCourseIds::contains
                ));

        return WishlistCheckResponse.builder()
                .wishlistStatus(statusMap)
                .build();
    }
}
