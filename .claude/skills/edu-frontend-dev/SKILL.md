---
name: edu-frontend-dev
description: >
  교육 도메인 프론트엔드 개발자 에이전트. Teacher Agent 프로젝트의 Next.js 프론트엔드 개발 전반을 담당한다.
  모바일 퍼스트 반응형 설계, 커스텀 컴포넌트 시스템, React 훅 패턴, API 레이어, Tailwind CSS 디자인 시스템을 수행한다.
  사용자가 "프론트엔드", "UI", "UX", "컴포넌트", "페이지", "모바일", "반응형", "디자인", "스타일",
  "Tailwind", "Next.js", "React", "훅", "hook", "화면", "레이아웃", "모달", "카드", "버튼",
  "frontend", "프론트 개발", "화면 추가", "UI 수정", "CSS", "애니메이션", "드래그", "dnd-kit" 등을
  언급하면 이 skill을 사용할 것.
---

# 교육 도메인 프론트엔드 개발자

## 역할

모바일 퍼스트 사고방식을 가진 시니어 프론트엔드 개발자.
사용자(선생님)가 수업 전후로 빠르게 사용하는 도구를 만든다.
불필요한 복잡함 없이 의도가 바로 보이는 UI를 설계하고, 유지보수하기 좋은 코드를 작성한다.

## 기술 스택

| 레이어 | 기술 |
|--------|------|
| 프레임워크 | Next.js 16 (App Router), React 19 |
| 언어 | TypeScript 5 |
| 스타일 | Tailwind CSS 4 (PostCSS 플러그인 방식: `@tailwindcss/postcss`) |
| 드래그드롭 | dnd-kit |
| 폰트 | Geist (next/font) |
| 배포 | Vercel |

## 프로젝트 구조

```
frontend/src/app/
├── layout.tsx                    — 루트 레이아웃 (Geist 폰트, AuthProvider)
├── globals.css                   — 글로벌 스타일
├── login/page.tsx                — 로그인 페이지
├── (app)/                        — 인증 보호 영역
│   ├── layout.tsx                — 앱 레이아웃 (Sidebar + BottomNav + 인증 가드)
│   ├── page.tsx                  — 메인 (리다이렉트)
│   ├── calendar/page.tsx         — 주간 캘린더 뷰
│   ├── intro/page.tsx            — 인트로 페이지
│   ├── students/page.tsx         — 학생 관리
│   ├── students/[id]/page.tsx    — 학생 상세
│   └── lessons/[lessonId]/page.tsx — 수업 상세
├── api/                          — API Route (스트리밍 프록시)
├── components/                   — UI 컴포넌트
├── hooks/                        — 커스텀 훅
├── context/                      — React Context (AuthContext)
├── lib/                          — API 레이어, 유틸리티, 상수
└── types/                        — TypeScript 타입 정의
```

---

## 핵심 원칙

### 1. 모바일 퍼스트 반응형 설계

모든 UI는 모바일 화면을 먼저 설계하고, 데스크톱으로 확장한다.

```tsx
// 모바일 기본 → md: 이상에서 데스크톱 레이아웃
<div className="flex flex-col md:flex-row">
  <div className="w-full md:w-1/3">사이드바</div>
  <div className="flex-1">메인 콘텐츠</div>
</div>

// 모바일: 하단 네비게이션, 데스크톱: 사이드바
<Sidebar />          {/* md: 이상에서만 표시 */}
<BottomNav />        {/* md: 미만에서만 표시 */}

// 모바일 하단 여백 확보 (BottomNav 겹침 방지)
<div className="flex-1 min-w-0 pb-16 md:pb-0">
  {children}
</div>
```

규칙:
- 터치 타겟은 최소 44x44px (모바일 접근성).
- 모바일에서 가로 스크롤이 발생하지 않도록 `min-w-0`, `overflow-hidden` 활용.
- 그리드 칼럼 수는 화면 크기에 따라 조정 (`grid-cols-1 md:grid-cols-2 lg:grid-cols-3`).
- `useIsMobile` 훅으로 모바일 여부를 감지하여 조건부 렌더링.

### 2. 의미가 보이는 UI

사용자가 버튼/요소를 보는 순간 용도를 파악할 수 있어야 한다.

- 아이콘보다 텍스트 레이블을 우선한다 (아이콘 단독 사용 지양).
- 레이블은 동사 + 목적어 형태로 명확하게 쓴다 (예: "추가하기", "저장", "복사").
- 상태 변화가 있는 버튼은 상태를 반영한 레이블을 사용한다 (예: "복사됨 ✓").

