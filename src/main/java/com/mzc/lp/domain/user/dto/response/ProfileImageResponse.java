package com.mzc.lp.domain.user.dto.response;

public record ProfileImageResponse(
        String profileImageUrl
) {
    public static ProfileImageResponse from(String profileImageUrl) {
        return new ProfileImageResponse(profileImageUrl);
    }
}
