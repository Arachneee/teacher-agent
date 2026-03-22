'use client';

import Link from 'next/link';
import { useState } from 'react';
import type { SchoolGrade } from '../lib/api';
import { Student, deleteStudent, updateStudent } from '../lib/api';
import { SCHOOL_GRADE_LABELS, getAvatarColor } from '../lib/constants';
import GradeSelect from './GradeSelect';
import { formatDateKorean } from '../lib/dateTimeUtils';
import ConfirmModal from './ConfirmModal';

interface Props {
  student: Student;
  onUpdate: () => void;
  onDelete: (id: number) => void;
  dragHandleProps?: React.HTMLAttributes<HTMLDivElement>;
}

export default function StudentManagementCard({ student, onUpdate, onDelete, dragHandleProps }: Props) {
  const [editing, setEditing] = useState(false);
  const [name, setName] = useState(student.name);
  const [memo, setMemo] = useState(student.memo || '');
  const [grade, setGrade] = useState<SchoolGrade>(student.grade ?? 'ELEMENTARY_1');
  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  const avatarColor = getAvatarColor(student.id);

  const handleSave = async () => {
    if (!name.trim()) { setErrorMessage('학생 이름을 입력해주세요.'); return; }
    setSaving(true);
    setErrorMessage(null);
    try {
      await updateStudent(student.id, name.trim(), memo.trim(), grade);
      onUpdate();
      setEditing(false);
    } catch {
      setErrorMessage('학생 정보를 저장하지 못했어요');
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    setName(student.name);
    setMemo(student.memo || '');
    setGrade(student.grade ?? 'ELEMENTARY_1');
    setEditing(false);
    setErrorMessage(null);
  };

  const handleDelete = () => {
    setShowDeleteConfirm(true);
  };

  const handleConfirmDelete = async () => {
    setShowDeleteConfirm(false);
    try {
      await deleteStudent(student.id);
      onDelete(student.id);
    } catch {
      setErrorMessage('학생을 삭제하지 못했어요');
    }
  };

  return (
    <div className="bg-white rounded-3xl p-5 shadow-sm hover:shadow-md transition-all duration-200 flex flex-col gap-3">
      {/* Drag handle */}
      {dragHandleProps && !editing && (
        <div
          {...dragHandleProps}
          className="flex justify-center cursor-grab active:cursor-grabbing text-gray-300 hover:text-gray-400 transition-colors -mt-1 -mb-1"
          aria-label="드래그하여 순서 변경"
        >
          <svg width="20" height="10" viewBox="0 0 20 10" fill="currentColor">
            <circle cx="4" cy="2" r="1.5" />
            <circle cx="10" cy="2" r="1.5" />
            <circle cx="16" cy="2" r="1.5" />
            <circle cx="4" cy="8" r="1.5" />
            <circle cx="10" cy="8" r="1.5" />
            <circle cx="16" cy="8" r="1.5" />
          </svg>
        </div>
      )}
      {/* Avatar + Name */}
      <div className="flex items-center gap-3">
        <div
          className={`w-11 h-11 rounded-2xl flex items-center justify-center text-lg font-bold shrink-0 ${avatarColor}`}
        >
          {student.name.charAt(0)}
        </div>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-1">
            {editing ? (
              <input
                value={name}
                onChange={event => setName(event.target.value)}
                className="flex-1 min-w-0 text-base font-semibold text-gray-800 bg-purple-50 rounded-xl px-3 py-1 outline-none focus:ring-2 focus:ring-purple-300"
                placeholder="이름"
                autoFocus
              />
            ) : (
              <div className="flex items-center gap-2 flex-1 min-w-0">
                <p className="text-base font-semibold text-gray-800 truncate">
                  {student.name}
                </p>
                {student.grade && (
                  <span className="shrink-0 text-xs font-medium bg-purple-100 text-purple-600 rounded-lg px-2 py-0.5">
                    {SCHOOL_GRADE_LABELS[student.grade]}
                  </span>
                )}
              </div>
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
          <p className="text-xs text-gray-400">{formatDateKorean(student.createdAt)} 등록</p>
        </div>
      </div>

      {/* Grade select in edit mode */}
      {editing && (
        <GradeSelect value={grade} onChange={setGrade} />
      )}

      {/* Error */}
      {errorMessage && (
        <p className="text-xs text-rose-400 bg-rose-50 rounded-xl px-3 py-2">{errorMessage}</p>
      )}

      {/* Memo */}
      {editing ? (
        <textarea
          value={memo}
          onChange={event => setMemo(event.target.value)}
          className="w-full text-sm text-gray-600 bg-purple-50 rounded-2xl px-3 py-2 outline-none focus:ring-2 focus:ring-purple-300 resize-none"
          placeholder="메모를 입력하세요 (선택)"
          rows={3}
          maxLength={500}
        />
      ) : (
        <p className="text-sm text-gray-500 leading-relaxed whitespace-pre-wrap break-words min-h-[2.5rem]">
          {student.memo || <span className="text-gray-300 italic">메모 없음</span>}
        </p>
      )}

      {/* Edit actions */}
      {editing && (
        <div className="flex gap-2">
          <button
            onClick={handleSave}
            disabled={saving || !name.trim()}
            className="flex-1 bg-purple-400 hover:bg-purple-500 disabled:bg-purple-200 text-white text-sm font-medium py-2 rounded-2xl transition-colors duration-150"
          >
            {saving ? '저장 중...' : '저장'}
          </button>
          <button
            onClick={handleCancel}
            className="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-500 text-sm font-medium py-2 rounded-2xl transition-colors duration-150"
          >
            취소
          </button>
        </div>
      )}

      {/* 기록 보기 링크 */}
      {!editing && (
        <Link
          href={`/students/${student.id}`}
          className="w-full text-center text-sm text-purple-400 hover:text-purple-600 bg-purple-50 hover:bg-purple-100 rounded-2xl py-2 transition-colors duration-150"
        >
          피드백 기록 보기
        </Link>
      )}

      {showDeleteConfirm && (
        <ConfirmModal
          title="학생 삭제"
          message={`${student.name} 학생을 삭제할까요?`}
          confirmText="삭제"
          variant="danger"
          onConfirm={handleConfirmDelete}
          onCancel={() => setShowDeleteConfirm(false)}
        />
      )}
    </div>
  );
}
