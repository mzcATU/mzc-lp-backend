package com.mzc.lp.domain.content.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateContentRequest(
        @Size(max = 500, message = "File name must not exceed 500 characters")
        String originalFileName,

        Integer duration,

        @Size(max = 20, message = "Resolution must not exceed 20 characters")
        String resolution,

        Boolean downloadable
) {}
