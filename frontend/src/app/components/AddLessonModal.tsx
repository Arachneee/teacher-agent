'use client';

import { useState } from 'react';
import { Lesson, createLesson, updateLesson } from '../lib/api';

interface Props {
  lesson?: Lesson;
  initialStartTime?: string;
  initialEndTime?: string;
  onSave: () => void;
  onClose: () => void;
}

const HOURS = Array.from({ length: 24 }, (_, i) => i);
const MINUTES = [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55];

function pad(n: number): string {
  return String(n).padStart(2, '0');
}

function parseDateTime(iso: string): { date: string; hour: number; minute: number } {
  const [datePart, timePart] = iso.slice(0, 16).split('T');
  const [hour, minute] = timePart.split(':').map(Number);
  return { date: datePart, hour, minute };
}

function TimePicker({
  label,
  hour,
  minute,
  onHourChange,
  onMinuteChange,
}: {
  label: string;
  hour: number;
  minute: number;
  onHourChange: (h: number) => void;
  onMinuteChange: (m: number) => void;
}) {
  const minuteOptions = MINUTES.includes(minute) ? MINUTES : [...MINUTES, minute].sort((a, b) => a - b);
  return (
    <div className="flex-1">
      <label className="block text-xs font-medium text-gray-500 mb-1 ml-1">{label}</label>
      <div className="flex items-center gap-1.5">
        <select
          value={hour}
          onChange={e => onHourChange(parseInt(e.target.value, 10))}
          className="flex-1 bg-white rounded-xl px-2 py-2 text-gray-700 text-sm outline-none focus:ring-2 focus:ring-purple-300 cursor-pointer appearance-none text-center"
        >
          {HOURS.map(h => (
            <option key={h} value={h}>{pad(h)}시</option>
          ))}
        </select>
        <span className="text-purple-300 font-bold select-none">:</span>
        <select
          value={minute}
          onChange={e => onMinuteChange(parseInt(e.target.value, 10))}
          className="flex-1 bg-white rounded-xl px-2 py-2 text-gray-700 text-sm outline-none focus:ring-2 focus:ring-purple-300 cursor-pointer appearance-none text-center"
        >
          {minuteOptions.map(m => (
            <option key={m} value={m}>{pad(m)}분</option>
          ))}
        </select>
      </div>
    </div>
  );
}

function getInitialValues(lesson?: Lesson, initialStartTime?: string, initialEndTime?: string) {
  if (lesson) {
    const start = parseDateTime(lesson.startTime);
    const end = parseDateTime(lesson.endTime);
    return { date: start.date, startHour: start.hour, startMinute: start.minute, endHour: end.hour, endMinute: end.minute };
  }
  if (initialStartTime && initialEndTime) {
    const start = parseDateTime(initialStartTime);
    const end = parseDateTime(initialEndTime);
    return { date: start.date, startHour: start.hour, startMinute: start.minute, endHour: end.hour, endMinute: end.minute };
  }
  const now = new Date();
  const roundedMinute = Math.round(now.getMinutes() / 5) * 5 % 60;
  const startHour = roundedMinute === 60 ? now.getHours() + 1 : now.getHours();
  const todayStr = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
  return { date: todayStr, startHour, startMinute: roundedMinute % 60, endHour: (startHour + 1) % 24, endMinute: roundedMinute % 60 };
}

export default function AddLessonModal({ lesson, initialStartTime, initialEndTime, onSave, onClose }: Props) {
  const isEditMode = lesson !== undefined;
  const initial = getInitialValues(lesson, initialStartTime, initialEndTime);

  const [title, setTitle] = useState(lesson?.title ?? '');
  const [date, setDate] = useState(initial.date);
  const [startHour, setStartHour] = useState(initial.startHour);
  const [startMinute, setStartMinute] = useState(initial.startMinute);
  const [endHour, setEndHour] = useState(initial.endHour);
  const [endMinute, setEndMinute] = useState(initial.endMinute);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const buildIso = (hour: number, minute: number) =>
    `${date}T${pad(hour)}:${pad(minute)}:00`;

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!title.trim() || !date) return;
    setLoading(true);
    setErrorMessage(null);
    try {
      if (isEditMode) {
        await updateLesson(lesson.id, title.trim(), buildIso(startHour, startMinute), buildIso(endHour, endMinute));
      } else {
        await createLesson(title.trim(), buildIso(startHour, startMinute), buildIso(endHour, endMinute));
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
              날짜 <span className="text-rose-400">*</span>
            </label>
            <input
              type="date"
              value={date}
              onChange={event => setDate(event.target.value)}
              className="w-full bg-purple-50 rounded-2xl px-4 py-3 text-gray-700 text-sm outline-none focus:ring-2 focus:ring-purple-300 cursor-pointer"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-600 mb-2 ml-1">
              시간 <span className="text-rose-400">*</span>
            </label>
            <div className="bg-purple-50 rounded-2xl px-4 py-3 flex items-end gap-3">
              <TimePicker
                label="시작"
                hour={startHour}
                minute={startMinute}
                onHourChange={setStartHour}
                onMinuteChange={setStartMinute}
              />
              <span className="text-gray-300 font-medium pb-2">–</span>
              <TimePicker
                label="종료"
                hour={endHour}
                minute={endMinute}
                onHourChange={setEndHour}
                onMinuteChange={setEndMinute}
              />
            </div>
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
              disabled={loading || !title.trim() || !date}
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
