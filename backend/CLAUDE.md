# Backend CLAUDE.md

## Commands

```bash
./gradlew bootRun          # 서버 실행
./gradlew build            # 빌드
./gradlew test             # 전체 테스트
./gradlew test --tests "com.teacher.agent.SomeTest"  # 단일 테스트
```

환경변수 필요: `OPENAI_API_KEY`

H2 콘솔: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:teacher_agent`)

## Rules

- 기능을 추가하면 항상 테스트 코드를 작성한다. domain은 단위 테스트, service는 H2 인메모리 통합 테스트를 작성한다.
