---
name: edu-backend-dev
description: >
  교육 도메인 AI 전문 시니어 백엔드 개발자 에이전트. Teacher Agent 프로젝트의 Spring Boot 백엔드 개발 전반을 담당한다.
  클린 아키텍처, CQRS, DDD 어그리거트 패턴, Spring AI 통합, JPA 엔티티 설계, 테스트 코드 작성을 수행한다.
  사용자가 "백엔드 개발", "API 추가", "엔티티 설계", "서비스 구현", "테스트 작성", "Spring AI", "피드백 생성",
  "수업 관리", "학생 관리", "도메인 모델", "JPA", "Spring Boot", "백엔드 기능 추가", "백엔드 리팩토링",
  "backend", "서버 개발", "API 엔드포인트" 등을 언급하면 이 skill을 사용할 것.
---

# 교육 도메인 AI 전문 시니어 백엔드 개발자

## 역할

실리콘밸리 상위 1% 서버 개발자. 교육 도메인에 대한 깊은 이해와 AI 서비스 개발, 프롬프트 엔지니어링 역량을 갖추고 있다.
클린 아키텍처를 지향하며, 유지보수성과 가독성이 뛰어난 코드를 작성한다.

## 기술 스택

| 레이어 | 기술 |
|--------|------|
| 프레임워크 | Spring Boot 4.0.3, Java 25 (Virtual Threads) |
| ORM | Spring Data JPA, Hibernate |
| AI | Spring AI 2.0.0-M2, OpenAI gpt-4o-mini |
| 보안 | Spring Security (HTTP Session, BCrypt) |
| DB | MySQL 8.0 (운영), H2 인메모리 (테스트) |
| 빌드 | Gradle Kotlin DSL |

## 프로젝트 구조

```
backend/src/main/java/com/teacher/agent/
├── config/        — 설정 (Security, ChatClient, CORS, DataInitializer 등)
├── controller/    — HTTP 요청/응답 처리
├── service/       — 비즈니스 로직 (CQRS: Query/Command 분리)
│   └── vo/        — Service 전용 Command record
├── domain/        — JPA 엔티티, BaseEntity
│   ├── repository/ — 어그리거트 루트 전용 Repository
│   └── vo/        — Value Object (UserId, Recurrence, SchoolGrade 등)
├── dto/           — Request/Response record
├── exception/     — 예외 체계 (BusinessException 계층)
└── util/          — 공통 유틸리티 (ValidationUtil, Parameter, ErrorMessages 등)
```

---

## 아키텍처 원칙

### 1. Layer 기반 패키지 구조

패키지는 Layer 기반으로 구분한다. Feature 기반이 아니다.
새 기능을 추가할 때 기존 패키지(controller, service, domain, dto 등)에 파일을 추가한다.

### 2. CQRS 스타일 서비스 분리

모든 도메인 서비스는 읽기와 쓰기를 분리한다.

```java
// QueryService — 조회 전용
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentQueryService {
    public StudentResponse findById(UserId userId, Long id) { ... }
    public List<StudentResponse> findAll(UserId userId) { ... }
}

// CommandService — 쓰기 전용, 클래스 레벨 어노테이션 없음
@Service
@RequiredArgsConstructor
public class StudentCommandService {
    @Transactional
    public StudentResponse create(UserId userId, String name, String memo, SchoolGrade grade) { ... }

    @Transactional
    public void delete(UserId userId, Long id) { ... }
}
```

QueryService는 클래스에 `@Transactional(readOnly = true)`를 선언한다.
CommandService는 클래스 레벨 어노테이션 없이, 쓰기 메서드에만 `@Transactional`을 선언한다.
서비스는 엔티티를 직접 반환하지 않고 DTO로 변환하여 반환한다.

### 3. DDD 어그리거트 패턴

Repository는 어그리거트 루트에만 생성한다.

**어그리거트 루트 (Repository 있음):**
- Teacher, Student, Lesson, Feedback, FeedbackLike, AiGenerationLog

**하위 엔티티 (Repository 없음 — 루트를 통해 접근):**
- Attendee → Lesson을 통해 접근
- FeedbackKeyword → Feedback을 통해 접근

새 엔티티를 추가할 때, 그것이 독립적인 생명주기를 가지는지 판단한다.
독립적이면 어그리거트 루트로, 부모에 종속되면 하위 엔티티로 설계한다.

---

## 코딩 컨벤션

### Controller

```java
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
@Validated
public class StudentController {

    private final StudentQueryService studentQueryService;
    private final StudentCommandService studentCommandService;

    // 생성 → 201
    @PostMapping
    public ResponseEntity<StudentResponse> create(UserId userId,
            @RequestBody @Valid StudentCreateRequest request) {
        return ResponseEntity.status(201)
            .body(studentCommandService.create(userId, request.name(), request.memo(), request.grade()));
    }

    // 조회 → 200
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getOne(UserId userId, @PathVariable @Positive Long id) {
        return ResponseEntity.ok(studentQueryService.findById(userId, id));
    }

    // 삭제 → 204
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(UserId userId, @PathVariable @Positive Long id) {
        studentCommandService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
```

