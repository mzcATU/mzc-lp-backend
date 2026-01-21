package com.mzc.lp.common.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

/**
 * 시드 데이터 초기화 설정
 * local, dev 프로파일에서만 시드 데이터를 로드합니다.
 *
 * 시드 파일 순서:
 * V001: 테이블 초기화 (TRUNCATE)
 * V002: 테넌트
 * V003: 부서
 * V004: 사용자
 * V004_5: 직원
 * V005: 사용자 역할 (멀티롤 지원)
 * V006: 카테고리
 * V007: 콘텐츠
 * V007_5: 러닝오브젝트
 * V008: 코스 + 태그 + 아이템
 * V009: 스냅샷 + LO + 아이템
 * V010: 차수
 * V011: 수강
 * V011_5: 아이템별 학습 진도
 * V012: 강사 배정
 * V013: 사용자 코스 역할
 * V014: 커뮤니티
 * V015: 장바구니/찜
 * V016: 리뷰
 * V017: 로드맵 (비활성 - 스킵)
 * V018: 회원 풀 + 자동 입과 규칙
 */
@Configuration
public class DataSourceInitConfig {

    @Value("${spring.sql.init.mode:never}")
    private String sqlInitMode;

    @Bean
    @Profile({"local", "dev"})
    public DataSourceInitializer seedDataInitializer(DataSource dataSource) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setSqlScriptEncoding("UTF-8");
        populator.setSeparator(";");
        populator.setContinueOnError(false);

        // 시드 파일들을 순서대로 추가
        populator.addScript(new ClassPathResource("db/seed/V001__truncate_tables.sql"));
        populator.addScript(new ClassPathResource("db/seed/V002__tenants.sql"));
        populator.addScript(new ClassPathResource("db/seed/V003__departments.sql"));
        populator.addScript(new ClassPathResource("db/seed/V004__users.sql"));
        populator.addScript(new ClassPathResource("db/seed/V004_5__employees.sql"));
        populator.addScript(new ClassPathResource("db/seed/V005__user_roles.sql"));
        populator.addScript(new ClassPathResource("db/seed/V006__categories.sql"));
        populator.addScript(new ClassPathResource("db/seed/V007__contents.sql"));
        populator.addScript(new ClassPathResource("db/seed/V007_5__learning_objects.sql"));
        populator.addScript(new ClassPathResource("db/seed/V008__courses.sql"));
        populator.addScript(new ClassPathResource("db/seed/V009__snapshots.sql"));
        populator.addScript(new ClassPathResource("db/seed/V010__course_times.sql"));
        populator.addScript(new ClassPathResource("db/seed/V011__enrollments.sql"));
        populator.addScript(new ClassPathResource("db/seed/V011_5__item_progress.sql"));
        populator.addScript(new ClassPathResource("db/seed/V012__instructor_assignments.sql"));
        populator.addScript(new ClassPathResource("db/seed/V013__user_course_roles.sql"));
        populator.addScript(new ClassPathResource("db/seed/V014__community.sql"));
        populator.addScript(new ClassPathResource("db/seed/V015__cart_wishlist.sql"));
        populator.addScript(new ClassPathResource("db/seed/V016__reviews.sql"));
        // V017 로드맵 - 비활성 기능이므로 스킵
        // populator.addScript(new ClassPathResource("db/seed/V017__roadmaps.sql"));
        populator.addScript(new ClassPathResource("db/seed/V018__member_pools_and_auto_enrollment_rules.sql"));

        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(populator);
        // sql init mode가 always가 아니면 초기화하지 않음
        initializer.setEnabled("always".equals(sqlInitMode));

        return initializer;
    }
}
