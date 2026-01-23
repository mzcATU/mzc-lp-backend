# mzc-lp-backend

> MZC Learn Platform - Backend API Server

---

## 기술 스택

| 구분 | 기술 | 버전 |
|------|------|------|
| Language | Java | 21 |
| Framework | Spring Boot | 3.4.12 |
| ORM | Spring Data JPA | - |
| Security | Spring Security + JWT | jjwt 0.12.6 |
| Database | MySQL | 8.0 |
| API Docs | SpringDoc OpenAPI | 2.7.0 |
| Build | Gradle | - |
| Test Coverage | JaCoCo | 0.8.12 |
| Code Quality | SonarQube | - |

---

## 실행 방법

### 1. 환경 설정

```bash
cp .env.example .env
# .env 파일 수정 (DB, JWT 설정)
```

### 2. 서버 실행

```bash
./gradlew bootRun
```

### 3. 접속 확인

- API: http://localhost:8080
- Health: http://localhost:8080/actuator/health
- Swagger UI: http://localhost:8080/swagger-ui.html

---

## 프로젝트 구조

```
src/main/java/com/mzc/lp/
├── common/                  # 공통 모듈
│   ├── config/              # 설정 (Security, Web, JPA 등)
│   ├── constant/            # 상수 정의
│   ├── context/             # 컨텍스트 (TenantContext 등)
│   ├── dto/                 # 공통 DTO
│   ├── entity/              # 공통 엔티티 (BaseEntity 등)
│   ├── exception/           # 예외 처리
│   ├── filter/              # 필터 (JWT, Tenant 등)
│   ├── security/            # 보안 관련
│   ├── service/             # 공통 서비스
│   └── util/                # 유틸리티
│
└── domain/                  # 도메인 모듈
    ├── user/                # 사용자 관리
    ├── tenant/              # 테넌트 관리
    ├── department/          # 부서 관리
    ├── employee/            # 직원 관리
    ├── course/              # 과정 관리
    ├── content/             # 콘텐츠 관리
    ├── learning/            # 학습 관리
    ├── enrollment/          # 수강 신청
    ├── assignment/          # 과제 관리
    ├── certificate/         # 수료증 관리
    ├── iis/                 # 강사 정보 시스템
    ├── analytics/           # 학습 분석
    ├── dashboard/           # 대시보드
    ├── notification/        # 알림
    ├── notice/              # 공지사항
    ├── tenantnotice/        # 테넌트 공지
    ├── banner/              # 배너 관리
    ├── category/            # 카테고리
    ├── roadmap/             # 로드맵
    ├── community/           # 커뮤니티
    ├── cart/                # 장바구니
    ├── wishlist/            # 위시리스트
    ├── memberpool/          # 멤버풀
    ├── student/             # 학생 정보
    ├── snapshot/            # 스냅샷
    ├── system/              # 시스템 설정
    ├── sa/                  # 시스템 어드민
    ├── ts/                  # 테넌트 설정
    └── tu/                  # 테넌트 사용자
```

---

## 테스트

```bash
# 테스트 실행
./gradlew test

# 테스트 커버리지 리포트 생성
./gradlew jacocoTestReport
# 리포트 위치: build/reports/jacoco/test/html/index.html
```

---

## 환경 변수

| 변수 | 설명 | 기본값 |
|------|------|--------|
| DB_HOST | 데이터베이스 호스트 | localhost |
| DB_PORT | 데이터베이스 포트 | 3306 |
| DB_NAME | 데이터베이스 이름 | mza_newlp |
| DB_USERNAME | 데이터베이스 사용자 | root |
| DB_PASSWORD | 데이터베이스 비밀번호 | - |
| JWT_SECRET | JWT 서명 키 | - |
| JWT_ACCESS_EXPIRATION | Access 토큰 만료 시간 (ms) | 900000 |
| JWT_REFRESH_EXPIRATION | Refresh 토큰 만료 시간 (ms) | 604800000 |
| CORS_ALLOWED_ORIGINS | CORS 허용 도메인 | http://localhost:3000 |
| JPA_DDL_AUTO | JPA DDL 자동 생성 모드 | update |
| FILE_UPLOAD_DIR | 파일 업로드 경로 | ./uploads |
| SERVER_PORT | 서버 포트 | 8080 |
| SPRING_PROFILES_ACTIVE | 활성 프로파일 | local |

---

## 관련 문서

| 문서 | 위치 |
|------|------|
| 전체 문서 | [mzc-lp-docs](https://github.com/mzcATU/mzc-lp-docs) |
| 컨벤션 | [docs/conventions/](https://github.com/mzcATU/mzc-lp-docs/tree/main/docs/conventions) |
| API 스펙 | [docs/structure/backend/](https://github.com/mzcATU/mzc-lp-docs/tree/main/docs/structure/backend) |
