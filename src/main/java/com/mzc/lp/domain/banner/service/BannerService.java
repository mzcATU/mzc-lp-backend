package com.mzc.lp.domain.banner.service;

import com.mzc.lp.domain.banner.constant.BannerPosition;
import com.mzc.lp.domain.banner.dto.request.CreateBannerRequest;
import com.mzc.lp.domain.banner.dto.request.UpdateBannerRequest;
import com.mzc.lp.domain.banner.dto.response.BannerResponse;

import java.util.List;

public interface BannerService {

    BannerResponse create(Long tenantId, CreateBannerRequest request);

    BannerResponse update(Long tenantId, Long bannerId, UpdateBannerRequest request);

    void delete(Long tenantId, Long bannerId);

    BannerResponse getById(Long tenantId, Long bannerId);

    List<BannerResponse> getAll(Long tenantId);

    List<BannerResponse> getByPosition(Long tenantId, BannerPosition position);

    List<BannerResponse> getActiveBanners(Long tenantId);

    List<BannerResponse> getDisplayableBanners(Long tenantId);

    List<BannerResponse> getDisplayableBannersByPosition(Long tenantId, BannerPosition position);

    BannerResponse activate(Long tenantId, Long bannerId);

    BannerResponse deactivate(Long tenantId, Long bannerId);
}
