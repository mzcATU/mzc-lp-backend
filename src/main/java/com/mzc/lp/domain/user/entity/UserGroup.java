package com.mzc.lp.domain.user.entity;

import com.mzc.lp.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * 사용자 그룹 엔티티
 * 테넌트 내에서 사용자를 그룹화하여 관리
 */
@Entity
@Table(name = "user_groups", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id", "name"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserGroup extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean isActive = true;

    @ManyToMany
    @JoinTable(
            name = "user_group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    // 정적 팩토리 메서드
    public static UserGroup create(String name, String description) {
        UserGroup group = new UserGroup();
        group.name = name;
        group.description = description;
        return group;
    }

    // 비즈니스 메서드
    public void update(String name, String description) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        this.description = description;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void addMember(User user) {
        this.members.add(user);
    }

    public void removeMember(User user) {
        this.members.remove(user);
    }

    public int getMemberCount() {
        return this.members.size();
    }
}
