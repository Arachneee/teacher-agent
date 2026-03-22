# PRD (Product Requirements Document)

## 1. 제품 개요

### 서비스명
Teacher Agent — AI 기반 선생님 보조 서비스

### 목적
초등학교 선생님이 학생 관리, 수업 일정 운영, 학부모 피드백 문자 생성을 효율적으로 처리할 수 있도록 돕는 웹 서비스. AI가 선생님의 키워드 메모를 바탕으로 학부모에게 전달할 문자 문구를 자동 생성한다.

### 대상 사용자
- 초등학교 선생님 (1인 또는 소수 인원 운영 기준)

### 핵심 가치
- 학부모 문자 작성 시간 단축
- 따뜻하고 일관된 톤의 피드백 유지
- 학생별 관찰 메모와 수업 기록의 통합 관리

---

## 2. 기술 스택

| 영역 | 기술 |
|------|------|
| 백엔드 | Spring Boot 4.0, Java 25, Spring Data JPA |
| AI | Spring AI + OpenAI gpt-4o-mini |
| 데이터베이스 | MySQL (운영), H2 인메모리 (테스트) |
| 프론트엔드 | Next.js 16 (App Router), React 19, TypeScript, Tailwind CSS 4 |
| 인프라 | AWS EC2 (백엔드), Vercel (프론트엔드), Terraform |

---

## 3. 도메인 모델

```
Teacher
├── userId (unique)
├── password (BCrypt 해시)
├── name
└── subject (선택, 최대 100자)

Student
├── userId (담당 선생님)
├── name
└── memo (선택, 최대 500자)

Lesson
├── userId (담당 선생님)
├── title
├── startTime / endTime
├── recurrence (반복 설정 — 선택)
│   ├── type: DAILY | WEEKLY | MONTHLY
│   ├── endDate (선택)
│   └── daysOfWeek (WEEKLY일 경우)
├── recurrenceGroupId (반복 그룹 묶음)
└── attendees: Attendee[] (cascade 삭제)

Attendee (Lesson의 하위 엔티티)
└── studentId

Feedback
├── studentId
├── lessonId (studentId와 함께 unique)
├── aiContent (AI 생성 문구, TEXT)
├── liked (좋아요 여부)
└── keywords: FeedbackKeyword[] (cascade 삭제)

FeedbackKeyword (Feedback의 하위 엔티티)
└── keyword (최대 100자)
```

**주요 관계:**
- Teacher 1 : N Student
- Teacher 1 : N Lesson
- Lesson 1 : N Attendee (← Student ID 참조)
- (Student, Lesson) 쌍 당 Feedback 1개
- Feedback 1 : N FeedbackKeyword

---

## 4. 기능 요구사항

### 4-1. 인증

| 항목 | 내용 |
|------|------|
| 방식 | HTTP 세션 (JSESSIONID 쿠키) |
| 로그인 | POST `/auth/login` — userId + password |
| 로그아웃 | POST `/auth/logout` |
| 현재 사용자 | GET `/auth/me` |
| 비밀번호 | BCrypt 해시 저장 |
| 최초 계정 | 서버 기동 시 환경변수(`INITIAL_TEACHER_USER_ID`, `INITIAL_TEACHER_PASSWORD`)로 관리자 계정 자동 생성 |

### 4-2. 선생님 프로필

- 이름, 담당 과목 조회 및 수정
- GET/PUT `/teachers/me`

### 4-3. 학생 관리

| 기능 | 설명 |
|------|------|
| 등록 | 이름 + 메모(선택) |
| 목록 | 해당 선생님의 전체 학생 목록 |
| 수정 | 이름, 메모 변경 |
| 삭제 | 학생 및 연관 데이터 삭제 |

**제약사항:**
- 학생 메모는 500자 이하

### 4-4. 수업 일정 관리

#### 캘린더 뷰
- 주간 달력 (월요일 시작)
- 이전/다음 주 이동, "오늘" 버튼, 날짜 선택

#### 수업 생성
- 제목, 시작/종료 시간 필수
- 반복 설정 선택 가능: 매일 / 매주(요일 선택) / 매월
- 반복 종료일 설정 선택 가능
- 생성 시 학생 자동 추가 옵션

#### 수업 수정 / 삭제
반복 수업의 경우 수정·삭제 범위 선택:
- `SINGLE` — 이 수업만
- `THIS_AND_FOLLOWING` — 이 수업과 이후 수업
- `ALL` — 전체 반복 수업

### 4-5. 수업 상세 (출석 관리)

- 수업에 학생 추가 / 제거
- 학생 카드 드래그 앤 드롭으로 순서 변경
- 그리드 열 수 조정 (1~4열)
- 각 학생 카드: 아바타, 이름, 등록일, 메모 표시

### 4-6. 피드백 시스템 (핵심 기능)

#### 키워드 관리
- 학생 관찰 키워드 추가 (예: `성실함`, `주간테스트 90점`, `집중력 부족`)
- 키워드 수정 / 삭제
- 키워드 1개 최대 100자, 개수 제한 없음

