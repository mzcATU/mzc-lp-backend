# Backend - AI 작업 가이드

> MZC Learn Platform Backend API Server

---

## 문서 참조

| 작업 | 문서 |
|------|------|
| 컨벤션 | [mzc-lp-docs/docs/conventions/](https://github.com/mzcATU/mzc-lp-docs/tree/main/docs/conventions) |
| API 스펙 | [mzc-lp-docs/docs/structure/backend/](https://github.com/mzcATU/mzc-lp-docs/tree/main/docs/structure/backend) |
| 설정 가이드 | [mzc-lp-docs/docs/context/backend-setup.md](https://github.com/mzcATU/mzc-lp-docs/blob/main/docs/context/backend-setup.md) |

> 전체 문서: [mzc-lp-docs](https://github.com/mzcATU/mzc-lp-docs)

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.4.12 |
| ORM | Spring Data JPA |
| Database | MySQL 8.0 |
| Build | Gradle |

---

## 핵심 규칙

```
✅ Entity: Setter 금지 → 비즈니스 메서드 사용
✅ Service: @Transactional(readOnly=true) 클래스 레벨
✅ Controller: try-catch 금지 → GlobalExceptionHandler
✅ DTO: Java Record + from() 정적 팩토리
✅ Enum: @Enumerated(EnumType.STRING)
```

---

## 작업 순서

```
Entity → Repository → DTO → Exception → Service → Controller → Test
```

---

## 실행 방법

```bash
# 1. DB 실행
docker-compose up -d

# 2. 서버 실행
./gradlew bootRun
```
