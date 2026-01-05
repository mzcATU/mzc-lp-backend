package com.mzc.lp.domain.banner.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.banner.constant.BannerPosition;
import com.mzc.lp.domain.banner.dto.request.CreateBannerRequest;
import com.mzc.lp.domain.banner.dto.request.UpdateBannerRequest;
import com.mzc.lp.domain.banner.dto.response.BannerResponse;
import com.mzc.lp.domain.banner.entity.Banner;
import com.mzc.lp.domain.banner.exception.BannerNotFoundException;
import com.mzc.lp.domain.banner.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;

    @Override
    @Transactional
    public BannerResponse create(Long tenantId, CreateBannerRequest request) {
        log.info("Creating banner: tenantId={}, title={}, position={}",
                tenantId, request.title(), request.position());

        TenantContext.setTenantId(tenantId);

        try {
            Banner banner = Banner.create(
                    request.title(),
                    request.imageUrl(),
                    request.position(),
                    request.linkUrl(),
                    request.startDate(),
                    request.endDate()
            );

            if (request.linkTarget() != null) {
                banner.update(null, null, null, request.linkTarget(), null, request.description());
            }

            if (request.sortOrder() != null) {
                banner.setSortOrder(request.sortOrder());
            }

            if (request.description() != null) {
                banner.update(null, null, null, null, null, request.description());
            }

            Banner saved = bannerRepository.save(banner);
            return BannerResponse.from(saved);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    @Transactional
    public BannerResponse update(Long tenantId, Long bannerId, UpdateBannerRequest request) {
        log.info("Updating banner: tenantId={}, bannerId={}", tenantId, bannerId);

        Banner banner = bannerRepository.findByIdAndTenantId(bannerId, tenantId)
                .orElseThrow(() -> new BannerNotFoundException(bannerId));

        banner.update(
                request.title(),
                request.imageUrl(),
                request.linkUrl(),
                request.linkTarget(),
                request.position(),
                request.description()
        );

        banner.updatePeriod(request.startDate(), request.endDate());

        if (request.sortOrder() != null) {
            banner.setSortOrder(request.sortOrder());
        }

        return BannerResponse.from(banner);
    }

    @Override
    @Transactional
    public void delete(Long tenantId, Long bannerId) {
        log.info("Deleting banner: tenantId={}, bannerId={}", tenantId, bannerId);

        Banner banner = bannerRepository.findByIdAndTenantId(bannerId, tenantId)
                .orElseThrow(() -> new BannerNotFoundException(bannerId));

        bannerRepository.delete(banner);
    }

    @Override
    public BannerResponse getById(Long tenantId, Long bannerId) {
        Banner banner = bannerRepository.findByIdAndTenantId(bannerId, tenantId)
                .orElseThrow(() -> new BannerNotFoundException(bannerId));

        return BannerResponse.from(banner);
    }

    @Override
    public List<BannerResponse> getAll(Long tenantId) {
        return bannerRepository.findByTenantIdOrderBySortOrderAsc(tenantId)
                .stream()
                .map(BannerResponse::from)
                .toList();
    }

    @Override
    public List<BannerResponse> getByPosition(Long tenantId, BannerPosition position) {
        return bannerRepository.findByTenantIdAndPositionOrderBySortOrderAsc(tenantId, position)
                .stream()
                .map(BannerResponse::from)
                .toList();
    }

    @Override
    public List<BannerResponse> getActiveBanners(Long tenantId) {
        return bannerRepository.findByTenantIdAndIsActiveTrueOrderBySortOrderAsc(tenantId)
                .stream()
                .map(BannerResponse::from)
                .toList();
    }

    @Override
    public List<BannerResponse> getDisplayableBanners(Long tenantId) {
        return bannerRepository.findDisplayableBanners(tenantId, LocalDate.now())
                .stream()
                .map(BannerResponse::from)
                .toList();
    }

    @Override
    public List<BannerResponse> getDisplayableBannersByPosition(Long tenantId, BannerPosition position) {
        return bannerRepository.findDisplayableBannersByPosition(tenantId, position, LocalDate.now())
                .stream()
                .map(BannerResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public BannerResponse activate(Long tenantId, Long bannerId) {
        log.info("Activating banner: tenantId={}, bannerId={}", tenantId, bannerId);

        Banner banner = bannerRepository.findByIdAndTenantId(bannerId, tenantId)
                .orElseThrow(() -> new BannerNotFoundException(bannerId));

        banner.activate();
        return BannerResponse.from(banner);
    }

    @Override
    @Transactional
    public BannerResponse deactivate(Long tenantId, Long bannerId) {
        log.info("Deactivating banner: tenantId={}, bannerId={}", tenantId, bannerId);

        Banner banner = bannerRepository.findByIdAndTenantId(bannerId, tenantId)
                .orElseThrow(() -> new BannerNotFoundException(bannerId));

        banner.deactivate();
        return BannerResponse.from(banner);
    }
}
