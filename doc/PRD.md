# PRD: 선생님 조교 에이전트 — MVP

## 1. 개요

### 문제 정의

학원/과외 선생님은 수업이 끝날 때마다 학부모에게 학생별 피드백 문자를 작성해야 한다. 학생 수가 많을수록 개별 피드백을 정성스럽게 쓰는 데 상당한 시간이 소요되며, 반복적인 문구 작성에 피로감을 느낀다.

### 해결 방안

학생별 키워드만 입력하면 AI가 학부모용 피드백 문자를 자동 생성해주는 서비스를 제공한다. 선생님은 생성된 텍스트를 복사하여 메신저나 문자로 전송하면 된다.

## 2. 사용자

- **타겟**: 학원 강사, 과외 선생님
- **핵심 니즈**: 수업 후 학부모 피드백 작성 시간 단축
- **사용 환경**: PC 또는 모바일 웹 브라우저

## 3. 핵심 기능

### 3.1 학생 관리

- 학생 생성, 수정, 삭제
- 학생 정보: 이름 + 메모 (학생 특성, 주의사항 등)
- 학생 목록 조회

### 3.2 피드백 생성

- 학생을 선택하고 키워드 입력 (예: "집중력 좋았음, 3단원 이해 부족")
- AI가 키워드 + 학생 메모를 바탕으로 학부모용 피드백 문자 생성

### 3.3 피드백 복사

- 생성된 텍스트를 클립보드에 복사하는 버튼 제공
- 복사 후 메신저/문자앱에서 붙여넣기하여 전송

## 4. 사용자 플로우

```
학생 생성 → 학생 선택 → 키워드 입력 → AI 피드백 생성 → 텍스트 복사 → 메신저로 전송
```

1. 선생님이 학생을 생성한다 (이름 + 메모).
2. 피드백을 생성할 학생을 선택한다.
3. 오늘 수업에 대한 키워드를 입력한다.
4. AI가 학부모용 피드백 문자를 생성한다.
5. 생성된 텍스트를 복사하여 학부모에게 전송한다.

## 5. 데이터 모델

### Student (학생)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| name | String | 학생 이름 |
| memo | String | 학생 특성/메모 |
| createdAt | DateTime | 생성일시 |
| updatedAt | DateTime | 수정일시 |

### Feedback (피드백)

Student와는 ID 참조만 유지하는 독립 어그리거트 루트.

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| studentId | Long | Student ID (단순 컬럼, @ManyToOne 없음) |
| aiContent | String | AI 생성 피드백 텍스트 (nullable) |
| createdAt | DateTime | 생성일시 |
| updatedAt | DateTime | 수정일시 |

### FeedbackKeyword (키워드)

Feedback 어그리거트 내부 엔티티. `Feedback.addKeyword()` / `removeKeyword()`를 통해서만 접근.

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| feedbackId | Long | FK → Feedback |
| keyword | String | 키워드 텍스트 (최대 100자) |
| createdAt | DateTime | 생성일시 |

## 6. API 설계

### 학생 관리

| Method | URL | 설명 |
|--------|-----|------|
| POST | /students | 학생 생성 |
| GET | /students | 학생 목록 조회 |
| GET | /students/{id} | 학생 단건 조회 |
| PUT | /students/{id} | 학생 수정 |
| DELETE | /students/{id} | 학생 삭제 |

**POST /students**
```json
// Request
{ "name": "홍길동", "memo": "집중력 좋음" }

// Response 201
{ "id": 1, "name": "홍길동", "memo": "집중력 좋음", "createdAt": "...", "updatedAt": "..." }
```

### 피드백 관리

| Method | URL | 설명 |
|--------|-----|------|
| POST | /feedbacks | 피드백 생성 또는 기존 반환 (get-or-create) |
| GET | /feedbacks?studentId={id} | 학생의 피드백 목록 조회 |
| GET | /feedbacks/{id} | 피드백 상세 조회 (키워드 포함) |
| DELETE | /feedbacks/{id} | 피드백 삭제 (키워드 cascade) |

**POST /feedbacks**
```json
// Request
{ "studentId": 1 }

// Response 201
{ "id": 1, "studentId": 1, "aiContent": null, "keywords": [], "createdAt": "...", "updatedAt": "..." }
```

**GET /feedbacks/{id}**
```json
// Response 200
{
  "id": 1, "studentId": 1, "aiContent": "안녕하세요...",
  "keywords": [{ "id": 1, "keyword": "집중력 좋았음", "createdAt": "..." }],
  "createdAt": "...", "updatedAt": "..."
}
```

### 키워드 관리

| Method | URL | 설명 |
|--------|-----|------|
| POST | /feedbacks/{id}/keywords | 키워드 추가 |
| DELETE | /feedbacks/{id}/keywords/{keywordId} | 키워드 삭제 |

**POST /feedbacks/{id}/keywords**
```json
// Request
{ "keyword": "집중력 좋았음" }

// Response 201 — 키워드 포함 피드백 상세 반환
```

### AI 생성

| Method | URL | 설명 |
|--------|-----|------|
| POST | /feedbacks/{feedbackId}/generate | AI 피드백 문자 생성 |

## 7. 기술 스택

| 구분 | 기술 |
|------|------|
| 백엔드 | Spring Boot 4.0, Java 25, JPA, H2 (인메모리) |
| AI | Spring AI + OpenAI gpt-4o-mini |
| 프론트엔드 | Next.js 16 (App Router), React 19, Tailwind CSS 4, TypeScript |
| 통신 | REST API |

## 8. MVP 범위

### 포함

- 학생 CRUD (이름 + 메모)
- 키워드 기반 AI 피드백 생성
- 피드백 클립보드 복사

### 미포함

- 수업(반) 관리
- 피드백 히스토리 조회
- 사용자 인증/로그인
- 실제 SMS/메시지 발송
- 피드백 템플릿 관리
- 다중 선생님 지원

## 9. 향후 확장

- **수업(반) 관리**: Classroom 엔티티 도입, 학생-수업 다대다 연결
- **수업 회차(Lesson)**: 회차별 메모 관리, 피드백을 회차와 연결
  - Lesson 도입 시 `Feedback.lessonId` 컬럼 추가, unique 제약을 `(studentId, lessonId)`로 변경
  - `POST /feedbacks`는 동일한 get-or-create 패턴 유지 (`studentId` + `lessonId` 기준)
- **피드백 히스토리**: 학생별 이전 피드백 기록 조회
- **인증**: 선생님별 로그인 및 데이터 격리
- **SMS 발송**: 문자/카카오톡 API 연동을 통한 직접 발송