#### AI 학부모 문자 생성
- 키워드 기반으로 학부모에게 보낼 문자 문구 자동 생성
- 생성 문구 길이: 150~250자
- 재생성 기능
- 클립보드 복사 (카카오톡 등에 바로 붙여넣기)
- 수동 편집 가능
- 좋아요(♥) 표시로 마음에 드는 문구 보관

#### AI 생성 규칙 (프롬프트 적용)
- 경어체 (`습니다`/`입니다`) 사용. 비격식체 금지
- 구조: 이름 호칭 → 칭찬 → 학습 상황 안내 (마무리 인사 없음)
- 이름 뒤 조사: 받침 유무에 따라 `은/는`, `이/가` 자동 적용
- 긍정 키워드 → 칭찬, 부정 키워드 → 성장 관점의 부드러운 표현
- 학부모에게 학습 지도 요청 금지; 칭찬 요청만 허용
- `가정` 키워드 있을 경우 가정 협조 요청 가능

---

## 5. API 엔드포인트 목록

### 인증
| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/auth/login` | 로그인 |
| POST | `/auth/logout` | 로그아웃 |
| GET | `/auth/me` | 현재 사용자 정보 |

### 선생님
| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/teachers/me` | 프로필 조회 |
| PUT | `/teachers/me` | 프로필 수정 |

### 학생
| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/students` | 학생 등록 |
| GET | `/students` | 전체 학생 목록 |
| GET | `/students/{id}` | 학생 상세 |
| PUT | `/students/{id}` | 학생 수정 |
| DELETE | `/students/{id}` | 학생 삭제 |

### 수업
| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/lessons` | 수업 생성 |
| GET | `/lessons?weekStart={date}` | 주간 수업 목록 |
| GET | `/lessons/{id}` | 수업 상세 |
| GET | `/lessons/{id}/detail` | 수업 + 출석 + 피드백 상세 |
| PUT | `/lessons/{id}` | 수업 수정 |
| DELETE | `/lessons/{id}?scope={scope}` | 수업 삭제 |

### 출석 (수업 내 학생)
| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/lessons/{lessonId}/attendees` | 학생 추가 |
| GET | `/lessons/{lessonId}/attendees` | 출석 목록 |
| DELETE | `/lessons/{lessonId}/attendees/{attendeeId}` | 학생 제거 |

### 피드백
| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/feedbacks` | 피드백 생성 |
| GET | `/feedbacks?studentId={id}` | 학생 피드백 목록 |
| GET | `/feedbacks/{id}` | 피드백 상세 |
| PATCH | `/feedbacks/{id}` | AI 문구 수동 수정 |
| DELETE | `/feedbacks/{id}` | 피드백 삭제 |
| POST | `/feedbacks/{id}/generate` | AI 문자 생성 |
| POST | `/feedbacks/{id}/like` | 좋아요 토글 |
| POST | `/feedbacks/{id}/keywords` | 키워드 추가 |
| PUT | `/feedbacks/{id}/keywords/{keywordId}` | 키워드 수정 |
| DELETE | `/feedbacks/{id}/keywords/{keywordId}` | 키워드 삭제 |

---

## 6. 비기능 요구사항

| 항목 | 내용 |
|------|------|
| 인증 | 세션 기반, 미인증 요청 시 401 반환 |
| CORS | `http://localhost:3000`, `https://*.vercel.app` 허용 (credentials 포함) |
| CSRF | 비활성화 (REST API) |
| 데이터 격리 | 모든 데이터는 로그인한 선생님의 `userId` 기준으로 격리 |
| 환경변수 | `OPENAI_API_KEY`, `INITIAL_TEACHER_USER_ID`, `INITIAL_TEACHER_PASSWORD` 필수 |

---

## 7. 주요 UX 흐름

### 수업 생성 및 출석 관리
1. 홈(주간 캘린더)에서 `+` 버튼 클릭
2. 수업 모달: 제목, 시간, 반복 설정 입력
3. 학생 자동 추가 옵션 선택 후 저장
4. 캘린더에서 수업 클릭 → 수업 상세 페이지
5. 학생 카드 드래그 앤 드롭으로 순서 조정

### AI 학부모 문자 생성
1. 수업 상세 페이지에서 학생 카드 확인
2. 학생 관찰 키워드 입력 (예: `성실함`, `주간테스트 90점`)
3. `AI 학부모 문자 생성` 버튼 클릭
4. AI 생성 문구 확인
5. 마음에 들면 클립보드 복사 → 카카오톡 등으로 전송
6. 수정이 필요하면 재생성 또는 직접 편집
7. 좋은 문구는 ♥로 보관

---

## 8. 향후 고려사항

- 다중 선생님 계정 (현재 단일 관리자 계정 구조)
- 학부모 포털 연동 (직접 알림 발송)
- 피드백 히스토리 및 통계 대시보드
- 학생별 성장 추이 시각화
