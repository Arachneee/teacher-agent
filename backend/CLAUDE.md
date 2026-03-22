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
