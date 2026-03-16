const BASE_URL = '/api';

export interface Student {
  id: number;
  name: string;
  memo: string;
  createdAt: string;
  updatedAt: string;
}

export interface FeedbackKeyword {
  id: number;
  keyword: string;
  createdAt: string;
}

export interface Feedback {
  id: number;
  studentId: number;
  aiContent: string | null;
  keywords: FeedbackKeyword[];
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  username: string;
}

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

// Auth

export async function login(username: string, password: string): Promise<AuthResponse> {
  const response = await fetch(`${BASE_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ username, password }),
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

export async function getStudents(): Promise<Student[]> {
  const res = await fetchWithAuth(`${BASE_URL}/students`);
  if (!res.ok) throw new Error('학생 목록을 불러오지 못했어요');
  return res.json();
}

export async function createStudent(name: string, memo: string): Promise<Student> {
  const res = await fetchWithAuth(`${BASE_URL}/students`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, memo }),
  });
  if (!res.ok) throw new Error('학생을 추가하지 못했어요');
  return res.json();
}

export async function updateStudent(id: number, name: string, memo: string): Promise<Student> {
  const res = await fetchWithAuth(`${BASE_URL}/students/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, memo }),
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

// Feedbacks

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
