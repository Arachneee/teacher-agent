# 기능 명세서: 사용 통계 집계 + 어드민 대시보드

## 1. 개요

### 기능 설명
기존에 쌓이고 있는 AiGenerationLog, FeedbackLike, Feedback, UserEvent 데이터를 집계하여 KPI를 계산하고, 리포팅 API를 통해 제공한다. 프론트엔드에서는 별도 `/admin` 경로에 대시보드 UI를 구성하여 숫자 카드 + Recharts 차트로 시각화한다.

### 사용자 스토리
- 관리자(선생님)로서, 서비스 사용 현황을 한눈에 파악하고 싶다.
- AI 생성 횟수, 좋아요율, 복사 전환율, 재생성율 등 핵심 KPI를 확인하고 싶다.
- 일별 추이를 차트로 보고 싶다.
- 자주 사용하는 키워드 Top N을 확인하고 싶다.

### 선행 조건
- 1-1 프론트엔드 이벤트 추적 완료 (UserEvent 엔티티 + POST /events API 구현됨)
- AiGenerationLog, FeedbackLike, Feedback 엔티티 존재

### ROADMAP 범위
- 1-2: 기존 데이터 집계 쿼리 추가
- 1-3: 사용 통계 리포팅 API
- 추가: 어드민 대시보드 UI (사용자 요청)

---

## 2. API 스펙

### 2-1. GET /usage/summary — 전체 요약

- Method: GET
- Path: `/usage/summary`
- Query Parameters: 없음
- Response (200):
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
- Error Cases:
  - 401: 미인증

### 2-2. GET /usage/daily — 일별 추이

- Method: GET
- Path: `/usage/daily`
- Query Parameters:
  - `days` (optional, default: 30, max: 90) — 조회할 일수
- Response (200):
```json
[
  {
    "date": "2026-03-28",
    "generations": 8,
    "copies": 5,
    "likes": 6,
    "regenerations": 1
  }
]
```
- Error Cases:
  - 400: days가 1 미만 또는 90 초과
  - 401: 미인증

### 2-3. GET /usage/keywords/top — 인기 키워드 Top N

- Method: GET
- Path: `/usage/keywords/top`
- Query Parameters:
  - `limit` (optional, default: 20, max: 50) — 조회할 키워드 수
- Response (200):
```json
[
  { "keyword": "성실함", "count": 23 },
  { "keyword": "집중력 향상", "count": 18 }
]
```
- Error Cases:
  - 400: limit가 1 미만 또는 50 초과
  - 401: 미인증

---

## 3. 백엔드 구현 범위

### Repository 집계 쿼리 추가

#### AiGenerationLogRepository
- `countByCreatedAtBetween(LocalDateTime start, LocalDateTime end)` — 기간별 AI 생성 횟수
- `findByFeedbackIdOrderByCreatedAtDesc(Long feedbackId)` — 피드백별 생성 이력
- `@Query averageDurationMs(LocalDateTime start, LocalDateTime end)` — 평균 생성 소요시간 (JPQL AVG)
- `countAll()` — 전체 AI 생성 횟수

#### FeedbackLikeRepository
- `countByCreatedAtBetween(LocalDateTime start, LocalDateTime end)` — 기간별 좋아요 수
- `count()` — 전체 좋아요 수 (JpaRepository 기본 제공)

#### FeedbackRepository
- `countByAiContentIsNotNull()` — AI 생성된 피드백 수 (좋아요율 분모)

#### UserEventRepository
- `countByEventType(String eventType)` — 이벤트 타입별 전체 카운트
- `countByEventTypeAndCreatedAtBetween(String eventType, LocalDateTime start, LocalDateTime end)` — 기간별 이벤트 카운트
- `@Query countDistinctActiveDays(String userId, LocalDateTime start, LocalDateTime end)` — 활성 일수 (DISTINCT DATE)
- `@Query findDailyEventCounts(LocalDateTime start, LocalDateTime end)` — 일별 이벤트 집계 (GROUP BY DATE, eventType)

#### FeedbackKeywordRepository (신규 또는 기존 확장)
- `@Query findTopKeywords(int limit)` — 키워드별 사용 횟수 Top N (GROUP BY content ORDER BY COUNT DESC)

### Service

#### UsageQueryService (신규)
- `@Service`, `@RequiredArgsConstructor`, `@Transactional(readOnly = true)`
- `getUsageSummary(UserId userId)` → `UsageSummaryResponse`
  - AiGenerationLogRepository.count() → totalAiGenerations
  - FeedbackLikeRepository.count() → totalLikes
  - FeedbackRepository.countByAiContentIsNotNull() → likeRate 분모
  - UserEventRepository.countByEventType("feedback_copy") → totalCopyClicks
  - UserEventRepository.countByEventType("feedback_regenerate") → totalRegenerations
  - AiGenerationLogRepository.averageDurationMs() → avgGenerationDurationMs
  - UserEventRepository.countDistinctActiveDays(last7days) → activeDaysLast7
  - UserEventRepository.countDistinctActiveDays(last30days) → activeDaysLast30
  - 비율 계산: likeRate, copyRate, regenerationRate
- `getDailyUsage(UserId userId, int days)` → `List<DailyUsageResponse>`
  - UserEventRepository.findDailyEventCounts() → 일별 이벤트 집계
  - AiGenerationLog 일별 카운트와 조합
- `getTopKeywords(UserId userId, int limit)` → `List<TopKeywordResponse>`
  - FeedbackKeyword 테이블에서 GROUP BY content

### Controller

#### UsageController (신규)
- `@RestController`, `@RequestMapping("/usage")`, `@RequiredArgsConstructor`
- `@GetMapping("/summary")` → `ResponseEntity<UsageSummaryResponse>`
- `@GetMapping("/daily")` → `ResponseEntity<List<DailyUsageResponse>>`
  - `@RequestParam(defaultValue = "30") @Min(1) @Max(90) int days`
