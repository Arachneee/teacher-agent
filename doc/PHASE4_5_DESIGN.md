# Phase 4 & 5 설계 문서

## 1. 사용자 플로우 (User Flow)

```
[로그인]
   │
   ▼
[수업 목록 페이지 - /]
  - 선생님의 Lesson 카드 전체 목록 표시
  - Lesson 추가 버튼
  - 각 카드에서 수정 / 삭제 가능
   │
   │ Lesson 카드 클릭
   ▼
[수업 상세 페이지 - /lessons/:lessonId]
  - 해당 Lesson의 수강생(Attendee) 카드 목록 표시
  - 수강생 추가 버튼 → 수강생 추가 모달
  - 각 수강생 카드에서 수정 / 삭제 가능
   │
   │ 수강생 추가 버튼 클릭
   ▼
[수강생 추가 모달]
  - 기존 학생 목록에서 선택 (검색 가능)
  - "새 학생 추가" 인라인 폼 (이름 + 메모 입력 후 바로 Lesson에 추가)
```

### 화면별 상세 플로우

#### 수업 목록 페이지 (`/`)
- 선생님 로그인 후 진입하는 메인 페이지
- Lesson 카드: 제목, 시작/종료 시간 표시
- 카드 추가: 모달 또는 인라인 폼으로 새 Lesson 생성
- 카드 수정: 제목/시간 편집
- 카드 삭제: 확인 후 삭제 (소속 Attendee/Feedback 연쇄 삭제)

#### 수업 상세 페이지 (`/lessons/:lessonId`)
- 수강생 카드 그리드 (현재 StudentCard와 동일한 레이아웃)
- 각 수강생 카드에 표시되는 내용:
  - 학생 이름 / 메모 (편집 가능)
  - 피드백 키워드 목록 (추가 / 삭제)
  - AI 문자 (생성 / 수정 / 좋아요)
- 수강생 "삭제" = 이 Lesson에서 제거 (학생 자체는 삭제되지 않음)
- 수강생 "수정" = 학생 정보(이름/메모) 수정

---

## 2. API 설계

### 변경 없는 엔드포인트

| 엔드포인트 | 메서드 | 설명 |
|-----------|--------|------|
| `POST /auth/login` | POST | 로그인 |
| `POST /auth/logout` | POST | 로그아웃 |
| `GET /auth/me` | GET | 현재 사용자 |
| `GET /students` | GET | 전체 학생 목록 (수강생 추가 모달용) |
| `POST /students` | POST | 학생 생성 |
| `PUT /students/:id` | PUT | 학생 정보 수정 |
| `DELETE /students/:id` | DELETE | 학생 삭제 |

### Lesson CRUD (기존 Phase 2 구현 완료)

| 엔드포인트 | 메서드 | 설명 |
|-----------|--------|------|
| `GET /lessons` | GET | 내 수업 목록 |
| `POST /lessons` | POST | 수업 생성 |
| `GET /lessons/:id` | GET | 수업 단건 조회 |
| `PUT /lessons/:id` | PUT | 수업 수정 |
| `DELETE /lessons/:id` | DELETE | 수업 삭제 |

### Attendee CRUD (기존 Phase 3 구현 완료, 응답 보강 필요)

| 엔드포인트 | 메서드 | 설명 |
|-----------|--------|------|
| `GET /lessons/:lessonId/attendees` | GET | 수업의 수강생 목록 |
| `POST /lessons/:lessonId/attendees` | POST | 수강생 추가 |
| `DELETE /lessons/:lessonId/attendees/:attendeeId` | DELETE | 수강생 제거 |

#### AttendeeResponse 보강 (Phase 4에서 수정)

현재 응답:
```json
{
  "id": 1,
  "lessonId": 10,
  "studentId": 5,
  "createdAt": "..."
}
```

