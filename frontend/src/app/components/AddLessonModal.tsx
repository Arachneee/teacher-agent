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

const HOURS = Array.from({ length: 24 }, (_, i) => i);
const MINUTES = [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55];

function DateTimePicker({
  label,
  value,
  onChange,
  required,
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  required?: boolean;
}) {
  const [datePart, timePart] = value ? value.split('T') : ['', '00:00'];
  const hour = timePart ? parseInt(timePart.split(':')[0], 10) : 0;
  const minute = timePart ? parseInt(timePart.split(':')[1], 10) : 0;

  const minuteOptions = MINUTES.includes(minute) ? MINUTES : [...MINUTES, minute].sort((a, b) => a - b);

  const update = (newDate: string, newHour: number, newMinute: number) => {
    if (!newDate) return;
    onChange(`${newDate}T${String(newHour).padStart(2, '0')}:${String(newMinute).padStart(2, '0')}`);
  };

  return (
    <div>
      <label className="block text-sm font-medium text-gray-600 mb-1 ml-1">
        {label} {required && <span className="text-rose-400">*</span>}
      </label>
      <div className="bg-purple-50 rounded-2xl px-4 pt-3 pb-3 flex flex-col gap-2">
        <input
          type="date"
          value={datePart}
          onChange={event => update(event.target.value, hour, minute)}
          className="w-full bg-white rounded-xl px-3 py-2 text-gray-700 text-sm outline-none focus:ring-2 focus:ring-purple-300 cursor-pointer"
          required={required}
        />
        <div className="flex items-center gap-2">
          <select
            value={hour}
            onChange={event => update(datePart, parseInt(event.target.value, 10), minute)}
            className="flex-1 bg-white rounded-xl px-3 py-2 text-gray-700 text-sm outline-none focus:ring-2 focus:ring-purple-300 cursor-pointer appearance-none text-center"
          >
            {HOURS.map(h => (
              <option key={h} value={h}>
                {String(h).padStart(2, '0')}시
              </option>
            ))}
          </select>
          <span className="text-purple-300 font-bold text-lg select-none">:</span>
          <select
            value={minute}
            onChange={event => update(datePart, hour, parseInt(event.target.value, 10))}
            className="flex-1 bg-white rounded-xl px-3 py-2 text-gray-700 text-sm outline-none focus:ring-2 focus:ring-purple-300 cursor-pointer appearance-none text-center"
          >
            {minuteOptions.map(m => (
              <option key={m} value={m}>
                {String(m).padStart(2, '0')}분
              </option>
            ))}
          </select>
        </div>
      </div>
    </div>
  );
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

          <DateTimePicker
            label="시작 시간"
            value={startTime}
            onChange={setStartTime}
            required
          />

          <DateTimePicker
            label="종료 시간"
            value={endTime}
            onChange={setEndTime}
            required
          />

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
