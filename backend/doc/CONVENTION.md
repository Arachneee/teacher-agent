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
- **DTO ↔ Domain 변환 로직은 반드시 DTO 내부 팩토리 메서드로 처리한다.** Service에서 직접 변환하지 않는다.
  - Request → Entity: `toEntity()` 메서드
  - Entity → Response: `static from(Entity)` 메서드

```java
// Request DTO
public record StudentCreateRequest(String name, String memo) {
    public Student toEntity() {
        return Student.create(name, memo);
    }
}

// Response DTO
public record StudentResponse(...) {
    public static StudentResponse from(Student student) { ... }
}
```

```java
// Good — Service에서 변환 책임 없음
studentRepository.save(request.toEntity());
StudentResponse.from(student);

// Bad — Service에서 직접 변환
Student.create(request.name(), request.memo());
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
