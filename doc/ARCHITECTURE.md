# Teacher Agent — 프로젝트 아키텍처 문서

> 이 문서는 다른 AI가 이 문서만 보고 동일한 프로젝트를 처음부터 재현할 수 있도록 작성되었다.
> PRD(제품 요구사항)와 기술 아키텍처를 통합한 단일 문서이다.

---

## 1. 제품 개요

### 서비스명
Teacher Agent — AI 기반 선생님 보조 서비스

### 목적
선생님이 학생 관리, 수업 일정 운영, 학부모 피드백 문자 생성을 효율적으로 처리할 수 있도록 돕는 웹 서비스. AI가 선생님의 키워드 메모를 바탕으로 학부모에게 전달할 문자 문구를 자동 생성한다.

### 대상 사용자
- 선생님 (1인 또는 소수 인원 운영 기준)

### 핵심 가치
- 학부모 문자 작성 시간 단축
- 따뜻하고 일관된 톤의 피드백 유지
- 학생별 관찰 메모와 수업 기록의 통합 관리

### 핵심 기능
- 수업(Lesson) CRUD + 반복 수업(Recurrence) 지원 (DAILY/WEEKLY/MONTHLY)
- 학생(Student) CRUD + 학년(SchoolGrade) 관리
- 수업별 수강생(Attendee) 관리 (반복 수업 시 범위 선택: SINGLE/THIS_AND_FOLLOWING/ALL)
- 피드백(Feedback) 키워드 입력 → OpenAI gpt-4o-mini로 학부모 문자 메시지 자동 생성 (스트리밍 지원)
- 피드백 좋아요(Like) + 스냅샷 저장
- AI 생성 로그(AiGenerationLog) 추적 (프롬프트, 응답, 토큰 사용량, 소요시간)
- HTTP Session 기반 인증 (JSESSIONID 쿠키, 12시간 타임아웃)

---

## 2. 기술 스택

| 레이어 | 기술 |
|--------|------|
| 백엔드 | Spring Boot 4.0.3, Java 25 (Virtual Threads), JPA, Spring Security, Spring AI 2.0.0-M2 |
| 프론트엔드 | Next.js 16.1.6, React 19.2.3, TypeScript 5, Tailwind CSS 4, dnd-kit |
| DB | MySQL 8.0 (운영), H2 인메모리 (테스트) |
| AI | OpenAI gpt-4o-mini (Spring AI ChatClient, temperature 0.9, streaming 지원) |
| 인프라 | AWS EC2 (t3.micro) + RDS MySQL (db.t3.micro), Terraform |
| 프론트 배포 | Vercel |
| CI/CD | GitHub Actions |

---

## 3. 기능 요구사항

### 3-1. 인증

| 항목 | 내용 |
|------|------|
| 방식 | HTTP 세션 (JSESSIONID 쿠키), 12시간 타임아웃 |
| 로그인 | POST `/auth/login` — userId + password |
| 로그아웃 | POST `/auth/logout` |
| 현재 사용자 | GET `/auth/me` |
| 비밀번호 | BCrypt 해시 저장 |
| 최초 계정 | 서버 기동 시 환경변수로 관리자 계정 자동 생성 |

### 3-2. 선생님 프로필
- 이름, 담당 과목 조회 및 수정
- GET/PUT `/teachers/me`

### 3-3. 학생 관리

| 기능 | 설명 |
|------|------|
| 등록 | 이름 + 메모(선택) + 학년(필수) |
| 목록 | 해당 선생님의 전체 학생 목록 |
| 수정 | 이름, 메모, 학년 변경 |
| 삭제 | 학생 및 연관 데이터 삭제 |

**학년(SchoolGrade):** ELEMENTARY_1~6(초1~초6), MIDDLE_1~3(중1~중3), HIGH_1~3(고1~고3)

### 3-4. 수업 일정 관리

- 주간 달력 (월요일 시작), 이전/다음 주 이동, "오늘" 버튼
- 수업 생성: 제목, 시작/종료 시간 필수, 반복 설정 선택 가능 (매일/매주/매월)
- 반복 수업 수정·삭제 범위: `SINGLE` / `THIS_AND_FOLLOWING` / `ALL`

### 3-5. 수업 상세 (출석 관리)

- 수업에 학생 추가/제거 (반복 수업 범위 지원)
- 학생 카드 드래그 앤 드롭으로 순서 변경
- 그리드 열 수 조정 (1~6열)

### 3-6. 피드백 시스템 (핵심 기능)

**키워드 관리:**
- 학생 관찰 키워드 추가/수정/삭제
- 키워드별 `required` 플래그: true면 AI가 해당 내용을 문자에 그대로 포함
- 키워드 1개 최대 100자

**AI 학부모 문자 생성:**
- 키워드 기반 학부모 문자 문구 자동 생성 (150~250자)
- 스트리밍 생성 지원 (실시간 청크 전송)
- 재생성 시 이전 문자와 다른 표현 사용
- 클립보드 복사, 수동 편집, 좋아요(♥) 보관
- 모든 AI 호출은 AiGenerationLog에 기록 (프롬프트, 응답, 토큰, 소요시간)

