'use client';

import { useState } from 'react';
import { Student, deleteStudent, updateStudent } from '../lib/api';
import { getAvatarColor } from '../lib/constants';
import { formatDateKorean } from '../lib/dateTimeUtils';
import ConfirmModal from './ConfirmModal';

interface Props {
  student: Student;
  onUpdate: () => void;
  onDelete: (id: number) => void;
}

export default function StudentManagementCard({ student, onUpdate, onDelete }: Props) {
  const [editing, setEditing] = useState(false);
  const [name, setName] = useState(student.name);
  const [memo, setMemo] = useState(student.memo || '');
  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  const avatarColor = getAvatarColor(student.id);

  const handleSave = async () => {
    if (!name.trim()) { setErrorMessage('학생 이름을 입력해주세요.'); return; }
    setSaving(true);
    setErrorMessage(null);
    try {
      await updateStudent(student.id, name.trim(), memo.trim());
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
              <p className="flex-1 min-w-0 text-base font-semibold text-gray-800 truncate">
                {student.name}
              </p>
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
