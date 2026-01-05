package com.mzc.lp.domain.department.entity;

import com.mzc.lp.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 부서 엔티티
 * 테넌트 내 조직 구조를 관리
 */
@Entity
@Table(name = "departments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id", "code"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Department extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Department parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Department> children = new ArrayList<>();

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private Boolean isActive = true;

    // 정적 팩토리 메서드
    public static Department create(String name, String code, String description) {
        Department department = new Department();
        department.name = name;
        department.code = code.toUpperCase();
        department.description = description;
        return department;
    }

    // 비즈니스 메서드
    public void update(String name, String code, String description) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (code != null && !code.isBlank()) {
            this.code = code.toUpperCase();
        }
        this.description = description;
    }

    public void setParent(Department parent) {
        this.parent = parent;
        if (parent != null && !parent.getChildren().contains(this)) {
            parent.getChildren().add(this);
        }
    }

    public void removeParent() {
        if (this.parent != null) {
            this.parent.getChildren().remove(this);
            this.parent = null;
        }
    }

    public void setManager(Long managerId) {
        this.managerId = managerId;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isRoot() {
        return this.parent == null;
    }

    public int getDepth() {
        int depth = 0;
        Department current = this.parent;
        while (current != null) {
            depth++;
            current = current.getParent();
        }
        return depth;
    }
}