**AI 생성 규칙 (프롬프트 핵심):**
- 경어체 (`습니다`/`입니다`) 사용, 비격식체 금지
- 구조: 이름 호칭 → 칭찬 → 학습 상황 안내 (마무리 인사 없음)
- 이름 뒤 조사: 받침 유무에 따라 자동 적용
- 긍정 키워드 → 담백한 칭찬, 부정 키워드 → 부드러운 순화
- `required` 키워드는 한 글자도 바꾸지 않고 그대로 포함
- 이전 생성 문자와 다른 문장 구조·어휘 사용 (표현 다양성)
- 학부모에게 학습 지도 요청 금지, 칭찬 요청만 허용
- 느낌표 금지, ^^ 이모지 최대 2회

### 3-7. 주요 UX 흐름

**수업 생성 및 출석 관리:**
1. 홈(주간 캘린더)에서 `+` 버튼 → 수업 모달 → 저장
2. 캘린더에서 수업 클릭 → 수업 상세 → 학생 카드 드래그 앤 드롭

**AI 학부모 문자 생성:**
1. 수업 상세에서 학생 카드 확인 → 키워드 입력
2. AI 생성 버튼 → 실시간 스트리밍으로 문구 표시
3. 클립보드 복사 → 카카오톡 등으로 전송

---

## 4. 디렉토리 구조

```
teacher-agent/
├── backend/
│   ├── build.gradle.kts
│   ├── src/main/java/com/teacher/agent/
│   │   ├── TeacherAgentApplication.java
│   │   ├── config/
│   │   │   ├── ChatClientConfig.java
│   │   │   ├── DataInitializer.java
│   │   │   ├── LoggingInterceptor.java
│   │   │   ├── OpenAiLoggingAdvisor.java
│   │   │   ├── RequestCachingFilter.java
│   │   │   ├── SecurityConfig.java
│   │   │   └── WebMvcConfig.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── TeacherController.java
│   │   │   ├── StudentController.java
│   │   │   ├── LessonController.java
│   │   │   ├── AttendeeController.java
│   │   │   ├── FeedbackController.java
│   │   │   └── UserIdArgumentResolver.java
│   │   ├── domain/
│   │   │   ├── BaseEntity.java
│   │   │   ├── Teacher.java
│   │   │   ├── Student.java
│   │   │   ├── Lesson.java
│   │   │   ├── Attendee.java
│   │   │   ├── Feedback.java
│   │   │   ├── FeedbackKeyword.java
│   │   │   ├── FeedbackLike.java
│   │   │   ├── AiGenerationLog.java
│   │   │   ├── repository/
│   │   │   │   ├── TeacherRepository.java
│   │   │   │   ├── StudentRepository.java
│   │   │   │   ├── LessonRepository.java
│   │   │   │   ├── FeedbackRepository.java
│   │   │   │   ├── FeedbackLikeRepository.java
│   │   │   │   └── AiGenerationLogRepository.java
│   │   │   └── vo/
│   │   │       ├── UserId.java, UserIdConverter.java
│   │   │       ├── Recurrence.java, RecurrenceType.java
│   │   │       ├── DayOfWeekConverter.java
│   │   │       ├── UpdateScope.java
│   │   │       └── SchoolGrade.java
│   │   ├── dto/                      # 모든 DTO는 record
│   │   │   ├── LoginRequest, AuthResponse
│   │   │   ├── TeacherResponse, TeacherUpdateRequest
│   │   │   ├── StudentCreateRequest, StudentResponse, StudentUpdateRequest
│   │   │   ├── LessonCreateRequest, LessonResponse, LessonUpdateRequest, LessonDetailResponse
│   │   │   ├── RecurrenceCreateRequest
│   │   │   ├── AttendeeCreateRequest, AttendeeResponse
│   │   │   ├── FeedbackCreateRequest, FeedbackResponse, FeedbackUpdateRequest
│   │   │   └── FeedbackKeywordCreateRequest, FeedbackKeywordUpdateRequest
│   │   ├── exception/
│   │   │   ├── ErrorCode, ErrorResponse, BusinessException
│   │   │   ├── ResourceNotFoundException, BadRequestException
│   │   │   ├── ConflictException, UnauthorizedException
│   │   │   └── GlobalExceptionHandler
│   │   ├── service/
│   │   │   ├── AuthService, TeacherUserDetailsService
│   │   │   ├── TeacherQueryService, TeacherCommandService
│   │   │   ├── StudentQueryService, StudentCommandService
│   │   │   ├── LessonQueryService, LessonCommandService
│   │   │   ├── LessonDetailQueryService, LessonFactory
│   │   │   ├── AttendeeQueryService, AttendeeCommandService
│   │   │   ├── FeedbackQueryService, FeedbackCommandService
│   │   │   ├── FeedbackAiService, FeedbackPromptBuilder
│   │   │   ├── FeedbackKeywordService, FeedbackLikeService
│   │   │   ├── AiGenerationLogCommandService
│   │   │   └── vo/
│   │   │       ├── GenerationContext, LessonCreateCommand
│   │   │       ├── LessonUpdateCommand, LessonDetailRow
│   │   └── util/
│   │       ├── ValidationUtil, Parameter
│   │       ├── ErrorMessages, RepositoryUtil
│   │       └── UsageTokenExtractor
│   └── src/main/resources/
│       ├── application.yml, application-prod.yml
│       └── prompts/feedback_message.md
├── frontend/
│   ├── package.json, next.config.ts, tsconfig.json
│   └── src/app/
│       ├── layout.tsx, globals.css
│       ├── login/page.tsx
│       ├── (app)/
│       │   ├── layout.tsx
│       │   ├── page.tsx              # 메인 (리다이렉트)
│       │   ├── calendar/page.tsx     # 주간 캘린더 뷰
│       │   ├── intro/page.tsx        # 인트로 페이지
│       │   ├── students/page.tsx     # 학생 관리
│       │   ├── students/[id]/page.tsx # 학생 상세
│       │   └── lessons/[lessonId]/page.tsx
│       ├── api/feedbacks/[id]/generate/stream/route.ts  # 스트리밍 프록시
│       ├── components/
│       │   ├── WeeklyCalendarView, StudentsView, Sidebar, BottomNav
│       │   ├── AddLessonModal/ (LessonDetailsStep, StudentSelectionStep, NewStudentForm)
│       │   ├── AddStudentModal, AddAttendeeModal, RecurringScopeModal, ConfirmModal
│       │   ├── LessonCard, LessonEditForm
│       │   ├── StudentCard, StudentManagementCard, AttendeeCard
│       │   ├── KeywordsSection, AiFeedbackSection, FeedbackHistoryCard
│       │   ├── DatePicker, TimePicker, CustomSelect, GradeSelect
│       │   └── icons/NavIcons
│       ├── hooks/ (useFeedback, useLessonDetail, useLessonEdit, useGridLayout, useDropdown, useIsMobile)
│       ├── context/AuthContext.tsx
│       ├── lib/ (api.ts, constants.ts, dateTimeUtils.ts, highlightKeywords.tsx)
│       └── types/api.ts
├── infra/ (main.tf, variables.tf, outputs.tf)
├── .github/workflows/ci-cd.yml
└── doc/ (AUTH.md, COMMIT.md, PRD.md)
```


