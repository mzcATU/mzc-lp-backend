package com.mzc.lp.domain.wishlist.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WishlistAddRequest {

    @NotNull(message = "차수 ID는 필수입니다")
    private Long courseTimeId;

    public WishlistAddRequest(Long courseTimeId) {
        this.courseTimeId = courseTimeId;
    }
}
