import type {
  Attendee,
  AuthResponse,
  DayOfWeek,
  Feedback,
  Lesson,
  LessonDetail,
  RecurrenceCreateRequest,
  RecurrenceType,
  SchoolGrade,
  Student,
  UpdateScope,
} from '../types/api';

export type {
  Attendee,
  AttendeeStudent,
  AuthResponse,
  DayOfWeek,
  Feedback,
  FeedbackKeyword,
  Lesson,
  LessonDetail,
  LessonDetailAttendee,
  LessonDetailFeedback,
  RecurrenceCreateRequest,
  RecurrenceType,
  SchoolGrade,
  Student,
  UpdateScope,
} from '../types/api';

const BASE_URL = '/api';

async function fetchWithAuth(url: string, options: RequestInit = {}): Promise<Response> {
  const res = await fetch(url, { credentials: 'include', ...options });
  if (res.status === 401) {
    if (typeof window !== 'undefined') {
      window.location.href = '/login';
    }
    throw new Error('세션이 만료됐어요. 다시 로그인해주세요.');
  }
  if (!res.ok) {
    const body = await res.text().catch(() => '');
    console.error(`[API] ${options.method ?? 'GET'} ${url} → ${res.status}`, body);
  }
  return res;
}

// Auth

export async function login(userId: string, password: string): Promise<AuthResponse> {
  const response = await fetch(`${BASE_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ userId, password }),
  });
  if (!response.ok) throw new Error('로그인에 실패했어요');
  return response.json();
}

export async function logout(): Promise<void> {
  await fetch(`${BASE_URL}/auth/logout`, {
    method: 'POST',
    credentials: 'include',
  });
}

export async function getMe(): Promise<AuthResponse> {
  const response = await fetch(`${BASE_URL}/auth/me`, {
    credentials: 'include',
  });
  if (!response.ok) throw new Error('인증 정보를 확인할 수 없어요');
  return response.json();
}

// Students

export async function getStudent(id: number): Promise<Student> {
  const res = await fetchWithAuth(`${BASE_URL}/students/${id}`);
  if (!res.ok) throw new Error('학생 정보를 불러오지 못했어요');
  return res.json();
}

export async function getStudents(): Promise<Student[]> {
  const res = await fetchWithAuth(`${BASE_URL}/students`);
  if (!res.ok) throw new Error('학생 목록을 불러오지 못했어요');
  return res.json();
}

export async function createStudent(name: string, memo: string, grade: SchoolGrade): Promise<Student> {
  const res = await fetchWithAuth(`${BASE_URL}/students`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, memo, grade }),
  });
  if (!res.ok) throw new Error('학생을 추가하지 못했어요');
  return res.json();
}

export async function updateStudent(id: number, name: string, memo: string, grade: SchoolGrade): Promise<Student> {
  const res = await fetchWithAuth(`${BASE_URL}/students/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, memo, grade }),
  });
  if (!res.ok) throw new Error('학생 정보를 수정하지 못했어요');
  return res.json();
}

export async function deleteStudent(id: number): Promise<void> {
  const res = await fetchWithAuth(`${BASE_URL}/students/${id}`, {
    method: 'DELETE',
  });
  if (!res.ok) throw new Error('학생을 삭제하지 못했어요');
}

// Lessons

export async function getLessons(weekStart: string): Promise<Lesson[]> {
  const url = `${BASE_URL}/lessons?weekStart=${weekStart}`;
  const res = await fetchWithAuth(url);
  if (!res.ok) throw new Error('수업 목록을 불러오지 못했어요');
  return res.json();
}

export async function createLesson(
  title: string,
  startTime: string,
  endTime: string,
  recurrence?: RecurrenceCreateRequest,
  studentIds?: number[],
): Promise<Lesson> {
  const res = await fetchWithAuth(`${BASE_URL}/lessons`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      title,
      startTime,
      endTime,
      recurrence: recurrence ?? null,
      studentIds: studentIds?.length ? studentIds : null,
    }),
  });
  if (!res.ok) throw new Error('수업을 추가하지 못했어요');
  return res.json();
}