---

## 5. 백엔드 아키텍처

### 5.1 빌드 설정 (build.gradle.kts)

```kotlin
plugins {
    java
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "7.0.3"
}

group = "com.teacher"
version = "0.0.1-SNAPSHOT"
java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }
extra["springAiVersion"] = "2.0.0-M2"

dependencies {
    implementation("org.springframework.boot:spring-boot-h2console")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    runtimeOnly("com.mysql:mysql-connector-j")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("com.h2database:h2")
}

dependencyManagement {
    imports { mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}") }
}
```

### 5.2 설정 파일

**application.yml (기본)**
```yaml
spring:
  application:
    name: teacher-agent-backend
  threads:
    virtual:
      enabled: true
  datasource:
    url: jdbc:mysql://localhost:3306/teacheragent
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: admin
    password: local-password
  jpa:
    database-platform: org.hibernate.dialect.MySQLDialect
    hibernate:
      ddl-auto: update
    open-in-view: false
    properties:
      hibernate:
        default_batch_fetch_size: 50
        session.events.log.LOG_QUERIES_SLOWER_THAN_MS: 100
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:dummy-key}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.9
          stream-usage: true

server:
  servlet:
    session:
      timeout: 12h

cors:
  allowed-origin-patterns: http://localhost:3000

app:
  initial-teacher:
    user-id: ${INITIAL_TEACHER_USER_ID:admin}
    password: ${INITIAL_TEACHER_PASSWORD:123}
    name: ${INITIAL_TEACHER_NAME:관리자}
    subject: ${INITIAL_TEACHER_SUBJECT:}
  warmup-teacher:
    user-id: ${WARMUP_TEACHER_USER_ID:warmup}
    password: ${WARMUP_TEACHER_PASSWORD:warmup-password}
```

**application-prod.yml**
```yaml
cors:
  allowed-origin-patterns: https://teacher-agent-nine.vercel.app
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
```

### 5.3 도메인 모델

```
Teacher (어그리거트 루트)
├── id: Long (PK, IDENTITY)
├── userId: UserId (VO, unique) — 로그인 ID
├── password: String — BCrypt 해시
├── name: String
├── subject: String (nullable, max 100)
└── createdAt / updatedAt (BaseEntity)

Student (어그리거트 루트)
├── id: Long (PK, IDENTITY)
├── userId: UserId — 소유 선생님의 userId
├── name: String
├── memo: String (TEXT, nullable, max 500)
├── grade: SchoolGrade (enum) — 학년
└── INDEX: idx_student_user_id (userId)

Lesson (어그리거트 루트)
├── id: Long (PK, IDENTITY)
├── userId: UserId
├── title: String
├── startTime / endTime: LocalDateTime
├── recurrence: Recurrence (Embedded, nullable)
│   ├── recurrenceType: DAILY | WEEKLY | MONTHLY
│   ├── intervalValue: Integer
│   ├── daysOfWeek: List<DayOfWeek> (DayOfWeekConverter → 콤마 구분 문자열)
│   └── endDate: LocalDate
├── recurrenceGroupId: UUID (nullable)
├── attendees: List<Attendee> (OneToMany, cascade ALL, orphanRemoval)
└── INDEX: idx_lesson_user_id, idx_lesson_recurrence_group_id

Attendee (Lesson의 하위 엔티티 — 별도 Repository 없음)
├── id: Long (PK, IDENTITY)
├── lesson: Lesson (ManyToOne, LAZY)
├── studentId: Long
└── UNIQUE: uk_attendee_lesson_student (lesson_id, student_id)

Feedback (어그리거트 루트)
├── id: Long (PK, IDENTITY)
├── studentId: Long
├── lessonId: Long
├── aiContent: String (TEXT, nullable) — AI 생성 피드백 문자
├── liked: boolean (default false)
├── keywords: List<FeedbackKeyword> (OneToMany, cascade ALL, orphanRemoval)
├── INDEX: idx_feedback_student_id
└── UNIQUE: uk_feedback_student_lesson (studentId, lessonId)

FeedbackKeyword (Feedback의 하위 엔티티 — 별도 Repository 없음)
├── id: Long (PK, IDENTITY)
├── feedback: Feedback (ManyToOne, LAZY)
├── keyword: String (max 100)
└── required: boolean — true면 AI가 그대로 포함

FeedbackLike (어그리거트 루트)
├── id: Long (PK, IDENTITY)
├── feedbackId: Long
├── aiContentSnapshot: String (TEXT)
├── keywordsSnapshot: String (TEXT)
└── INDEX: idx_feedback_like_feedback_id_id (feedback_id, id)

AiGenerationLog (어그리거트 루트)
├── id: Long (PK, IDENTITY)
├── feedbackId: Long
├── promptContent: String (TEXT)
├── completionContent: String (TEXT)
├── streaming: boolean
├── durationMs: long
├── promptTokens: Integer (nullable)
├── completionTokens: Integer (nullable)
└── INDEX: idx_ai_generation_log_feedback_id
```