- `@GetMapping("/keywords/top")` → `ResponseEntity<List<TopKeywordResponse>>`
  - `@RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit`

### DTO (record)

#### UsageSummaryResponse
```java
public record UsageSummaryResponse(
    long totalAiGenerations,
    long totalLikes,
    double likeRate,
    long totalCopyClicks,
    double copyRate,
    long totalRegenerations,
    double regenerationRate,
    double avgGenerationDurationMs,
    int activeDaysLast7,
    int activeDaysLast30
) {}
```

#### DailyUsageResponse
```java
public record DailyUsageResponse(
    LocalDate date,
    long generations,
    long copies,
    long likes,
    long regenerations
) {}
```

#### TopKeywordResponse
```java
public record TopKeywordResponse(
    String keyword,
    long count
) {}
```

### 예외 처리
- 기존 ErrorCode 활용 (INVALID_INPUT 등)
- 새 ErrorCode 추가 불필요 — @Min/@Max 검증은 Spring Validation이 자동 처리

### 테스트
- `UsageQueryServiceTest` — 각 메서드별 단위 테스트
  - 데이터 없을 때 0/빈 리스트 반환 확인
  - 비율 계산 정확성 (분모 0일 때 0.0 반환)
  - 기간 필터링 정확성
- `UsageControllerTest` — MockMvc 기반 API 테스트
  - 정상 응답 200
  - days/limit 범위 초과 시 400
  - 미인증 시 401

---

## 4. 프론트엔드 구현 범위

### 페이지
- `/admin` → `src/app/(app)/admin/page.tsx` — 어드민 대시보드 메인 페이지

### 레이아웃
- 기존 `(app)/layout.tsx` 내 Sidebar에 '통계' 탭 추가 (📊 아이콘)
- `/admin` 경로는 기존 `(app)` 그룹 안에 배치하여 Sidebar/BottomNav 공유

### 컴포넌트 (신규)

#### AdminDashboard (페이지 컴포넌트)
- KPI 카드 섹션 + 일별 추이 차트 + 인기 키워드 차트 구성
- 데이터 로딩 중 스켈레톤 UI

#### KpiCard
- 숫자 + 라벨 + 부가 정보(비율 등)
- 디자인: `rounded-3xl bg-white shadow-sm` 카드 스타일
- 표시할 KPI:
  - AI 생성 횟수 (totalAiGenerations)
  - 좋아요율 (likeRate, %)
  - 복사 전환율 (copyRate, %)
  - 재생성율 (regenerationRate, %)
  - 평균 생성 시간 (avgGenerationDurationMs, 초 변환)
  - 최근 7일 활성일 (activeDaysLast7)

#### DailyUsageChart
- Recharts `LineChart` 또는 `AreaChart`
- X축: 날짜, Y축: 횟수
- 4개 라인: generations, copies, likes, regenerations
- 기간 선택: 7일 / 14일 / 30일 토글
- 디자인: 파스텔 색상 라인, `rounded-3xl` 카드 래핑

#### TopKeywordsChart
- Recharts `BarChart` (가로 바)
- 키워드명 + 사용 횟수
- 상위 10개 표시 (기본)
- 디자인: `pink-400` 계열 바 색상

### 훅 (신규)

#### useUsageSummary
```typescript
function useUsageSummary(): {
  data: UsageSummaryResponse | null;
  loading: boolean;
  error: string | null;
}
```

#### useDailyUsage
```typescript
function useDailyUsage(days: number): {
  data: DailyUsageResponse[] | null;
  loading: boolean;
  error: string | null;
}
```

#### useTopKeywords
```typescript
function useTopKeywords(limit: number): {
  data: TopKeywordResponse[] | null;
  loading: boolean;
  error: string | null;
}
```

### API 함수 (lib/api.ts에 추가)
```typescript
export async function getUsageSummary(): Promise<UsageSummaryResponse>
export async function getDailyUsage(days?: number): Promise<DailyUsageResponse[]>
export async function getTopKeywords(limit?: number): Promise<TopKeywordResponse[]>
```

### 타입 (types/api.ts에 추가)
```typescript
interface UsageSummaryResponse {
  totalAiGenerations: number;
  totalLikes: number;
  likeRate: number;
  totalCopyClicks: number;
  copyRate: number;
  totalRegenerations: number;
  regenerationRate: number;
  avgGenerationDurationMs: number;
  activeDaysLast7: number;
  activeDaysLast30: number;
}

interface DailyUsageResponse {
  date: string;
  generations: number;
  copies: number;
  likes: number;
  regenerations: number;
}

interface TopKeywordResponse {
  keyword: string;
  count: number;
}
```

### 의존성 추가
- `recharts` — 차트 라이브러리 (npm install recharts)

### 반응형
- 모바일 (375px): KPI 카드 2열 그리드, 차트 풀 너비, 스크롤
- 태블릿 (768px): KPI 카드 3열, 차트 풀 너비
- 데스크톱 (1280px): KPI 카드 3열 × 2행, 차트 2열 배치 (일별 추이 + 키워드)

---

## 5. 선택 사항 결정 내역

| 선택 사항 | 결정 | 이유 |
|-----------|------|------|
| 대시보드 배치 | 별도 /admin 경로 | 일반 사용자 플로우와 분리, 관리 목적 명확 |
| 데이터 범위 | 1-2 + 1-3 통합 | 집계 쿼리와 리포팅 API를 한번에 구현하여 대시보드에서 바로 활용 |
| 시각화 수준 | 숫자 카드 + Recharts 차트 | 시각적으로 풍부한 대시보드, Recharts는 React 생태계 표준 |
