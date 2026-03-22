'use client';

import { useMemo, useCallback, useEffect, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Lesson, deleteLesson, UpdateScope } from '../lib/api';
import { padTwoDigits } from '../lib/dateTimeUtils';
import RecurringScopeModal from '../components/RecurringScopeModal';
import ConfirmModal from './ConfirmModal';

const FIRST_HOUR = 0;
const LAST_HOUR = 24;
const TOTAL_HOURS = LAST_HOUR - FIRST_HOUR;
const CELL_HEIGHT = 80;

const DAY_NAMES = ['월', '화', '수', '목', '금', '토', '일'];

function addDays(date: Date, days: number): Date {
  const result = new Date(date);
  result.setDate(result.getDate() + days);
  return result;
}

function formatKoreanTime(isoString: string): string {
  const date = new Date(isoString);
  return `${padTwoDigits(date.getHours())}:${padTwoDigits(date.getMinutes())}`;
}

function getLessonPosition(lesson: Lesson): { top: number; height: number } {
  const start = new Date(lesson.startTime);
  const end = new Date(lesson.endTime);
  const startHourDecimal = start.getHours() + start.getMinutes() / 60;
  const endHourDecimal = end.getHours() + end.getMinutes() / 60;

  const clampedStart = Math.max(startHourDecimal, FIRST_HOUR);
  const clampedEnd = Math.min(endHourDecimal, LAST_HOUR);

  const top = (clampedStart - FIRST_HOUR) * CELL_HEIGHT;
  const height = Math.max((clampedEnd - clampedStart) * CELL_HEIGHT, 24);
  return { top, height };
}

interface Props {
  lessons: Lesson[];
  weekStart: Date;
  onEdit: (lesson: Lesson) => void;
  onDelete: (id: number, didDeleteMultiple: boolean) => void;
  onCellClick: (startTime: string, endTime: string) => void;
  mobileSelectedDayIndex: number;
}