**Value Objects:**
- `UserId(String value)` — record, 빈 값 검증, `UserIdConverter`로 JPA 자동 변환
- `Recurrence` — Embeddable, `create()` 팩토리 메서드
- `RecurrenceType` — enum: DAILY, WEEKLY, MONTHLY
- `UpdateScope` — enum: SINGLE, THIS_AND_FOLLOWING, ALL
- `SchoolGrade` — enum: ELEMENTARY_1~6(초1~초6), MIDDLE_1~3(중1~중3), HIGH_1~3(고1~고3) + `displayName()`
- `DayOfWeekConverter` — `List<DayOfWeek>` ↔ 콤마 구분 문자열 변환

**도메인 규칙:**
- 엔티티 생성은 `static create(...)` 팩토리 메서드 사용
- 필드 변경은 엔티티 내부 메서드(`update`, `updateXxx`)로만 처리
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 필수
- Repository는 어그리거트 루트에만 생성 (Teacher, Student, Lesson, Feedback, FeedbackLike, AiGenerationLog)
- 하위 엔티티(Attendee, FeedbackKeyword)는 어그리거트 루트를 통해 접근

### 5.4 서비스 레이어

**CQRS 스타일 분리:**
- `XxxQueryService` — 조회 전용, `@Transactional(readOnly = true)`
- `XxxCommandService` — 쓰기 작업, 메서드별 `@Transactional`

**서비스 목록:**

| 서비스 | 역할 |
|--------|------|
| `AuthService` | 로그인(세션 생성), 로그아웃(세션 무효화), 현재 사용자 조회 |
| `TeacherUserDetailsService` | Spring Security UserDetailsService 구현 |
| `TeacherQueryService` / `TeacherCommandService` | 선생님 조회 / 프로필 수정 |
| `StudentQueryService` / `StudentCommandService` | 학생 목록·단건 조회 / CRUD |
| `LessonQueryService` | 수업 조회 (주간 범위), 반복 수업 시리즈 조회 |
| `LessonCommandService` | 수업 CRUD, 반복→단건 변환, 수강생 변경 적용 |
| `LessonDetailQueryService` | 수업 상세 (JPQL JOIN으로 수강생+피드백+키워드 한번에) |
| `LessonFactory` | 반복 수업 생성 로직 (DAILY/WEEKLY/MONTHLY 패턴별 날짜 생성) |
| `AttendeeQueryService` / `AttendeeCommandService` | 수강생 목록 조회 / 추가·제거 (반복 수업 범위 지원) |
| `FeedbackQueryService` | 피드백 조회, 소유권 검증 |
| `FeedbackCommandService` | 피드백 CRUD, AI 콘텐츠 생성 요청, 스트리밍 생성 (`streamAiContent()` → `Flux<String>`) |
| `FeedbackAiService` | ChatClient로 OpenAI 호출, 동기/스트리밍 두 방식, 토큰 사용량 추적, AiGenerationLog 저장 |
| `FeedbackPromptBuilder` | 프롬프트 빌드 로직 분리: required/normal 키워드 파티셔닝, previous_content 지원 |
| `FeedbackKeywordService` | 키워드 추가/수정/삭제 (required 플래그 지원) |
| `FeedbackLikeService` | 좋아요 처리 + FeedbackLike 스냅샷 저장 |
| `AiGenerationLogCommandService` | AI 생성 로그 저장 (프롬프트, 응답, 토큰, 소요시간) |

**Config 클래스:**
- `ChatClientConfig` — ChatClient Bean 생성 (OpenAiLoggingAdvisor 포함)
- `SecurityConfig` — 보안 설정 (프로필별 FilterChain 분리)
- `WebMvcConfig` — UserIdArgumentResolver 등록
- `DataInitializer` — 초기 Teacher 계정 생성
- `LoggingInterceptor` — HTTP 요청 로깅
- `OpenAiLoggingAdvisor` — AI 호출 로깅
- `RequestCachingFilter` — 요청 본문 캐싱

