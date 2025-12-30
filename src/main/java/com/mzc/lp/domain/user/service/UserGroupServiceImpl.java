package com.mzc.lp.domain.user.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.user.dto.request.CreateUserGroupRequest;
import com.mzc.lp.domain.user.dto.request.UpdateUserGroupRequest;
import com.mzc.lp.domain.user.dto.response.UserGroupResponse;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.entity.UserGroup;
import com.mzc.lp.domain.user.exception.DuplicateUserGroupNameException;
import com.mzc.lp.domain.user.exception.UserGroupNotFoundException;
import com.mzc.lp.domain.user.exception.UserNotFoundException;
import com.mzc.lp.domain.user.repository.UserGroupRepository;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserGroupServiceImpl implements UserGroupService {

    private final UserGroupRepository userGroupRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserGroupResponse createGroup(CreateUserGroupRequest request) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 중복 이름 검증
        if (userGroupRepository.existsByTenantIdAndName(tenantId, request.name())) {
            throw new DuplicateUserGroupNameException(request.name());
        }

        UserGroup group = UserGroup.create(request.name(), request.description());
        UserGroup savedGroup = userGroupRepository.save(group);

        return UserGroupResponse.from(savedGroup);
    }

    @Override
    public Page<UserGroupResponse> getGroups(String keyword, Pageable pageable) {
        Long tenantId = TenantContext.getCurrentTenantId();

        Page<UserGroup> groups;
        if (keyword != null && !keyword.isBlank()) {
            groups = userGroupRepository.searchByKeyword(tenantId, keyword, pageable);
        } else {
            groups = userGroupRepository.findAllByTenantId(tenantId, pageable);
        }

        return groups.map(UserGroupResponse::from);
    }

    @Override
    public List<UserGroupResponse> getAllActiveGroups() {
        Long tenantId = TenantContext.getCurrentTenantId();
        return userGroupRepository.findAllByTenantIdAndIsActive(tenantId, true)
                .stream()
                .map(UserGroupResponse::from)
                .toList();
    }

    @Override
    public UserGroupResponse getGroup(Long groupId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        UserGroup group = userGroupRepository.findByIdAndTenantId(groupId, tenantId)
                .orElseThrow(() -> new UserGroupNotFoundException(groupId));
        return UserGroupResponse.from(group);
    }

    @Override
    @Transactional
    public UserGroupResponse updateGroup(Long groupId, UpdateUserGroupRequest request) {
        Long tenantId = TenantContext.getCurrentTenantId();

        UserGroup group = userGroupRepository.findByIdAndTenantId(groupId, tenantId)
                .orElseThrow(() -> new UserGroupNotFoundException(groupId));

        // 이름 변경 시 중복 검증
        if (request.name() != null && !request.name().equals(group.getName())) {
            if (userGroupRepository.existsByTenantIdAndName(tenantId, request.name())) {
                throw new DuplicateUserGroupNameException(request.name());
            }
        }

        group.update(request.name(), request.description());

        if (request.isActive() != null) {
            if (request.isActive()) {
                group.activate();
            } else {
                group.deactivate();
            }
        }

        return UserGroupResponse.from(group);
    }

    @Override
    @Transactional
    public void deleteGroup(Long groupId) {
        Long tenantId = TenantContext.getCurrentTenantId();

        UserGroup group = userGroupRepository.findByIdAndTenantId(groupId, tenantId)
                .orElseThrow(() -> new UserGroupNotFoundException(groupId));

        userGroupRepository.delete(group);
    }

    @Override
    @Transactional
    public void addMember(Long groupId, Long userId) {
        Long tenantId = TenantContext.getCurrentTenantId();

        UserGroup group = userGroupRepository.findByIdAndTenantId(groupId, tenantId)
                .orElseThrow(() -> new UserGroupNotFoundException(groupId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        group.addMember(user);
    }

    @Override
    @Transactional
    public void removeMember(Long groupId, Long userId) {
        Long tenantId = TenantContext.getCurrentTenantId();

        UserGroup group = userGroupRepository.findByIdAndTenantId(groupId, tenantId)
                .orElseThrow(() -> new UserGroupNotFoundException(groupId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        group.removeMember(user);
    }
}
