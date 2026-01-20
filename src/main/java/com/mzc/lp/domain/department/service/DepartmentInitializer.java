package com.mzc.lp.domain.department.service;

import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 애플리케이션 시작 시 기존 사용자들의 부서 정보를 기반으로 부서를 자동 생성하는 초기화 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DepartmentInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final DepartmentService departmentService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Starting department initialization from existing user data...");

        try {
            // 테넌트별 고유 부서명 목록 조회
            List<Object[]> tenantDepartments = userRepository.findDistinctDepartmentsByTenant();

            int createdCount = 0;
            for (Object[] row : tenantDepartments) {
                Long tenantId = (Long) row[0];
                String departmentName = (String) row[1];

                if (tenantId != null && departmentName != null && !departmentName.isBlank()) {
                    try {
                        departmentService.getOrCreateByName(tenantId, departmentName);
                        createdCount++;
                    } catch (Exception e) {
                        log.warn("Failed to create department: tenantId={}, name={}, error={}",
                                tenantId, departmentName, e.getMessage());
                    }
                }
            }

            log.info("Department initialization completed. Processed {} tenant-department pairs.", createdCount);
        } catch (Exception e) {
            log.error("Department initialization failed", e);
        }
    }
}
