# 기능 명세서: 프론트엔드 이벤트 추적 (1-1 축소 버전)

## 1. 개요
- 기능 설명: 사용자의 핵심 행동 3가지(복사, 편집, 재생성)를 추적하여 Phase 1 KPI를 측정한다.
- 사용자 스토리: PM이 AI 생성 → 복사 전환율, 재생성율, AI 품질 만족도를 파악할 수 있다.
- 선행 조건: 없음 (기존 코드에 추적 인프라 없음)

## 2. 추적 이벤트 (3개)

| 이벤트 | 트리거 시점 | KPI 연결 |
|--------|------------|----------|
| `feedback_copy` | 복사 버튼 클릭 (AiFeedbackSection handleCopy) | AI 생성 → 복사 전환율 |
| `feedback_edit` | AI 콘텐츠 편집 모드 진입 (AiFeedbackSection setEditing(true)) | AI 품질 부족 신호 |
| `feedback_regenerate` | 다시 생성 버튼 클릭 (feedback.aiContent가 이미 존재할 때 onGenerate) | 재생성율 |

### 제외 이벤트 및 이유
- `feedback_generate`: AiGenerationLog에서 이미 추적 중
- `feedback_like`: FeedbackLike에서 이미 추적 중
- `keyword_add`: DB에 키워드 저장됨, Phase 1 KPI와 무관
- `lesson_view`, `page_view`: 1명 사용자, 7개 페이지 → 가치 낮음

## 3. API 스펙

### POST /events
- Method: POST
- Path: /events
- Request Body:
  ```json
  {
    "eventType": "feedback_copy",
    "metadata": "{\"feedbackId\": 42}"
  }
  ```
- Response (200):
  ```json
  { "id": 1 }
  ```
- Error Cases:
  - 400: eventType이 null 또는 빈 문자열
  - 401: 미인증 사용자

## 4. 백엔드 구현 범위

### Entity: UserEvent
```
UserEvent extends BaseEntity
├── id: Long (PK, IDENTITY)
├── userId: Long (NOT NULL)
├── eventType: String (NOT NULL)
├── metadata: String (TEXT, nullable) — JSON 문자열
├── INDEX: idx_user_event_user_id_created_at (userId, createdAt)
├── INDEX: idx_user_event_event_type_created_at (eventType, createdAt)
```

### Repository: UserEventRepository
- `JpaRepository<UserEvent, Long>` — 기본 save만 사용

### Service: UserEventCommandService
- `save(Long userId, String eventType, String metadata): UserEvent`

### Controller: UserEventController
- `POST /events` → `UserEventCommandService.save()`
- 현재 인증된 사용자의 userId를 자동 주입

### DTO
- `UserEventRequest`: eventType (String, NotBlank), metadata (String, nullable)
- `UserEventResponse`: id (Long)

### 예외 처리
- eventType 빈 문자열 → 400 (기존 ValidationUtil 활용)

### 테스트
- `UserEventTest`: 도메인 단위 테스트 (create 성공, eventType blank 실패)
- `UserEventCommandServiceTest`: 통합 테스트 (save 성공)

## 5. 프론트엔드 구현 범위

### 유틸리티: lib/tracking.ts
```typescript
export function trackEvent(eventType: string, metadata?: Record<string, any>) {
  fetch('/api/events', {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      eventType,
      metadata: metadata ? JSON.stringify(metadata) : null,
    }),
  }).catch(() => {}); // fire-and-forget
}
```

### 통합 지점: AiFeedbackSection.tsx (3곳)
1. `handleCopy` 함수 내: `trackEvent('feedback_copy', { feedbackId: feedback.id })`
2. `setEditing(true)` 시점: `trackEvent('feedback_edit', { feedbackId: feedback.id })`
3. `onGenerate` 호출 전 분기: `feedback.aiContent`가 존재하면 `trackEvent('feedback_regenerate', { feedbackId: feedback.id })`

### 타입: 없음 (trackEvent는 단순 유틸리티 함수)

### 반응형: 해당 없음 (UI 변경 없음)

## 6. 선택 사항 결정 내역
- 저장 방식: 동기 저장 — 1명 사용자에게 비동기는 오버엔지니어링
- 재생성 구분: 프론트에서 구분 — eventType으로 명확히 분리
- metadata 범위: 최소 (feedbackId만) — 나머지는 JOIN으로 추적 가능
