package com.mzc.lp.domain.learning.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateLearningObjectRequest(
        @Size(max = 500, message = "Name must not exceed 500 characters")
        String name
) {}