### 3. 뎁스 최소화

```
보기 → 바로 행동 → 결과 확인
```

- 삭제처럼 되돌릴 수 없는 동작만 confirm 또는 추가 확인 단계를 둔다.
- 그 외 수정/저장 등은 인라인으로 처리한다 (페이지 이동 없이 카드 내부에서 편집).
- 단순 입력 후 바로 결과가 보여야 한다 (피드백 생성 → 같은 화면에 결과 표시).

---

## 디자인 시스템

### 색상 팔레트

| 용도 | Tailwind 클래스 |
|------|-----------------|
| 페이지 배경 | `bg-gradient-to-br from-purple-50 via-pink-50 to-orange-50` |
| 카드 배경 | `bg-white` |
| 주 포인트 | `pink-400` |
| 보조 포인트 | `purple-400` |
| 입력 필드 배경 | `bg-purple-50` |
| 위험 동작 | `rose-400` |
| 일반 텍스트 | `text-gray-800` |
| 보조 텍스트 | `text-gray-400` |

### 컴포넌트 스타일 규칙

**카드:**
```tsx
<div className="bg-white rounded-3xl p-6 shadow-sm hover:shadow-md transition-all duration-200">
  {/* 카드 내부 편집은 인라인 전환 (별도 페이지/모달 없음) */}
</div>
```

**버튼:**
```tsx
// 주요 액션
<button className="bg-pink-400 hover:bg-pink-500 text-white rounded-2xl px-4 py-2">
  추가하기 ✨
</button>

// 보조 액션
<button className="bg-gray-100 hover:bg-gray-200 text-gray-600 rounded-2xl px-4 py-2">
  취소
</button>

// 위험 액션 (배경 연하게, 눈에 덜 띄게)
<button className="bg-rose-50 hover:bg-rose-100 text-rose-400 rounded-2xl px-4 py-2">
  삭제
</button>

// FAB (플로팅 액션 버튼)
<button className="fixed bottom-20 right-4 md:bottom-6 w-14 h-14 rounded-full bg-pink-400 hover:bg-pink-500 text-white shadow-lg">
  +
</button>
```

**입력 필드:**
```tsx
<input className="w-full bg-purple-50 rounded-2xl px-3 py-2 outline-none focus:ring-2 focus:ring-purple-300 text-sm text-gray-700 placeholder:text-gray-300" />

// 에러 상태
<input className="... focus:ring-rose-300 ring-2 ring-rose-300" />
```

**모달:**
```tsx
<div className="fixed inset-0 bg-black/30 backdrop-blur-sm z-50 flex items-center justify-center">
  <div className="bg-white rounded-3xl p-6 max-w-md w-full mx-4">
    {/* 모달 바깥 클릭으로 닫기 지원 */}
  </div>
</div>
```

### 네이티브 입력 금지

`<input type="date">`, `<input type="time">`, `<select>` 요소를 직접 사용하지 않는다.
반드시 커스텀 컴포넌트를 사용한다:

| 용도 | 컴포넌트 | 경로 |
|------|----------|------|
| 날짜 선택 | `DatePicker` | `components/DatePicker.tsx` |
| 시간 선택 | `TimePicker` | `components/TimePicker.tsx` |
| 드롭다운 선택 | `CustomSelect` | `components/CustomSelect.tsx` |
| 학년 선택 | `GradeSelect` | `components/GradeSelect.tsx` |

모든 드롭다운은 `fixed` 포지셔닝으로 모달 내부에서도 잘리지 않는다.
화면 하단 공간이 부족하면 자동으로 위쪽으로 열린다.

### 이모지 사용 지침

이모지는 레이블의 의미를 직관적으로 전달하는 보조 수단으로만 사용한다.

- 페이지 헤더: 서비스 성격을 나타내는 이모지 1개 (예: 🍎)
- 버튼: 텍스트와 함께 사용 (예: "추가하기 ✨", "복사 📋")
- 아이콘 전용 버튼: 의미가 명확한 경우에만 허용 (예: 🗑️ 삭제, ✏️ 수정)

---

## 상태 관리

### React Context + useState (외부 라이브러리 없음)

```tsx
// context/AuthContext.tsx — 전역 인증 상태
'use client';

import { createContext, useContext, useEffect, useState, ReactNode } from 'react';

interface AuthContextType {
  user: AuthResponse | null;
  loading: boolean;
  setUser: (user: AuthResponse | null) => void;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getMe()
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  // ...
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}
```

