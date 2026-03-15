const BASE_URL = 'http://localhost:8080';

export interface Student {
  id: number;
  name: string;
  memo: string;
  createdAt: string;
  updatedAt: string;
}

export async function getStudents(): Promise<Student[]> {
  const res = await fetch(`${BASE_URL}/students`);
  if (!res.ok) throw new Error('학생 목록을 불러오지 못했어요');
  return res.json();
}

export async function createStudent(name: string, memo: string): Promise<Student> {
  const res = await fetch(`${BASE_URL}/students`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, memo }),
  });
  if (!res.ok) throw new Error('학생을 추가하지 못했어요');
  return res.json();
}

export async function updateStudent(id: number, name: string, memo: string): Promise<Student> {
  const res = await fetch(`${BASE_URL}/students/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, memo }),
  });
  if (!res.ok) throw new Error('학생 정보를 수정하지 못했어요');
  return res.json();
}

export async function deleteStudent(id: number): Promise<void> {
  const res = await fetch(`${BASE_URL}/students/${id}`, { method: 'DELETE' });
  if (!res.ok) throw new Error('학생을 삭제하지 못했어요');
}