Phase 4 이후 필요한 응답 (수강생 카드 렌더링에 필요한 정보 포함):
```json
{
  "id": 1,
  "lessonId": 10,
  "student": {
    "id": 5,
    "name": "홍길동",
    "memo": "수학 잘함"
  },
  "feedback": {
    "id": 20,
    "attendeeId": 1,
    "aiContent": "...",
    "keywords": [
      { "id": 1, "keyword": "집중력", "createdAt": "..." }
    ],
    "liked": false,
    "createdAt": "...",
    "updatedAt": "..."
  },
  "createdAt": "..."
}
```

> **설계 결정**: Attendee 목록 조회 시 Student 정보와 Feedback을 함께 반환한다.
> 수강생 카드를 렌더링하는 데 필요한 모든 데이터를 한 번의 API 호출로 가져와 N+1 호출을 방지한다.

### Feedback (Phase 4에서 변경)

**핵심 변경**: `studentId` → `attendeeId`

| 엔드포인트 | 메서드 | 설명 |
|-----------|--------|------|
| `GET /feedbacks?attendeeId={id}` | GET | 수강생의 피드백 조회 |
| `POST /feedbacks` | POST | 피드백 생성 |
| `GET /feedbacks/:id` | GET | 피드백 단건 조회 |
| `PATCH /feedbacks/:id` | PATCH | AI 문자 수정 |
| `DELETE /feedbacks/:id` | DELETE | 피드백 삭제 |
| `POST /feedbacks/:id/generate` | POST | AI 문자 생성 |
| `POST /feedbacks/:id/keywords` | POST | 키워드 추가 |
| `DELETE /feedbacks/:id/keywords/:keywordId` | DELETE | 키워드 삭제 |
| `POST /feedbacks/:id/like` | POST | 좋아요 |

#### FeedbackCreateRequest 변경

```json
// Before (Phase 3 이전)
{ "studentId": 5 }

// After (Phase 4)
{ "attendeeId": 1 }
```

#### FeedbackResponse 변경

```json
// Before
{ "id": 20, "studentId": 5, "aiContent": "...", "keywords": [...], "liked": false, ... }

// After
{ "id": 20, "attendeeId": 1, "aiContent": "...", "keywords": [...], "liked": false, ... }
```

---

## 3. Phase 4 백엔드 작업 목록

### 3-1. Feedback 엔티티 마이그레이션

- [ ] `Feedback.java`: `studentId` → `attendeeId` 필드 교체
  - `@Column(nullable = false) private Long attendeeId`
  - `@Table` 인덱스: `idx_feedback_student_id` → `idx_feedback_attendee_id`
- [ ] `Feedback.create(Long attendeeId)` 팩토리 메서드 변경
- [ ] `Parameter.java`: `STUDENT_ID` 사용처 → `ATTENDEE_ID` 추가

### 3-2. Repository / Service 변경

- [ ] `FeedbackRepository.java`: `findByStudentId` → `findByAttendeeId`
- [ ] `FeedbackService.java`: 생성 / 조회 로직 attendeeId 기반으로 변경
- [ ] `FeedbackAiService.java`: `attendeeId → attendee → student` 이름 조회 경로 변경
  - `attendeeId`로 Attendee 조회 → `studentId`로 Student 조회 → 이름 가져오기

### 3-3. DTO 변경

- [ ] `FeedbackCreateRequest.java`: `studentId` → `attendeeId`
- [ ] `FeedbackResponse.java`: `studentId` → `attendeeId`

### 3-4. AttendeeResponse 보강

- [ ] `AttendeeResponse.java`: student 정보 + feedback 포함한 응답으로 확장
  - `AttendeeService.getAll()`에서 Student / Feedback을 함께 조회하여 응답 구성

### 3-5. FeedbackController 변경

- [ ] `getAll()`: `@RequestParam Long studentId` → `@RequestParam Long attendeeId`
- [ ] `create()`: request에서 attendeeId 사용

### 3-6. 테스트 코드 업데이트

