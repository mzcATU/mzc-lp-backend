package com.mzc.lp.domain.department.dto.response;

import com.mzc.lp.domain.department.entity.Department;

import java.time.Instant;
import java.util.List;

public record DepartmentResponse(
        Long id,
        String name,
        String code,
        String description,
        Long parentId,
        String parentName,
        Long managerId,
        String managerName,
        Integer sortOrder,
        Boolean isActive,
        Integer memberCount,
        List<DepartmentResponse> children,
        Instant createdAt,
        Instant updatedAt
) {
    // Entity -> Response (단일)
    public static DepartmentResponse from(Department department) {
        return from(department, null, 0);
    }

    public static DepartmentResponse from(Department department, String managerName, int memberCount) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getCode(),
                department.getDescription(),
                department.getParent() != null ? department.getParent().getId() : null,
                department.getParent() != null ? department.getParent().getName() : null,
                department.getManagerId(),
                managerName,
                department.getSortOrder(),
                department.getIsActive(),
                memberCount,
                List.of(),
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }

    // Entity -> Response (계층 구조 포함)
    public static DepartmentResponse fromWithChildren(Department department, String managerName, int memberCount) {
        List<DepartmentResponse> childResponses = department.getChildren().stream()
                .map(child -> fromWithChildren(child, null, 0))
                .toList();

        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getCode(),
                department.getDescription(),
                department.getParent() != null ? department.getParent().getId() : null,
                department.getParent() != null ? department.getParent().getName() : null,
                department.getManagerId(),
                managerName,
                department.getSortOrder(),
                department.getIsActive(),
                memberCount,
                childResponses,
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }
}
