# Branch Protection Rules

> GitHub Repository Settings → Branches에서 설정

---

## main 브랜치 보호 규칙

### Settings → Branches → Add branch protection rule

**Branch name pattern:** `main`

#### Protect matching branches

- [x] **Require a pull request before merging**
  - [x] Require approvals: `1`
  - [x] Dismiss stale pull request approvals when new commits are pushed
  - [x] Require approval of the most recent reviewable push

- [x] **Require status checks to pass before merging**
  - [x] Require branches to be up to date before merging
  - Status checks: `build`, `test` (CI 설정 후 추가)

- [x] **Require conversation resolution before merging**

- [x] **Do not allow bypassing the above settings**

#### Rules applied to everyone including administrators

- [x] **Restrict who can push to matching branches**
  - 허용: Repository admins only

---

## dev 브랜치 보호 규칙

**Branch name pattern:** `dev`

#### Protect matching branches

- [x] **Require a pull request before merging**
  - [x] Require approvals: `1`

- [x] **Require status checks to pass before merging**
  - [x] Require branches to be up to date before merging

- [x] **Require conversation resolution before merging**

---

## 브랜치 네이밍 규칙

| 브랜치 | 패턴 | 예시 |
|--------|------|------|
| 기능 | `feat/{issue}-{description}` | `feat/123-user-login` |
| 버그 | `fix/{issue}-{description}` | `fix/456-auth-error` |
| 리팩토링 | `refactor/{issue}-{description}` | `refactor/789-split-service` |
| 문서 | `docs/{issue}-{description}` | `docs/101-api-docs` |
| 긴급 | `hotfix/{issue}-{description}` | `hotfix/999-critical-bug` |

---

## Merge 전략

| 브랜치 | Merge 방식 |
|--------|-----------|
| `feat/*` → `dev` | **Squash and merge** |
| `fix/*` → `dev` | **Squash and merge** |
| `dev` → `main` | **Merge commit** |
| `hotfix/*` → `main` | **Merge commit** |

---

## 설정 방법

1. GitHub Repository → **Settings**
2. 좌측 메뉴 → **Branches**
3. **Add branch protection rule** 클릭
4. 위 규칙에 따라 설정