**DTO 컨벤션:**
- 모든 DTO는 Java `record` 사용
- Service는 Request DTO를 직접 받지 않음 → Controller에서 분해하여 전달
  - 파라미터 1~3개: primitive 직접 전달
  - 파라미터 4개 이상: `service/vo/` 패키지에 Command record 생성
  - Request → Command: Request 내부 `toCommand()` 메서드
  - Entity → Response: `static from(Entity)` 메서드

### 5.5 API 엔드포인트

#### 인증 (`/auth`)
| Method | Path | Auth | 설명 |
|--------|------|------|------|
| POST | `/auth/login` | No | 로그인 → JSESSIONID 쿠키 발급 |
| POST | `/auth/logout` | No* | 세션 무효화 |
| GET | `/auth/me` | No* | 현재 로그인 사용자 (미인증 시 401) |

#### 선생님 (`/teachers`)
| Method | Path | Auth | 설명 |
|--------|------|------|------|
| GET | `/teachers/me` | Yes | 내 프로필 조회 |
| PUT | `/teachers/me` | Yes | 내 프로필 수정 (name, subject) |

#### 학생 (`/students`)
| Method | Path | Auth | 설명 |
|--------|------|------|------|
| POST | `/students` | Yes | 학생 생성 (name, memo, grade) |
| GET | `/students` | Yes | 내 학생 전체 조회 |
| GET | `/students/{id}` | Yes | 학생 단건 조회 |
| PUT | `/students/{id}` | Yes | 학생 수정 (name, memo, grade) |
| DELETE | `/students/{id}` | Yes | 학생 삭제 |

#### 수업 (`/lessons`)
| Method | Path | Auth | 설명 |
|--------|------|------|------|
| POST | `/lessons` | Yes | 수업 생성 (title, startTime, endTime, recurrence?, studentIds?) |
| GET | `/lessons?weekStart=YYYY-MM-DD` | Yes | 주간 수업 목록 |
| GET | `/lessons/{id}` | Yes | 수업 단건 조회 |
| GET | `/lessons/{id}/detail` | Yes | 수업 상세 (수강생+피드백+키워드 JOIN) |
| PUT | `/lessons/{id}` | Yes | 수업 수정 (scope, recurrence, addStudentIds, removeStudentIds) |
| DELETE | `/lessons/{id}?scope=SINGLE` | Yes | 수업 삭제 (SINGLE/THIS_AND_FOLLOWING/ALL) |

#### 수강생 (`/lessons/{lessonId}/attendees`)
| Method | Path | Auth | 설명 |
|--------|------|------|------|
| POST | `/lessons/{lessonId}/attendees` | Yes | 수강생 추가 (studentId, scope?) |
| GET | `/lessons/{lessonId}/attendees` | Yes | 수강생 목록 |
| DELETE | `/lessons/{lessonId}/attendees/{attendeeId}?scope=` | Yes | 수강생 제거 |

#### 피드백 (`/feedbacks`)
| Method | Path | Auth | 설명 |
|--------|------|------|------|
| POST | `/feedbacks` | Yes | 피드백 생성 (studentId, lessonId) |
| GET | `/feedbacks?studentId=` | Yes | 학생별 피드백 목록 |
| GET | `/feedbacks/{id}` | Yes | 피드백 단건 조회 |
| PATCH | `/feedbacks/{id}` | Yes | AI 콘텐츠 수정 (aiContent) |
| DELETE | `/feedbacks/{id}` | Yes | 피드백 삭제 |
| POST | `/feedbacks/{id}/generate` | Yes | AI 피드백 문자 생성 (동기) |
| GET | `/feedbacks/{id}/generate/stream` | Yes | AI 피드백 스트리밍 생성 (text/plain, Flux) |
| POST | `/feedbacks/{id}/keywords` | Yes | 키워드 추가 (keyword, required) |
| PUT | `/feedbacks/{id}/keywords/{keywordId}` | Yes | 키워드 수정 (keyword, required) |
| DELETE | `/feedbacks/{id}/keywords/{keywordId}` | Yes | 키워드 삭제 |
| POST | `/feedbacks/{id}/like` | Yes | 좋아요 (스냅샷 저장) |

### 5.6 인증/보안 구성

**SecurityConfig.java:**
- CSRF 비활성화 (REST API)
- Form Login / HTTP Basic 비활성화
- 세션 기반 인증 (HttpSessionSecurityContextRepository), 타임아웃 12시간
- 프로필별 SecurityFilterChain 분리:
  - `!prod`: `/auth/**`, `/h2-console/**` permitAll, 나머지 authenticated
  - `prod`: `/auth/**` permitAll, 나머지 authenticated, H2 콘솔 비활성화
- CORS: `allowCredentials=true`, 메서드 `GET/POST/PUT/PATCH/DELETE/OPTIONS`
- BCryptPasswordEncoder 사용

**UserIdArgumentResolver:** Controller 메서드에서 `UserId` 타입 파라미터 자동 주입 (SecurityContext → UserId 변환)

**DataInitializer:** CommandLineRunner로 서버 시작 시 초기 Teacher 계정 생성/업데이트

### 5.7 예외 처리 체계

```
BusinessException (추상 기반)
├── ResourceNotFoundException (404)
│   ├── teacher(userId), student(id), lesson(id)
│   └── feedback(id), feedbackKeyword(id), attendee(id)
├── BadRequestException (400)
│   ├── keywordRequired(), studentNotEnrolled()
│   └── noLessonGenerated(), feedbackLikeRequiresAiContent()
├── ConflictException (409) — duplicateAttendee()
└── UnauthorizedException (401)
```

