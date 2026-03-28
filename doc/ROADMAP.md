# Teacher Agent — 전략적 로드맵

> 작성일: 2026-03-28
> 현재 상태: MVP 완료, 프로덕션 배포 완료 (EC2 + Vercel)
> 현재 사용자: 1명 (신입 학원 초등국어 선생님)

---

## 현재 상태 요약

| 항목 | 상태 | 비고 |
|------|------|------|
| 핵심 기능 | ✅ 완료 | 수업/학생/피드백 CRUD, AI 문자 생성 |
| 백엔드 테스트 | ✅ 양호 | 23개 파일 (도메인 + 서비스) |
| 프론트엔드 테스트 | ❌ 없음 | 0개 |
| API 문서 | ❌ 없음 | Swagger/OpenAPI 미도입 |
| 모니터링 | ❌ 없음 | Actuator/메트릭스 미설정 |
| DB 마이그레이션 | ❌ 없음 | hibernate ddl-auto: update |
| 인프라 | ✅ 완료 | t3.micro + RDS, 프리티어 최적화 |
| 사용 데이터 추적 | ⚠️ 부분 | AiGenerationLog 엔티티 존재, 집계/리포팅 없음 |

### 이미 수집 중인 데이터
- `AiGenerationLog` — AI 생성 호출마다 feedbackId, 프롬프트, 응답, 소요시간(ms), 토큰 수, 스트리밍 여부 기록
- `FeedbackLike` — 좋아요 시점의 AI 콘텐츠 + 키워드 스냅샷 저장
- `LoggingInterceptor` — HTTP 요청/응답 로깅 (SLF4J, DB 미저장)
- `OpenAiLoggingAdvisor` — OpenAI API 호출 로깅 (SLF4J, DB 미저장)

### 수집되지 않는 데이터 (갭)
- 프론트엔드 이벤트 (복사 클릭, 재생성 클릭, 페이지 체류시간 등) — 0개
- 집계 쿼리 (일별/주별 생성 수, 좋아요율, 재생성율 등) — Repository에 메서드 0개
- 리포팅/대시보드 엔드포인트 — 0개
- 사용자 행동 흐름 (키워드 입력 → AI 생성 → 복사 전환율) — 추적 불가

---

## Phase 1: 사용 데이터 추적 + 프로덕션 안정화 (1~2개월)

### 목표
**1명의 실사용자가 서비스를 어떻게 쓰는지 정확히 파악한다.** 이 데이터가 모든 후속 의사결정의 기반이 된다.

### 왜 데이터 추적이 최우선인가
현재 사용자는 신입 학원 초등국어 선생님 1명이다. 이 선생님이:
- AI 생성 문자를 실제로 학부모에게 보내는지
- 생성된 문자를 그대로 쓰는지, 수정하는지
- 어떤 키워드를 주로 쓰는지
- 어떤 기능을 안 쓰는지

이걸 모르면 Phase 2 이후의 모든 기능 개발이 추측에 기반하게 된다.

### 핵심 지표 (KPI)
- AI 생성 → 복사 전환율 (목표: 60% 이상이면 UX 충분)
- 좋아요율 (목표: 70% 이상이면 AI 품질 충분)
- 재생성율 (30% 이하면 첫 생성 품질 양호)
- 주간 AI 생성 횟수 (사용 빈도 = 서비스 가치)
- 주간 활성 사용일 수 (7일 중 몇 일 접속하는지)

---

### 1-1. 프론트엔드 이벤트 추적 (최우선)

사용자의 핵심 행동을 추적한다. 외부 분석 도구 없이, 백엔드 API로 이벤트를 전송하고 MySQL에 저장한다.

#### 추적할 이벤트 목록