export async function updateLesson(
  id: number,
  title: string,
  startTime: string,
  endTime: string,
  scope?: UpdateScope,
  recurrence?: RecurrenceCreateRequest,
  addStudentIds?: number[],
  removeStudentIds?: number[]
): Promise<Lesson> {
  const res = await fetchWithAuth(`${BASE_URL}/lessons/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      title,
      startTime,
      endTime,
      scope: scope ?? 'SINGLE',
      recurrence: recurrence ?? null,
      addStudentIds: addStudentIds ?? null,
      removeStudentIds: removeStudentIds ?? null,
    }),
  });
  if (!res.ok) throw new Error('수업을 수정하지 못했어요');
  return res.json();
}

export async function deleteLesson(id: number, scope?: UpdateScope): Promise<void> {
  const params = new URLSearchParams({ scope: scope ?? 'SINGLE' });
  const res = await fetchWithAuth(`${BASE_URL}/lessons/${id}?${params}`, {
    method: 'DELETE',
  });
  if (!res.ok) throw new Error('수업을 삭제하지 못했어요');
}

export async function getLessonDetail(id: number): Promise<LessonDetail> {
  const res = await fetchWithAuth(`${BASE_URL}/lessons/${id}/detail`);
  if (!res.ok) throw new Error('수업 정보를 불러오지 못했어요');
  return res.json();
}

// Attendees

export async function addAttendee(lessonId: number, studentId: number, scope?: UpdateScope): Promise<Attendee> {
  const res = await fetchWithAuth(`${BASE_URL}/lessons/${lessonId}/attendees`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ studentId, scope: scope ?? null }),
  });
  if (!res.ok) throw new Error('수강생을 추가하지 못했어요');
  return res.json();
}

export async function removeAttendee(lessonId: number, attendeeId: number, scope?: UpdateScope): Promise<void> {
  const params = scope ? new URLSearchParams({ scope }) : '';
  const url = `${BASE_URL}/lessons/${lessonId}/attendees/${attendeeId}${params ? `?${params}` : ''}`;
  const res = await fetchWithAuth(url, {
    method: 'DELETE',
  });
  if (!res.ok) throw new Error('수강생을 제거하지 못했어요');
}

// Feedbacks

export async function getFeedback(feedbackId: number): Promise<Feedback> {
  const res = await fetchWithAuth(`${BASE_URL}/feedbacks/${feedbackId}`);
  if (!res.ok) throw new Error('피드백을 불러오지 못했어요');
  return res.json();
}

export async function getFeedbacks(studentId: number): Promise<Feedback[]> {
  const res = await fetchWithAuth(`${BASE_URL}/feedbacks?studentId=${studentId}`);
  if (!res.ok) throw new Error('피드백 목록을 불러오지 못했어요');
  return res.json();
}

export async function createFeedback(studentId: number): Promise<Feedback> {
  const res = await fetchWithAuth(`${BASE_URL}/feedbacks`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ studentId }),
  });
  if (!res.ok) throw new Error('피드백을 생성하지 못했어요');
  return res.json();
}

export async function addKeyword(feedbackId: number, keyword: string): Promise<void> {
  const res = await fetchWithAuth(`${BASE_URL}/feedbacks/${feedbackId}/keywords`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ keyword }),
  });
  if (!res.ok) throw new Error('키워드를 추가하지 못했어요');
}

export async function updateKeyword(feedbackId: number, keywordId: number, keyword: string): Promise<void> {
  const res = await fetchWithAuth(`${BASE_URL}/feedbacks/${feedbackId}/keywords/${keywordId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ keyword }),
  });
  if (!res.ok) throw new Error('키워드를 수정하지 못했어요');
}

export async function removeKeyword(feedbackId: number, keywordId: number): Promise<void> {
  const res = await fetchWithAuth(`${BASE_URL}/feedbacks/${feedbackId}/keywords/${keywordId}`, {
    method: 'DELETE',
  });
  if (!res.ok) throw new Error('키워드를 삭제하지 못했어요');
}

export async function updateFeedback(feedbackId: number, aiContent: string | null): Promise<Feedback> {
  const res = await fetchWithAuth(`${BASE_URL}/feedbacks/${feedbackId}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ aiContent }),
  });
  if (!res.ok) throw new Error('피드백을 수정하지 못했어요');
  return res.json();
}

export async function generateAiContent(feedbackId: number): Promise<Feedback> {
  const res = await fetchWithAuth(`${BASE_URL}/feedbacks/${feedbackId}/generate`, {
    method: 'POST',
  });
  if (!res.ok) throw new Error('AI 문자를 생성하지 못했어요');
  return res.json();
}

export async function likeFeedback(feedbackId: number): Promise<Feedback> {
  const res = await fetchWithAuth(`${BASE_URL}/feedbacks/${feedbackId}/like`, {
    method: 'POST',
  });
  if (!res.ok) throw new Error('좋아요 처리에 실패했어요');
  return res.json();
}
