package com.mzc.lp.domain.wishlist.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class WishlistCheckRequest {

    @NotEmpty(message = "강의 ID 목록은 필수입니다")
    private List<Long> courseIds;

    public WishlistCheckRequest(List<Long> courseIds) {
        this.courseIds = courseIds;
    }
}