규칙:
- 전역 상태는 React Context만 사용한다. Redux, Zustand 등 외부 라이브러리를 추가하지 않는다.
- 컴포넌트 로컬 상태는 `useState`를 사용한다.
- 복잡한 상태 로직은 커스텀 훅으로 분리한다.

---

## 커스텀 훅 패턴

비즈니스 로직은 컴포넌트에서 분리하여 커스텀 훅으로 관리한다.

| 훅 | 역할 |
|----|------|
| `useFeedback` | 피드백 상태: 키워드 CRUD, AI 생성(스트리밍), 좋아요, 디바운스 업데이트(1초) |
| `useLessonDetail` | 수업 상세 fetch, Attendee 타입 매핑 |
| `useLessonEdit` | 수업 수정 폼 상태, 저장 (scope 지원) |
| `useGridLayout` | 수강생 그리드: 칼럼 수(1-6), 슬롯 순서, localStorage 영속화 |
| `useDropdown` | 드롭다운 위치: fixed 포지션, 자동 위로 열기, 외부 클릭 닫기 |
| `useIsMobile` | 모바일 반응형 감지 |

### 훅 작성 규칙

```tsx
// hooks/useFeedback.ts
'use client';

import { useCallback, useEffect, useRef, useState } from 'react';

export function useFeedback(studentId: number, initialFeedback?: Feedback | null) {
  const [feedback, setFeedback] = useState<Feedback | null>(initialFeedback ?? null);
  const [aiGenerating, setAiGenerating] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // 낙관적 업데이트 패턴: UI 먼저 반영 → API 호출 → 실패 시 롤백
  const handleRemoveKeyword = async (keywordId: number) => {
    if (!feedback) return;
    const previousFeedback = feedback;  // 롤백용 스냅샷
    setFeedback(prev =>
      prev ? { ...prev, keywords: prev.keywords.filter(k => k.id !== keywordId) } : null
    );
    try {
      await removeKeyword(feedback.id, keywordId);
      setFeedback(await reloadFeedback(feedback.id));
    } catch {
      setErrorMessage('키워드를 삭제하지 못했어요');
      setFeedback(previousFeedback);  // 롤백
    }
  };

  // 디바운스 패턴: 1초 후 서버 저장
  const handleUpdateAiContent = (content: string) => {
    setFeedback(prev => prev ? { ...prev, aiContent: content || null } : null);
    if (debounceTimerRef.current) clearTimeout(debounceTimerRef.current);
    debounceTimerRef.current = setTimeout(async () => {
      await updateFeedback(feedbackId, content);
    }, 1000);
  };

  return { feedback, aiGenerating, errorMessage, handleRemoveKeyword, handleUpdateAiContent, ... };
}
```

규칙:
- 훅 파일 최상단에 `'use client'` 선언.
- 낙관적 업데이트: UI를 먼저 반영하고, API 실패 시 이전 상태로 롤백한다.
- 디바운스: 사용자 입력은 1초 디바운스 후 서버에 저장한다.
- 에러 메시지는 한국어 친근한 톤으로 작성한다 (예: "키워드를 삭제하지 못했어요").

---

## API 레이어

### lib/api.ts — 중앙 API 모듈

```tsx
const BASE_URL = '/api';  // Next.js rewrite로 백엔드 프록시

// 공통 fetch 래퍼: credentials + 401 자동 리다이렉트
async function fetchWithAuth(url: string, options: RequestInit = {}): Promise<Response> {
  const res = await fetch(url, { credentials: 'include', ...options });
  if (res.status === 401) {
    if (typeof window !== 'undefined') {
      window.location.href = '/login';
    }
    throw new Error('세션이 만료됐어요. 다시 로그인해주세요.');
  }
  return res;
}

// API 함수: 타입드 파라미터 + 반환값
export async function getStudents(): Promise<Student[]> {
  const res = await fetchWithAuth(`${BASE_URL}/students`);
  if (!res.ok) throw new Error('학생 목록을 불러오지 못했어요');
  return res.json();
}
```

규칙:
- 모든 API 함수는 `fetchWithAuth`를 사용한다 (인증 API 제외).
- 에러 메시지는 한국어 친근한 톤 (예: "~하지 못했어요").
- 반환 타입을 명시한다 (`Promise<Student[]>`).
- `fetch` API를 직접 사용한다. axios 등 외부 라이브러리를 추가하지 않는다.
- 스트리밍 API는 `ReadableStream` + `TextDecoder`로 처리한다.

### 스트리밍 패턴

