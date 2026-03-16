# Backend 개선 계획 (V1 → V2)

## 현재 상태 vs PRD_V2 목표

| 구분 | 현재 (V1) | PRD_V2 목표 |
|------|-----------|-------------|
| Feedback 연결 | Student에 직접 연결 | **Attendee**를 통해 연결 |
| Lesson | 없음 | 수업 생성/조회/수정 |
| Attendee | 없음 | 수업별 학생 매핑 + 출결 관리 |
| Keyword | Feedback에 연결 | 동일 (변경 없음) |
| Teacher | username/password만 | + `name`, `subject` 필드 추가 |

## Phase 1: Teacher 엔티티 확장

**범위**: Teacher에 `name`, `subject` 필드 추가

**이유**: 다른 엔티티들의 FK 기반이므로 가장 먼저 정비

**작업**:

- Teacher 엔티티에 `name`, `subject` 필드 추가
- Teacher DTO (Response/UpdateRequest) 추가
- 기존 Auth 흐름과의 호환성 유지

**리스크**: 낮음 — 기존 기능에 영향 최소

## Phase 2: Lesson 엔티티 & CRUD

**범위**: Lesson 도메인 신규 생성

**작업**:

- `Lesson` 엔티티 (`id`, `teacherId`, `groupId`, `title`, `startTime`, `endTime`)
- `LessonRepository`
- `LessonService` (create, getAll, getOne, update, delete)
- `LessonController` (`/lessons`)
- DTO: `LessonCreateRequest`, `LessonUpdateRequest`, `LessonResponse`

**리스크**: 낮음 — 완전히 새로운 도메인, 기존 코드 수정 없음

## Phase 3: Attendee 엔티티 & 출결 관리

**범위**: 수업-학생 매핑 + 출결 상태 관리

**작업**:

- `Attendee` 엔티티 (`id`, `lessonId`, `studentId`, `status`)
- `AttendanceStatus` enum (`PRESENT`, `LATE`, `ABSENT`)
- `AttendeeRepository`
- `AttendeeService` (수업에 학생 추가/제거, 출결 상태 변경)
- `AttendeeController` (`/lessons/{lessonId}/attendees`)
- DTO: `AttendeeCreateRequest`, `AttendeeResponse`, `AttendeeStatusUpdateRequest`

**리스크**: 낮음 — 신규 도메인, Lesson/Student FK 참조만 추가

## Phase 4: Feedback 연결 구조 마이그레이션

**범위**: Feedback의 연결 대상을 `studentId` → `attendeeId`로 변경

**이유**: PRD_V2의 핵심 — 피드백이 "특정 수업의 특정 학생"에 대한 것이어야 함

**작업**:

- `Feedback` 엔티티: `studentId` → `attendeeId` (FK, Unique)
- `FeedbackRepository`: 쿼리 메서드 변경 (`findByAttendeeId` 등)
- `FeedbackService`: 생성 로직 변경 (attendeeId 기반)
- `FeedbackController`: 엔드포인트 파라미터 변경
- `FeedbackCreateRequest`: `studentId` → `attendeeId`
- `FeedbackResponse`: attendee 정보 포함
- AI 피드백 생성: attendee → student 이름 조회 경로 변경

**리스크**: **높음** — 기존 프론트엔드 API 계약이 깨짐. 프론트엔드 동시 수정 필요

## Phase 5: 프론트엔드 연동 & 통합 테스트

**범위**: Phase 4에서 변경된 API 계약에 맞춰 프론트엔드 수정 + 전체 흐름 검증

**작업**:

- 프론트엔드 API 호출부 수정
- Lesson/Attendee 관련 UI 추가
- 통합 테스트 작성 (각 엔티티 CRUD + 관계 검증)
- DataInitializer에 샘플 Lesson/Attendee 데이터 추가
