package com.mzc.lp.domain.banner.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.banner.constant.BannerPosition;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "banners")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Banner extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Column(name = "link_target", length = 20)
    private String linkTarget = "_self";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BannerPosition position;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(length = 500)
    private String description;

    // 정적 팩토리 메서드
    public static Banner create(String title, String imageUrl, BannerPosition position) {
        Banner banner = new Banner();
        banner.title = title;
        banner.imageUrl = imageUrl;
        banner.position = position;
        return banner;
    }

    public static Banner create(String title, String imageUrl, BannerPosition position,
                                 String linkUrl, LocalDate startDate, LocalDate endDate) {
        Banner banner = create(title, imageUrl, position);
        banner.linkUrl = linkUrl;
        banner.startDate = startDate;
        banner.endDate = endDate;
        return banner;
    }

    // 비즈니스 메서드
    public void update(String title, String imageUrl, String linkUrl, String linkTarget,
                       BannerPosition position, String description) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (imageUrl != null && !imageUrl.isBlank()) {
            this.imageUrl = imageUrl;
        }
        this.linkUrl = linkUrl;
        this.linkTarget = linkTarget != null ? linkTarget : "_self";
        if (position != null) {
            this.position = position;
        }
        this.description = description;
    }

    public void updatePeriod(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isDisplayable() {
        if (!this.isActive) {
            return false;
        }
        LocalDate today = LocalDate.now();
        if (startDate != null && today.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && today.isAfter(endDate)) {
            return false;
        }
        return true;
    }
}
