'use client';

import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { Lesson, deleteLesson, UpdateScope } from '../lib/api';
import RecurringScopeModal from '../components/RecurringScopeModal';
import ConfirmModal from './ConfirmModal';

interface Props {
  lesson: Lesson;
  onEdit: (lesson: Lesson) => void;
  onDelete: (id: number, didDeleteMultiple: boolean) => void;
}

export default function LessonCard({ lesson, onEdit, onDelete }: Props) {
  const router = useRouter();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [showScopeModal, setShowScopeModal] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  const handleDelete = async (event: React.MouseEvent) => {
    event.stopPropagation();
    
    if (lesson.recurrenceGroupId !== null) {
      setShowScopeModal(true);
      return;
    }
    
    setShowDeleteConfirm(true);
  };

  const handleConfirmDelete = async () => {
    setShowDeleteConfirm(false);
    try {
      await deleteLesson(lesson.id);
      onDelete(lesson.id, false);
    } catch {
      setErrorMessage('수업을 삭제하지 못했어요');
    }
  };

  const handleScopeDelete = async (scope: UpdateScope) => {
    setShowScopeModal(false);
    try {
      await deleteLesson(lesson.id, scope);
      onDelete(lesson.id, scope !== 'SINGLE');
    } catch {
      setErrorMessage('수업을 삭제하지 못했어요');
    }
  };

  const handleEdit = (event: React.MouseEvent) => {
    event.stopPropagation();
    onEdit(lesson);
  };

  const formatDateTime = (iso: string) =>
    new Date(iso).toLocaleString('ko-KR', {
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });

  return (
    <>
      <div
        onClick={() => router.push(`/lessons/${lesson.id}`)}
        className="bg-white rounded-3xl p-6 shadow-sm hover:shadow-md transition-all duration-200 cursor-pointer flex flex-col gap-3"
      >
        <div className="flex items-start justify-between gap-2">
          <div className="flex items-center gap-2 flex-1 min-w-0">
            <h3 className="text-lg font-semibold text-gray-800 truncate">
              {lesson.title}
            </h3>
            {lesson.recurrenceGroupId !== null && (
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" className="text-purple-300 flex-shrink-0">
                <path d="M17 1l4 4-4 4" />
                <path d="M3 11V9a4 4 0 014-4h14" />
                <path d="M7 23l-4-4 4-4" />
                <path d="M21 13v2a4 4 0 01-4 4H3" />
              </svg>
            )}
          </div>
        <div className="flex items-center gap-1 shrink-0">
          <button
            onClick={handleEdit}
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
      </div>

      <div className="flex flex-col gap-1 text-sm text-gray-500">
        <div className="flex items-center gap-2">
          <span className="text-purple-300">시작</span>
          <span>{formatDateTime(lesson.startTime)}</span>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-pink-300">종료</span>
          <span>{formatDateTime(lesson.endTime)}</span>
        </div>
      </div>

      {errorMessage && (
        <p className="text-xs text-rose-400 bg-rose-50 rounded-xl px-3 py-2">{errorMessage}</p>
      )}
    </div>

    {showScopeModal && (
      <RecurringScopeModal
        mode="delete"
        lessonTitle={lesson.title}
        onSelect={handleScopeDelete}
        onClose={() => setShowScopeModal(false)}
      />
    )}

    {showDeleteConfirm && (
      <ConfirmModal
        title="수업 삭제"
        message={`"${lesson.title}" 수업을 삭제할까요?\n수강생과 피드백도 함께 삭제됩니다.`}
        confirmText="삭제"
        variant="danger"
        onConfirm={handleConfirmDelete}
        onCancel={() => setShowDeleteConfirm(false)}
      />
    )}
    </>
  );
}
