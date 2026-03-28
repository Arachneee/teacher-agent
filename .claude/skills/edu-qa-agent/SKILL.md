---
name: edu-qa-agent
description: >
  교육 도메인 QA 에이전트. Playwright를 사용하여 Teacher Agent 프로젝트의 프론트엔드부터 백엔드까지
  E2E(End-to-End) 기능 테스트를 수행하고, 어떤 기능에 문제가 있는지 보고한다.
  사용자가 "QA", "테스트", "E2E", "Playwright", "기능 테스트", "동작 확인", "버그 확인",
  "전체 테스트", "통합 테스트", "화면 테스트", "시나리오 테스트", "회귀 테스트",
  "정상 동작", "기능 검증", "QA 보고서", "테스트 실행", "e2e 테스트 작성",
  "qa", "test", "playwright", "end-to-end" 등을 언급하면 이 skill을 사용할 것.
---

# 교육 도메인 QA 에이전트

## 역할

Teacher Agent 프로젝트의 품질 보증 전문가.
Playwright를 사용하여 프론트엔드(Next.js)부터 백엔드(Spring Boot)까지 전체 기능이 정상 동작하는지 검증하고,
문제가 발견되면 정확한 위치와 재현 방법을 포함한 보고서를 작성한다.

## 기술 스택

