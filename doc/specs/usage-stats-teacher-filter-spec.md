# 기능 명세서: 사용 통계 선생님 필터링

## 1. 개요

- **기능 설명**: 사용 통계 API(`/usage/summary`, `/usage/daily`, `/usage/keywords/top`)가 전체 시스템 집계 대신 로그인한 선생님 본인의 데이터만 반환하도록 수정
- **사용자 스토리**: 선생님이 통계 페이지를 열면 자신의 AI 생성 횟수, 복사율, 좋아요율 등을 확인할 수 있다
- **선행 조건**: 선생님이 세션 로그인 상태

## 2. API 스펙

### GET /usage/summary

- **변경 사항**: `@AuthenticationPrincipal`로 로그인한 선생님의 userId 추출 → 해당 userId 기준 집계
- **인증**: 필수 (비로그인 시 401)
- **Request**: 없음 (teacherId는 세션에서 자동 추출)
- **Response (200)**: 기존과 동일

```json
{
  "totalAiGenerations": 42,
  "totalLikes": 30,
  "likeRate": 71.4,
  "totalCopyClicks": 25,
  "copyRate": 59.5,
  "totalRegenerations": 8,
  "regenerationRate": 19.0,
  "avgGenerationDurationMs": 2100.0,
  "activeDaysLast7": 5,
  "activeDaysLast30": 18
}
```

### GET /usage/daily?days=30

- **변경 사항**: 로그인한 선생님의 userId 기준 집계
- **인증**: 필수
- **Response (200)**: 기존과 동일

### GET /usage/keywords/top?limit=20

- **변경 사항**: 로그인한 선생님 소속 학생들의 키워드만 집계
- **인증**: 필수
- **Response (200)**: 기존과 동일

## 3. 백엔드 구현 범위

### 데이터 접근 방식 (JOIN 방식, 스키마 변경 없음)

| 데이터 소스 | 필터링 방법 |
|------------|------------|
| `UserEvent` | `WHERE userId = :userId` |
| `AiGenerationLog` | `JOIN Feedback f ON feedbackId = f.id JOIN Student s ON f.studentId = s.id WHERE s.userId = :userId` |
| `FeedbackLike` | `JOIN Feedback f ON feedbackId = f.id JOIN Student s ON f.studentId = s.id WHERE s.userId = :userId` |
| `Feedback` (count) | `JOIN Student s ON studentId = s.id WHERE s.userId = :userId` |
| `FeedbackKeyword` | `JOIN Feedback f ON feedbackId = f.id JOIN Student s ON f.studentId = s.id WHERE s.userId = :userId` |

### Controller: `UsageController`
- 3개 메서드에 `@AuthenticationPrincipal UserId userId` 파라미터 추가
- userId를 Service로 전달

### Service: `UsageQueryService`
- `getUsageSummary()` → `getUsageSummary(UserId userId)`
- `getDailyUsage(int days)` → `getDailyUsage(int days, UserId userId)`
- `getTopKeywords(int limit)` → `getTopKeywords(int limit, UserId userId)`
- 각 Repository 호출에 userId 전달

### Repository 변경

#### UserEventRepository (기존 메서드 확장)
```
countByEventTypeAndUserId(String eventType, String userId)
countDistinctActiveDaysByUserId(LocalDateTime start, LocalDateTime end, String userId)
findDailyEventCountsByUserId(LocalDateTime start, LocalDateTime end, String userId)
```

#### AiGenerationLogRepository (JPQL JOIN 쿼리 추가)
```
countByTeacherId(String userId)
  → SELECT COUNT(a) FROM AiGenerationLog a
    JOIN Feedback f ON a.feedbackId = f.id
    JOIN Student s ON f.studentId = s.id
    WHERE s.userId = :userId

averageDurationMsByTeacherId(String userId)
  → SELECT COALESCE(AVG(a.durationMs), 0) FROM AiGenerationLog a
    JOIN Feedback f ON a.feedbackId = f.id
    JOIN Student s ON f.studentId = s.id
    WHERE s.userId = :userId
```

#### FeedbackLikeRepository (JPQL JOIN 쿼리 추가)
```
countByTeacherId(String userId)
  → SELECT COUNT(fl) FROM FeedbackLike fl
    JOIN Feedback f ON fl.feedbackId = f.id
    JOIN Student s ON f.studentId = s.id
    WHERE s.userId = :userId
```

#### FeedbackRepository (JPQL JOIN 쿼리 추가)
```
countByAiContentIsNotNullAndTeacherId(String userId)
  → SELECT COUNT(f) FROM Feedback f
    JOIN Student s ON f.studentId = s.id
    WHERE s.userId = :userId AND f.aiContent IS NOT NULL

findTopKeywordsByTeacherId(String userId, Pageable pageable)
  → SELECT new ...KeywordCountRow(fk.keyword, COUNT(fk))
    FROM FeedbackKeyword fk
    JOIN Feedback f ON fk.feedbackId = f.id
    JOIN Student s ON f.studentId = s.id
    WHERE s.userId = :userId
    GROUP BY fk.keyword
    ORDER BY COUNT(fk) DESC
```

### 테스트
- `UsageQueryServiceTest`: 각 통계 메서드가 teacherId 기준으로 올바르게 필터링하는지 검증
  - 선생님 A의 이벤트만 집계되는지
  - 선생님 B의 데이터가 혼입되지 않는지

## 4. 프론트엔드 구현 범위

- **변경 없음**: 프론트엔드는 이미 로그인 세션 쿠키를 포함하여 API 호출 중
- API가 세션 기반으로 자동 필터링하므로 프론트엔드 수정 불필요

## 5. 선택 사항 결정 내역

- **필터링 방식**: JOIN 방식 — 스키마 변경 없이 안전 (Flyway 미도입 상태 고려)
- **인증 처리**: 세션 기반 자동 추출 (`@AuthenticationPrincipal`) — 현재 세션 인증 체계와 일치
