'use client';

import { useState } from 'react';
import { Student, updateStudent, deleteStudent } from '../lib/api';

const AVATAR_COLORS = [
  'bg-pink-200 text-pink-600',
  'bg-purple-200 text-purple-600',
  'bg-blue-200 text-blue-600',
  'bg-green-200 text-green-600',
  'bg-yellow-200 text-yellow-600',
  'bg-orange-200 text-orange-600',
  'bg-rose-200 text-rose-600',
  'bg-teal-200 text-teal-600',
];

interface Props {
  student: Student;
  onUpdate: (student: Student) => void;
  onDelete: (id: number) => void;
}

export default function StudentCard({ student, onUpdate, onDelete }: Props) {
  const [editing, setEditing] = useState(false);
  const [name, setName] = useState(student.name);
  const [memo, setMemo] = useState(student.memo || '');
  const [loading, setLoading] = useState(false);

  const avatarColor = AVATAR_COLORS[student.id % AVATAR_COLORS.length];

  const handleSave = async () => {
    if (!name.trim()) return;
    setLoading(true);
    try {
      const updated = await updateStudent(student.id, name.trim(), memo.trim());
      onUpdate(updated);
      setEditing(false);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    setName(student.name);
    setMemo(student.memo || '');
    setEditing(false);
  };

  const handleDelete = async () => {
    if (!confirm(`${student.name} 학생을 삭제할까요?`)) return;
    try {
      await deleteStudent(student.id);
      onDelete(student.id);
    } catch (e) {
      console.error(e);
    }
  };

  const formatDate = (iso: string) =>
    new Date(iso).toLocaleDateString('ko-KR', { month: 'long', day: 'numeric' });

  return (
    <div className="bg-white rounded-3xl p-6 shadow-sm hover:shadow-md transition-all duration-200 flex flex-col gap-4">
      {/* Avatar */}
      <div className="flex items-center gap-3">
        <div
          className={`w-12 h-12 rounded-2xl flex items-center justify-center text-xl font-bold shrink-0 ${avatarColor}`}
        >
          {student.name.charAt(0)}
        </div>
        <div className="flex-1 min-w-0">
          {editing ? (
            <input
              value={name}
              onChange={e => setName(e.target.value)}
              className="w-full text-lg font-semibold text-gray-800 bg-purple-50 rounded-xl px-3 py-1 outline-none focus:ring-2 focus:ring-purple-300"
              placeholder="이름"
              autoFocus
            />
          ) : (
            <p className="text-lg font-semibold text-gray-800 truncate">{student.name}</p>
          )}
          <p className="text-xs text-gray-400">{formatDate(student.createdAt)} 등록</p>
        </div>
      </div>

      {/* Memo */}
      <div className="flex-1">
        {editing ? (
          <textarea
            value={memo}
            onChange={e => setMemo(e.target.value)}
            className="w-full text-sm text-gray-600 bg-purple-50 rounded-2xl px-3 py-2 outline-none focus:ring-2 focus:ring-purple-300 resize-none"
            placeholder="메모를 입력하세요 (선택)"
            rows={3}
            maxLength={500}
          />
        ) : (
          <p className="text-sm text-gray-500 leading-relaxed whitespace-pre-wrap break-words min-h-[3rem]">
            {student.memo || <span className="text-gray-300 italic">메모 없음</span>}
          </p>
        )}
      </div>

      {/* Actions */}
      {editing ? (
        <div className="flex gap-2">
          <button
            onClick={handleSave}
            disabled={loading || !name.trim()}
            className="flex-1 bg-purple-400 hover:bg-purple-500 disabled:bg-purple-200 text-white text-sm font-medium py-2 rounded-2xl transition-colors duration-150"
          >
            {loading ? '저장 중...' : '저장'}
          </button>
          <button
            onClick={handleCancel}
            className="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-500 text-sm font-medium py-2 rounded-2xl transition-colors duration-150"
          >
            취소
          </button>
        </div>
      ) : (
        <div className="flex gap-2">
          <button
            onClick={() => setEditing(true)}
            className="flex-1 bg-purple-50 hover:bg-purple-100 text-purple-500 text-sm font-medium py-2 rounded-2xl transition-colors duration-150"
          >
            ✏️ 수정
          </button>
          <button
            onClick={handleDelete}
            className="px-4 bg-rose-50 hover:bg-rose-100 text-rose-400 text-sm font-medium py-2 rounded-2xl transition-colors duration-150"
          >
            🗑️
          </button>
        </div>
      )}
    </div>
  );
}
