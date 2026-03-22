# Backend Convention

## 패키지 구조

Layer 기반으로 패키지를 구분한다.

```
com.teacher.agent
├── controller/   — HTTP 요청/응답 처리
├── service/      — 비즈니스 로직
├── domain/       — JPA 엔티티, BaseEntity, Repository 인터페이스
├── dto/          — Request / Response 레코드
└── util/         — 공통 유틸리티 (ValidationUtil 등)
```

Repository 인터페이스는 엔티티와 같은 `domain` 패키지에 위치한다. Repository는 도메인 객체의 컬렉션 개념이므로 도메인 레이어에 속한다.

## Controller

- 모든 핸들러 메서드는 `ResponseEntity<T>`를 반환한다.
- `@ResponseStatus` 사용 금지.
- 상태 코드 기준:
  - 생성: `ResponseEntity.status(201).body(...)`
  - 조회/수정: `ResponseEntity.ok(...)`
  - 삭제: `ResponseEntity.noContent().build()`

```java
// Good
public ResponseEntity<StudentResponse> create(@RequestBody StudentCreateRequest request) {
    return ResponseEntity.status(201).body(studentService.create(request));
}

// Bad
@ResponseStatus(HttpStatus.CREATED)
public StudentResponse create(@RequestBody StudentCreateRequest request) { ... }
```

## Service

- 클래스 레벨에 `@Transactional(readOnly = true)` 선언.
- 쓰기 작업 메서드에만 `@Transactional` 추가.
- 엔티티를 직접 반환하지 않고 DTO로 변환하여 반환.

## DTO

- `record` 사용.
- **Service는 Request DTO를 직접 받지 않는다.** Controller에서 Request를 분해하여 primitive 파라미터 또는 Command 객체로 전달한다.
  - 파라미터 1~3개: primitive 파라미터로 직접 전달
  - 파라미터 4개 이상 또는 복잡한 구조: `service/vo/` 패키지에 Command record를 생성하여 전달
  - Request → Command 변환: Request DTO 내부 `toCommand()` 메서드 사용
  - Entity → Response: `static from(Entity)` 메서드

```java
// Request DTO (복잡한 경우 — Command 변환 메서드 제공)
public record LessonCreateRequest(String title, ...) {
    public LessonCreateCommand toCommand(UserId userId) {
        return new LessonCreateCommand(userId, title, ...);
    }
}

// Response DTO
public record StudentResponse(...) {
    public static StudentResponse from(Student student) { ... }
}
```

```java
// Good — Controller에서 분해 후 Service에 전달 (primitive)
studentCommandService.create(userId, request.name(), request.memo());

// Good — Controller에서 Command로 변환 후 Service에 전달 (복잡한 경우)
lessonCommandService.create(request.toCommand(userId));

// Bad — Service가 Request DTO를 직접 받음
studentCommandService.create(userId, request);
```

## Domain (Entity)

- 생성자 대신 `static create(...)` 팩토리 메서드 사용.
- 필드 변경은 엔티티 내부 메서드(`update`, `updateXxx` 등)로만 처리.
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)`.

## ValidationUtil

입력값 검증은 `ValidationUtil`의 정적 메서드를 사용한다. 검증 실패 시 `IllegalArgumentException`을 던진다.

| 메서드 | 설명 |
|--------|------|
| `checkNotNull(value, message)` | null 여부 검증 |
| `checkNotBlank(value, message)` | null 또는 빈 문자열 여부 검증 |
| `checkMaxLength(value, max, message)` | 최대 길이 검증 |
| `checkPositive(value, message)` | 양수 검증 (0 포함 불가) |
| `checkNotNegative(value, message)` | 0 이상 검증 |
| `checkNotEmpty(collection, message)` | null 또는 빈 컬렉션 여부 검증 |
| `checkArgument(condition, message)` | 임의 조건 검증 |

파라미터 이름은 `Parameter` 상수 클래스를 사용한다.

```java
ValidationUtil.checkNotBlank(request.name(), Parameter.NAME);       // → "name은(는) 비어 있을 수 없습니다."
ValidationUtil.checkMaxLength(request.memo(), 500, Parameter.MEMO); // → "memo은(는) 500자 이하여야 합니다."
ValidationUtil.checkPositive(id, Parameter.ID);                     // → "id은(는) 양수여야 합니다."
```

### 반환값 직접 할당 패턴

`checkNotBlank`, `checkNotNull`, `checkMaxLength` 등의 검증 메서드는 검증을 통과한 값을 반환한다. 엔티티 필드에 직접 할당하자.

```java
// Good — 검증과 할당을 동시에 처리
public void update(String title, LocalDateTime startTime, LocalDateTime endTime) {
    this.title = checkNotBlank(title, TITLE);
    this.startTime = checkNotNull(startTime, START_TIME);
    this.endTime = checkNotNull(endTime, END_TIME);

    checkArgument(this.endTime.isAfter(this.startTime), END_TIME);
}

// Bad — 검증 후 별도로 할당 (불필요한 반복)
public void update(String title, LocalDateTime startTime, LocalDateTime endTime) {
    checkNotBlank(title, TITLE);
    checkNotNull(startTime, START_TIME);
    checkNotNull(endTime, END_TIME);

    this.title = title;
    this.startTime = startTime;
    this.endTime = endTime;
}
```

## 주석
주석은 코드만으로 의도를 파악하기 어려운 경우에만 작성한다.

- 메서드명·변수명으로 충분히 알 수 있는 내용에는 주석을 달지 않는다.
- 복잡한 비즈니스 규칙, 비직관적인 로직, 의도적인 예외 처리 등에만 작성한다.

```java
// Bad — 코드가 이미 설명하고 있음
/** null 여부 검증 */
public static <T> T checkNotNull(T value, String message) { ... }

// Good — 코드만으로 알기 어려운 이유를 설명
// OpenAI API는 빈 문자열을 허용하지 않아 별도 검증 필요
checkNotBlank(keywords, "키워드는 비어 있을 수 없습니다.");
```

## 예외 처리

- 리소스를 찾지 못한 경우 `ResponseStatusException(HttpStatus.NOT_FOUND, ...)` 사용.
