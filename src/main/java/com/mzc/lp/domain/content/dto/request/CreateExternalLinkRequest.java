package com.mzc.lp.domain.content.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateExternalLinkRequest(
        @NotBlank(message = "URL is required")
        @Size(max = 2000, message = "URL must not exceed 2000 characters")
        String url,

        @NotBlank(message = "Name is required")
        @Size(max = 500, message = "Name must not exceed 500 characters")
        String name,

        Long folderId
) {}
