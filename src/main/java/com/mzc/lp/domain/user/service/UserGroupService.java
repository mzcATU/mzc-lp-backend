package com.mzc.lp.domain.user.service;

import com.mzc.lp.domain.user.dto.request.CreateUserGroupRequest;
import com.mzc.lp.domain.user.dto.request.UpdateUserGroupRequest;
import com.mzc.lp.domain.user.dto.response.UserGroupResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserGroupService {

    UserGroupResponse createGroup(CreateUserGroupRequest request);

    Page<UserGroupResponse> getGroups(String keyword, Pageable pageable);

    List<UserGroupResponse> getAllActiveGroups();

    UserGroupResponse getGroup(Long groupId);

    UserGroupResponse updateGroup(Long groupId, UpdateUserGroupRequest request);

    void deleteGroup(Long groupId);

    void addMember(Long groupId, Long userId);

    void removeMember(Long groupId, Long userId);
}