export default function WeeklyCalendarView({ lessons, weekStart, onEdit, onDelete, onCellClick, mobileSelectedDayIndex }: Props) {
  const router = useRouter();
  const scrollRef = useRef<HTMLDivElement>(null);
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const [deleteErrorMessage, setDeleteErrorMessage] = useState<string | null>(null);
  const [scopeModalLesson, setScopeModalLesson] = useState<Lesson | null>(null);
  const [deleteConfirmLesson, setDeleteConfirmLesson] = useState<Lesson | null>(null);

  const weekDays = useMemo(
    () => Array.from({ length: 7 }, (_, i) => addDays(weekStart, i)),
    [weekStart]
  );

  const hours = useMemo(
    () => Array.from({ length: TOTAL_HOURS }, (_, i) => FIRST_HOUR + i),
    []
  );

  const lessonsByDay = useMemo(() => {
    const map = new Map<number, Lesson[]>();
    for (let i = 0; i < 7; i++) map.set(i, []);
    lessons.forEach(lesson => {
      const lessonDate = new Date(lesson.startTime);
      weekDays.forEach((day, index) => {
        if (
          lessonDate.getFullYear() === day.getFullYear() &&
          lessonDate.getMonth() === day.getMonth() &&
          lessonDate.getDate() === day.getDate()
        ) {
          map.get(index)!.push(lesson);
        }
      });
    });
    return map;
  }, [lessons, weekDays]);

  useEffect(() => {
    if (scrollRef.current) {
      let scrollTop: number;
      if (lessons.length > 0) {
        const earliestHour = Math.min(
          ...lessons.map(lesson => new Date(lesson.startTime).getHours())
        );
        scrollTop = Math.max(0, (earliestHour - FIRST_HOUR - 1) * CELL_HEIGHT);
      } else {
        const containerHeight = scrollRef.current.clientHeight;
        const noonPosition = (12 - FIRST_HOUR) * CELL_HEIGHT;
        scrollTop = noonPosition - containerHeight / 2 + CELL_HEIGHT;
      }
      scrollRef.current.scrollTop = scrollTop;
    }
  }, [lessons]);

  const handleCellClick = useCallback(
    (day: Date, hour: number) => {
      const startDate = new Date(day);
      startDate.setHours(hour, 0, 0, 0);
      const endDate = new Date(startDate);
      endDate.setHours(hour + 1, 0, 0, 0);

      const fmt = (date: Date) =>
        `${date.getFullYear()}-${padTwoDigits(date.getMonth() + 1)}-${padTwoDigits(date.getDate())}T${padTwoDigits(date.getHours())}:${padTwoDigits(date.getMinutes())}:00`;

      onCellClick(fmt(startDate), fmt(endDate));
    },
    [onCellClick]
  );

  const handleConfirmDelete = async () => {
    if (!deleteConfirmLesson) return;
    const lesson = deleteConfirmLesson;
    setDeleteConfirmLesson(null);
    setDeletingId(lesson.id);
    setDeleteErrorMessage(null);
    try {
      await deleteLesson(lesson.id);
      onDelete(lesson.id, false);
    } catch {
      setDeleteErrorMessage(`"${lesson.title}" 수업을 삭제하지 못했어요.`);
    } finally {
      setDeletingId(null);
    }
  };

  const today = new Date();

  const currentTimeTop = useMemo(() => {
    const now = new Date();
    const hourDecimal = now.getHours() + now.getMinutes() / 60;
    if (hourDecimal < FIRST_HOUR || hourDecimal > LAST_HOUR) return null;
    return (hourDecimal - FIRST_HOUR) * CELL_HEIGHT;
  }, []);

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden flex flex-col">
      {deleteErrorMessage && (
        <div className="flex items-center justify-between bg-rose-50 text-rose-500 text-xs px-4 py-2.5 border-b border-rose-100">
          <span>{deleteErrorMessage}</span>
          <button onClick={() => setDeleteErrorMessage(null)} className="ml-2 hover:text-rose-700 transition-colors">✕</button>
        </div>
      )}
      {/* Scrollable container wrapping header + grid for consistent width */}
      <div ref={scrollRef} className="overflow-y-scroll flex-1 max-h-[calc(100vh-200px)] md:max-h-[calc(100vh-200px)]" style={{ maxHeight: 'min(calc(100vh - 200px), calc(100dvh - 200px))' }}>

      {/* Day Headers - sticky inside scroll container */}
      <div className="flex border-b border-gray-100 bg-white sticky top-0 z-20" style={{ paddingLeft: '56px' }}>
        {weekDays.map((day, i) => {
          const dayName = DAY_NAMES[i];
          const isToday =
            day.getFullYear() === today.getFullYear() &&
            day.getMonth() === today.getMonth() &&
            day.getDate() === today.getDate();
          const isWeekend = i === 5 || i === 6;

          return (
            <div
              key={i}
              className={`py-3 text-center border-l border-gray-100 ${isWeekend ? 'bg-slate-50/80' : ''} ${i === mobileSelectedDayIndex ? 'flex-1' : 'hidden md:block md:flex-1'}`}
            >
              <div
                className={`text-xs font-medium mb-1.5 ${
                  isWeekend ? 'text-rose-400' : isToday ? 'text-purple-500' : 'text-gray-400'
                }`}
              >
                {dayName}
              </div>
              <div
                className={`text-sm font-bold w-8 h-8 flex items-center justify-center mx-auto rounded-full transition-colors ${
                  isToday
                    ? 'bg-purple-500 text-white'
                    : isWeekend
                    ? 'text-rose-500'
                    : 'text-gray-700'
                }`}
              >
                {day.getDate()}
              </div>
            </div>
          );
        })}
      </div>

      {/* Grid */}
        <div className="flex">
          {/* Time Labels */}
          <div className="flex-shrink-0 w-14">
            {hours.map(hour => (
              <div
                key={hour}
                className="flex items-start justify-end pr-3 border-b border-gray-100"
                style={{ height: `${CELL_HEIGHT}px` }}
              >
                <span className="text-xs text-gray-400 mt-1 tabular-nums">
                  {String(hour).padStart(2, '0')}:00
                </span>
              </div>
            ))}
          </div>

          {/* Day Columns */}
          {weekDays.map((day, dayIndex) => {
            const dayLessons = lessonsByDay.get(dayIndex) || [];
            const isToday =
              day.getFullYear() === today.getFullYear() &&
              day.getMonth() === today.getMonth() &&
              day.getDate() === today.getDate();
            const isWeekend = dayIndex === 5 || dayIndex === 6;

            return (
              <div
                key={dayIndex}
                className={`relative border-l border-gray-100 min-w-0 ${isWeekend ? 'bg-slate-50/40' : ''} ${dayIndex === mobileSelectedDayIndex ? 'flex-1' : 'hidden md:block md:flex-1'}`}
                style={{ height: `${TOTAL_HOURS * CELL_HEIGHT}px` }}
              >
                {/* Hour slot click targets */}
                {hours.map(hour => (
                  <div
                    key={hour}
                    className="absolute w-full border-b border-gray-100 hover:bg-purple-50/60 transition-colors cursor-pointer group"
                    style={{
                      top: `${(hour - FIRST_HOUR) * CELL_HEIGHT}px`,
                      height: `${CELL_HEIGHT}px`,
                    }}
                    onClick={() => handleCellClick(day, hour)}
                  >
                    <span className="opacity-0 group-hover:opacity-100 absolute bottom-1 right-1 text-purple-300 text-xs transition-opacity select-none">
                      +
                    </span>
                  </div>
                ))}

                {/* Current time indicator */}
                {isToday && currentTimeTop !== null && (
                  <div
                    className="absolute left-0 right-0 z-10 pointer-events-none"
                    style={{ top: `${currentTimeTop}px` }}
                  >
                    <div className="flex items-center">
                      <div className="w-2 h-2 rounded-full bg-rose-400 -ml-1 flex-shrink-0" />
                      <div className="flex-1 h-px bg-rose-400" />
                    </div>
                  </div>
                )}

                {/* Lesson blocks */}
                {dayLessons.map(lesson => {
                  const { top, height } = getLessonPosition(lesson);
                  return (
                    <div
                      key={lesson.id}
                      className="absolute left-0.5 right-0.5 rounded-lg overflow-hidden cursor-pointer z-10 shadow-sm hover:shadow-md transition-shadow group/lesson border border-violet-300"
                      style={{ top: `${top}px`, height: `${height}px` }}
                      onClick={event => {
                        event.stopPropagation();
                        router.push(`/lessons/${lesson.id}`);
                      }}
                    >
                      <div className="h-full bg-violet-400 px-2 py-1 relative">
                        <div className="text-white text-xs font-semibold truncate leading-tight pr-12 flex items-center gap-1">
                          <span className="truncate">{lesson.title}</span>
                          {lesson.recurrenceGroupId !== null && (
                            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" className="text-purple-100 flex-shrink-0">
                              <path d="M17 1l4 4-4 4" />
                              <path d="M3 11V9a4 4 0 014-4h14" />
                              <path d="M7 23l-4-4 4-4" />
                              <path d="M21 13v2a4 4 0 01-4 4H3" />
                            </svg>
                          )}
                        </div>
                        {height > 32 && (
                          <div className="text-purple-100 text-xs leading-tight mt-0.5 truncate pr-12">
                            {formatKoreanTime(lesson.startTime)} –{' '}
                            {formatKoreanTime(lesson.endTime)}
                          </div>
                        )}
                        {/* Edit / Delete buttons */}
                        <div className="absolute top-0.5 right-0.5 flex gap-0.5 opacity-0 group-hover/lesson:opacity-100 transition-opacity">
                          <button
                            onClick={event => {
                              event.stopPropagation();
                              onEdit(lesson);
                            }}
                            className="w-5 h-5 flex items-center justify-center rounded bg-white/25 hover:bg-white/45 text-white transition-colors"
                            aria-label="수정"
                          >
                            <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                            </svg>
                          </button>
                          <button
                            onClick={event => {
                              event.stopPropagation();
                              
                              if (lesson.recurrenceGroupId !== null) {
                                setScopeModalLesson(lesson);
                                return;
                              }
                              
                              setDeleteConfirmLesson(lesson);
                            }}
                            disabled={deletingId === lesson.id}
                            className="w-5 h-5 flex items-center justify-center rounded bg-white/20 hover:bg-rose-400/80 text-white transition-colors"
                            aria-label="삭제"
                          >
                            <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                              <polyline points="3 6 5 6 21 6" />
                              <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
                              <path d="M10 11v6M14 11v6" />
                              <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" />
                            </svg>
                          </button>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            );
          })}
        </div>
      </div>
      
      {scopeModalLesson && (
        <RecurringScopeModal
          mode="delete"
          lessonTitle={scopeModalLesson.title}
          onSelect={async (scope: UpdateScope) => {
            const lessonToDelete = scopeModalLesson;
            setScopeModalLesson(null);
            setDeletingId(lessonToDelete.id);
            setDeleteErrorMessage(null);
            try {
              await deleteLesson(lessonToDelete.id, scope);
              onDelete(lessonToDelete.id, scope !== 'SINGLE');
            } catch {
              setDeleteErrorMessage(`"${lessonToDelete.title}" 수업을 삭제하지 못했어요.`);
            } finally {
              setDeletingId(null);
            }
          }}
          onClose={() => setScopeModalLesson(null)}
        />
      )}

      {deleteConfirmLesson && (
        <ConfirmModal
          title="수업 삭제"
          message={`"${deleteConfirmLesson.title}" 수업을 삭제할까요?\n수강생과 피드백도 함께 삭제됩니다.`}
          confirmText="삭제"
          variant="danger"
          onConfirm={handleConfirmDelete}
          onCancel={() => setDeleteConfirmLesson(null)}
        />
      )}
    </div>
  );
}