- [ ] Feedback 관련 단위 / 통합 테스트 수정
- [ ] AttendeeController 통합 테스트에 보강된 응답 검증 추가

---

## 4. Phase 5 프론트엔드 작업 목록

### 4-1. 라우팅 구조 변경

```
/ (홈)              → 수업 목록 페이지  (현재: 학생 목록)
/lessons/:lessonId  → 수업 상세 페이지  (신규)
```

### 4-2. `api.ts` 타입 / 함수 추가·변경

```typescript
// 추가할 타입
interface Lesson { id: number; title: string; startTime: string; endTime: string; }
interface AttendeeStudent { id: number; name: string; memo: string; }
interface AttendeeFeedback { id: number; attendeeId: number; aiContent: string | null; keywords: FeedbackKeyword[]; liked: boolean; ... }
interface Attendee { id: number; lessonId: number; student: AttendeeStudent; feedback: AttendeeFeedback; createdAt: string; }

// 변경할 타입
interface Feedback { attendeeId: number; /* studentId 제거 */ ... }

// 추가할 함수
getLessons(), createLesson(), updateLesson(), deleteLesson()
getAttendees(lessonId), addAttendee(lessonId, studentId), removeAttendee(lessonId, attendeeId)

// 변경할 함수
getFeedbacks(attendeeId) // studentId → attendeeId
createFeedback(attendeeId) // studentId → attendeeId
```

### 4-3. 신규 컴포넌트

| 컴포넌트 | 설명 |
|---------|------|
| `LessonCard.tsx` | 수업 카드 (제목, 시간, 수정/삭제 버튼) |
| `AddLessonModal.tsx` | 수업 추가/수정 모달 |
| `AttendeeCard.tsx` | 수강생 카드 (현재 StudentCard 기반, attendeeId 기준 feedback 사용) |
| `AddAttendeeModal.tsx` | 수강생 추가 모달 (학생 목록 선택 + 인라인 학생 생성) |

### 4-4. 신규 페이지

| 파일 | 설명 |
|------|------|
| `app/page.tsx` | 수업 목록 (현재 학생 목록 → Lesson 목록으로 교체) |
| `app/lessons/[lessonId]/page.tsx` | 수업 상세 (Attendee 카드 그리드) |

### 4-5. 훅 변경

- `useFeedback.ts`: `studentId` → `attendeeId` 파라미터로 변경
  - Feedback 단건 조회 로직이 Attendee 응답에 내장되므로 초기 fetch 로직 단순화 가능

### 4-6. DataInitializer 샘플 데이터 추가

- 샘플 Lesson 2~3개 생성
- 각 Lesson에 샘플 Attendee 연결
- Feedback도 attendeeId 기반으로 재생성

---

## 5. 마이그레이션 주의사항

### 파괴적 변경 (Breaking Changes)

| 구분 | 변경 전 | 변경 후 |
|------|--------|--------|
| Feedback 생성 | `POST /feedbacks` `{studentId}` | `POST /feedbacks` `{attendeeId}` |
| Feedback 조회 | `GET /feedbacks?studentId=1` | `GET /feedbacks?attendeeId=1` |
| FeedbackResponse | `studentId` 필드 | `attendeeId` 필드 |
| AttendeeResponse | id/lessonId/studentId/createdAt | id/lessonId/student{}/feedback{}/createdAt |

### 기존 Feedback 데이터 처리

H2 인메모리 DB를 사용하므로 데이터 마이그레이션 스크립트 불필요.
서버 재시작 시 `DataInitializer`가 신규 구조로 샘플 데이터를 재생성.

### 개발 순서

1. 백엔드 Phase 4 완료 후 프론트엔드 Phase 5 진행 (API 계약이 먼저 확정되어야 함)
2. 백엔드 변경 중 기존 프론트엔드는 API 불일치로 동작하지 않음 → 브랜치 전략 필요