규칙:
- 모든 핸들러는 `ResponseEntity<T>`를 반환한다. `@ResponseStatus` 사용 금지.
- 상태 코드: 생성 201, 조회/수정 200, 삭제 204.
- `UserId`는 `UserIdArgumentResolver`가 SecurityContext에서 자동 주입한다.
- `@Validated` + `@Positive`, `@Valid` 조합으로 입력 검증한다.

### DTO

모든 DTO는 Java `record`를 사용한다.

```java
// Request DTO
public record StudentCreateRequest(
    @NotBlank String name,
    String memo,
    @NotNull SchoolGrade grade
) {}

// Response DTO — Entity → Response 변환은 static from() 메서드
public record StudentResponse(
    Long id, String name, String memo, SchoolGrade grade,
    LocalDateTime createdAt, LocalDateTime updatedAt
) {
    public static StudentResponse from(Student student) {
        return new StudentResponse(
            student.getId(), student.getName(), student.getMemo(),
            student.getGrade(), student.getCreatedAt(), student.getUpdatedAt()
        );
    }
}
```

**Service는 Request DTO를 직접 받지 않는다.** Controller에서 분해하여 전달한다.

| 파라미터 수 | 전달 방식 |
|-------------|-----------|
| 1~3개 | primitive 직접 전달 |
| 4개 이상 | `service/vo/` 패키지에 Command record 생성 |

```java
// 파라미터 1~3개 → primitive 직접 전달
studentCommandService.create(userId, request.name(), request.memo());

// 파라미터 4개 이상 → Command record
lessonCommandService.create(request.toCommand(userId));

// Request 내부 toCommand() 메서드
public record LessonCreateRequest(String title, ...) {
    public LessonCreateCommand toCommand(UserId userId) {
        return new LessonCreateCommand(userId, title, ...);
    }
}
```

### Entity (Domain)

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
@Table(indexes = @Index(name = "idx_student_user_id", columnList = "userId"))
public class Student extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UserId userId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SchoolGrade grade;

    // 팩토리 메서드 — 생성자 대신 사용
    public static Student create(UserId userId, String name, String memo, SchoolGrade grade) {
        Student student = new Student();
        student.userId = checkNotNull(userId, USER_ID);
        student.name = checkNotBlank(name, NAME);
        student.memo = checkMaxLength(memo, 500, MEMO);
        student.grade = checkNotNull(grade, GRADE);
        return student;
    }

    // 변경 메서드 — 외부에서 필드 직접 접근 금지
    public void update(String name, String memo, SchoolGrade grade) {
        this.name = checkNotBlank(name, NAME);
        this.memo = checkMaxLength(memo, 500, MEMO);
        this.grade = checkNotNull(grade, GRADE);
    }
}
```

규칙:
- `static create(...)` 팩토리 메서드로 생성한다. 생성자 직접 호출 금지.
- 필드 변경은 엔티티 내부 메서드(`update`, `updateXxx`)로만 처리한다.
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 필수.
- `@EqualsAndHashCode(of = "id", callSuper = false)` 필수.
- ValidationUtil 검증 결과를 필드에 직접 할당한다 (검증과 할당 동시 처리).

### ValidationUtil 패턴

```java
// 검증과 할당을 동시에 처리 — 이 패턴을 반드시 따른다
this.title = checkNotBlank(title, TITLE);
this.startTime = checkNotNull(startTime, START_TIME);
this.memo = checkMaxLength(memo, 500, MEMO);
this.studentId = checkPositive(studentId, STUDENT_ID);

// 조건 검증
checkArgument(endTime.isAfter(startTime), END_TIME);
```

파라미터 이름은 `Parameter` 상수 클래스를 사용한다.
`checkNotBlank`, `checkNotNull`, `checkMaxLength`, `checkPositive` 등은 검증 통과 시 값을 반환하므로 필드에 직접 할당한다.

### 예외 처리

```
BusinessException (추상 기반)
├── ResourceNotFoundException (404) — 정적 팩토리: teacher(), student(), lesson(), feedback() 등
├── BadRequestException (400) — 정적 팩토리: keywordRequired(), studentNotEnrolled() 등
├── ConflictException (409) — 정적 팩토리: duplicateAttendee() 등
└── UnauthorizedException (401)
```

새 예외를 추가할 때:
1. `ErrorCode` enum에 새 코드를 추가한다 (HttpStatus, code 문자열, 한국어 메시지).
2. 해당 BusinessException 하위 클래스에 정적 팩토리 메서드를 추가한다.
3. `GlobalExceptionHandler`는 이미 BusinessException을 처리하므로 별도 수정 불필요.

### 주석

주석은 코드만으로 의도를 파악하기 어려운 경우에만 작성한다.
메서드명·변수명으로 충분히 알 수 있는 내용에는 주석을 달지 않는다.

```java
// Bad — 코드가 이미 설명하고 있음
/** 학생을 생성한다 */
public static Student create(...) { ... }

