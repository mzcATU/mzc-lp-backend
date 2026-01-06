package com.mzc.lp.domain.memberpool.service;

import com.mzc.lp.domain.memberpool.dto.request.CreateMemberPoolRequest;
import com.mzc.lp.domain.memberpool.dto.request.MemberPoolConditionDto;
import com.mzc.lp.domain.memberpool.dto.request.UpdateMemberPoolRequest;
import com.mzc.lp.domain.memberpool.dto.response.MemberPoolMemberResponse;
import com.mzc.lp.domain.memberpool.dto.response.MemberPoolResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberPoolService {

    MemberPoolResponse create(Long tenantId, CreateMemberPoolRequest request);

    MemberPoolResponse update(Long tenantId, Long poolId, UpdateMemberPoolRequest request);

    void delete(Long tenantId, Long poolId);

    MemberPoolResponse getById(Long tenantId, Long poolId);

    List<MemberPoolResponse> getAll(Long tenantId);

    List<MemberPoolResponse> getActivePoolls(Long tenantId);

    Page<MemberPoolMemberResponse> getMembers(Long tenantId, Long poolId, Pageable pageable);

    Page<MemberPoolMemberResponse> previewMembers(Long tenantId, MemberPoolConditionDto conditions, Pageable pageable);

    MemberPoolResponse activate(Long tenantId, Long poolId);

    MemberPoolResponse deactivate(Long tenantId, Long poolId);
}
