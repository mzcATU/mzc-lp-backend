package com.mzc.lp.domain.category.entity;

import com.mzc.lp.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cm_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private Boolean active = true;

    // ===== 정적 팩토리 메서드 =====
    public static Category create(String name, String code) {
        Category category = new Category();
        category.name = name;
        category.code = code;
        category.sortOrder = 0;
        category.active = true;
        return category;
    }

    public static Category create(String name, String code, Integer sortOrder) {
        Category category = new Category();
        category.name = name;
        category.code = code;
        category.sortOrder = sortOrder != null ? sortOrder : 0;
        category.active = true;
        return category;
    }

    // ===== 비즈니스 메서드 =====
    public void updateName(String name) {
        validateName(name);
        this.name = name;
    }

    public void updateCode(String code) {
        validateCode(code);
        this.code = code;
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void update(String name, String code, Integer sortOrder, Boolean active) {
        if (name != null) {
            updateName(name);
        }
        if (code != null) {
            updateCode(code);
        }
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }
        if (active != null) {
            this.active = active;
        }
    }

    // ===== Private 검증 메서드 =====
    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("카테고리 이름은 필수입니다");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("카테고리 이름은 100자 이하여야 합니다");
        }
    }

    private void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("카테고리 코드는 필수입니다");
        }
        if (code.length() > 50) {
            throw new IllegalArgumentException("카테고리 코드는 50자 이하여야 합니다");
        }
    }
}
