# mzc-lp-backend

> MZC Learn Platform - Backend API Server

---

## 기술 스택

| 구분 | 기술 | 버전 |
|------|------|------|
| Language | Java | 21 |
| Framework | Spring Boot | 3.2.x |
| ORM | Spring Data JPA | - |
| Database | MySQL | 8.0 |
| Cache | Redis | 7.x |
| Build | Gradle | 8.5 |

---

## 실행 방법

### 1. 환경 설정

```bash
cp .env.example .env
# .env 파일 수정
```

### 2. DB 실행

```bash
docker-compose up -d
```

### 3. 서버 실행

```bash
./gradlew bootRun
```

### 4. 접속 확인

- API: http://localhost:8080
- Health: http://localhost:8080/actuator/health

---

## 프로젝트 구조

```
src/main/java/com/mzc/lp/
├── common/                  # 공통 (config, exception, response)
├── domain/                  # 도메인 모듈
│   ├── user/                # UM (User Master)
│   ├── course/              # CM (Course Matrix) + CR
│   ├── content/             # CMS (Content Management)
│   ├── learning/            # LO (Learning Object)
│   ├── enrollment/          # SIS (Student Info)
│   └── instructor/          # IIS (Instructor Info)
└── infra/                   # S3, Redis 연동
```

---

## 관련 문서

| 문서 | 위치 |
|------|------|
| 전체 문서 | [mzc-lp-docs](https://github.com/mzcATU/mzc-lp-docs) |
| 컨벤션 | [docs/conventions/](https://github.com/mzcATU/mzc-lp-docs/tree/main/docs/conventions) |
| API 스펙 | [docs/structure/backend/](https://github.com/mzcATU/mzc-lp-docs/tree/main/docs/structure/backend) |