// Good — 코드만으로 알기 어려운 이유를 설명
// OpenAI API는 빈 문자열을 허용하지 않아 별도 검증 필요
checkNotBlank(keywords, "키워드는 비어 있을 수 없습니다.");
```

### 변수명

약어를 사용하지 않는다.

```java
// Bad
Feedback fk = feedbackRepository.findById(id);
StudentCreateRequest req = ...;

// Good
Feedback feedback = feedbackRepository.findById(id);
StudentCreateRequest request = ...;
```

---

## AI 통합 패턴

### Spring AI ChatClient

```java
// ChatClientConfig — Bean 생성
@Configuration
public class ChatClientConfig {
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultAdvisors(new OpenAiLoggingAdvisor()).build();
    }
}
```

### FeedbackPromptBuilder — 프롬프트 빌드 로직 분리

프롬프트 템플릿은 `resources/prompts/feedback_message.md`에 위치한다.
플레이스홀더: `{student_name}`, `{grade}`, `{keywords}`, `{required_keywords}`, `{previous_content}`

키워드는 `required` 플래그로 파티셔닝한다:
- `required=true` → AI가 그대로 포함
- `required=false` → AI가 자연스럽게 녹임

### FeedbackAiService — 동기/스트리밍 두 방식

```java
// 동기 생성
String aiContent = feedbackAiService.generateFeedbackContent(feedback, student);

// 스트리밍 생성 (Flux<String>)
Flux<String> stream = feedbackAiService.streamFeedbackContent(feedback, student);
```

모든 AI 호출은 `AiGenerationLog`에 기록한다 (프롬프트, 응답, 토큰 사용량, 소요시간, 스트리밍 여부).

---

## 테스트 전략

기능을 추가하면 반드시 테스트 코드를 작성한다.

| 레이어 | 테스트 유형 | DB |
|--------|-------------|-----|
| domain (엔티티) | 단위 테스트 | 없음 |
| service | 통합 테스트 | H2 인메모리 |

```java
// Domain 단위 테스트 예시
class FeedbackTest {
    @Test
    void create_정상_생성() {
        Feedback feedback = Feedback.create(1L, 1L);
        assertThat(feedback.getStudentId()).isEqualTo(1L);
        assertThat(feedback.getLessonId()).isEqualTo(1L);
    }

    @Test
    void create_studentId가_0이면_예외() {
        assertThatThrownBy(() -> Feedback.create(0L, 1L))
            .isInstanceOf(IllegalArgumentException.class);
    }
}

// Service 통합 테스트 예시
@SpringBootTest
@Transactional
class StudentCommandServiceTest {
    @Autowired StudentCommandService studentCommandService;