**ErrorCode enum:** 각 에러에 HttpStatus, code 문자열, 한국어 메시지 매핑
**ErrorResponse record:** `{ code, message, timestamp }`
**GlobalExceptionHandler:** BusinessException, Validation, IllegalArgument/State, catch-all 처리

### 5.8 AI 통합

**ChatClientConfig:** ChatClient Bean 생성 (OpenAiLoggingAdvisor 포함)

**FeedbackPromptBuilder:** 프롬프트 빌드 로직 분리
- 템플릿: `resources/prompts/feedback_message.md`
- 플레이스홀더: `{student_name}`, `{grade}`, `{keywords}`, `{required_keywords}`, `{previous_content}`
- 키워드 파티셔닝: `required=true` → 그대로 포함, `required=false` → 자연스럽게 녹임
- 이전 생성 문자(`previous_content`)를 포함하여 표현 다양성 확보

**FeedbackAiService:** 동기/스트리밍 두 방식 지원
- `generateFeedbackContent(feedback, student)` → String (동기)
- `streamFeedbackContent(feedback, student)` → Flux<String> (스트리밍)
- 모든 호출에서 토큰 사용량 추적 (UsageTokenExtractor)
- 모든 호출 결과 AiGenerationLog에 저장

**AiGenerationLog:** 모든 AI 호출 로깅 (프롬프트, 응답, 소요시간, 토큰, 스트리밍 여부)

### 5.9 유틸리티

**ValidationUtil** — 정적 검증 메서드, 검증 통과 시 값 반환 (엔티티 필드에 직접 할당 가능)
- `checkNotNull`, `checkNotBlank`, `checkMaxLength`, `checkPositive`, `checkNotNegative`, `checkNotEmpty`, `checkArgument`

**Parameter** — 파라미터 이름 상수 (ID, NAME, MEMO, KEYWORD, GRADE, STUDENT_ID, TITLE, START_TIME, END_TIME, PROMPT_CONTENT, COMPLETION_CONTENT, DURATION_MS 등)

**ErrorMessages** — 에러 메시지 템플릿 상수

**RepositoryUtil** — 공통 조회 헬퍼 (findByIdOrThrow, findTeacherByUserIdOrThrow, findLessonByIdAndUserIdOrThrow, findStudentByIdAndUserIdOrThrow)

**UsageTokenExtractor** — Spring AI Usage → Integer 변환 (promptTokens, completionTokens)

---

## 6. 프론트엔드 아키텍처

### 6.1 의존성

```json
{
  "dependencies": {
    "@dnd-kit/core": "^6.3.1",
    "@dnd-kit/sortable": "^10.0.0",
    "@dnd-kit/utilities": "^3.2.2",
    "next": "16.1.6",
    "react": "19.2.3",
    "react-dom": "19.2.3"
  },
  "devDependencies": {
    "@tailwindcss/postcss": "^4",
    "tailwindcss": "^4",
    "typescript": "^5",
    "eslint": "^9",
    "@playwright/test": "^1.58.2"
  }
}
```

### 6.2 Next.js 설정

```typescript
// next.config.ts — /api/* 요청을 백엔드로 프록시
const nextConfig: NextConfig = {
  async rewrites() {
    const backendUrl = process.env.API_URL || 'http://localhost:8080';
    return [{ source: '/api/:path*', destination: `${backendUrl}/:path*` }];
  },
};
```

### 6.3 페이지 구조 (App Router)

| 경로 | 파일 | 설명 |
|------|------|------|
| `/` | `(app)/page.tsx` | 메인 (리다이렉트) |
| `/calendar` | `(app)/calendar/page.tsx` | 주간 캘린더 뷰 |
| `/intro` | `(app)/intro/page.tsx` | 인트로 페이지 |
| `/students` | `(app)/students/page.tsx` | 학생 관리 |
| `/students/[id]` | `(app)/students/[id]/page.tsx` | 학생 상세 |
| `/lessons/[lessonId]` | `(app)/lessons/[lessonId]/page.tsx` | 수업 상세 (수강생, 피드백) |
| `/login` | `login/page.tsx` | 로그인 |

**API Route:** `api/feedbacks/[id]/generate/stream/route.ts` — 스트리밍 프록시

**레이아웃 계층:**
- `layout.tsx` (루트): Geist 폰트, AuthProvider
- `(app)/layout.tsx`: 인증 보호 + Sidebar/BottomNav + children

### 6.4 상태 관리

- **AuthContext**: 전역 인증 상태 (React Context + useState, 외부 라이브러리 없음)
- 마운트 시 `/api/auth/me` 호출하여 세션 확인

### 6.5 커스텀 훅

| 훅 | 역할 |
|------|------|
| `useFeedback` | 피드백 상태: 키워드 CRUD, AI 생성, 좋아요, 디바운스 업데이트 (1초) |
| `useLessonDetail` | 수업 상세 fetch, Attendee 타입 매핑 |
| `useLessonEdit` | 수업 수정 폼 상태, 저장 (scope 지원) |
| `useGridLayout` | 수강생 그리드: 칼럼 수 (1-6), 슬롯 순서, localStorage 영속화 |
| `useDropdown` | 드롭다운 위치: fixed 포지션, 자동 위로 열기, 외부 클릭 닫기 |
| `useIsMobile` | 모바일 반응형 감지 |

### 6.6 API 레이어 (lib/api.ts)