| 이벤트 | 트리거 시점 | 추적 이유 |
|--------|------------|-----------|
| `feedback_copy` | 복사 버튼 클릭 | AI 생성 → 실제 사용 전환율 |
| `feedback_generate` | AI 생성 버튼 클릭 | 생성 빈도, 재생성 여부 |
| `feedback_regenerate` | "다시 생성" 클릭 | 첫 생성 품질 불만족 신호 |
| `feedback_like` | 좋아요 클릭 | AI 품질 만족도 |
| `feedback_edit` | AI 문자 수동 편집 | 생성 품질 부족 신호 |
| `keyword_add` | 키워드 추가 | 키워드 사용 패턴 |
| `lesson_view` | 수업 상세 페이지 진입 | 핵심 플로우 사용 빈도 |
| `page_view` | 페이지 전환 | 전체 사용 패턴 |

#### 구현 방식

**백엔드: `UserEvent` 엔티티 + API**
```
UserEvent
├── id: Long (PK)
├── userId: UserId
├── eventType: String (예: "feedback_copy")
├── metadata: String (JSON, nullable) — 추가 컨텍스트
├── createdAt: LocalDateTime
├── INDEX: idx_user_event_user_id_created_at (userId, createdAt)
├── INDEX: idx_user_event_event_type_created_at (eventType, createdAt)
```

- `POST /events` — 이벤트 수신 (eventType + metadata)
- 비동기 저장 (`@Async`) — 사용자 경험에 영향 없도록

**프론트엔드: `useTracking` 훅**
```typescript
// lib/tracking.ts
export function trackEvent(eventType: string, metadata?: Record<string, any>) {
  // fire-and-forget, 실패해도 무시
  fetch('/api/events', {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ eventType, metadata }),
  }).catch(() => {}); // 추적 실패가 UX를 방해하면 안 됨
}
```

**통합 지점:**
- `AiFeedbackSection.tsx` — `handleCopy`에 `trackEvent('feedback_copy')` 추가
- `AiFeedbackSection.tsx` — 생성 버튼에 `trackEvent('feedback_generate')` / `trackEvent('feedback_regenerate')` 분기
- `AiFeedbackSection.tsx` — 좋아요 버튼에 `trackEvent('feedback_like')` 추가
- `AiFeedbackSection.tsx` — 편집 시작 시 `trackEvent('feedback_edit')` 추가
- `KeywordsSection.tsx` — 키워드 추가 시 `trackEvent('keyword_add')` 추가
- `(app)/lessons/[lessonId]/page.tsx` — 마운트 시 `trackEvent('lesson_view')` 추가

### 1-2. 기존 데이터 집계 쿼리 추가

`AiGenerationLog`와 `FeedbackLike`에 이미 쌓이고 있는 데이터를 활용한다.

**AiGenerationLogRepository에 추가할 쿼리:**
- `countByCreatedAtBetween(start, end)` — 기간별 AI 생성 횟수
- `findByFeedbackIdOrderByCreatedAtDesc(feedbackId)` — 피드백별 생성 이력 (재생성 횟수 파악)
- `averageDurationMs(start, end)` — 평균 생성 소요시간

**FeedbackLikeRepository에 추가할 쿼리:**
- `countByCreatedAtBetween(start, end)` — 기간별 좋아요 수

**FeedbackRepository에 추가할 쿼리:**
- `countByAiContentIsNotNull()` — AI 생성된 피드백 수 (좋아요율 분모)

### 1-3. 사용 통계 리포팅 API

간단한 통계 엔드포인트. 복잡한 대시보드 UI는 불필요 — 1명 사용자이므로 API 응답을 직접 확인하면 충분.

**`GET /usage/summary`** — 전체 요약
```json
{
  "totalAiGenerations": 142,
  "totalLikes": 98,
  "likeRate": 0.69,
  "totalCopyClicks": 87,
  "copyRate": 0.61,
  "totalRegenerations": 31,
  "regenerationRate": 0.22,
  "avgGenerationDurationMs": 2340,
  "activeDaysLast7": 5,
  "activeDaysLast30": 18
}
```

**`GET /usage/daily?days=30`** — 일별 추이
```json
[
  { "date": "2026-03-28", "generations": 8, "copies": 5, "likes": 6, "regenerations": 1 },
  ...
]
```

**`GET /usage/keywords/top?limit=20`** — 자주 사용하는 키워드 Top N
```json
[
  { "keyword": "성실함", "count": 23 },
  { "keyword": "집중력 향상", "count": 18 },
  ...
]
```

