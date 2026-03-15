# 인증/인가 정책

## 개요

HTTP Session 기반 인증을 사용한다. JWT를 사용하지 않으며, 로그인 성공 시 서버 세션에 인증 정보를 저장하고 브라우저 쿠키(`JSESSIONID`)로 세션을 유지한다.

## 사용자 모델

현재 단일 역할(`TEACHER`)만 존재한다. 별도의 권한 테이블 없이 모든 인증된 사용자는 동일한 권한을 가진다.

```
Teacher
├── id         (PK, auto-increment)
├── username   (unique)
├── password   (BCrypt 해시)
├── createdAt
└── updatedAt
```

## 인증 흐름

```
POST /auth/login
  ↓
AuthenticationManager.authenticate(username, password)
  ↓
TeacherUserDetailsService.loadUserByUsername(username)
  → DB에서 Teacher 조회 → UserDetails(username, encodedPassword, role=TEACHER) 반환
  ↓
BCrypt 비밀번호 검증
  ↓
SecurityContext 생성 → HttpSession에 저장
  ↓
응답: { username } + Set-Cookie: JSESSIONID=...
```

이후 요청마다 브라우저가 `JSESSIONID` 쿠키를 전송하면 Spring Security가 세션에서 인증 정보를 복원한다.

## API 엔드포인트

| 메서드 | 경로 | 인증 필요 | 설명 |
|--------|------|-----------|------|
| POST | `/auth/login` | 불필요 | 로그인, 세션 발급 |
| POST | `/auth/logout` | 불필요 | 로그아웃, 세션 무효화 |
| GET | `/auth/me` | 불필요* | 현재 로그인 사용자 정보 |
| * | `/api/**` 외 나머지 | 필요 | 모든 비즈니스 API |

> `/auth/me`는 `permitAll`이지만, 미인증 상태면 `Authentication`이 null이므로 401을 반환한다.

### 요청/응답

**POST /auth/login**
```json
// Request
{ "username": "admin", "password": "secret" }

// Response 200
{ "username": "admin" }

// Response 401
인증 실패 시 Spring Security가 자동 처리
```

**GET /auth/me**
```json
// Response 200 (인증됨)
{ "username": "admin" }

// Response 401 (미인증)
```

**POST /auth/logout**
```
// Response 204 No Content
세션 무효화 + SecurityContext 초기화
```

## 인가 정책

Spring Security 설정(`SecurityConfig.java`):

```
/auth/**       → 인증 없이 허용 (permitAll)
/h2-console/** → 인증 없이 허용 (로컬 프로파일만)
그 외 모든 경로 → 인증 필요 (anyRequest().authenticated())
```

역할 기반 접근 제어(RBAC)는 적용되지 않는다. 인증된 사용자는 모두 동일한 권한을 가진다.

## 비밀번호 정책

- 해시 알고리즘: **BCrypt**
- 평문 비밀번호는 저장하지 않는다.
- 비밀번호 변경/재설정 기능은 현재 없다.

### 초기 계정

서버 기동 시 `DataInitializer`가 실행되어 초기 선생님 계정을 생성한다.

| 환경변수 | 기본값 | 설명 |
|----------|--------|------|
| `INITIAL_TEACHER_USERNAME` | `admin` | 초기 계정 아이디 |
| `INITIAL_TEACHER_PASSWORD` | `changeme` | 초기 계정 비밀번호 |

계정이 이미 존재하면 비밀번호를 환경변수 값으로 **업데이트**한다. 운영 환경에서는 `INITIAL_TEACHER_PASSWORD`를 반드시 강력한 값으로 설정해야 한다.

## CORS 정책

`allowCredentials: true`로 설정되어 있어 쿠키 기반 세션이 cross-origin 요청에서도 동작한다.

| 프로파일 | 허용 오리진 |
|----------|-------------|
| 로컬 (기본) | `http://localhost:3000` |
| prod | `https://*.vercel.app` |

- 허용 메서드: `GET, POST, PUT, DELETE, OPTIONS`
- 허용 헤더: `*`

## 보안 설정 요약

| 항목 | 설정값 | 비고 |
|------|--------|------|
| CSRF | 비활성화 | REST API이므로 불필요 |
| Form Login | 비활성화 | 커스텀 `/auth/login` 사용 |
| HTTP Basic | 비활성화 | 세션 방식으로 대체 |
| 세션 저장소 | `HttpSessionSecurityContextRepository` | Spring Security 기본 |
| H2 Console | 로컬 프로파일에서만 허용 | `frameOptions` 비활성화 포함 |
