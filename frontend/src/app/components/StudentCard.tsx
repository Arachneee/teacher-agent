'use client';

import { forwardRef, useEffect, useImperativeHandle, useRef, useState } from 'react';
import {
  Feedback,
  Student,
  addKeyword,
  createFeedback,
  deleteStudent,
  getFeedbacks,
  removeKeyword,
  updateStudent,
} from '../lib/api';

export interface StudentCardHandle {
  focusKeywordInput: () => void;
}

async function fetchLatestFeedback(studentId: number): Promise<Feedback | null> {
  const feedbacks = await getFeedbacks(studentId);
  return feedbacks.length > 0 ? feedbacks[0] : null;
}

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
  onNavigate?: (direction: 'prev' | 'next' | 'up' | 'down') => void;
  dragHandleProps?: React.HTMLAttributes<HTMLDivElement>;
}

const StudentCard = forwardRef<StudentCardHandle, Props>(function StudentCard(
  { student, onUpdate, onDelete, onNavigate, dragHandleProps },
  ref
) {
  useImperativeHandle(ref, () => ({
    focusKeywordInput: () => keywordInputRef.current?.focus(),
  }));
  const [editing, setEditing] = useState(false);
  const [name, setName] = useState(student.name);
  const [memo, setMemo] = useState(student.memo || '');
  const [loading, setLoading] = useState(false);

  const [feedback, setFeedback] = useState<Feedback | null>(null);
  const [keywordInput, setKeywordInput] = useState('');
  const keywordSubmittingRef = useRef(false);
  const keywordInputRef = useRef<HTMLInputElement>(null);

  const avatarColor = AVATAR_COLORS[student.id % AVATAR_COLORS.length];

  useEffect(() => {
    fetchLatestFeedback(student.id).then(setFeedback).catch(console.error);
  }, [student.id]);

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

  const handleAddKeyword = async () => {
    const trimmed = keywordInput.trim();
    if (!trimmed || keywordSubmittingRef.current) return;
    keywordSubmittingRef.current = true;
    setKeywordInput('');
    try {
      let feedbackId = feedback?.id;
      if (!feedbackId) {
        const created = await createFeedback(student.id);
        feedbackId = created.id;
      }
      await addKeyword(feedbackId, trimmed);
      setFeedback(await fetchLatestFeedback(student.id));
    } catch (e) {
      console.error(e);
      setKeywordInput(trimmed);
    } finally {
      keywordSubmittingRef.current = false;
    }
  };

  const handleRemoveKeyword = async (keywordId: number) => {
    if (!feedback) return;
    const previousFeedback = feedback;
    setFeedback(prev =>
      prev ? { ...prev, keywords: prev.keywords.filter(k => k.id !== keywordId) } : null
    );
    try {
      await removeKeyword(feedback.id, keywordId);
      setFeedback(await fetchLatestFeedback(student.id));
    } catch (e) {
      console.error(e);
      setFeedback(previousFeedback);
    }
  };

  const formatDate = (iso: string) =>
    new Date(iso).toLocaleDateString('ko-KR', { month: 'long', day: 'numeric' });

  return (
    <div className="h-full bg-white rounded-3xl p-6 shadow-sm hover:shadow-md transition-all duration-200 flex flex-col gap-4">
      {/* Drag Handle */}
      {dragHandleProps && (
        <div
          {...dragHandleProps}
          className="flex justify-center items-center h-4 cursor-grab active:cursor-grabbing -mt-2 -mx-2"
          aria-label="드래그하여 순서 변경"
        >
          <svg width="20" height="8" viewBox="0 0 20 8" fill="none" className="text-gray-300">
            <circle cx="4" cy="2" r="1.5" fill="currentColor" />
            <circle cx="10" cy="2" r="1.5" fill="currentColor" />
            <circle cx="16" cy="2" r="1.5" fill="currentColor" />
            <circle cx="4" cy="6" r="1.5" fill="currentColor" />
            <circle cx="10" cy="6" r="1.5" fill="currentColor" />
            <circle cx="16" cy="6" r="1.5" fill="currentColor" />
          </svg>
        </div>
      )}
      {/* Avatar */}
      <div className="flex items-center gap-3">
        <div
          className={`w-12 h-12 rounded-2xl flex items-center justify-center text-xl font-bold shrink-0 ${avatarColor}`}
        >
          {student.name.charAt(0)}
        </div>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-1">
            {editing ? (
              <input
                value={name}
                onChange={e => setName(e.target.value)}
                className="flex-1 min-w-0 text-lg font-semibold text-gray-800 bg-purple-50 rounded-xl px-3 py-1 outline-none focus:ring-2 focus:ring-purple-300"
                placeholder="이름"
                autoFocus
              />
            ) : (
              <p className="flex-1 min-w-0 text-lg font-semibold text-gray-800 truncate">{student.name}</p>
            )}
            {!editing && (
              <div className="flex items-center gap-1 shrink-0">
                <button
                  onClick={() => setEditing(true)}
                  className="w-7 h-7 flex items-center justify-center rounded-xl bg-purple-50 hover:bg-purple-100 text-purple-400 transition-colors duration-150"
                  aria-label="수정"
                >
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                  </svg>
                </button>
                <button
                  onClick={handleDelete}
                  className="w-7 h-7 flex items-center justify-center rounded-xl bg-rose-50 hover:bg-rose-100 text-rose-400 transition-colors duration-150"
                  aria-label="삭제"
                >
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                    <polyline points="3 6 5 6 21 6" />
                    <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
                    <path d="M10 11v6M14 11v6" />
                    <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" />
                  </svg>
                </button>
              </div>
            )}
          </div>
          <p className="text-xs text-gray-400">{formatDate(student.createdAt)} 등록</p>
        </div>
      </div>

      {/* Memo */}
      <div className={editing ? 'flex-1 flex flex-col' : ''}>
        {editing ? (
          <textarea
            value={memo}
            onChange={e => setMemo(e.target.value)}
            className="flex-1 w-full text-sm text-gray-600 bg-purple-50 rounded-2xl px-3 py-2 outline-none focus:ring-2 focus:ring-purple-300 resize-none"
            placeholder="메모를 입력하세요 (선택)"
            maxLength={500}
          />
        ) : (
          <p className="text-sm text-gray-500 leading-relaxed whitespace-pre-wrap break-words min-h-[3rem]">
            {student.memo || <span className="text-gray-300 italic">메모 없음</span>}
          </p>
        )}
      </div>

      {/* Keywords */}
      {!editing && (
        <div className="flex-1 flex flex-col gap-2">
          <p className="text-xs font-semibold text-gray-400 tracking-wide">수업 키워드</p>
          {feedback && feedback.keywords.length > 0 && (
            <div className="flex flex-wrap gap-1.5">
              {feedback.keywords.map(k => (
                <span
                  key={k.id}
                  className="inline-flex items-center gap-1 bg-pink-50 text-pink-500 text-xs font-medium px-2.5 py-1 rounded-full"
                >
                  {k.keyword}
                  <button
                    onClick={() => handleRemoveKeyword(k.id)}
                    className="w-4 h-4 flex items-center justify-center rounded-full hover:bg-pink-200 transition-colors text-sm leading-none"
                    aria-label={`${k.keyword} 삭제`}
                  >
                    ×
                  </button>
                </span>
              ))}
            </div>
          )}
          <input
            ref={keywordInputRef}
            value={keywordInput}
            onChange={e => setKeywordInput(e.target.value)}
            onKeyDown={e => {
              if (e.key === 'Enter' && !e.nativeEvent.isComposing) {
                handleAddKeyword();
              } else if (keywordInput === '' && e.key === 'ArrowLeft') {
                e.preventDefault();
                onNavigate?.('prev');
              } else if (keywordInput === '' && e.key === 'ArrowRight') {
                e.preventDefault();
                onNavigate?.('next');
              } else if (keywordInput === '' && e.key === 'ArrowUp') {
                e.preventDefault();
                onNavigate?.('up');
              } else if (keywordInput === '' && e.key === 'ArrowDown') {
                e.preventDefault();
                onNavigate?.('down');
              }
            }}
            className="mt-auto w-full text-sm bg-pink-50 text-gray-700 rounded-2xl px-3 py-2 outline-none focus:ring-2 focus:ring-pink-200 placeholder:text-gray-300 transition-colors"
            placeholder="키워드 입력 후 Enter ↵"
            maxLength={100}
          />
        </div>
      )}

      {/* Actions */}
      {editing ? (
        <div className="mt-auto flex gap-2">
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
        <button
          className="w-full bg-gradient-to-r from-blue-50 to-indigo-50 hover:from-blue-100 hover:to-indigo-100 text-indigo-500 text-sm font-medium py-2.5 rounded-2xl transition-colors duration-150 flex items-center justify-center gap-2"
        >
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
            <path d="M9 9h.01M12 9h.01M15 9h.01" strokeWidth="2.5" />
          </svg>
          AI 학부모 문자 생성
        </button>
      )}
    </div>
  );
});

export default StudentCard;
