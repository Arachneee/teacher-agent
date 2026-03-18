'use client';

import { forwardRef, useEffect, useImperativeHandle, useRef, useState } from 'react';
import { Attendee, updateStudent } from '../lib/api';
import { useFeedback } from '../hooks/useFeedback';
import AiFeedbackSection from './AiFeedbackSection';
import KeywordsSection from './KeywordsSection';

export interface AttendeeCardHandle {
  focusKeywordInput: () => void;
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
  attendee: Attendee;
  onUpdate: () => void;
  onRemove: (attendeeId: number) => void;
  dragHandleProps?: React.HTMLAttributes<HTMLDivElement>;
}

const AttendeeCard = forwardRef<AttendeeCardHandle, Props>((
  { attendee, onUpdate, onRemove, dragHandleProps },
  ref
) => {
  const keywordInputRef = useRef<HTMLInputElement>(null);
  useImperativeHandle(ref, () => ({
    focusKeywordInput: () => keywordInputRef.current?.focus(),
  }));

  const [editing, setEditing] = useState(false);
  const [name, setName] = useState(attendee.student.name);
  const [memo, setMemo] = useState(attendee.student.memo || '');

  // 편집 중이 아닐 때 부모로부터 내려온 최신 학생 정보를 동기화
  useEffect(() => {
    if (!editing) {
      setName(attendee.student.name);
      setMemo(attendee.student.memo || '');
    }
  }, [attendee.student.name, attendee.student.memo, editing]);
  const [saving, setSaving] = useState(false);
  const [editErrorMessage, setEditErrorMessage] = useState<string | null>(null);
  const [keywordInput, setKeywordInput] = useState('');
  const [editingKeywordId, setEditingKeywordId] = useState<number | null>(null);

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

  const avatarColor = AVATAR_COLORS[attendee.student.id % AVATAR_COLORS.length];

  const handleSave = async () => {
    if (!name.trim()) return;
    setSaving(true);
    setEditErrorMessage(null);
    try {
      await updateStudent(attendee.student.id, name.trim(), memo.trim());
      onUpdate();
      setEditing(false);
    } catch {
      setEditErrorMessage('학생 정보를 저장하지 못했어요');
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    setName(attendee.student.name);
    setMemo(attendee.student.memo || '');
    setEditing(false);
    setEditErrorMessage(null);
  };

  const handleRemoveClick = async () => {
    if (!confirm(`${attendee.student.name} 학생을 이 수업에서 제거할까요?`)) return;
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
      {/* Avatar + Name */}
      <div className="flex items-center gap-3">
        <div
          className={`w-12 h-12 rounded-2xl flex items-center justify-center text-xl font-bold shrink-0 ${avatarColor}`}
        >
          {attendee.student.name.charAt(0)}
        </div>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-1">
            {editing ? (
              <input
                value={name}
                onChange={event => setName(event.target.value)}
                className="flex-1 min-w-0 text-lg font-semibold text-gray-800 bg-purple-50 rounded-xl px-3 py-1 outline-none focus:ring-2 focus:ring-purple-300"
                placeholder="이름"
                autoFocus
              />
            ) : (
              <p className="flex-1 min-w-0 text-lg font-semibold text-gray-800 truncate">
                {attendee.student.name}
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
                  onClick={handleRemoveClick}
                  className="w-7 h-7 flex items-center justify-center rounded-xl bg-rose-50 hover:bg-rose-100 text-rose-400 transition-colors duration-150"
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
            )}
          </div>
          <p className="text-xs text-gray-400">{formatDate(attendee.createdAt)} 등록</p>
        </div>
      </div>

      {/* Edit / Remove Error */}
      {editErrorMessage && (
        <p className="text-xs text-rose-400 bg-rose-50 rounded-xl px-3 py-2">{editErrorMessage}</p>
      )}

      {/* Memo */}
      <div className={editing ? 'flex-1 flex flex-col' : ''}>
        {editing ? (
          <textarea
            value={memo}
            onChange={event => setMemo(event.target.value)}
            className="flex-1 w-full text-sm text-gray-600 bg-purple-50 rounded-2xl px-3 py-2 outline-none focus:ring-2 focus:ring-purple-300 resize-none"
            placeholder="메모를 입력하세요 (선택)"
            maxLength={500}
          />
        ) : (
          <p className="text-sm text-gray-500 leading-relaxed whitespace-pre-wrap break-words min-h-[3rem]">
            {attendee.student.memo || <span className="text-gray-300 italic">메모 없음</span>}
          </p>
        )}
      </div>

      {/* Keywords */}
      {!editing && (
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
      )}

      {/* Feedback Error */}
      {feedbackErrorMessage && !editing && (
        <p className="text-xs text-rose-400 bg-rose-50 rounded-xl px-3 py-2">{feedbackErrorMessage}</p>
      )}

      {/* Actions */}
      {editing ? (
        <div className="mt-auto flex gap-2">
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
      ) : (
        <AiFeedbackSection
          feedback={feedback}
          aiGenerating={aiGenerating}
          isEditingAiContent={isEditingAiContent}
          onGenerate={handleGenerate}
          onUpdateAiContent={handleUpdateAiContent}
          onLike={handleLike}
        />
      )}
    </div>
  );
});

export default AttendeeCard;
