package com.mzc.lp.domain.community.dto.response;

import java.util.List;

public record CategoryResponse(
        List<CategoryItem> categories
) {
    public record CategoryItem(
            String id,
            String name,
            String description,
            Long count,
            String icon
    ) {
        public static CategoryItem of(String id, String name, Long count) {
            return new CategoryItem(id, name, null, count, null);
        }

        public static CategoryItem of(String id, String name, String description, Long count, String icon) {
            return new CategoryItem(id, name, description, count, icon);
        }
    }

    public static CategoryResponse of(List<CategoryItem> categories) {
        return new CategoryResponse(categories);
    }
}
