package com.mzc.lp.domain.sa.service;

import com.mzc.lp.domain.dashboard.constant.DashboardPeriod;
import com.mzc.lp.domain.sa.dto.response.SaDashboardResponse;
import com.mzc.lp.domain.tenant.constant.TenantStatus;
import com.mzc.lp.domain.tenant.entity.Tenant;
import com.mzc.lp.domain.tenant.repository.TenantRepository;
import com.mzc.lp.domain.user.constant.UserStatus;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaDashboardServiceImpl implements SaDashboardService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public SaDashboardResponse getDashboard(DashboardPeriod period) {
        // 기간 필터 설정
        Instant startDate = period != null ? period.getStartInstant() : null;
        Instant endDate = period != null ? period.getEndInstant() : null;

        log.debug("SA 대시보드 조회 - 기간: {}", period != null ? period.getCode() : "전체");

        return SaDashboardResponse.builder()
                .tenantStats(getTenantStats(startDate, endDate))
                .userStats(getUserStats(startDate, endDate))
                .recentTenants(getRecentTenants())  // recentTenants는 기간 필터 적용 제외
                .build();
    }

    private SaDashboardResponse.TenantStats getTenantStats(Instant startDate, Instant endDate) {
        List<Tenant> tenants;
        if (startDate != null && endDate != null) {
            tenants = tenantRepository.findAllWithPeriod(startDate, endDate);
        } else {
            tenants = tenantRepository.findAll();
        }

        Map<TenantStatus, Long> statusCount = tenants.stream()
                .collect(Collectors.groupingBy(Tenant::getStatus, Collectors.counting()));

        Map<String, Long> planCount = tenants.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getPlan().name(),
                        Collectors.counting()
                ));

        return SaDashboardResponse.TenantStats.builder()
                .total(tenants.size())
                .active(statusCount.getOrDefault(TenantStatus.ACTIVE, 0L))
                .pending(statusCount.getOrDefault(TenantStatus.PENDING, 0L))
                .suspended(statusCount.getOrDefault(TenantStatus.SUSPENDED, 0L))
                .terminated(statusCount.getOrDefault(TenantStatus.TERMINATED, 0L))
                .byPlan(planCount)
                .build();
    }

    private SaDashboardResponse.UserStats getUserStats(Instant startDate, Instant endDate) {
        List<User> users;
        if (startDate != null && endDate != null) {
            users = userRepository.findAllWithPeriod(startDate, endDate);
        } else {
            users = userRepository.findAll();
        }

        Map<UserStatus, Long> statusCount = users.stream()
                .collect(Collectors.groupingBy(User::getStatus, Collectors.counting()));

        return SaDashboardResponse.UserStats.builder()
                .total(users.size())
                .active(statusCount.getOrDefault(UserStatus.ACTIVE, 0L))
                .suspended(statusCount.getOrDefault(UserStatus.SUSPENDED, 0L))
                .withdrawn(statusCount.getOrDefault(UserStatus.WITHDRAWN, 0L))
                .build();
    }

    private List<SaDashboardResponse.RecentTenant> getRecentTenants() {
        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Tenant> recentTenants = tenantRepository.findAll(pageRequest).getContent();

        return recentTenants.stream()
                .map(tenant -> SaDashboardResponse.RecentTenant.builder()
                        .id(tenant.getId())
                        .code(tenant.getCode())
                        .name(tenant.getName())
                        .status(tenant.getStatus().name())
                        .plan(tenant.getPlan().name())
                        .createdAt(tenant.getCreatedAt().atZone(ZoneId.systemDefault())
                                .format(DATE_FORMATTER))
                        .build())
                .toList();
    }
}