- `BASE_URL = '/api'` (프록시 경유)
- `fetchWithAuth(url, options)` — 공통 fetch 래퍼: `credentials: 'include'`, 401 시 `/login` 리다이렉트
- 스트리밍 API: Next.js API Route로 프록시 (`api/feedbacks/[id]/generate/stream/route.ts`)
- 모든 API 함수는 타입드 파라미터와 반환값 사용

### 6.7 타입 정의 (types/api.ts)

```typescript
SchoolGrade = 'ELEMENTARY_1' | ... | 'HIGH_3'
Student { id, name, memo, grade: SchoolGrade|null, createdAt, updatedAt }
FeedbackKeyword { id, keyword, required: boolean, createdAt }
Feedback { id, studentId, lessonId, lessonTitle, lessonStartTime, aiContent, keywords[], liked, createdAt, updatedAt }
Lesson { id, title, startTime, endTime, recurrenceGroupId }
AttendeeStudent { id, name, memo, grade }
Attendee { id, lessonId, student: AttendeeStudent, feedback: Feedback|null, createdAt }
LessonDetail { id, title, startTime, endTime, recurrenceGroupId, createdAt, updatedAt, attendees: LessonDetailAttendee[] }
LessonDetailAttendee { attendeeId, student: Student, feedback: LessonDetailFeedback|null }
AuthResponse { userId }
UpdateScope = 'SINGLE' | 'THIS_AND_FOLLOWING' | 'ALL'
RecurrenceType = 'DAILY' | 'WEEKLY' | 'MONTHLY'
DayOfWeek = 'MONDAY' | ... | 'SUNDAY'
RecurrenceCreateRequest { recurrenceType, intervalValue, daysOfWeek?, endDate }
```

### 6.8 컴포넌트

**페이지 컴포넌트:**
- `WeeklyCalendarView` — 7일 캘린더 그리드, 수업 블록, 현재 시간 인디케이터
- `StudentsView` — 학생 목록 + 검색 + 그리드 + FAB

**모달:**
- `AddLessonModal` — 2단계: 수업 정보 → 학생 선택 (LessonDetailsStep, StudentSelectionStep, NewStudentForm)
- `AddStudentModal`, `AddAttendeeModal`, `RecurringScopeModal`, `ConfirmModal`

**카드:**
- `StudentCard`, `StudentManagementCard`, `AttendeeCard`, `LessonCard`, `LessonEditForm`, `FeedbackHistoryCard`

**UI:**
- `Sidebar`, `BottomNav` — 네비게이션 (데스크톱/모바일)
- `DatePicker`, `TimePicker`, `CustomSelect`, `GradeSelect` — 커스텀 입력 컴포넌트
- `KeywordsSection` — 키워드 표시/편집 (required 플래그 지원)
- `AiFeedbackSection` — AI 피드백 표시 + 복사 + 좋아요 + 재생성
- `icons/NavIcons` — 네비게이션 아이콘

**유틸리티:**
- `highlightKeywords.tsx` — AI 생성 문자에서 키워드 하이라이트

### 6.9 디자인 시스템

**컨셉:** 귀엽고 심플하다. 선생님이 수업 전후로 빠르게 사용하는 도구.

**색상 팔레트:**
| 용도 | 값 |
|------|----|  
| 페이지 배경 | `from-purple-50 via-pink-50 to-orange-50` (그라디언트) |
| 카드 배경 | `white` |
| 주 포인트 | `pink-400` |
| 보조 포인트 | `purple-400` |
| 입력 필드 | `purple-50` |
| 위험 동작 | `rose-400` |

**컴포넌트 스타일:** 카드 `rounded-3xl`, 버튼 `rounded-2xl`, FAB `rounded-full bg-pink-400`, 모달 `bg-black/30 backdrop-blur-sm`

**네이티브 입력 금지:** `<input type="date">`, `<input type="time">`, `<select>` 직접 사용 금지 → DatePicker, TimePicker, CustomSelect, GradeSelect 커스텀 컴포넌트 사용

---

## 7. 인프라 아키텍처

### 7.1 Terraform (infra/)

**AWS 리소스:**
| 리소스 | 스펙 | 설명 |
|---------|------|------|
| EC2 | t3.micro, Amazon Linux 2023 | 백엔드 서버, 30GB gp3 암호화 볼륨 |
| RDS MySQL | db.t3.micro, MySQL 8.0, 20GB gp2 | 데이터베이스 |
| Security Group (EC2) | SSH(22), App(8080) open | EC2 접근 제어 |
| Security Group (RDS) | MySQL(3306) EC2 SG에서만 | RDS 접근 제어 |
| SSH Key Pair | RSA 4096-bit | EC2 접속용 |
| VPC | Default VPC | 프리티어 활용 |

**변수:** region(ap-northeast-2), instance_type(t3.micro), key_name(teacher-agent-key), project_name(teacher-agent), rds_db_name(teacheragent), rds_username(admin), rds_password(sensitive)

### 7.2 CI/CD (GitHub Actions)

**트리거:** `main` 브랜치 push, `backend/**` 경로 변경 시

**Stage 1 — Build & Test:** JDK 25 설정 → Gradle 빌드+테스트 → JAR 아티팩트 업로드 (7일)

**Stage 2 — Deploy (main push만):** JAR 다운로드 → SSH 키 설정 → EC2 환경 체크 → SCP 전송 → deploy.sh 실행