```tsx
export async function streamAiContent(feedbackId: number, onChunk: (chunk: string) => void): Promise<void> {
  const res = await fetchWithAuth(`${BASE_URL}/feedbacks/${feedbackId}/generate/stream`);
  if (!res.ok) throw new Error('AI 문자를 생성하지 못했어요');

  const reader = res.body!.getReader();
  const decoder = new TextDecoder();

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    onChunk(decoder.decode(value));
  }
}
```

---

## 타입 정의

### types/api.ts — 백엔드 API 응답 타입

```tsx
export type SchoolGrade =
  | 'ELEMENTARY_1' | 'ELEMENTARY_2' | 'ELEMENTARY_3'
  | 'ELEMENTARY_4' | 'ELEMENTARY_5' | 'ELEMENTARY_6'
  | 'MIDDLE_1' | 'MIDDLE_2' | 'MIDDLE_3'
  | 'HIGH_1' | 'HIGH_2' | 'HIGH_3';

export interface Student {
  id: number;
  name: string;
  memo: string;
  grade: SchoolGrade | null;
  createdAt: string;
  updatedAt: string;
}

export type UpdateScope = 'SINGLE' | 'THIS_AND_FOLLOWING' | 'ALL';
```

규칙:
- 백엔드 응답과 1:1 매핑되는 타입을 `types/api.ts`에 정의한다.
- `lib/api.ts`에서 re-export하여 컴포넌트에서는 `lib/api`에서 import한다.
- enum 대신 union type을 사용한다 (`type SchoolGrade = 'ELEMENTARY_1' | ...`).
- 날짜는 `string` (ISO 8601)으로 받고, 표시 시 유틸리티 함수로 변환한다.

---

## 컴포넌트 작성 규칙

### Props 인터페이스

```tsx
// 컴포넌트 파일 내부에 Props 인터페이스 정의
interface Props {
  attendee: Attendee;
  lessonId: number;
  isRecurring: boolean;
  onUpdate: () => void;
  onRemove: (attendeeId: number) => void;
}

export default function AttendeeCard({ attendee, lessonId, isRecurring, onUpdate, onRemove }: Props) {
  // ...
}
```

### forwardRef 패턴 (외부에서 내부 메서드 호출 필요 시)

```tsx
export interface AttendeeCardHandle {
  focusKeywordInput: () => void;
}

const AttendeeCard = forwardRef<AttendeeCardHandle, Props>((props, ref) => {
  const keywordInputRef = useRef<HTMLInputElement>(null);
  useImperativeHandle(ref, () => ({
    focusKeywordInput: () => keywordInputRef.current?.focus(),
  }));
  // ...
});

export default AttendeeCard;
```

### 파일 구조

- 모든 컴포넌트 파일 최상단에 `'use client'` 선언 (클라이언트 컴포넌트).
- 한 파일에 한 컴포넌트 (default export).
- Props 인터페이스는 컴포넌트 파일 내부에 정의한다 (별도 파일 분리 안 함).
- 이벤트 핸들러 네이밍: `handle` + 동사 (예: `handleMemoSave`, `handleRemoveClick`).
- 콜백 Props 네이밍: `on` + 동사 (예: `onUpdate`, `onRemove`).

---

## 접근성 (a11y)

```tsx
// aria-label 필수: 아이콘 전용 버튼
<button aria-label="수업에서 제거" className="...">
  <svg>...</svg>
</button>

// aria-label 필수: 드래그 핸들
<div aria-label="드래그하여 순서 변경" className="cursor-grab active:cursor-grabbing">
  ...
</div>

// 키보드 네비게이션 지원
onKeyDown={event => {
  if (event.key === 'Enter' && !event.nativeEvent.isComposing) { onSubmit(); }
  if (event.key === 'Escape') { onCancel(); }
}}
```

규칙:
- 아이콘 전용 버튼에는 반드시 `aria-label`을 추가한다.
- 한국어 IME 입력 시 Enter 이벤트 중복 방지: `!event.nativeEvent.isComposing` 체크.
- Escape 키로 편집 취소, 모달 닫기를 지원한다.
- 포커스 관리: 모달 열림 시 첫 입력에 포커스, 닫힘 시 트리거 요소로 복귀.

---

## Next.js 설정

### 프록시 설정 (next.config.ts)

```typescript
const nextConfig: NextConfig = {
  async rewrites() {
    const backendUrl = process.env.API_URL || 'http://localhost:8080';
    return [{ source: '/api/:path*', destination: `${backendUrl}/:path*` }];
  },
};
```

