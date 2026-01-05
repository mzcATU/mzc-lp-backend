package com.mzc.lp.domain.wishlist.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class WishlistCheckRequest {

    @NotEmpty(message = "차수 ID 목록은 필수입니다")
    private List<Long> courseTimeIds;

    public WishlistCheckRequest(List<Long> courseTimeIds) {
        this.courseTimeIds = courseTimeIds;
    }
}
