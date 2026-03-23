'use client';

import { useEffect, useState, useCallback, useMemo } from 'react';
import { Lesson, getLessons } from '../../lib/api';
import { padTwoDigits } from '../../lib/dateTimeUtils';
import { useAuth } from '../../context/AuthContext';
import WeeklyCalendarView from '../../components/WeeklyCalendarView';
import AddLessonModal from '../../components/AddLessonModal';
import DatePicker from '../../components/DatePicker';

function getWeekStart(date: Date): Date {
  const result = new Date(date);
  const dayOfWeek = result.getDay();
  const daysToMonday = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;
  result.setDate(result.getDate() + daysToMonday);
  result.setHours(0, 0, 0, 0);
  return result;
}

function addDays(date: Date, days: number): Date {
  const result = new Date(date);
  result.setDate(result.getDate() + days);
  return result;
}

function toISODateString(date: Date): string {
  return `${date.getFullYear()}-${padTwoDigits(date.getMonth() + 1)}-${padTwoDigits(date.getDate())}`;
}

const DAY_NAMES_FULL = ['일', '월', '화', '수', '목', '금', '토'];

function formatMobileDateLabel(date: Date): string {
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const dayName = DAY_NAMES_FULL[date.getDay()];
  return `${month}월 ${day}일 (${dayName})`;
}

export default function CalendarPage() {
  const { user, logout } = useAuth();
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingLesson, setEditingLesson] = useState<Lesson | undefined>(undefined);
  const [pendingTime, setPendingTime] = useState<{ startTime: string; endTime: string } | null>(null);

  const [currentDay, setCurrentDay] = useState<Date>(() => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return today;
  });

  const weekStart = useMemo(() => getWeekStart(currentDay), [currentDay]);
  const weekStartString = useMemo(() => toISODateString(weekStart), [weekStart]);

  const mobileSelectedDayIndex = useMemo(() => {
    const diffMs = currentDay.getTime() - weekStart.getTime();
    return Math.round(diffMs / (1000 * 60 * 60 * 24));
  }, [currentDay, weekStart]);

  const fetchLessons = useCallback(() => {
    setLoading(true);
    getLessons(weekStartString)
      .then(setLessons)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [weekStartString]);

  useEffect(() => {
    fetchLessons();
  }, [fetchLessons]);

  const handleSave = () => {
    setShowModal(false);
    setEditingLesson(undefined);
    setPendingTime(null);
    fetchLessons();
  };

  const handleEditClick = (lesson: Lesson) => {
    setEditingLesson(lesson);
    setPendingTime(null);
    setShowModal(true);
  };

  const handleCellClick = (startTime: string, endTime: string) => {
    setEditingLesson(undefined);
    setPendingTime({ startTime, endTime });
    setShowModal(true);
  };

  const handleDelete = (id: number, didDeleteMultiple: boolean) => {
    if (didDeleteMultiple) {
      fetchLessons();
    } else {
      setLessons(prev => prev.filter(lesson => lesson.id !== id));
    }
  };

  const handleModalClose = () => {
    setShowModal(false);
    setEditingLesson(undefined);
    setPendingTime(null);
  };

  const goToPrevWeek = () => setCurrentDay(prev => addDays(prev, -7));
  const goToNextWeek = () => setCurrentDay(prev => addDays(prev, 7));
  const goToPrevDay = () => setCurrentDay(prev => addDays(prev, -1));
  const goToNextDay = () => setCurrentDay(prev => addDays(prev, 1));

  const goToToday = () => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    setCurrentDay(today);
  };

  return (
    <div className="flex flex-col flex-1 px-4 md:px-6 py-5 md:py-8">
      <header className="relative mb-4 md:mb-6 flex items-center justify-between">
        <div className="flex items-center gap-3 flex-wrap">
          <h1 className="text-2xl md:text-3xl font-bold text-purple-500">내 수업</h1>

          <div className="hidden md:flex items-center gap-1">
            <button
              onClick={goToPrevWeek}
              className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-purple-100 text-gray-500 hover:text-purple-500 transition-colors text-lg"
              aria-label="이전 주"
            >
              ‹
            </button>
            <button
              onClick={goToToday}
              className="text-sm px-3 py-1 rounded-full border border-gray-200 hover:border-purple-300 hover:text-purple-500 text-gray-500 transition-colors"
            >
              오늘
            </button>
            <button
              onClick={goToNextWeek}
              className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-purple-100 text-gray-500 hover:text-purple-500 transition-colors text-lg"
              aria-label="다음 주"
            >
              ›
            </button>
            <div className="relative ml-1">
              <DatePicker
                value={toISODateString(currentDay)}
                onChange={value => {
                  if (value) {
                    const selected = new Date(value);
                    selected.setHours(0, 0, 0, 0);
                    setCurrentDay(selected);
                  }
                }}
                showWeekRange
              />
            </div>
          </div>

          <div className="flex md:hidden items-center gap-1">
            <button
              onClick={goToPrevDay}
              className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-purple-100 text-gray-500 hover:text-purple-500 transition-colors text-lg"
              aria-label="이전 날"
            >
              ‹
            </button>
            <button
              onClick={goToToday}
              className="text-xs px-2.5 py-1 rounded-full border border-gray-200 hover:border-purple-300 hover:text-purple-500 text-gray-500 transition-colors"
            >
              오늘
            </button>
            <button
              onClick={goToNextDay}
              className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-purple-100 text-gray-500 hover:text-purple-500 transition-colors text-lg"
              aria-label="다음 날"
            >
              ›
            </button>
          </div>
        </div>

        <div className="flex md:hidden absolute left-1/2 -translate-x-1/2 top-[1.25rem] pointer-events-none">
          <span className="text-sm font-medium text-gray-600">
            {formatMobileDateLabel(currentDay)}
          </span>
        </div>

        <div className="flex items-center gap-2 md:gap-3">
          <span className="hidden md:inline text-sm text-gray-400">{user?.userId}</span>
          <button
            onClick={logout}
            className="text-xs md:text-sm text-gray-400 hover:text-purple-500 transition-colors"
          >
            로그아웃
          </button>
        </div>
      </header>

      {loading ? (
        <div className="flex flex-col items-center justify-center h-96 gap-3 bg-white rounded-2xl shadow-sm border border-gray-100">
          <div className="w-10 h-10 border-4 border-purple-200 border-t-purple-400 rounded-full animate-spin" />
          <p className="text-gray-400 text-sm">불러오는 중...</p>
        </div>
      ) : (
        <WeeklyCalendarView
          lessons={lessons}
          weekStart={weekStart}
          onEdit={handleEditClick}
          onDelete={handleDelete}
          onCellClick={handleCellClick}
          mobileSelectedDayIndex={mobileSelectedDayIndex}
        />
      )}

      <button
        onClick={() => setShowModal(true)}
        className="fixed bottom-20 right-5 md:bottom-8 md:right-8 w-14 h-14 md:w-16 md:h-16 bg-pink-400 hover:bg-pink-500 active:scale-95 text-white text-3xl rounded-full shadow-lg hover:shadow-xl transition-all duration-200 flex items-center justify-center"
        aria-label="수업 추가"
      >
        +
      </button>

      {showModal && (
        <AddLessonModal
          lesson={editingLesson}
          initialStartTime={pendingTime?.startTime}
          initialEndTime={pendingTime?.endTime}
          onSave={handleSave}
          onClose={handleModalClose}
        />
      )}
    </div>
  );
}
