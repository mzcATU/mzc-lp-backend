package com.mzc.lp.domain.user.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.constant.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id", "email"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String phone;

    private String profileImageUrl;

    @Column(length = 100)
    private String department;  // 부서 (개발팀, 회계팀 등)

    @Column(length = 50)
    private String position;    // 직급 (인턴, 신입, 대리, 과장, 차장, 팀장 등)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TenantRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    // 정적 팩토리 메서드
    public static User create(String email, String name, String encodedPassword) {
        User user = new User();
        user.email = email;
        user.name = name;
        user.password = encodedPassword;
        user.role = TenantRole.USER;
        user.status = UserStatus.ACTIVE;
        return user;
    }

    public static User create(String email, String name, String encodedPassword, String phone) {
        User user = create(email, name, encodedPassword);
        user.phone = phone;
        return user;
    }

    // 비즈니스 메서드
    public void updateProfile(String name, String phone, String profileImageUrl) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        this.phone = phone;
        this.profileImageUrl = profileImageUrl;
    }

    public void updateProfile(String name, String phone, String profileImageUrl, String department, String position) {
        updateProfile(name, phone, profileImageUrl);
        this.department = department;
        this.position = position;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateRole(TenantRole role) {
        this.role = role;
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;
    }
}
