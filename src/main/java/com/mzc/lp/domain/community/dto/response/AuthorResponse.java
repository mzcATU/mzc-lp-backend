package com.mzc.lp.domain.community.dto.response;

import com.mzc.lp.domain.user.entity.User;

public record AuthorResponse(
        Long id,
        String name,
        String avatar
) {
    public static AuthorResponse from(User user) {
        if (user == null) {
            return new AuthorResponse(0L, "알 수 없음", null);
        }
        return new AuthorResponse(
                user.getId(),
                user.getName(),
                user.getProfileImageUrl()
        );
    }

    public static AuthorResponse of(Long id, String name, String avatar) {
        return new AuthorResponse(id, name, avatar);
    }
}
