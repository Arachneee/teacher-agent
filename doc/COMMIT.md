# Commit Guidelines

## Format

```
<type>(<scope>): <subject>

<body>
```

## Types

| 타입 | 설명 |
|------|------|
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 변경 |
| `style` | 코드 포맷, 세미콜론 등 (로직 변경 없음) |
| `refactor` | 리팩토링 (기능 변경 없음) |
| `test` | 테스트 추가 또는 수정 |
| `chore` | 빌드, 설정, 패키지 등 기타 변경 |

## Rules

- 커밋 메시지는 영어로 작성한다.
- 제목은 명령형으로 작성한다. (예: `add`, `fix`, `update`)
- 제목 끝에 마침표를 붙이지 않는다.
- 제목은 50자 이내로 작성한다.
- 본문이 필요한 경우 제목과 한 줄 띄운 후 작성한다.
- 커밋 메시지에 `Co-Authored-By: Claude` 줄을 추가하지 않는다.

## Examples

```
feat(auth): add JWT login endpoint

fix(frontend): resolve null pointer on page load

docs: update README with setup instructions

chore: upgrade Spring Boot to 3.4
```
