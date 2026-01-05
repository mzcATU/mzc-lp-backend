package com.mzc.lp.domain.department.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateDepartmentRequest(
        @Size(max = 100, message = "부서명은 100자 이하여야 합니다")
        String name,

        @Size(max = 50, message = "부서 코드는 50자 이하여야 합니다")
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "부서 코드는 영문, 숫자, 밑줄, 하이픈만 사용 가능합니다")
        String code,

        @Size(max = 500, message = "설명은 500자 이하여야 합니다")
        String description,

        Long parentId,

        Long managerId,

        Integer sortOrder
) {}