### 1-4. 사용자 인터뷰 (현재 1명 + 추가 4명)

**현재 사용자 (신입 학원 초등국어 선생님) 심층 인터뷰:**
- 수업 전후 워크플로우에서 이 서비스를 언제 쓰는지
- AI 생성 문자를 카카오톡에 그대로 붙여넣는지, 수정하는지
- 가장 유용한 기능 / 가장 불편한 점
- "이런 기능이 있으면 좋겠다" 리스트
- 동료 선생님에게 추천할 의향 (NPS)

**추가 인터뷰 대상 (4명):**
- 학원 경력 선생님 1명 (초등국어)
- 학원 선생님 1명 (수학 또는 영어 — 다른 과목)
- 학교 선생님 1명 (학원 vs 학교 니즈 차이 확인)
- 중등/고등 선생님 1명 (연령대별 니즈 차이 확인)

**검증 항목:**
- 카카오톡 복사-붙여넣기 UX가 충분한지 vs 직접 발송 필요한지
- AI 생성 품질 만족도 (과목별 차이)
- 학부모 소통 빈도와 패턴
- 유료 결제 의향 (가격 민감도)

---

### 1-5. 프로덕션 안정화 (데이터 추적과 병행)

#### 1-5a. 모니터링 + 헬스체크 도입
- Spring Boot Actuator 활성화 (`/actuator/health`, `/actuator/metrics`)
- JVM 힙 사용량, API 응답시간, 에러율 수집
- 서버 다운 시 알림 (이메일 또는 Slack webhook)
- **근거**: 현재 서버 상태를 전혀 알 수 없음. 데이터 추적 인프라의 안정성 보장에도 필요.

#### 1-5b. DB 마이그레이션 도구 도입 (Flyway)
- `hibernate.ddl-auto: update` → Flyway 전환
- 현재 스키마를 V1 baseline으로 캡처
- `UserEvent` 테이블 추가를 V2 마이그레이션으로 관리
- **근거**: Phase 2의 다중 계정 기능에서 스키마 변경이 복잡해지기 전에 반드시 선행.

#### 1-5c. AI API 장애 대응
- OpenAI 호출에 Circuit Breaker 패턴 적용 (Resilience4j)
- 타임아웃 설정 (10초)
- 장애 시 "일시적으로 AI 생성을 사용할 수 없습니다" 메시지 반환
- **근거**: 현재 OpenAI 다운 시 500 에러 → 사용자 경험 치명적.

#### 1-5d. API 문서 자동 생성 (Springdoc OpenAPI)
- Swagger UI + OpenAPI 3.0 스펙 자동 생성
- **근거**: 프론트엔드 개발 효율 + 외부 협업 시 필수.

---

## Phase 2: 사용자 확장 기반 (3~4개월)

### 목표
단일 사용자 → 다중 사용자 전환. 서비스의 확장 가능성을 입증한다.

### 핵심 지표 (KPI)
- 가입 선생님 20명 이상
- WAU (주간 활성 사용자) 10명 이상
- AI 피드백 좋아요율 70% 이상
- 프론트엔드 E2E 테스트 핵심 플로우 5개 이상

### 기능 개발

#### 2-1. 다중 선생님 계정 (회원가입)
- 자가 회원가입 플로우 (이메일 + 비밀번호)
- 기존 admin 계정 데이터 → 첫 번째 정식 계정으로 마이그레이션
- DB 스키마에 `Organization(학교)` 컬럼 예비 추가 (UI는 미구현)
- **⚠️ 의사결정 필요**: 가입 방식 — 자가 회원가입 / 관리자 초대 / 학교 코드 입력

#### 2-2. 피드백 히스토리 뷰
- 학생별 과거 피드백 목록 (날짜순)
- 좋아요한 피드백 필터
- **스코프 제한**: 차트/통계는 Phase 3로. 이 단계에서는 리스트 뷰만.

#### 2-3. 프롬프트 커스터마이징
- 선생님별 AI 프롬프트 톤/스타일 설정
- 과목별 프롬프트 템플릿 (국어/수학/영어 등)
- **근거**: 사용자 인터뷰에서 "내 말투로 바꾸고 싶다"는 니즈가 예상됨.

