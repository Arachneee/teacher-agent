'use client';

import { useState } from 'react';
import { Lesson, createLesson, updateLesson } from '../lib/api';

interface Props {
  lesson?: Lesson;
  onSave: () => void;
  onClose: () => void;
}

function toDatetimeLocalValue(iso: string): string {
  return iso.slice(0, 16);
}

function toIsoString(datetimeLocalValue: string): string {
  return datetimeLocalValue + ':00';
}

export default function AddLessonModal({ lesson, onSave, onClose }: Props) {
  const isEditMode = lesson !== undefined;
  const [title, setTitle] = useState(lesson?.title ?? '');
  const [startTime, setStartTime] = useState(lesson ? toDatetimeLocalValue(lesson.startTime) : '');
  const [endTime, setEndTime] = useState(lesson ? toDatetimeLocalValue(lesson.endTime) : '');
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!title.trim() || !startTime || !endTime) return;
    setLoading(true);
    setErrorMessage(null);
    try {
      if (isEditMode) {
        await updateLesson(lesson.id, title.trim(), toIsoString(startTime), toIsoString(endTime));
      } else {
        await createLesson(title.trim(), toIsoString(startTime), toIsoString(endTime));
      }
      onSave();
    } catch {
      setErrorMessage(isEditMode ? '수업을 수정하지 못했어요.' : '수업을 추가하지 못했어요.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="fixed inset-0 bg-black/30 backdrop-blur-sm flex items-center justify-center z-50 p-4"
      onClick={event => event.target === event.currentTarget && onClose()}
    >
      <div className="bg-white rounded-3xl p-8 w-full max-w-md shadow-2xl">
        <div className="text-center mb-6">
          <div className="text-4xl mb-2">{isEditMode ? '✏️' : '📚'}</div>
          <h2 className="text-2xl font-bold text-gray-800">
            {isEditMode ? '수업 수정' : '새 수업 추가'}
          </h2>
          <p className="text-sm text-gray-400 mt-1">
            {isEditMode ? '수업 정보를 수정해요' : '새로운 수업을 등록해요'}
          </p>
        </div>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-600 mb-1 ml-1">
              수업 제목 <span className="text-rose-400">*</span>
            </label>
            <input
              value={title}
              onChange={event => setTitle(event.target.value)}
              className="w-full bg-purple-50 rounded-2xl px-4 py-3 text-gray-800 outline-none focus:ring-2 focus:ring-purple-300 placeholder-gray-300"
              placeholder="수업 제목을 입력하세요"
              autoFocus
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-600 mb-1 ml-1">
              시작 시간 <span className="text-rose-400">*</span>
            </label>
            <input
              type="datetime-local"
              value={startTime}
              onChange={event => setStartTime(event.target.value)}
              className="w-full bg-purple-50 rounded-2xl px-4 py-3 text-gray-800 outline-none focus:ring-2 focus:ring-purple-300"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-600 mb-1 ml-1">
              종료 시간 <span className="text-rose-400">*</span>
            </label>
            <input
              type="datetime-local"
              value={endTime}
              onChange={event => setEndTime(event.target.value)}
              className="w-full bg-purple-50 rounded-2xl px-4 py-3 text-gray-800 outline-none focus:ring-2 focus:ring-purple-300"
              required
            />
          </div>

          {errorMessage && (
            <p className="text-xs text-rose-400 bg-rose-50 rounded-xl px-3 py-2">{errorMessage}</p>
          )}

          <div className="flex gap-3 mt-2">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-500 font-medium py-3 rounded-2xl transition-colors duration-150"
            >
              취소
            </button>
            <button
              type="submit"
              disabled={loading || !title.trim() || !startTime || !endTime}
              className="flex-1 bg-pink-400 hover:bg-pink-500 disabled:bg-pink-200 text-white font-medium py-3 rounded-2xl transition-colors duration-150"
            >
              {loading ? (isEditMode ? '저장 중...' : '추가 중...') : (isEditMode ? '저장하기' : '추가하기 ✨')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
