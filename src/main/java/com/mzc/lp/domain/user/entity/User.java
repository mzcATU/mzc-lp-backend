package com.mzc.lp.domain.user.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.constant.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
    private TenantRole role;  // 기본 역할 (하위 호환성 유지)

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<UserRole> userRoles = new HashSet<>();

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

    /**
     * 역할 추가 (1:N)
     */
    public void addRole(TenantRole role) {
        // 이미 해당 역할이 있는지 확인
        boolean hasRole = this.userRoles.stream()
                .anyMatch(ur -> ur.getRole() == role);
        if (!hasRole) {
            UserRole userRole = UserRole.create(this, role);
            this.userRoles.add(userRole);
        }
        // 기본 역할도 업데이트 (가장 높은 권한으로)
        updatePrimaryRole();
    }

    /**
     * 역할 제거 (1:N)
     */
    public void removeRole(TenantRole role) {
        this.userRoles.removeIf(ur -> ur.getRole() == role);
        // 기본 역할도 업데이트
        updatePrimaryRole();
    }

    /**
     * 역할 전체 설정 (기존 역할 교체)
     */
    public void setRoles(Set<TenantRole> roles) {
        // 제거할 역할 찾기 (기존에 있지만 새 목록에 없는 것)
        Set<UserRole> toRemove = this.userRoles.stream()
                .filter(ur -> !roles.contains(ur.getRole()))
                .collect(Collectors.toSet());
        this.userRoles.removeAll(toRemove);

        // 추가할 역할 찾기 (새 목록에 있지만 기존에 없는 것)
        Set<TenantRole> existingRoles = this.userRoles.stream()
                .map(UserRole::getRole)
                .collect(Collectors.toSet());
        for (TenantRole r : roles) {
            if (!existingRoles.contains(r)) {
                UserRole userRole = UserRole.create(this, r);
                this.userRoles.add(userRole);
            }
        }
        updatePrimaryRole();
    }

    /**
     * 모든 역할 조회
     */
    public Set<TenantRole> getRoles() {
        if (userRoles.isEmpty()) {
            // 하위 호환성: userRoles가 비어있으면 기본 role 반환
            return Set.of(this.role);
        }
        return userRoles.stream()
                .map(UserRole::getRole)
                .collect(Collectors.toSet());
    }

    /**
     * 특정 역할 보유 여부 확인
     */
    public boolean hasRole(TenantRole role) {
        if (userRoles.isEmpty()) {
            return this.role == role;
        }
        return userRoles.stream()
                .anyMatch(ur -> ur.getRole() == role);
    }

    /**
     * 기본 역할 업데이트 (가장 높은 권한으로 설정)
     */
    private void updatePrimaryRole() {
        if (userRoles.isEmpty()) {
            return;
        }
        // 우선순위: SYSTEM_ADMIN > TENANT_ADMIN > OPERATOR > DESIGNER > INSTRUCTOR > USER
        TenantRole highestRole = userRoles.stream()
                .map(UserRole::getRole)
                .min((r1, r2) -> r1.ordinal() - r2.ordinal())
                .orElse(TenantRole.USER);
        this.role = highestRole;
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