### 기술 부채 해소

#### 2-4. 프론트엔드 테스트 도입
- Playwright E2E: 핵심 플로우 5개 (로그인 → 수업 생성 → 학생 추가 → 키워드 입력 → AI 생성)
- Vitest 컴포넌트 테스트: 핵심 컴포넌트 커버리지 60%
- CI에서 테스트 자동 실행

#### 2-5. 컨트롤러 레벨 테스트
- MockMvc 기반 REST API 테스트
- 인증/인가 시나리오 포함

#### 2-6. 인증 체계 검토
- **⚠️ 의사결정 필요**: 세션 유지 vs JWT 전환
  - 세션 유지: Redis Session Store 추가 (수평 확장 대비)
  - JWT 전환: Vercel-EC2 간 쿠키 문제 해결, 모바일 앱 확장 용이
  - **추천**: 사용자 100명 미만이면 세션 유지, 이상이면 JWT 전환

---

## Phase 3: 제품 고도화 (5~6개월)

### 목표
사용자 리텐션을 높이고, 수익화 가능성을 검증한다.

### 핵심 지표 (KPI)
- 월간 활성 사용자 (MAU) 50명 이상
- 사용자 리텐션율 (4주) 40% 이상
- 학부모 공유 링크 생성율 30% 이상

### 기능 개발

#### 3-1. 학부모 공유 (읽기 전용 링크)
- 피드백 공유 링크 생성 (토큰 기반, 만료일 설정)
- 학부모가 링크로 피드백 확인 (로그인 불필요)
- **스코프 제한**: 풀 학부모 포털은 Phase 4+. 이 단계에서는 공유 링크만.
- **⚠️ 의사결정 필요**: 향후 알림 채널 — 카카오 알림톡 / SMS / 이메일

#### 3-2. 학생 성장 추이 시각화
- 학생별 피드백 키워드 빈도 분석 (워드클라우드 또는 태그 클라우드)
- 월별 피드백 수/좋아요 수 추이 차트
- **스코프 제한**: 단순 집계 차트만. 고급 분석은 Phase 4+.

#### 3-3. 피드백 통계 대시보드
- 선생님별 월간 피드백 생성 수
- AI 생성 vs 수동 편집 비율
- 가장 많이 사용된 키워드 Top 10

#### 3-4. 모바일 최적화
- 반응형 UI 개선 (현재 `useIsMobile` 훅 존재하나 완성도 미확인)
- PWA 지원 (오프라인 캐싱은 제외, 홈 화면 추가만)

### 기술 개선

#### 3-5. 캐싱 레이어 도입
- Redis 캐시: 학생 목록, 수업 목록 등 자주 조회되는 데이터
- AI 생성 결과 캐싱 (동일 키워드 조합 → 캐시 히트)

#### 3-6. 구조화된 로깅 + 분산 추적
- JSON 구조화 로깅 (ELK 또는 CloudWatch Logs)
- 요청 추적 ID (Spring Cloud Sleuth 또는 Micrometer Tracing)

#### 3-7. 데이터 보존 정책 수립
- 피드백 데이터 보존 기간 정의 (예: 2년)
- 오래된 데이터 아카이빙 전략
- **⚠️ 의사결정 필요**: 개인정보보호법 준수 검토 (학생 이름, 메모 등)

---

## Phase 4: 스케일링 + 수익화 (6개월+)

### 목표
지속 가능한 비즈니스 모델을 구축한다.

### 핵심 지표 (KPI)
- MAU 200명 이상
- 유료 전환율 5% 이상 (교육 SaaS 업계 평균)
- 월 매출 > 월 운영비 (손익분기점)

### 수익화 모델 (제안)

#### Freemium 구조
| 티어 | 가격 | AI 생성 | 학생 수 | 기능 |
|------|------|---------|---------|------|
| Free | 무료 | 일 10회 | 20명 | 기본 기능 |
| Pro | 월 9,900원 | 일 100회 | 무제한 | 프롬프트 커스터마이징, 통계 대시보드 |
| School | 월 49,900원 | 무제한 | 무제한 | 학교 단위 관리, 학부모 알림 발송 |

