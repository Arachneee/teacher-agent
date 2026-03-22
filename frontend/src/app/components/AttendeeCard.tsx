'use client';

import { forwardRef, useEffect, useImperativeHandle, useRef, useState } from 'react';
import Link from 'next/link';
import { Attendee, updateStudent } from '../lib/api';
import { SCHOOL_GRADE_LABELS, getAvatarColor } from '../lib/constants';
import { formatDateKorean } from '../lib/dateTimeUtils';
import { useFeedback } from '../hooks/useFeedback';
import AiFeedbackSection from './AiFeedbackSection';
import KeywordsSection from './KeywordsSection';
import ConfirmModal from './ConfirmModal';

export interface AttendeeCardHandle {
  focusKeywordInput: () => void;
}

interface Props {
  attendee: Attendee;
  lessonId: number;
  isRecurring: boolean;
  onUpdate: () => void;
  onRemove: (attendeeId: number) => void;
  dragHandleProps?: React.HTMLAttributes<HTMLDivElement>;
}

const AttendeeCard = forwardRef<AttendeeCardHandle, Props>((
  { attendee, lessonId, isRecurring, onUpdate, onRemove, dragHandleProps },
  ref
) => {
  const keywordInputRef = useRef<HTMLInputElement>(null);
  useImperativeHandle(ref, () => ({
    focusKeywordInput: () => keywordInputRef.current?.focus(),
  }));

  const [editingMemo, setEditingMemo] = useState(false);
  const [memo, setMemo] = useState(attendee.student.memo || '');
  const [savingMemo, setSavingMemo] = useState(false);
  const [editErrorMessage, setEditErrorMessage] = useState<string | null>(null);
  const [keywordInput, setKeywordInput] = useState('');
  const [editingKeywordId, setEditingKeywordId] = useState<number | null>(null);
  const [showRemoveConfirm, setShowRemoveConfirm] = useState(false);
  const memoTextareaRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    if (!editingMemo) {
      setMemo(attendee.student.memo || '');
    }
  }, [attendee.student.memo, editingMemo]);

  const {
    feedback,
    aiGenerating,
    isEditingAiContent,
    errorMessage: feedbackErrorMessage,
    handleAddKeyword,
    handleUpdateKeyword,
    handleRemoveKeyword,
    handleGenerate,
    handleUpdateAiContent,
    handleLike,
  } = useFeedback(attendee.student.id, attendee.feedback);

  const avatarColor = getAvatarColor(attendee.student.id);

  const handleMemoSave = async () => {
    const trimmed = memo.trim();
    if (trimmed === (attendee.student.memo || '')) {
      setEditingMemo(false);
      return;
    }
    setSavingMemo(true);
    setEditErrorMessage(null);
    try {
      await updateStudent(attendee.student.id, attendee.student.name, trimmed, attendee.student.grade ?? 'ELEMENTARY_1');
      onUpdate();
      setEditingMemo(false);
    } catch {
      setEditErrorMessage('메모를 저장하지 못했어요');
    } finally {
      setSavingMemo(false);
    }
  };

  const handleMemoCancel = () => {
    setMemo(attendee.student.memo || '');
    setEditingMemo(false);
    setEditErrorMessage(null);
  };

  const handleMemoKeyDown = (event: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key === 'Escape') {
      handleMemoCancel();
    }
  };

  const handleRemoveClick = () => {
    if (isRecurring) {
      onRemove(attendee.id);
      return;
    }
    setShowRemoveConfirm(true);
  };

  const handleConfirmRemove = () => {
    setShowRemoveConfirm(false);
    onRemove(attendee.id);
  };

  const handleStartEditKeyword = (keyword: { id: number; keyword: string }) => {
    setEditingKeywordId(keyword.id);
    setKeywordInput(keyword.keyword);
    keywordInputRef.current?.focus();
  };

  const handleCancelEditKeyword = () => {
    setEditingKeywordId(null);
    setKeywordInput('');
  };

  const handleSubmitKeyword = async () => {
    if (editingKeywordId !== null) {
      const trimmed = keywordInput.trim();
      const success = await handleUpdateKeyword(editingKeywordId, trimmed);
      if (success) {
        setEditingKeywordId(null);
        setKeywordInput('');
      }
    } else {
      const trimmed = keywordInput.trim();
      if (!trimmed) return;
      setKeywordInput('');
      const success = await handleAddKeyword(trimmed);
      if (!success) {
        setKeywordInput(trimmed);
      }
    }
  };

  return (
    <div className="h-full bg-white rounded-3xl p-6 shadow-sm hover:shadow-md transition-all duration-200 flex flex-col gap-4">
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

      {/* Avatar + Name (read-only) + Remove button */}
      <div className="flex items-center gap-3">
        <div
          className={`w-12 h-12 rounded-2xl flex items-center justify-center text-xl font-bold shrink-0 ${avatarColor}`}
        >
          {attendee.student.name.charAt(0)}
        </div>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-1">
            <div className="flex items-center gap-2 flex-1 min-w-0">
              <p className="text-lg font-semibold text-gray-800 truncate">
                {attendee.student.name}
              </p>
              {attendee.student.grade && (
                <span className="shrink-0 text-xs font-medium bg-purple-100 text-purple-600 rounded-lg px-2 py-0.5">
                  {SCHOOL_GRADE_LABELS[attendee.student.grade]}
                </span>
              )}
            </div>
            <Link
              href={`/students/${attendee.student.id}?from=/lessons/${lessonId}`}
              className="w-7 h-7 flex items-center justify-center rounded-xl bg-purple-50 hover:bg-purple-100 text-purple-400 transition-colors duration-150 shrink-0"
              aria-label="피드백 기록 보기"
            >
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
                <line x1="16" y1="13" x2="8" y2="13" />
                <line x1="16" y1="17" x2="8" y2="17" />
                <polyline points="10 9 9 9 8 9" />
              </svg>
            </Link>
            <button
              onClick={handleRemoveClick}
              className="w-7 h-7 flex items-center justify-center rounded-xl bg-rose-50 hover:bg-rose-100 text-rose-400 transition-colors duration-150 shrink-0"
              aria-label="수업에서 제거"
            >
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <polyline points="3 6 5 6 21 6" />
                <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
                <path d="M10 11v6M14 11v6" />
                <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" />
              </svg>
            </button>
          </div>
          <p className="text-xs text-gray-400">{formatDateKorean(attendee.createdAt)} 등록</p>
        </div>
      </div>

      {editErrorMessage && (
        <p className="text-xs text-rose-400 bg-rose-50 rounded-xl px-3 py-2">{editErrorMessage}</p>
      )}

      {/* Memo — inline editable */}
      <div>
        {editingMemo ? (
          <div className="flex flex-col gap-2">
            <textarea
              ref={memoTextareaRef}
              value={memo}
              onChange={event => setMemo(event.target.value)}
              onKeyDown={handleMemoKeyDown}
              className="w-full text-sm text-gray-600 bg-purple-50 rounded-2xl px-3 py-2 outline-none focus:ring-2 focus:ring-purple-300 resize-none"
              placeholder="메모를 입력하세요"
              maxLength={500}
              rows={3}
              autoFocus
            />
            <div className="flex items-center justify-between">
              <p className="text-xs text-gray-300">{memo.length}/500</p>
              <div className="flex gap-1.5">
                <button
                  onClick={handleMemoCancel}
                  disabled={savingMemo}
                  className="text-xs text-gray-400 hover:text-gray-600 px-2 py-1 rounded-lg transition-colors"
                >
                  취소
                </button>
                <button
                  onClick={handleMemoSave}
                  disabled={savingMemo}
                  className="text-xs text-white bg-purple-400 hover:bg-purple-500 disabled:bg-purple-200 px-3 py-1 rounded-lg transition-colors font-medium"
                >
                  {savingMemo ? '저장 중...' : '저장'}
                </button>
              </div>
            </div>
          </div>
        ) : (
          <button
            type="button"
            onClick={() => setEditingMemo(true)}
            className="w-full text-left group"
          >
            <p className="text-sm text-gray-500 leading-relaxed whitespace-pre-wrap break-words min-h-[3rem] rounded-2xl px-3 py-2 -mx-3 group-hover:bg-purple-50/60 transition-colors duration-150">
              {attendee.student.memo || <span className="text-gray-300 italic">메모를 추가하려면 클릭하세요</span>}
            </p>
          </button>
        )}
      </div>

      <KeywordsSection
        keywords={feedback?.keywords ?? []}
        keywordInput={keywordInput}
        editingKeywordId={editingKeywordId}
        onKeywordInputChange={setKeywordInput}
        onSubmitKeyword={handleSubmitKeyword}
        onStartEditKeyword={handleStartEditKeyword}
        onCancelEditKeyword={handleCancelEditKeyword}
        onRemoveKeyword={handleRemoveKeyword}
        inputRef={keywordInputRef}
      />

      {feedbackErrorMessage && (
        <p className="text-xs text-rose-400 bg-rose-50 rounded-xl px-3 py-2">{feedbackErrorMessage}</p>
      )}

      <AiFeedbackSection
        feedback={feedback}
        aiGenerating={aiGenerating}
        isEditingAiContent={isEditingAiContent}
        onGenerate={handleGenerate}
        onUpdateAiContent={handleUpdateAiContent}
        onLike={handleLike}
      />

      {showRemoveConfirm && (
        <ConfirmModal
          title="수강생 제거"
          message={`${attendee.student.name} 학생을 이 수업에서 제거할까요?`}
          confirmText="제거"
          variant="danger"
          onConfirm={handleConfirmRemove}
          onCancel={() => setShowRemoveConfirm(false)}
        />
      )}
    </div>
  );
});

export default AttendeeCard;
