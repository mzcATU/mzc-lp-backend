package com.mzc.lp.domain.learning.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateLearningObjectRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 500, message = "Name must not exceed 500 characters")
        String name,

        @NotNull(message = "Content ID is required")
        Long contentId,

        Long folderId
) {}