- **근거**: 교사 개인 → 학교 단위로 확장하는 B2B2C 모델
- **⚠️ 의사결정 필요**: PG사 연동 (토스페이먼츠 / 카카오페이), 사업자 등록

### 기능 개발

#### 4-1. 학부모 알림 발송
- 카카오 알림톡 또는 SMS 연동
- 선생님이 피드백 확정 → 학부모에게 자동 발송
- 발송 이력 관리

#### 4-2. 학교 단위 관리
- Organization(학교) 엔티티 활성화
- 학교 관리자 → 선생님 초대/관리
- 학교 단위 통계

#### 4-3. AI 모델 다양화
- gpt-4o-mini 외 Claude, Gemini 등 선택 가능
- Spring AI의 모델 추상화 활용
- **⚠️ 주의**: Spring AI GA 릴리즈 대기 후 안정 버전으로 전환

### 인프라 스케일링

#### 4-4. 수평 확장 준비
- EC2 → ECS Fargate 또는 EKS 전환
- ALB (Application Load Balancer) 도입
- Redis Session Store (세션 유지 시) 또는 JWT (전환 시)
- RDS Multi-AZ 활성화

#### 4-5. 비용 최적화
- Reserved Instance 또는 Savings Plan
- CloudFront CDN (프론트엔드 정적 자산)
- AI 호출 비용 모니터링 대시보드

---

## 미결정 사항 (의사결정 필요)

| # | 항목 | 영향 범위 | 결정 시점 |
|---|------|-----------|-----------|
| 1 | AI 호출 비용 상한 (월간) | Rate limiting, 수익화 티어 | Phase 1 시작 전 |
| 2 | 다중 계정 가입 방식 | 인증 체계, DB 스키마, API 설계 | Phase 2 시작 전 |
| 3 | 세션 vs JWT 전환 | 수평 확장, 모바일 앱, 쿠키 문제 | Phase 2 시작 전 |
| 4 | 학부모 알림 채널 | 외부 서비스 연동 복잡도/비용 | Phase 3 시작 전 |
| 5 | 데이터 보존 기간 | DB 용량, 개인정보보호법 | Phase 3 시작 전 |
| 6 | 수익화 PG사 선택 | 결제 연동, 사업자 등록 | Phase 4 시작 전 |
| 7 | Spring AI GA 전환 시점 | Breaking change 대응 비용 | GA 릴리즈 시 |

---

## 리스크 관리

### Critical Risks
1. **AI 비용 폭증** → Phase 1에서 Rate Limiting 필수 선행
2. **ddl-auto:update로 데이터 손실** → Phase 1에서 Flyway 전환 필수 선행
3. **서버 장애 감지 불가** → Phase 1에서 모니터링 필수 선행

### Scope Risks
4. **학부모 포털이 별도 프로덕트로 확장** → Phase 3에서 "공유 링크"로 스코프 제한
5. **통계 대시보드 범위 무한 확장** → Phase 3에서 단순 집계만, 고급 분석은 Phase 4+
6. **기술 부채가 기능 개발에 밀림** → 각 Phase에서 기능:기술부채 = 60:40 비율 고정

### Edge Cases (구현 시 주의)
7. **반복 수업 대량 생성 시 AI 비용** → 배치 AI 호출 금지, 개별 트리거만 허용
8. **세션 만료 중 AI 생성 진행** → AI 요청 시 세션 TTL 연장
9. **동시 AI 생성 요청 race condition** → 프론트엔드 버튼 비활성화 + 백엔드 idempotency

---

## 기능:기술부채 비율 가이드

| Phase | 기능 개발 | 기술 부채 해소 | 비고 |
|-------|-----------|---------------|------|
| Phase 1 | 20% | 80% | 안정화 집중 |
| Phase 2 | 60% | 40% | 확장 기반 구축 |
| Phase 3 | 70% | 30% | 제품 고도화 |
| Phase 4 | 60% | 40% | 스케일링 병행 |
