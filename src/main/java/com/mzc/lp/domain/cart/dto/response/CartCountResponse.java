package com.mzc.lp.domain.cart.dto.response;

public record CartCountResponse(
        long count
) {
    public static CartCountResponse of(long count) {
        return new CartCountResponse(count);
    }
}
