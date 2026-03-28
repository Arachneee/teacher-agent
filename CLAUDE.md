# CLAUDE.md (프로젝트 문서 허브)

이 파일은 이 리포지토리에서 코드를 작업할 때 AI 에이전트(Claude Code 및 Gemini 등)를 위한 지침을 제공합니다. 이 파일은 프로젝트 문서의 중앙 허브 역할을 합니다.

## 프로젝트 개요
AI 기반 선생님 에이전트 서비스. Spring Boot 백엔드와 Next.js 프론트엔드로 구성된 풀스택 프로젝트.

## 아키텍처
- `backend/` — Spring Boot 4.0 (Java 25), JPA + MySQL (테스트는 H2 인메모리), Spring AI(OpenAI gpt-4o-mini) 연동
- `frontend/` — Next.js 16 (App Router), React 19, Tailwind CSS 4, TypeScript
- `infra/` — Terraform 기반 AWS 인프라 (EC2, 보안 그룹 등)
- `doc/` — 프로젝트 문서 (COMMIT.md 등 Claude가 참조해야 할 파일)

백엔드와 프론트엔드는 독립적으로 실행되며 API로 통신한다.

## 일반 규칙
- `doc/` 내 파일은 해당 작업에 직접 필요할 때만 읽는다.
- 코드 수정 사항은 관련 context(CLAUDE.md 등)에도 반영한다.
- 코드를 수정할 때마다 `doc/ARCHITECTURE.md`를 확인하고, 변경 사항이 아키텍처 문서에 기술된 내용(디렉토리 구조, 도메인 모델, API 엔드포인트, 서비스 목록, 설정, 컨벤션 등)에 영향을 주면 해당 섹션을 함께 업데이트한다.

## 코딩 컨벤션
- 변수명에 약어를 사용하지 않는다. 예: `fk` → `feedbackKeyword`, `req` → `request`

## 커밋 컨벤션
커밋 전 반드시 `doc/COMMIT.md`를 읽는다. Conventional Commits 방식 사용.

- 형식: `<type>(<scope>): <subject>`
- `Co-Authored-By: Claude` 줄을 커밋 메시지에 추가하지 않는다.

---

## Markdown 파일 인덱스

이 섹션은 리포지토리 내의 다른 중요한 Markdown 문서들에 대한 개요와 링크를 제공합니다.

### 최상위 레벨 문서
-   **`GEMINI.md`**: (CLAUDE.md에 대한 심볼릭 링크) - Gemini CLI를 위한 중앙 문서 허브.
-   **`CLAUDE.md`**: (이 파일) - AI 에이전트 지침 및 프로젝트 문서 인덱스.

### 백엔드 문서 (`backend/`)
-   **`backend/CLAUDE.md`**: 백엔드 작업 시 Claude Code를 위한 특정 지침.
-   **`backend/doc/CONVENTION.md`**: 패키지 구조, 컨트롤러/서비스/도메인 디자인, DTO 사용, 유효성 검사, 예외 처리, JPA 및 주석 지침을 포함한 백엔드 개발 컨벤션을 설명합니다.
-   **`backend/src/main/resources/prompts/feedback_message.md`**: 백엔드 AI 통합에서 사용될 가능성이 있는 피드백 관련 프롬프트 내용을 포함합니다.

### 프론트엔드 문서 (`frontend/`)
-   **`frontend/README.md`**: Next.js 프론트엔드 프로젝트의 설정, 로컬 개발 및 배포 가이드입니다. 빌드 명령, 학습 자료 및 Vercel 배포를 다룹니다.
-   **`frontend/CLAUDE.md`**: 프론트엔드 작업 시 Claude Code를 위한 특정 지침.
-   **`frontend/doc/DESIGN.md`**: 프론트엔드 디자인 문서.

### 인프라 문서 (`infra/`)
-   **`infra/doc/deploy-backend.md`**: Spring Boot 백엔드를 AWS EC2에 배포하기 위한 가이드로, 인프라 설정, CI/CD 및 수동 배포 단계를 포함합니다.
-   **`infra/doc/deploy-frontend.md`**: Vercel에 Next.js 프론트엔드를 배포하기 위한 가이드로, 환경 변수, API 프록시 구성 및 자동 배포 프로세스를 포함합니다.

### 일반 프로젝트 문서 (`doc/`)
-   **`doc/ARCHITECTURE.md`**: 프로젝트 전체 아키텍처 + 제품 요구사항 통합 문서. 다른 AI가 이 문서만 보고 동일한 프로젝트를 재현할 수 있도록 작성됨.
-   **`doc/AUTH.md`**: 사용자 모델, 인증 흐름, API 엔드포인트 및 보안 구성을 포함하여 백엔드의 인증 및 인가 정책을 자세히 설명합니다.
-   **`doc/COMMIT.md`**: Conventional Commits를 따르는 프로젝트의 커밋 메시지 컨벤션을 정의합니다.

---
*참고: 일부 `.md` 파일은 다른 하위 디렉토리에 존재하거나 여기에 완전히 자세히 설명되지 않은 특정 목적을 가질 수 있습니다. 가장 정확한 정보를 위해서는 항상 관련 파일을 참조하십시오.*