**필요 GitHub Secrets:** EC2_HOST, EC2_SSH_KEY, OPENAI_API_KEY, INITIAL_TEACHER_PASSWORD

### 7.3 배포 구성

**백엔드 (EC2):** Terraform 프로비저닝 → GitHub Actions 자동 빌드/배포 → 로그 `/home/ec2-user/app/app.log`

**프론트엔드 (Vercel):** GitHub 연동, 루트 `frontend/`, 환경변수 `API_URL`, main push 시 자동 배포, URL: `https://teacher-agent-nine.vercel.app`

---

## 8. 개발 컨벤션

### 8.1 백엔드

**패키지 구조:** Layer 기반 (controller, service, domain, dto, exception, util)

**Controller:** `ResponseEntity<T>` 반환, `@ResponseStatus` 금지, 생성 201 / 조회·수정 200 / 삭제 204

**Service:** 클래스 `@Transactional(readOnly = true)`, 쓰기 메서드만 `@Transactional`, 엔티티 직접 반환 금지

**코딩:** 변수명 약어 금지, 주석은 코드만으로 의도 파악이 어려운 경우에만, ValidationUtil 검증 결과 필드 직접 할당

### 8.2 프론트엔드

- 상태 관리: React Context + useState (외부 라이브러리 없음)
- API 호출: fetch API 직접 사용
- 네이티브 입력 요소 직접 사용 금지 → 커스텀 컴포넌트
- 드래그드롭: dnd-kit
- 디바운스 업데이트: 1초 (useFeedback)
- 그리드 레이아웃 + 순서 localStorage 영속화

### 8.3 커밋

- Conventional Commits: `<type>(<scope>): <subject>`
- `Co-Authored-By: Claude` 추가 금지

---

## 9. 환경변수 요약

| 변수 | 용도 | 기본값 |
|--------|------|--------|
| `OPENAI_API_KEY` | OpenAI API 키 | dummy-key |
| `INITIAL_TEACHER_USER_ID` | 초기 선생님 로그인 ID | admin |
| `INITIAL_TEACHER_PASSWORD` | 초기 선생님 비밀번호 | 123 |
| `INITIAL_TEACHER_NAME` | 초기 선생님 이름 | 관리자 |
| `INITIAL_TEACHER_SUBJECT` | 초기 선생님 과목 | (빈 값) |
| `WARMUP_TEACHER_USER_ID` | 워밍업용 선생님 ID | warmup |
| `WARMUP_TEACHER_PASSWORD` | 워밍업용 선생님 비밀번호 | warmup-password |
| `API_URL` | 프론트엔드 → 백엔드 프록시 대상 | http://localhost:8080 |
| `SPRING_DATASOURCE_URL` | 운영 DB JDBC URL | - |
| `SPRING_DATASOURCE_USERNAME` | 운영 DB 사용자 | - |
| `SPRING_DATASOURCE_PASSWORD` | 운영 DB 비밀번호 | - |
| `SPRING_PROFILES_ACTIVE` | 스프링 프로필 | - (로컬: 기본, 운영: prod) |

---

## 10. 로컬 개발 실행 방법

### 백엔드
```bash
cd backend
export OPENAI_API_KEY=your-key-here
./gradlew bootRun
# http://localhost:8080
```

### 프론트엔드
```bash
cd frontend
npm install
npm run dev
# http://localhost:3000
```

### 인프라
```bash
cd infra
terraform init
terraform plan -var="rds_password=your-password"
terraform apply -var="rds_password=your-password"
```

---

## 11. 재현 시 주의사항

1. **Java 25 필수** — Spring Boot 4.0.3은 Java 25 툴체인 필요
2. **Virtual Threads** — `spring.threads.virtual.enabled: true` 설정 필요
3. **Spring AI BOM** — `spring-ai-bom:2.0.0-M2` 사용 (Maven Central)
4. **MySQL 8.0** — 로컬에서는 MySQL 설치 필요 (DB: teacheragent, user: admin)
5. **H2는 테스트 전용** — `testImplementation("com.h2database:h2")`
6. **프롬프트 파일** — `feedback_message.md`는 `{placeholder}` 방식으로 치환 (String.replace)
7. **세션 인증** — JWT 아님, JSESSIONID 쿠키 기반, 12시간 타임아웃
8. **CORS** — 로컬 `localhost:3000`, 운영 `*.vercel.app`
9. **Next.js rewrite** — `/api/*` → 백엔드 프록시 (백엔드 URL에 `/api` prefix 없음)
10. **Tailwind CSS 4** — PostCSS 플러그인 방식 (`@tailwindcss/postcss`)
11. **dnd-kit** — 수강생 카드 드래그드롭 재정렬에 사용
12. **AI 스트리밍** — `GET /feedbacks/{id}/generate/stream` (text/plain, Flux<String>)
13. **키워드 required 플래그** — `required=true`인 키워드는 AI가 그대로 포함
14. **SchoolGrade** — Student에 학년 필드 필수 (ELEMENTARY_1~6, MIDDLE_1~3, HIGH_1~3)
15. **AiGenerationLog** — 모든 AI 호출은 로그 엔티티에 기록됨

---

## 12. 향후 고려사항

- 다중 선생님 계정 (현재 단일 관리자 계정 구조)
- 학부모 포털 연동 (직접 알림 발송)
- 피드백 히스토리 및 통계 대시보드
- 학생별 성장 추이 시각화