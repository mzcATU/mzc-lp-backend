package com.mzc.lp.domain.sa.service;

import com.mzc.lp.domain.sa.dto.response.SaDashboardResponse;
import com.mzc.lp.domain.tenant.constant.TenantStatus;
import com.mzc.lp.domain.tenant.entity.Tenant;
import com.mzc.lp.domain.tenant.repository.TenantRepository;
import com.mzc.lp.domain.user.constant.UserStatus;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaDashboardServiceImpl implements SaDashboardService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public SaDashboardResponse getDashboard() {
        return SaDashboardResponse.builder()
                .tenantStats(getTenantStats())
                .userStats(getUserStats())
                .recentTenants(getRecentTenants())
                .build();
    }

    private SaDashboardResponse.TenantStats getTenantStats() {
        List<Tenant> allTenants = tenantRepository.findAll();

        Map<TenantStatus, Long> statusCount = allTenants.stream()
                .collect(Collectors.groupingBy(Tenant::getStatus, Collectors.counting()));

        Map<String, Long> planCount = allTenants.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getPlan().name(),
                        Collectors.counting()
                ));

        return SaDashboardResponse.TenantStats.builder()
                .total(allTenants.size())
                .active(statusCount.getOrDefault(TenantStatus.ACTIVE, 0L))
                .pending(statusCount.getOrDefault(TenantStatus.PENDING, 0L))
                .suspended(statusCount.getOrDefault(TenantStatus.SUSPENDED, 0L))
                .terminated(statusCount.getOrDefault(TenantStatus.TERMINATED, 0L))
                .byPlan(planCount)
                .build();
    }

    private SaDashboardResponse.UserStats getUserStats() {
        List<com.mzc.lp.domain.user.entity.User> allUsers = userRepository.findAll();

        Map<UserStatus, Long> statusCount = allUsers.stream()
                .collect(Collectors.groupingBy(
                        com.mzc.lp.domain.user.entity.User::getStatus,
                        Collectors.counting()
                ));

        return SaDashboardResponse.UserStats.builder()
                .total(allUsers.size())
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
                        .createdAt(tenant.getCreatedAt().atZone(java.time.ZoneId.systemDefault())
                                .format(DATE_FORMATTER))
                        .build())
                .toList();
    }
}
