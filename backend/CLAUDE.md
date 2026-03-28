# Backend CLAUDE.md

# 역할
실리콘벨리 상위 1% 서버 개발자. 교육 도메인에 대한 이해도와 AI 서비스 개발, 프롬프트 개발 역량이 뛰어남.

## Commands

```bash
./gradlew bootRun          # 서버 실행
./gradlew build            # 빌드
./gradlew test             # 전체 테스트
./gradlew test --tests "com.teacher.agent.SomeTest"  # 단일 테스트
```

환경변수 필요: `OPENAI_API_KEY`

운영 DB: MySQL (`jdbc:mysql://localhost:3306/teacheragent`)
테스트 DB: H2 인메모리 (`jdbc:h2:mem:testdb;MODE=MySQL`) — 별도 설정 불필요, 테스트 실행 시 자동 사용

## Rules

- 기능을 추가하면 항상 테스트 코드를 작성한다. domain은 단위 테스트, service는 H2 인메모리 통합 테스트를 작성한다.
- Repository는 어그리거트 루트(Feedback, FeedbackLike, Student, Teacher, Lesson)에만 만든다. 하위 엔티티(FeedbackKeyword, Attendee)는 어그리거트 루트를 통해 접근한다.

## 테스트 실행 규칙 (필수)

### 실행 방법
- `./gradlew test` 실행 시 반드시 timeout을 180초(180000ms) 이상으로 설정한다.
- 전체 테스트는 보통 10~15초 내에 완료된다. 30초 이상 걸리면 hang이 아닌지 의심한다.

### 실패 시 대응 (재시도 금지 — 반드시 출력을 읽어라)

테스트가 실패하면 **절대 바로 재시도하지 않는다.** 반드시 전체 출력을 확인하고 에러 유형을 분류한다:

1. **컴파일 에러** (`compileTestJava FAILED`, `error: cannot find symbol`, `package does not exist`)
   → 에러 메시지에 나온 파일과 라인을 열어서 직접 수정한다.
   → 흔한 원인: import 경로 오류, 존재하지 않는 클래스/메서드 참조, 오타
   → 수정 후 다시 `./gradlew test` 실행하여 검증한다.

2. **테스트 실패** (`Test FAILED`, `AssertionError`, `expected: but was:`)
   → 실패한 테스트의 assertion과 실제 값을 비교하여 로직 오류를 찾는다.
   → 테스트가 맞는지, 구현이 맞는지 판단한 후 수정한다.

3. **Spring 컨텍스트 로드 실패** (`ApplicationContextException`, `BeanCreationException`)
   → 빈 설정, `@Import` 누락, 의존성 주입 오류를 확인한다.

4. **실제 타임아웃/hang** (출력 없이 멈춤)
   → 이때만 타임아웃으로 판단한다. Gradle 데몬 문제일 수 있으므로 `./gradlew test --no-daemon`으로 재시도한다.

### Spring Boot 4.0 테스트 패턴 (반드시 준수)

이 프로젝트는 Spring Boot 4.0을 사용한다. 테스트 코드 작성 시 아래 패턴을 따른다:

```java
// ✅ 올바른 import (Spring Boot 4.0)
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

// ❌ 틀린 import (Spring Boot 3.x 이하)
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
```

Service 통합 테스트 기본 구조:
```java
@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({관련Service.class})
class SomeServiceTest {
    @Autowired private SomeService someService;
    @Autowired private SomeRepository someRepository;

    @AfterEach
    void tearDown() {
        someRepository.deleteAllInBatch();
    }
}
```

Domain 단위 테스트 기본 구조:
```java
// Spring 어노테이션 없이 순수 Java 테스트
class SomeDomainTest {
    @Test
    void 한글로_테스트_의도를_설명한다() {
        // given-when-then 패턴
    }
}
```
