# 기능 명세서: 재생성 방향 입력

## 1. 개요

- **기능 설명**: AI 학부모 문자 재생성 시, 선생님이 원하는 수정 방향(예: "너무 딱딱해", "칭찬 더 강하게", "짧게")을 자유 텍스트로 입력하면 AI가 해당 방향을 반영하여 문자를 생성한다.
- **사용자 스토리**: 선생님이 생성된 문자가 마음에 들지 않을 때, 재생성 전에 방향을 구체적으로 지정해 재생성 횟수를 줄인다.
- **선행 조건**: 피드백에 `aiContent`가 이미 존재할 때(재생성 모드)만 방향 입력란이 노출된다. 최초 생성 시에는 노출하지 않는다.

---

## 2. API 스펙

### GET /feedbacks/{id}/generate/stream (기존 엔드포인트 확장)

- **Method**: GET
- **Path**: `/feedbacks/{id}/generate/stream`
- **Query Parameters**:
  - `instruction` (선택, string): 재생성 방향 지시. 비어 있으면 기존 동작과 동일.
- **Response**: `text/plain;charset=UTF-8` 스트리밍 (기존과 동일)
- **변경 없는 항목**: 인증, 에러 응답, 스트리밍 방식 모두 기존과 동일

**요청 예시:**
```
GET /feedbacks/42/generate/stream?instruction=너무 딱딱해, 더 따뜻하게
```

---

## 3. 백엔드 구현 범위

### Controller

- `FeedbackController.streamAiContent()` 메서드에 `@RequestParam(required = false) String instruction` 파라미터 추가
- `feedbackCommandService.streamAiContent(userId, id, instruction)` 호출로 변경

### Service

- `FeedbackCommandService.streamAiContent(UserId, Long, String instruction)` 시그니처 변경
- `FeedbackAiService.streamFeedbackContent(...)` 및 `generateFeedbackContent(...)` 에 `instruction` 파라미터 추가
- `FeedbackPromptBuilder.build(...)` 에 `instruction` 파라미터 추가

### 프롬프트 (`feedback_message.md`)

`# 수정 방향` 섹션을 `# 핵심 규칙` 바로 앞에 추가:

```markdown
# 수정 방향

<instruction>{instruction}</instruction>이 비어 있지 않은 경우, 이 방향을 최우선으로 반영하여 문자를 작성한다.
다른 모든 규칙(어조, 구성, 길이 등)은 이 방향과 충돌하지 않는 범위에서 적용한다.
```

프롬프트 `# 입력` 섹션에 항목 추가:
```markdown
- 수정 방향: <instruction>{instruction}</instruction>
```

`{instruction}` 변수:
- instruction이 null이거나 blank면 → `"없음"` 치환

### 테스트

- `FeedbackPromptBuilder` 단위 테스트: instruction이 있을 때 / 없을 때(null, blank) 프롬프트 포함 여부 검증
- `FeedbackCommandService` 통합 테스트: instruction 파라미터가 서비스 레이어까지 정상 전달되는지 검증 (AI 실제 호출 없이 mock 또는 프롬프트 내용만 검증)

---

## 4. 프론트엔드 구현 범위

### 컴포넌트: `AiFeedbackSection.tsx`

- `feedback?.aiContent`가 존재할 때(재생성 모드)만 방향 입력란 노출
- 입력란: 한 줄 `<input type="text">`, placeholder: `"수정 방향을 입력하세요 (선택)"`
- 입력 상태는 컴포넌트 내부 state로 관리 (`useState`)
- AI 생성 완료 후(스트리밍 종료) 입력란 내용을 초기화

### 훅: `useFeedback.ts`

- `handleGenerate(instruction?: string)` 시그니처 변경
- `streamAiContent(feedbackId, onChunk, instruction)` 호출로 변경

### API 함수: `lib/api.ts`

- `streamAiContent(feedbackId, onChunk, instruction?: string)` 시그니처 변경
- instruction이 있을 때: `?instruction=encodeURIComponent(instruction)` 쿼리 파라미터 추가
- instruction이 비어 있으면 쿼리 파라미터 생략

### 반응형

- 입력란: 모바일/데스크톱 모두 버튼 위에 전체 너비로 표시
- 기존 버튼 레이아웃 변경 없음

---

## 5. 선택 사항 결정 내역

- **UI 배치 방식**: 항상 보이는 인풋, 단 `aiContent` 존재 시(재생성 모드)만 노출 — 최초 생성 시 불필요한 UI 노출 방지
- **입력 방식**: 자유 입력만 — 구현 단순, 선생님의 표현 자유도 최대
- **방향 없는 재생성 허용**: 선택 입력 (비워도 재생성 가능) — 기존 동작 하위 호환 유지
