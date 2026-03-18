'use client';

import { useEffect, useState, useCallback, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { Lesson, getLessons } from './lib/api';
import { padTwoDigits } from './lib/dateTimeUtils';
import { useAuth } from './context/AuthContext';
import WeeklyCalendarView from './components/WeeklyCalendarView';
import AddLessonModal from './components/AddLessonModal';
import Sidebar, { Tab } from './components/Sidebar';
import StudentsView from './components/StudentsView';

function getWeekStart(date: Date): Date {
  const result = new Date(date);
  const dayOfWeek = result.getDay();
  const daysToMonday = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;
  result.setDate(result.getDate() + daysToMonday);
  result.setHours(0, 0, 0, 0);
  return result;
}

function toISODateString(date: Date): string {
  return `${date.getFullYear()}-${padTwoDigits(date.getMonth() + 1)}-${padTwoDigits(date.getDate())}`;
}

function formatWeekRange(weekStart: Date): string {
  const weekEnd = new Date(weekStart);
  weekEnd.setDate(weekEnd.getDate() + 6);
  const startMonth = weekStart.getMonth() + 1;
  const endMonth = weekEnd.getMonth() + 1;
  const year = weekStart.getFullYear();
  if (startMonth === endMonth) {
    return `${year}년 ${startMonth}월 ${weekStart.getDate()}일 – ${weekEnd.getDate()}일`;
  }
  return `${year}년 ${startMonth}월 ${weekStart.getDate()}일 – ${endMonth}월 ${weekEnd.getDate()}일`;
}

export default function Home() {
  const { user, loading: authLoading, logout } = useAuth();
  const router = useRouter();
  const [activeTab, setActiveTab] = useState<Tab>('calendar');
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingLesson, setEditingLesson] = useState<Lesson | undefined>(undefined);
  const [pendingTime, setPendingTime] = useState<{ startTime: string; endTime: string } | null>(null);
  const [weekStart, setWeekStart] = useState<Date>(() => getWeekStart(new Date()));
  const dateInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (!authLoading && !user) {
      router.replace('/login');
    }
  }, [authLoading, user, router]);

  const fetchLessons = useCallback(() => {
    setLoading(true);
    getLessons(toISODateString(weekStart))
      .then(setLessons)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [weekStart]);

  useEffect(() => {
    if (activeTab === 'calendar') {
      fetchLessons();
    }
  }, [activeTab, fetchLessons]);

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

  const handleDelete = (id: number) => {
    setLessons(prev => prev.filter(lesson => lesson.id !== id));
  };

  const handleModalClose = () => {
    setShowModal(false);
    setEditingLesson(undefined);
    setPendingTime(null);
  };

  const goToPrevWeek = () => {
    setWeekStart(prev => {
      const next = new Date(prev);
      next.setDate(next.getDate() - 7);
      return next;
    });
  };

  const goToNextWeek = () => {
    setWeekStart(prev => {
      const next = new Date(prev);
      next.setDate(next.getDate() + 7);
      return next;
    });
  };

  const goToToday = () => {
    setWeekStart(getWeekStart(new Date()));
  };


  if (authLoading || !user) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-orange-50 flex items-center justify-center">
        <div className="w-10 h-10 border-4 border-purple-200 border-t-purple-400 rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-orange-50 flex">
      <Sidebar activeTab={activeTab} onTabChange={setActiveTab} />

      <div className="flex-1 flex flex-col min-w-0">
        <div className="px-6 py-8 flex-1">
          {/* Header */}
          <header className="mb-6 flex items-center justify-between">
            <div className="flex items-center gap-4 flex-wrap">
              {activeTab === 'calendar' ? (
                <>
                  <h1 className="text-3xl font-bold text-purple-500">내 수업</h1>
                  <div className="flex items-center gap-1">
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
                      <button
                        onClick={() => dateInputRef.current?.showPicker()}
                        className="text-sm font-medium text-gray-600 hover:text-purple-500 transition-colors cursor-pointer"
                      >
                        {formatWeekRange(weekStart)}
                      </button>
                      <input
                        ref={dateInputRef}
                        type="date"
                        className="absolute inset-0 opacity-0 pointer-events-none w-full h-full"
                        value={toISODateString(weekStart)}
                        onChange={e => {
                          if (e.target.value) setWeekStart(getWeekStart(new Date(e.target.value)));
                        }}
                      />
                    </div>
                  </div>
                </>
              ) : (
                <h1 className="text-3xl font-bold text-purple-500">학생 관리</h1>
              )}
            </div>
            <div className="flex items-center gap-3">
              <span className="text-sm text-gray-400">{user.userId}</span>
              <button
                onClick={logout}
                className="text-sm text-gray-400 hover:text-purple-500 transition-colors"
              >
                로그아웃
              </button>
            </div>
          </header>

          {/* Content */}
          {activeTab === 'calendar' ? (
            loading ? (
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
              />
            )
          ) : (
            <StudentsView />
          )}
        </div>
      </div>

      {/* Calendar FAB */}
      {activeTab === 'calendar' && (
        <button
          onClick={() => setShowModal(true)}
          className="fixed bottom-8 right-8 w-16 h-16 bg-pink-400 hover:bg-pink-500 active:scale-95 text-white text-3xl rounded-full shadow-lg hover:shadow-xl transition-all duration-200 flex items-center justify-center"
          aria-label="수업 추가"
        >
          +
        </button>
      )}

      {/* Modal */}
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