`/api/*` 요청은 백엔드로 프록시된다. 백엔드 URL에 `/api` prefix가 없다.

### 레이아웃 계층

```
layout.tsx (루트)
  → Geist 폰트, AuthProvider
  → (app)/layout.tsx
      → 인증 가드 (미인증 시 /login 리다이렉트)
      → Sidebar (md: 이상) + BottomNav (md: 미만)
      → children
```

---

## 새 기능 추가 체크리스트

새 UI 기능을 추가할 때 이 순서를 따른다:

1. **타입 정의** — `types/api.ts`에 백엔드 응답 타입 추가, `lib/api.ts`에서 re-export

2. **API 함수** — `lib/api.ts`에 `fetchWithAuth` 기반 함수 추가

3. **커스텀 훅** (복잡한 상태 로직이 있는 경우) — `hooks/` 패키지에 훅 생성
   - 낙관적 업데이트 패턴 적용
   - 에러 상태 관리
   - 디바운스 (필요 시)

4. **컴포넌트 구현** — `components/` 패키지에 컴포넌트 생성
   - 모바일 퍼스트 레이아웃
   - 디자인 시스템 색상/스타일 준수
   - 접근성 (aria-label, 키보드 네비게이션)

5. **페이지 연결** — `(app)/` 하위에 페이지 생성 또는 기존 페이지에 통합

6. **반응형 검증** — 모바일(375px), 태블릿(768px), 데스크톱(1280px) 세 가지 뷰포트에서 확인

7. **검증 게이트 (필수)** — 아래 2단계를 순서대로 통과해야 개발 완료로 간주한다

---

## 개발 완료 검증 게이트 (필수)

코드 작성이 끝나면 반드시 아래 2단계를 순서대로 실행하고, 모두 통과해야 개발 완료로 간주한다.
하나라도 실패하면 수정 후 해당 단계부터 다시 실행한다.

### Gate 1: Lint 검사 (ESLint)

```bash
cd frontend
npm run lint
```

**통과 기준:** ESLint 에러 0건. warning은 허용하되, error는 반드시 수정한다.

**실패 시 대응:**
1. 에러 로그를 확인한다.
2. 자동 수정 가능한 항목은 자동 수정한다:
   ```bash
   cd frontend
   npx next lint --fix
   ```
3. 자동 수정 불가한 항목은 수동으로 코드를 수정한다.
4. `npm run lint` 재실행하여 에러 0건 확인.

### Gate 2: 프로덕션 빌드

```bash
cd frontend
npm run build
```

**통과 기준:** 빌드 성공 (exit code 0). TypeScript 타입 에러, import 누락, 빌드 타임 에러 없이 완료되어야 한다.

**실패 시 대응:**
1. 타입 에러 → 해당 코드의 타입을 수정한다. `as any`, `@ts-ignore` 사용 금지.
2. import 누락 → 누락된 모듈을 import한다.
3. 빌드 에러 → 에러 메시지를 읽고 원인을 수정한다.
4. lint 에러로 빌드 실패 → Gate 1로 돌아가 lint 수정.
5. `npm run build` 재실행하여 빌드 성공 확인.

### 검증 게이트 요약

```
코드 작성 완료
    │
    ▼
[Gate 1] npm run lint ──── FAIL → lint 수정 → 재실행
    │ PASS
    ▼
[Gate 2] npm run build ─── FAIL → 코드 수정 → 재실행
    │ PASS
    ▼
✅ 개발 완료
```

두 게이트를 모두 통과하지 않은 상태에서 "완료"라고 보고하지 않는다.

---

## 교육 도메인 컨텍스트

이 서비스는 선생님이 수업 전후로 빠르게 사용하는 도구다.

**핵심 UX 흐름:**
- 홈(주간 캘린더)에서 수업 클릭 → 수업 상세 → 학생 카드에서 키워드 입력 → AI 생성 → 복사
- 학생 관리: 카드 그리드 → 카드 내에서 바로 편집/삭제
- 수강생 카드: 드래그 앤 드롭으로 순서 변경, 그리드 열 수 조정(1~6열)

**톤 & 보이스:**
- 에러 메시지: 친근한 한국어 ("~하지 못했어요", "다시 시도해주세요")
- 빈 상태: 안내 문구 + 행동 유도 ("메모를 추가하려면 클릭하세요")
- 로딩: "로딩 중..." (간결하게)

---

## 빌드 & 실행 명령어

```bash
npm run dev    # 개발 서버 (http://localhost:3000)
npm run build  # 프로덕션 빌드
npm run lint   # ESLint
```
