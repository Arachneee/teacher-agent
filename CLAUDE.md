# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AI 기반 선생님 에이전트 서비스. Spring Boot 백엔드와 Next.js 프론트엔드로 구성된 풀스택 프로젝트.

## Architecture

- `backend/` — Spring Boot 4.0 (Java 25), JPA + H2(인메모리), Spring AI(OpenAI gpt-4o-mini) 연동
- `frontend/` — Next.js 16 (App Router), React 19, Tailwind CSS 4, TypeScript
- `doc/` — 프로젝트 문서 (COMMIT.md 등 Claude가 참조해야 할 파일)

백엔드와 프론트엔드는 독립적으로 실행되며 API로 통신한다.

## Commands

### Backend

```bash
cd backend
./gradlew bootRun          # 서버 실행
./gradlew build            # 빌드
./gradlew test             # 전체 테스트
./gradlew test --tests "com.teacher.agent.SomeTest"  # 단일 테스트
```

환경변수 필요: `OPENAI_API_KEY`

H2 콘솔: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:teacher_agent`)

### Frontend

```bash
cd frontend
npm run dev    # 개발 서버
npm run build  # 빌드
npm run lint   # ESLint
```

## General Rules

- `doc/` 내 파일은 해당 작업에 직접 필요할 때만 읽는다.

## Commit Convention

`doc/COMMIT.md` 참조. Conventional Commits 방식 사용.

- 형식: `<type>(<scope>): <subject>`
- `Co-Authored-By: Claude` 줄을 커밋 메시지에 추가하지 않는다.