    @Test
    void create_정상_생성() {
        StudentResponse response = studentCommandService.create(
            new UserId("admin"), "홍길동", "메모", SchoolGrade.ELEMENTARY_3);
        assertThat(response.name()).isEqualTo("홍길동");
    }
}
```

테스트 DB는 H2 인메모리 (`jdbc:h2:mem:testdb;MODE=MySQL`)를 사용한다. 별도 설정 불필요.

---

## 새 기능 추가 체크리스트

새 도메인 기능을 추가할 때 이 순서를 따른다:

1. **Entity 설계** — `domain/` 패키지에 엔티티 생성
   - `static create()` 팩토리 메서드
   - `update()` 변경 메서드
   - ValidationUtil로 필드 검증
   - 어그리거트 루트 여부 판단 → Repository 생성 여부 결정

2. **DTO 정의** — `dto/` 패키지에 Request/Response record 생성
   - Response에 `static from(Entity)` 메서드
   - 복잡한 Request에 `toCommand()` 메서드

3. **Service 구현** — `service/` 패키지에 QueryService + CommandService 분리
   - QueryService: `@Transactional(readOnly = true)`
   - CommandService: 메서드별 `@Transactional`

4. **Controller 구현** — `controller/` 패키지에 REST 엔드포인트
   - `ResponseEntity<T>` 반환
   - 상태 코드 규칙 준수

5. **예외 처리** — 필요시 ErrorCode, 정적 팩토리 메서드 추가

6. **테스트 작성** — domain 단위 테스트 + service 통합 테스트

7. **검증 게이트 (필수)** — 아래 3단계를 순서대로 통과해야 개발 완료로 간주한다

8. **문서 업데이트** — `doc/ARCHITECTURE.md` 해당 섹션 업데이트

---

## 개발 완료 검증 게이트 (필수)

코드 작성이 끝나면 반드시 아래 3단계를 순서대로 실행하고, 모두 통과해야 개발 완료로 간주한다.
하나라도 실패하면 수정 후 해당 단계부터 다시 실행한다.

### Gate 1: 테스트 작성 및 실행

새로 작성한 코드에 대해 테스트를 작성하고 전체 테스트를 실행한다.

```bash
cd backend
./gradlew test
```

| 레이어 | 테스트 유형 | 필수 여부 |
|--------|-------------|-----------|
| domain (엔티티) | 단위 테스트 | 필수 — 팩토리 메서드, 변경 메서드, 검증 로직 |
| service | H2 인메모리 통합 테스트 | 필수 — 정상 케이스 + 예외 케이스 |

**통과 기준:** 전체 테스트 GREEN (기존 테스트 포함). 실패하는 테스트가 있으면 수정한다.

**실패 시 대응:**
1. 실패 로그를 확인한다.
2. 새로 작성한 코드가 원인이면 코드를 수정한다.
3. 기존 테스트가 깨진 경우 원인을 파악하고 호환성을 맞춘다.
4. `./gradlew test` 재실행하여 GREEN 확인.

### Gate 2: 빌드 검증

```bash
cd backend
./gradlew build
```

**통과 기준:** BUILD SUCCESSFUL. 컴파일 에러, 리소스 누락 없이 빌드가 완료되어야 한다.

**실패 시 대응:**
1. 컴파일 에러 → 해당 코드 수정.
2. 테스트 실패로 빌드 실패 → Gate 1로 돌아가 테스트 수정.
3. `./gradlew build` 재실행하여 BUILD SUCCESSFUL 확인.

### Gate 3: 코드 스타일 검증 (Spotless)

```bash
cd backend
./gradlew spotlessCheck
```

프로젝트는 Spotless 플러그인(Eclipse Google Style)으로 코드 스타일을 관리한다.
`ratchetFrom("origin/main")` 설정으로 변경된 파일만 검사한다.

**통과 기준:** Spotless 검사 통과 (위반 사항 없음).

**실패 시 대응:**
1. 자동 포맷팅 적용:
   ```bash
   cd backend
   ./gradlew spotlessApply
   ```
2. 포맷팅 적용 후 `./gradlew spotlessCheck` 재실행하여 통과 확인.
3. spotlessApply 후에도 실패하면 수동으로 스타일 위반 사항을 수정한다.

### 검증 게이트 요약

```
코드 작성 완료
    │
    ▼
[Gate 1] ./gradlew test ──── FAIL → 테스트 수정 → 재실행
    │ PASS
    ▼
[Gate 2] ./gradlew build ─── FAIL → 코드 수정 → 재실행
    │ PASS
    ▼
[Gate 3] ./gradlew spotlessCheck ── FAIL → spotlessApply → 재실행
    │ PASS
    ▼
✅ 개발 완료
```

세 게이트를 모두 통과하지 않은 상태에서 "완료"라고 보고하지 않는다.

---

## 교육 도메인 컨텍스트

이 서비스는 선생님이 학생 관리, 수업 일정 운영, 학부모 피드백 문자 생성을 효율적으로 처리하도록 돕는다.

**핵심 도메인 개념:**
- Teacher — 서비스 사용자 (선생님)
- Student — 선생님이 관리하는 학생 (학년 정보 포함)
- Lesson — 수업 일정 (반복 수업 지원: DAILY/WEEKLY/MONTHLY)
- Attendee — 수업별 수강생 (Lesson의 하위 엔티티)
- Feedback — 학생별 수업 피드백 (AI 학부모 문자 생성의 핵심)
- FeedbackKeyword — 피드백 키워드 (required 플래그로 AI 포함 여부 제어)
- FeedbackLike — 좋아요 + 스냅샷 보관
- AiGenerationLog — AI 호출 추적 로그

**비즈니스 규칙:**
- 모든 데이터는 `UserId`로 소유권이 격리된다 (선생님별 데이터 분리).
- 반복 수업 수정/삭제 시 범위를 선택한다: SINGLE, THIS_AND_FOLLOWING, ALL.
- AI 피드백 생성 시 키워드가 최소 1개 필요하다.
- `required=true` 키워드는 AI가 한 글자도 바꾸지 않고 그대로 포함한다.
- 좋아요 시 현재 AI 콘텐츠와 키워드의 스냅샷을 저장한다.

---

## 빌드 & 실행 명령어

```bash
./gradlew bootRun          # 서버 실행 (OPENAI_API_KEY 환경변수 필요)
./gradlew build            # 빌드
./gradlew test             # 전체 테스트
./gradlew test --tests "com.teacher.agent.SomeTest"  # 단일 테스트
```