| 항목 | 기술 |
|------|------|
| E2E 프레임워크 | Playwright 1.58.2 |
| 프론트엔드 | Next.js 16 (http://localhost:3000) |
| 백엔드 | Spring Boot 4.0.3 (http://localhost:8080) |
| 테스트 디렉토리 | `frontend/e2e/` |
| 설정 파일 | `frontend/playwright.config.ts` |

## Playwright 설정

```typescript
// frontend/playwright.config.ts
import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  timeout: 30000,
  use: {
    baseURL: 'http://localhost:3000',
    headless: true,
  },
});
```

- 테스트 디렉토리: `./e2e`
- 기본 URL: `http://localhost:3000`
- 타임아웃: 30초
- 헤드리스 모드
- webServer 미설정 → 프론트엔드/백엔드 수동 기동 필요

---

## 테스트 대상 전체 맵

### 프론트엔드 페이지

| 경로 | 파일 | 설명 |
|------|------|------|
| `/login` | `login/page.tsx` | 로그인 |
| `/` | `(app)/page.tsx` | 메인 (리다이렉트) |
| `/calendar` | `(app)/calendar/page.tsx` | 주간 캘린더 |
| `/intro` | `(app)/intro/page.tsx` | 인트로 |
| `/students` | `(app)/students/page.tsx` | 학생 관리 |
| `/students/[id]` | `(app)/students/[id]/page.tsx` | 학생 상세 |
| `/lessons/[lessonId]` | `(app)/lessons/[lessonId]/page.tsx` | 수업 상세 |

### 백엔드 API 엔드포인트

**인증 (`/auth`)**
- `POST /auth/login` — 로그인
- `POST /auth/logout` — 로그아웃
- `GET /auth/me` — 현재 사용자

**선생님 (`/teachers`)**
- `GET /teachers/me` — 프로필 조회
- `PUT /teachers/me` — 프로필 수정

**학생 (`/students`)**
- `POST /students` — 생성
- `GET /students` — 목록
- `GET /students/{id}` — 단건 조회
- `PUT /students/{id}` — 수정
- `DELETE /students/{id}` — 삭제

**수업 (`/lessons`)**
- `POST /lessons` — 생성
- `GET /lessons?weekStart=` — 주간 목록
- `GET /lessons/{id}` — 단건 조회
- `GET /lessons/{id}/detail` — 상세 (수강생+피드백)
- `PUT /lessons/{id}` — 수정
- `DELETE /lessons/{id}?scope=` — 삭제

**수강생 (`/lessons/{lessonId}/attendees`)**
- `POST /lessons/{lessonId}/attendees` — 추가
- `GET /lessons/{lessonId}/attendees` — 목록
- `DELETE /lessons/{lessonId}/attendees/{attendeeId}` — 제거

**피드백 (`/feedbacks`)**
- `POST /feedbacks` — 생성
- `GET /feedbacks?studentId=` — 목록
- `GET /feedbacks/{id}` — 단건 조회
- `PATCH /feedbacks/{id}` — 수정
- `DELETE /feedbacks/{id}` — 삭제
- `POST /feedbacks/{id}/generate` — AI 생성 (동기)
- `GET /feedbacks/{id}/generate/stream` — AI 생성 (스트리밍)
- `POST /feedbacks/{id}/keywords` — 키워드 추가
- `PUT /feedbacks/{id}/keywords/{keywordId}` — 키워드 수정
- `DELETE /feedbacks/{id}/keywords/{keywordId}` — 키워드 삭제
- `POST /feedbacks/{id}/like` — 좋아요

---

## 테스트 작성 컨벤션

### 파일 위치 및 네이밍

```
frontend/e2e/
├── auth.spec.ts              — 인증 플로우
├── student.spec.ts           — 학생 CRUD
├── lesson.spec.ts            — 수업 CRUD + API 헬퍼
├── lesson-crud.spec.ts       — 수업 생성/네비게이션
├── recurring-lesson.spec.ts  — 반복 수업
└── attendee.spec.ts          — 수강생 관리
```

새 테스트 파일: `frontend/e2e/{기능명}.spec.ts`

### 테스트 계정

| 항목 | 값 |
|------|-----|
| 로그인 ID | `admin` |
| 비밀번호 | `123` |

모든 E2E 테스트는 이 계정으로 로그인한다. 환경변수 `INITIAL_TEACHER_USER_ID`, `INITIAL_TEACHER_PASSWORD`로 서버 기동 시 자동 생성되는 계정이다.

### 로그인 헬퍼

모든 테스트는 로그인이 필요하다. 각 spec 파일에 인라인 로그인 헬퍼를 사용한다.

```typescript
import { test, expect, type Page } from '@playwright/test';

async function login(page: Page) {
  await page.goto('/login');
  await page.fill('input[type="text"]', 'admin');
  await page.fill('input[type="password"]', '123');
  await page.click('button[type="submit"]');
  await page.waitForURL('**/calendar', { timeout: 10000 });
}
```

### 테스트 구조 패턴

```typescript
// 의존성 있는 테스트는 serial로 실행
test.describe.serial('학생 관리', () => {
  let page: Page;

  test.beforeAll(async ({ browser }) => {
    page = await browser.newPage();
    await login(page);
  });

  test.afterAll(async () => {
    await page.close();
  });

  test('학생 생성', async () => {
    // ...
  });

  test('학생 수정', async () => {
    // ...
  });

  test('학생 삭제', async () => {
    // ...
  });
});
```

### API 헬퍼 패턴 (테스트 데이터 생성)

UI를 거치지 않고 API로 직접 테스트 데이터를 생성할 때 사용한다.

```typescript
async function createStudentViaAPI(page: Page, name: string, grade: string = 'ELEMENTARY_3') {
  return page.evaluate(
    async ({ name, grade }) => {
      const res = await fetch('/api/students', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ name, memo: '', grade }),
      });
      return res.json();
    },
    { name, grade }
  );
}

async function createLessonViaAPI(page: Page, title: string, startTime: string, endTime: string) {
  return page.evaluate(
    async ({ title, startTime, endTime }) => {
      const res = await fetch('/api/lessons', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ title, startTime, endTime }),
      });
      return res.json();
    },
    { title, startTime, endTime }
  );
}
```

### 테스트 데이터 유니크성

테스트 데이터는 `Date.now()`를 사용하여 고유하게 생성한다.

```typescript
const uniqueName = `테스트학생_${Date.now()}`;
const uniqueTitle = `테스트수업_${Date.now()}`;
```

### 셀렉터 우선순위

1. `aria-label` (접근성 기반, 가장 안정적)
2. 텍스트 콘텐츠 (`page.getByText()`, `page.getByRole()`)
3. CSS 셀렉터 (구조 기반)
4. XPath (최후의 수단, ancestor 탐색 등)

```typescript
// Good — aria-label 기반
await page.click('button[aria-label="수업에서 제거"]');

// Good — 텍스트 기반
await page.getByText('추가하기').click();

// OK — CSS 셀렉터
await page.fill('input[placeholder="키워드 / 문장 입력"]', '열심히 참여함');

// 최후의 수단 — XPath
await page.locator(`//span[contains(text(), "${name}")]/ancestor::div[contains(@class, "rounded-3xl")]`);
```

### 대기 전략

```typescript
// 네비게이션 대기
await page.waitForURL('**/calendar', { timeout: 10000 });

// 요소 가시성 대기
await page.waitForSelector(`text=${studentName}`, { timeout: 5000 });

// UI 안정화 대기 (애니메이션, 렌더링)
await page.waitForTimeout(300);  // 최소한으로 사용

// 네트워크 요청 완료 대기
await page.waitForResponse(res => res.url().includes('/api/students') && res.status() === 200);
```

---

## 핵심 검증 원칙 (절대 위반 금지)

### 변경 유형에 따라 검증 전략을 결정한다

QA 에이전트는 이번 개발에서 **무엇이 변경되었는지**를 먼저 파악하고, 변경 유형에 맞는 검증 전략을 선택한다.

#### 변경 유형 분류

`git diff --stat`으로 변경된 파일을 확인하여 아래 3가지 유형으로 분류한다:

| 변경 유형 | 판단 기준 | 검증 전략 |
|-----------|-----------|-----------|
| A. 프론트엔드 + 백엔드 변경 | `backend/` + `frontend/` 모두 변경 | UI 동작 → API 호출 → DB 반영 관통 검증 |
| B. 백엔드만 변경 | `backend/`만 변경, `frontend/` 미변경 | API 호출 → DB 반영 검증 |
| C. 프론트엔드만 변경 | `frontend/`만 변경, `backend/` 미변경 | UI 변화 검증 |

#### 유형 A: 프론트엔드 + 백엔드 변경 (UI → DB → UI 관통 검증)

**가장 중요한 검증 전략.** 사용자가 UI에서 동작을 수행했을 때, 그 결과가 DB까지 정확히 반영되고, DB에 반영된 결과가 다시 UI에 올바르게 표시되는지 **끝에서 끝까지 왕복 검증**한다.

검증 흐름:
```
[1] Playwright로 UI 동작 수행 (버튼 클릭, 폼 제출 등)
    │
    ▼
[2] API 호출이 발생했는지 확인 (네트워크 응답 가로채기)
    │
    ▼
[3] DB에 데이터가 정확히 반영되었는지 확인 (MySQL 직접 쿼리)
    │
    ▼
[4] DB 반영 결과가 UI에 올바르게 표시되는지 확인 (화면 재검증)
```

**[4]가 핵심이다.** DB에 데이터가 저장되었더라도, UI가 그 결과를 올바르게 반영하지 않으면 사용자에게는 동작하지 않는 것과 같다.

**관통 검증 E2E 테스트 패턴:**

```typescript
test('학생 생성 — UI → DB → UI 관통 검증', async () => {
  const uniqueName = `관통테스트_${Date.now()}`;

  // [1] UI 동작 수행
  await page.goto('/students');
  await page.getByText('학생 추가').click();
  await page.fill('input[placeholder="이름"]', uniqueName);
  await page.selectOption('select', 'MIDDLE_1');
  await page.getByText('저장').click();

  // [2] API 응답 확인
  const studentList = await page.evaluate(async () => {
    const res = await fetch('/api/students', { credentials: 'include' });
    return res.json();
  });
  const created = studentList.find((s: any) => s.name === uniqueName);
  expect(created).toBeTruthy();
  expect(created.grade).toBe('MIDDLE_1');

  // [3] DB 직접 쿼리로 적재 확인
  // (E2E 테스트 밖에서 별도 실행)

  // [4] DB 반영 결과가 UI에 표시되는지 확인
  // 목록 화면에서 새로 생성한 학생이 보이는지 검증
  await page.goto('/students');
  await expect(page.getByText(uniqueName)).toBeVisible({ timeout: 5000 });
  // 상세 화면에서 저장된 데이터가 정확히 표시되는지 검증
  await page.getByText(uniqueName).click();
  await expect(page.getByText('MIDDLE_1')).toBeVisible();
});
```

**DB 검증은 E2E 테스트 실행 후 별도로 수행한다:**

```bash
# UI에서 생성한 데이터가 DB에 정확히 반영되었는지 확인
docker exec teacher-agent-mysql mysql -u admin -plocal-password teacheragent \
  -e "SELECT id, name, grade, teacher_id FROM student WHERE name LIKE '관통테스트_%' ORDER BY id DESC LIMIT 1;"
```

**관통 검증 체크리스트:**

| UI 동작 | API 확인 | DB 확인 | UI 재검증 |
|---------|----------|---------|-----------|
| 생성 폼 제출 | POST 201 응답 | 새 레코드 존재, 필드 값 일치 | 목록에 새 항목 표시, 상세에서 필드 값 일치 |
| 수정 폼 제출 | PUT 200 응답 | 해당 레코드 필드 값 변경됨 | 목록/상세에서 변경된 값 표시 |
| 삭제 버튼 클릭 | DELETE 204 응답 | 해당 레코드 삭제됨 | 목록에서 항목 사라짐 |
| 토글/상태 변경 | PATCH/PUT 200 응답 | 상태 필드 값 변경됨 | UI에 변경된 상태 반영 (아이콘, 텍스트, 색상 등) |

**UI 재검증이 필요한 대표 시나리오:**

| 시나리오 | DB 반영 | UI 재검증 포인트 |
|----------|---------|-----------------|
| 학생 생성 | student 테이블에 INSERT | 학생 목록에 새 카드 표시, 이름/학년 정확 |
| 수업 수정 (반복) | lesson 테이블 UPDATE (여러 건) | 캘린더에서 수정된 수업 제목/시간 반영 |
| 수강생 추가 | attendee 테이블에 INSERT | 수업 상세에서 수강생 목록에 이름 표시 |
| AI 피드백 생성 | feedback 테이블 UPDATE (ai_content) | 피드백 화면에 AI 생성 내용 표시 |
| 키워드 삭제 | feedback_keyword 테이블 DELETE | 피드백 화면에서 키워드 태그 사라짐 |
| 좋아요 | feedback_like 테이블 INSERT + snapshot | 좋아요 아이콘 활성화, 스냅샷 저장 확인 |

#### 유형 B: 백엔드만 변경 (API → DB 검증)

프론트엔드 변경이 없으므로 UI 검증은 생략하고, API를 직접 호출하여 DB까지 정확히 반영되는지 검증한다.

검증 흐름:
```
[1] curl로 API 직접 호출
    │
    ▼
[2] API 응답 코드 및 응답 바디 확인
    │
    ▼
[3] DB에 데이터가 정확히 반영되었는지 확인 (MySQL 직접 쿼리)
```

**API → DB 검증 패턴:**

```bash
# [1] API 호출
RESPONSE=$(curl -sf -b /tmp/qa-cookies.txt \
  -H "Content-Type: application/json" \
  -d '{"name":"API테스트학생","memo":"","grade":"MIDDLE_1"}' \
  http://localhost:8080/students)

# [2] API 응답 확인
echo "$RESPONSE" | grep -q '"name":"API테스트학생"' && echo "API OK" || echo "API FAIL"

# [3] DB 적재 확인
docker exec teacher-agent-mysql mysql -u admin -plocal-password teacheragent \
  -e "SELECT id, name, grade FROM student WHERE name = 'API테스트학생' ORDER BY id DESC LIMIT 1;"
```

**기존 E2E 테스트도 실행하여 회귀를 확인한다** (백엔드 변경이 프론트엔드 동작을 깨뜨리지 않았는지).

#### 유형 C: 프론트엔드만 변경 (UI 변화 검증)

백엔드 변경이 없으므로 DB 검증은 생략하고, UI가 의도대로 변경되었는지만 검증한다.

검증 대상:
- 새 컴포넌트/페이지가 올바르게 렌더링되는가
- 기존 UI 동작이 깨지지 않았는가 (회귀 검증)
- 반응형 레이아웃이 정상인가 (모바일/데스크톱)
- 사용자 인터랙션(클릭, 입력, 네비게이션)이 정상인가

```typescript
test('프론트엔드 변경 — UI 렌더링 검증', async () => {
  await page.goto('/students');

  // 새 컴포넌트가 렌더링되는지 확인
  await expect(page.getByText('새로운 UI 요소')).toBeVisible();

  // 기존 동작이 깨지지 않았는지 확인
  await page.getByText('학생 추가').click();
  await expect(page.locator('.modal')).toBeVisible();
});
```

### 변경 유형과 무관한 필수 사항

어떤 유형이든 다음은 반드시 수행한다:

1. **기존 E2E 테스트 회귀 실행**: `npx playwright test` 전체 실행하여 기존 기능이 깨지지 않았는지 확인
2. **새 기능 E2E 테스트 작성**: 이번에 추가/변경된 기능에 대한 테스트 파일 작성
3. **QA 보고서 작성**: 검증 결과를 보고서 형식으로 정리

---

## QA 실행 워크플로우

### Step 1: 서버 상태 확인

테스트 실행 전 반드시 확인한다:

```bash
# 1. MySQL 실행 확인
docker ps --filter "name=teacher-agent-mysql" --format "{{.Status}}" 2>/dev/null

# 2. 백엔드 실행 확인
curl -sf http://localhost:8080/auth/me 2>/dev/null; echo "EXIT:$?"
# 401 응답이면 정상 (미인증 상태)

# 3. 프론트엔드 실행 확인
curl -sf http://localhost:3000 2>/dev/null | head -1
# HTML 응답이면 정상
```

### Step 2: 서버 자동 기동 (미실행 시)

서버가 실행 중이 아니면 **직접 기동한다.** 사용자에게 기동을 요청하지 않는다.

#### 2-1. MySQL 기동 (docker-compose)

```bash
cd backend && docker compose up -d
```

MySQL이 준비될 때까지 대기한다:

```bash
# MySQL 헬스체크 (최대 30초 대기)
for i in $(seq 1 30); do
  docker exec teacher-agent-mysql mysqladmin ping -h localhost -u admin -plocal-password 2>/dev/null && break
  sleep 1
done
```

#### 2-2. 백엔드 기동 (tmux)

```bash
# 기존 세션 정리
tmux kill-session -t backend 2>/dev/null

# 백엔드 기동 (tmux 백그라운드)
tmux new-session -d -s backend "cd /path/to/project/backend && OPENAI_API_KEY=dummy-key ./gradlew bootRun 2>&1"
```

백엔드가 준비될 때까지 대기한다:

```bash
# 백엔드 헬스체크 (최대 60초 대기)
for i in $(seq 1 60); do
  curl -sf http://localhost:8080/auth/me 2>/dev/null && break
  sleep 1
done
```

#### 2-3. 프론트엔드 기동 (tmux)

```bash
# 기존 세션 정리
tmux kill-session -t frontend 2>/dev/null

# 프론트엔드 기동 (tmux 백그라운드)
tmux new-session -d -s frontend "cd /path/to/project/frontend && npm run dev 2>&1"
```

프론트엔드가 준비될 때까지 대기한다:

```bash
# 프론트엔드 헬스체크 (최대 30초 대기)
for i in $(seq 1 30); do
  curl -sf http://localhost:3000 2>/dev/null && break
  sleep 1
done
```

#### 기동 실패 시

3개 서비스 중 하나라도 기동에 실패하면:
1. 에러 로그를 수집한다 (`tmux capture-pane -p -t backend` 등)
2. 실패 원인을 보고한다
3. QA를 중단한다 (서버 없이 QA 불가)

### Step 3: API 레벨 검증

Playwright E2E 테스트 전에, 먼저 API를 직접 호출하여 백엔드가 정상 동작하는지 확인한다.

#### 로그인 + 세션 쿠키 획득

```bash
# 로그인하여 세션 쿠키 저장
curl -sf -c /tmp/qa-cookies.txt \
  -H "Content-Type: application/json" \
  -d '{"userId":"admin","password":"123"}' \
  http://localhost:8080/auth/login
```

#### API 직접 호출 패턴

```bash
# 세션 쿠키로 인증된 API 호출
curl -sf -b /tmp/qa-cookies.txt \
  -H "Content-Type: application/json" \
  -d '{"eventType":"feedback_copy","metadata":"{\"feedbackId\":1}"}' \
  http://localhost:8080/events
```

### Step 4: DB 적재 검증

API 호출 후 데이터가 DB에 정확히 적재되었는지 확인한다.

```bash
# MySQL 직접 쿼리
docker exec teacher-agent-mysql mysql -u admin -plocal-password teacheragent \
  -e "SELECT id, user_id, event_type, metadata, created_at FROM user_event ORDER BY id DESC LIMIT 5;"
```

#### DB 검증 체크리스트

| 확인 항목 | 쿼리 예시 |
|-----------|-----------|
| 레코드 존재 여부 | `SELECT COUNT(*) FROM {table} WHERE {condition}` |
| 필드 값 정확성 | `SELECT * FROM {table} WHERE id = {id}` |
| 인덱스 동작 | `EXPLAIN SELECT * FROM {table} WHERE {indexed_column} = {value}` |
| 관계 무결성 | `SELECT * FROM {table} JOIN {related_table} ON ...` |

### Step 5: Playwright E2E 테스트

API + DB 검증이 통과한 후, UI 레벨 E2E 테스트를 실행한다.

#### 테스트 실행 명령어

```bash
cd frontend

# 전체 E2E 테스트 실행
npx playwright test

# 특정 파일 실행
npx playwright test e2e/auth.spec.ts

# 특정 테스트만 실행
npx playwright test -g "학생 생성"

# 디버그 모드 (브라우저 표시)
npx playwright test --headed

# 실패한 테스트만 재실행
npx playwright test --last-failed
```

#### 테스트 실행 순서

기능 간 의존성을 고려하여 이 순서로 실행한다:

```
1. auth.spec.ts        — 인증 (모든 테스트의 기반)
2. student.spec.ts     — 학생 CRUD
3. lesson.spec.ts      — 수업 CRUD
4. lesson-crud.spec.ts — 수업 생성/네비게이션
5. recurring-lesson.spec.ts — 반복 수업
6. attendee.spec.ts    — 수강생 관리
7. {신규 기능}.spec.ts — 새로 추가된 기능
```

### Step 6: 서버 정리 (QA 완료 후)

QA 완료 후 기동한 서버를 정리한다:

```bash
# tmux 세션 종료
tmux kill-session -t backend 2>/dev/null
tmux kill-session -t frontend 2>/dev/null

# Docker 컨테이너는 유지 (개발 중 재사용 가능)
# 완전 정리가 필요하면: cd backend && docker compose down
```

---

## QA 보고서 형식

테스트 실행 후 반드시 아래 형식으로 보고서를 작성한다.

### 전체 통과 시

```
## ✅ QA 보고서

**실행 시간**: 2024-01-15 14:30 KST
**테스트 환경**: localhost (프론트엔드 :3000, 백엔드 :8080)

### 결과 요약
| 항목 | 결과 |
|------|------|
| 총 테스트 | 15개 |
| 통과 | 15개 |
| 실패 | 0개 |
| 건너뜀 | 0개 |

### 검증된 기능
- ✅ 로그인/로그아웃
- ✅ 학생 CRUD
- ✅ 수업 생성/수정/삭제
- ✅ 수강생 추가/제거
- ✅ 반복 수업

모든 기능이 정상 동작합니다.
```

### 실패 발견 시

```
## ❌ QA 보고서

**실행 시간**: 2024-01-15 14:30 KST
**테스트 환경**: localhost (프론트엔드 :3000, 백엔드 :8080)

### 결과 요약
| 항목 | 결과 |
|------|------|
| 총 테스트 | 15개 |
| 통과 | 13개 |
| 실패 | 2개 |
| 건너뜀 | 0개 |

### ❌ 실패 목록

#### 1. [CRITICAL] 학생 삭제 후 목록 미갱신
- **파일**: `e2e/student.spec.ts` > "학생 삭제"
- **증상**: 학생 삭제 API는 204 반환하지만, 화면에서 학생 카드가 사라지지 않음
- **재현 경로**: 학생 관리 → 학생 카드 → 삭제 버튼 → 확인
- **예상 원인**: 삭제 후 학생 목록 re-fetch 누락
- **관련 코드**: `frontend/src/app/components/StudentsView.tsx`
- **심각도**: CRITICAL (핵심 기능 장애)

#### 2. [MINOR] 수업 제목 빈 값 허용
- **파일**: `e2e/lesson.spec.ts` > "수업 생성 — 빈 제목"
- **증상**: 제목 없이 수업 생성 시 에러 표시 없이 빈 제목으로 생성됨
- **재현 경로**: 캘린더 → + 버튼 → 제목 비우고 저장
- **예상 원인**: 프론트엔드 유효성 검증 누락
- **관련 코드**: `frontend/src/app/components/AddLessonModal/LessonDetailsStep.tsx`
- **심각도**: MINOR (UX 개선 필요)

### ✅ 정상 동작 기능
- ✅ 로그인/로그아웃
- ✅ 학생 생성/수정
- ✅ 수업 수정/삭제
- ✅ 수강생 추가/제거
- ✅ 반복 수업
```

### 심각도 기준

| 심각도 | 기준 | 예시 |
|--------|------|------|
| CRITICAL | 핵심 기능 장애, 데이터 손실 위험 | 로그인 불가, 데이터 저장 실패, 삭제 후 미반영 |
| MAJOR | 주요 기능 부분 장애 | AI 생성 실패, 반복 수업 범위 오류 |
| MINOR | UX 불편, 비핵심 기능 이슈 | 유효성 검증 누락, 스타일 깨짐 |
| INFO | 개선 제안, 성능 이슈 | 느린 로딩, 접근성 개선 |

---

## 테스트 시나리오 가이드

새 기능이 추가되거나 QA 범위를 확장할 때 이 가이드를 참고한다.

### 인증 시나리오
- 정상 로그인 → 캘린더 리다이렉트
- 잘못된 비밀번호 → 에러 메시지
- 미인증 상태에서 보호 페이지 접근 → 로그인 리다이렉트
- 로그아웃 → 로그인 페이지 이동

### 학생 관리 시나리오
- 학생 생성 (이름 + 학년)
- 학생 목록 조회
- 학생 정보 수정 (이름, 메모, 학년)
- 학생 삭제 + 확인 모달
- 빈 이름으로 생성 시도 → 에러

### 수업 관리 시나리오
- 단일 수업 생성 (제목, 시작/종료 시간)
- 반복 수업 생성 (DAILY/WEEKLY/MONTHLY)
- 수업 수정 (SINGLE/THIS_AND_FOLLOWING/ALL 범위)
- 수업 삭제 (범위 선택)
- 주간 캘린더에서 수업 표시 확인

### 수강생 관리 시나리오
- 수업에 학생 추가
- 수업에서 학생 제거 + 확인 모달
- 반복 수업 수강생 추가 (범위 선택)
- 드래그 앤 드롭 순서 변경

### 피드백 시나리오
- 키워드 추가 (일반 / required)
- 키워드 수정, 삭제
- AI 피드백 생성 (키워드 1개 이상 필요)
- AI 피드백 스트리밍 표시
- 피드백 수동 편집
- 좋아요 + 스냅샷 저장
- 클립보드 복사

---

## 새 테스트 추가 체크리스트

1. `frontend/e2e/` 디렉토리에 `{기능명}.spec.ts` 파일 생성
2. 로그인 헬퍼 함수 포함
3. `test.describe.serial()` 사용 (의존성 있는 테스트)
4. 테스트 데이터는 `Date.now()`로 유니크하게 생성
5. API 헬퍼로 사전 데이터 생성 (UI 테스트에 집중)
6. 셀렉터는 aria-label > 텍스트 > CSS > XPath 우선순위
7. 적절한 대기 전략 사용 (waitForURL, waitForSelector)
8. 테스트 실행 후 QA 보고서 형식으로 결과 보고

---

## 빌드 & 실행 명령어

```bash
cd frontend

# Playwright 설치 (최초 1회)
npx playwright install

# 전체 E2E 테스트
npx playwright test

# 특정 파일
npx playwright test e2e/auth.spec.ts

# 디버그 모드
npx playwright test --headed --debug

# 보고서 확인
npx playwright show-report
```
