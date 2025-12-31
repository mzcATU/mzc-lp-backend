package com.mzc.lp.domain.wishlist.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WishlistCountResponse {

    private long count;

    public static WishlistCountResponse of(long count) {
        return WishlistCountResponse.builder()
                .count(count)
                .build();
    }
}
