'use client';

import { useEffect, useMemo, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import {
  DndContext,
  DragEndEvent,
  PointerSensor,
  closestCenter,
  useSensor,
  useSensors,
} from '@dnd-kit/core';
import { SortableContext, rectSortingStrategy, useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { Attendee, Lesson, LessonDetailAttendee, getLessonDetail, removeAttendee, updateLesson } from '../../lib/api';
import { useAuth } from '../../context/AuthContext';
import AttendeeCard from '../../components/AttendeeCard';
import AddAttendeeModal from '../../components/AddAttendeeModal';
import { MAX_COLUMNS, MIN_COLUMNS, useGridLayout } from '../../hooks/useGridLayout';

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

function formatLessonDateTime(startIso: string, endIso: string): string {
  const start = new Date(startIso);
  const end = new Date(endIso);
  const days = ['일', '월', '화', '수', '목', '금', '토'];
  const month = start.getMonth() + 1;
  const day = start.getDate();
  const dayOfWeek = days[start.getDay()];
  return `${month}월 ${day}일 (${dayOfWeek})  ${pad(start.getHours())}:${pad(start.getMinutes())} – ${pad(end.getHours())}:${pad(end.getMinutes())}`;
}

function TimeSelect({
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
      <p className="text-xs font-medium text-gray-400 mb-1 ml-0.5">{label}</p>
      <div className="flex items-center gap-1.5">
        <select
          value={hour}
          onChange={e => onHourChange(parseInt(e.target.value, 10))}
          className="flex-1 bg-white rounded-xl px-2 py-2 text-gray-700 text-sm outline-none focus:ring-2 focus:ring-purple-300 cursor-pointer appearance-none text-center border border-purple-100"
        >
          {HOURS.map(h => (
            <option key={h} value={h}>{pad(h)}시</option>
          ))}
        </select>
        <span className="text-purple-300 font-bold select-none">:</span>
        <select
          value={minute}
          onChange={e => onMinuteChange(parseInt(e.target.value, 10))}
          className="flex-1 bg-white rounded-xl px-2 py-2 text-gray-700 text-sm outline-none focus:ring-2 focus:ring-purple-300 cursor-pointer appearance-none text-center border border-purple-100"
        >
          {minuteOptions.map(m => (
            <option key={m} value={m}>{pad(m)}분</option>
          ))}
        </select>
      </div>
    </div>
  );
}

const EMPTY_SLOT_PREFIX = 'empty-slot-';

function SortableEmptySlot({ id, isDragActive }: { id: string; isDragActive: boolean }) {
  const { setNodeRef, isOver, transform, transition } = useSortable({
    id,
    disabled: { draggable: true },
  });

  return (
    <div
      ref={setNodeRef}
      style={{ transform: CSS.Transform.toString(transform), transition }}
      className={`h-full min-h-[200px] rounded-2xl border-2 border-dashed transition-colors ${
        isDragActive
          ? isOver
            ? 'border-purple-300 bg-purple-50/50'
            : 'border-gray-200'
          : 'border-transparent'
      }`}
    />
  );
}

interface SortableAttendeeCardProps {
  attendee: Attendee;
  onUpdate: () => void;
  onRemove: (attendeeId: number) => void;
}

function SortableAttendeeCard({ attendee, onUpdate, onRemove }: SortableAttendeeCardProps) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: attendee.id,
  });

  return (
    <div
      ref={setNodeRef}
      style={{
        transform: CSS.Transform.toString(transform),
        transition,
        opacity: isDragging ? 0.5 : 1,
        zIndex: isDragging ? 10 : undefined,
      }}
      className="h-full"
    >
      <AttendeeCard
        attendee={attendee}
        onUpdate={onUpdate}
        onRemove={onRemove}
        dragHandleProps={{ ...attributes, ...listeners }}
      />
    </div>
  );
}

export default function LessonDetailPage() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();
  const params = useParams();
  const lessonId = parseInt(params.lessonId as string, 10);

  const [lesson, setLesson] = useState<Lesson | null>(null);
  const [attendees, setAttendees] = useState<Attendee[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [isDragActive, setIsDragActive] = useState(false);

  const [isEditingTime, setIsEditingTime] = useState(false);
  const [editTitle, setEditTitle] = useState('');
  const [editDate, setEditDate] = useState('');
  const [editStartHour, setEditStartHour] = useState(0);
  const [editStartMinute, setEditStartMinute] = useState(0);
  const [editEndHour, setEditEndHour] = useState(0);
  const [editEndMinute, setEditEndMinute] = useState(0);
  const [isSaving, setIsSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);

  const {
    gridSlots,
    setGridSlots,
    columnCount,
    handleColumnCountChange,
    initializeGridSlots,
    removeFromGrid,
  } = useGridLayout(`lesson_${lessonId}`);

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 8 } })
  );

  useEffect(() => {
    if (!authLoading && !user) {
      router.replace('/login');
    }
  }, [authLoading, user, router]);

  const toAttendee = ({ attendeeId, student, feedback }: LessonDetailAttendee, lessonDetailId: number): Attendee => ({
    id: attendeeId,
    lessonId: lessonDetailId,
    student: { id: student.id, name: student.name, memo: student.memo },
    feedback: feedback ? {
      id: feedback.id,
      studentId: feedback.studentId,
      aiContent: feedback.aiContent,
      keywords: feedback.keywords,
      liked: feedback.liked,
      createdAt: feedback.createdAt,
      updatedAt: feedback.updatedAt,
    } : null,
    createdAt: student.createdAt,
  });

  const fetchData = () => {
    getLessonDetail(lessonId)
      .then(detail => {
        setLesson({ id: detail.id, title: detail.title, startTime: detail.startTime, endTime: detail.endTime });
        const mapped = detail.attendees.map(attendee => toAttendee(attendee, detail.id));
        setAttendees(mapped);
        initializeGridSlots(mapped.map(attendee => attendee.id));
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchData();
  }, [lessonId]);

  const openEditTime = () => {
    if (!lesson) return;
    const start = parseDateTime(lesson.startTime);
    const end = parseDateTime(lesson.endTime);
    setEditTitle(lesson.title);
    setEditDate(start.date);
    setEditStartHour(start.hour);
    setEditStartMinute(start.minute);
    setEditEndHour(end.hour);
    setEditEndMinute(end.minute);
    setSaveError(null);
    setIsEditingTime(true);
  };

  const handleSaveTime = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!lesson || !editDate) return;
    setIsSaving(true);
    setSaveError(null);
    const startIso = `${editDate}T${pad(editStartHour)}:${pad(editStartMinute)}:00`;
    const endIso = `${editDate}T${pad(editEndHour)}:${pad(editEndMinute)}:00`;
    try {
      const updated = await updateLesson(lesson.id, editTitle.trim() || lesson.title, startIso, endIso);
      setLesson(updated);
      setIsEditingTime(false);
    } catch {
      setSaveError('수업 시간을 수정하지 못했어요.');
    } finally {
      setIsSaving(false);
    }
  };

  const handleRemoveAttendee = async (attendeeId: number) => {
    try {
      await removeAttendee(lessonId, attendeeId);
      setAttendees(prev => prev.filter(attendee => attendee.id !== attendeeId));
      removeFromGrid(attendeeId);
    } catch {
      fetchData();
    }
  };

  const handleUpdate = () => {
    fetchData();
  };

  const handleAdd = () => {
    setShowModal(false);
    setLoading(true);
    fetchData();
  };

  const handleDragEnd = (event: DragEndEvent) => {
    setIsDragActive(false);
    const { active, over } = event;
    if (!over || active.id === over.id) return;

    const activeId = active.id as number;

    if (String(over.id).startsWith(EMPTY_SLOT_PREFIX)) {
      const targetSlotIndex = parseInt(String(over.id).replace(EMPTY_SLOT_PREFIX, ''), 10);
      setGridSlots(prev => {
        const result = [...prev];
        const oldIndex = result.indexOf(activeId);
        if (oldIndex !== -1) result[oldIndex] = null;
        while (result.length <= targetSlotIndex) result.push(null);
        result[targetSlotIndex] = activeId;
        while (result.length > 0 && result[result.length - 1] === null) result.pop();
        return result;
      });
      return;
    }

    const overId = over.id as number;
    setGridSlots(prev => {
      const result = [...prev];
      const activeIndex = result.indexOf(activeId);
      const overIndex = result.indexOf(overId);
      if (activeIndex !== -1 && overIndex !== -1) {
        [result[activeIndex], result[overIndex]] = [result[overIndex], result[activeIndex]];
      }
      return result;
    });
  };

  const attendeeMap = useMemo(() => {
    const map = new Map<number, Attendee>();
    attendees.forEach(attendee => map.set(attendee.id, attendee));
    return map;
  }, [attendees]);

  const displaySlots = useMemo(() => {
    const filledCount = gridSlots.filter(id => id !== null).length;
    const nextRowStart = Math.ceil(filledCount / columnCount) * columnCount;
    // 드래그 중에는 아래에 빈 행을 추가해 빈 슬롯으로 이동할 수 있도록 한다
    const padTo = Math.max(nextRowStart, gridSlots.length) + (isDragActive ? columnCount : 0);
    const padded = [...gridSlots];
    while (padded.length < padTo) padded.push(null);
    return padded;
  }, [gridSlots, columnCount, isDragActive]);

  const existingStudentIds = useMemo(
    () => new Set(attendees.map(attendee => attendee.student.id)),
    [attendees]
  );

  if (authLoading || !user) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-orange-50 flex items-center justify-center">
        <div className="w-10 h-10 border-4 border-purple-200 border-t-purple-400 rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-orange-50">
      <div className="mx-auto px-6 py-10" style={{ maxWidth: `${columnCount * 24}rem` }}>
        {/* Header */}
        <header className="mb-10">
          <button
            onClick={() => router.push('/')}
            className="flex items-center gap-2 text-sm text-gray-400 hover:text-purple-500 transition-colors mb-4"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <polyline points="15 18 9 12 15 6" />
            </svg>
            수업 목록으로
          </button>
          <div className="flex items-start justify-between">
            <div className="flex-1 min-w-0">
              {loading ? (
                <>
                  <div className="h-10 w-48 bg-purple-100 rounded-2xl animate-pulse mb-3" />
                  <div className="h-6 w-64 bg-purple-50 rounded-xl animate-pulse" />
                </>
              ) : (
                <>
                  <h1 className="text-4xl font-bold text-purple-500">
                    {lesson?.title ?? '수업'}
                  </h1>
                  {/* 날짜/시간 표시 */}
                  {lesson && !isEditingTime && (
                    <button
                      onClick={openEditTime}
                      className="group flex items-center gap-2 mt-3 text-sm text-gray-500 hover:text-purple-500 transition-colors"
                    >
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="shrink-0 text-gray-400 group-hover:text-purple-400 transition-colors">
                        <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
                        <line x1="16" y1="2" x2="16" y2="6" />
                        <line x1="8" y1="2" x2="8" y2="6" />
                        <line x1="3" y1="10" x2="21" y2="10" />
                      </svg>
                      <span className="font-medium tabular-nums">
                        {formatLessonDateTime(lesson.startTime, lesson.endTime)}
                      </span>
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" className="opacity-0 group-hover:opacity-100 transition-opacity text-purple-400">
                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                      </svg>
                    </button>
                  )}

                  {/* 인라인 수정 패널 */}
                  {lesson && isEditingTime && (
                    <form
                      onSubmit={handleSaveTime}
                      className="mt-4 bg-white rounded-3xl shadow-lg border border-purple-100 p-5 max-w-sm"
                    >
                      <div className="flex items-center justify-between mb-4">
                        <p className="text-sm font-semibold text-gray-700">수업 시간 수정</p>
                        <button
                          type="button"
                          onClick={() => setIsEditingTime(false)}
                          className="w-7 h-7 flex items-center justify-center rounded-full text-gray-300 hover:text-gray-500 hover:bg-gray-100 transition-colors text-lg"
                          aria-label="닫기"
                        >
                          ✕
                        </button>
                      </div>

                      <div className="flex flex-col gap-3">
                        <div>
                          <p className="text-xs font-medium text-gray-400 mb-1 ml-0.5">제목</p>
                          <input
                            value={editTitle}
                            onChange={e => setEditTitle(e.target.value)}
                            className="w-full bg-purple-50 rounded-xl px-3 py-2 text-gray-800 text-sm outline-none focus:ring-2 focus:ring-purple-300"
                            required
                          />
                        </div>

                        <div>
                          <p className="text-xs font-medium text-gray-400 mb-1 ml-0.5">날짜</p>
                          <input
                            type="date"
                            value={editDate}
                            onChange={e => setEditDate(e.target.value)}
                            className="w-full bg-purple-50 rounded-xl px-3 py-2 text-gray-700 text-sm outline-none focus:ring-2 focus:ring-purple-300 cursor-pointer"
                            required
                          />
                        </div>

                        <div className="bg-purple-50 rounded-2xl px-4 py-3 flex items-end gap-3">
                          <TimeSelect
                            label="시작"
                            hour={editStartHour}
                            minute={editStartMinute}
                            onHourChange={setEditStartHour}
                            onMinuteChange={setEditStartMinute}
                          />
                          <span className="text-gray-300 font-medium pb-2">–</span>
                          <TimeSelect
                            label="종료"
                            hour={editEndHour}
                            minute={editEndMinute}
                            onHourChange={setEditEndHour}
                            onMinuteChange={setEditEndMinute}
                          />
                        </div>

                        {saveError && (
                          <p className="text-xs text-rose-400 bg-rose-50 rounded-xl px-3 py-2">{saveError}</p>
                        )}

                        <div className="flex gap-2 mt-1">
                          <button
                            type="button"
                            onClick={() => setIsEditingTime(false)}
                            className="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-500 text-sm font-medium py-2.5 rounded-xl transition-colors"
                          >
                            취소
                          </button>
                          <button
                            type="submit"
                            disabled={isSaving || !editTitle.trim() || !editDate}
                            className="flex-1 bg-purple-500 hover:bg-purple-600 disabled:bg-purple-200 text-white text-sm font-medium py-2.5 rounded-xl transition-colors"
                          >
                            {isSaving ? '저장 중...' : '저장'}
                          </button>
                        </div>
                      </div>
                    </form>
                  )}
                </>
              )}
              {!loading && <p className="text-gray-400 mt-2">수강생을 관리해요</p>}
            </div>
            <div className="flex items-center gap-3 mt-1">
              <span className="text-sm text-gray-400">{user.userId}</span>
            </div>
          </div>
        </header>

        {/* Content */}
        {loading ? (
          <div className="flex flex-col items-center justify-center h-64 gap-3">
            <div className="w-10 h-10 border-4 border-purple-200 border-t-purple-400 rounded-full animate-spin" />
            <p className="text-gray-400 text-sm">불러오는 중...</p>
          </div>
        ) : attendees.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-64 gap-3 text-gray-300">
            <div className="text-7xl">👨‍🎓</div>
            <p className="text-lg font-medium">아직 수강생이 없어요</p>
            <p className="text-sm">오른쪽 아래 + 버튼으로 추가해보세요!</p>
          </div>
        ) : (
          <>
            <div className="flex items-center justify-between mb-6">
              <p className="text-sm text-gray-400">
                총 <span className="font-semibold text-purple-400">{attendees.length}</span>명의 수강생
              </p>
              <div className="flex items-center gap-2">
                <span className="text-xs text-gray-400">열 수</span>
                <div className="flex items-center gap-1 bg-white rounded-2xl shadow-sm px-2 py-1">
                  <button
                    onClick={() => handleColumnCountChange(columnCount - 1)}
                    disabled={columnCount <= MIN_COLUMNS}
                    className="w-6 h-6 flex items-center justify-center rounded-xl text-gray-400 hover:text-purple-500 hover:bg-purple-50 disabled:opacity-30 disabled:cursor-not-allowed transition-colors text-lg leading-none"
                    aria-label="열 줄이기"
                  >
                    −
                  </button>
                  <span className="w-5 text-center text-sm font-semibold text-purple-500">
                    {columnCount}
                  </span>
                  <button
                    onClick={() => handleColumnCountChange(columnCount + 1)}
                    disabled={columnCount >= MAX_COLUMNS}
                    className="w-6 h-6 flex items-center justify-center rounded-xl text-gray-400 hover:text-purple-500 hover:bg-purple-50 disabled:opacity-30 disabled:cursor-not-allowed transition-colors text-lg leading-none"
                    aria-label="열 늘리기"
                  >
                    +
                  </button>
                </div>
              </div>
            </div>
            <DndContext
              sensors={sensors}
              collisionDetection={closestCenter}
              onDragStart={() => setIsDragActive(true)}
              onDragEnd={handleDragEnd}
              onDragCancel={() => setIsDragActive(false)}
              autoScroll={{ threshold: { x: 0.2, y: 0.2 }, interval: 5 }}
            >
              <SortableContext
                items={displaySlots.map((slotId, index) => slotId !== null ? slotId : `${EMPTY_SLOT_PREFIX}${index}`)}
                strategy={rectSortingStrategy}
              >
                <div
                  className="grid gap-12"
                  style={{ gridTemplateColumns: `repeat(${columnCount}, minmax(0, 1fr))` }}
                >
                  {displaySlots.map((slotId, slotIndex) => {
                    if (slotId === null) {
                      return (
                        <SortableEmptySlot
                          key={`${EMPTY_SLOT_PREFIX}${slotIndex}`}
                          id={`${EMPTY_SLOT_PREFIX}${slotIndex}`}
                          isDragActive={isDragActive}
                        />
                      );
                    }
                    const attendee = attendeeMap.get(slotId);
                    if (!attendee) return null;
                    return (
                      <SortableAttendeeCard
                        key={attendee.id}
                        attendee={attendee}
                        onUpdate={handleUpdate}
                        onRemove={handleRemoveAttendee}
                      />
                    );
                  })}
                </div>
              </SortableContext>
            </DndContext>
          </>
        )}
      </div>

      {/* FAB */}
      <button
        onClick={() => setShowModal(true)}
        className="fixed bottom-8 right-8 w-16 h-16 bg-pink-400 hover:bg-pink-500 active:scale-95 text-white text-3xl rounded-full shadow-lg hover:shadow-xl transition-all duration-200 flex items-center justify-center"
        aria-label="수강생 추가"
      >
        +
      </button>

      {/* Modal */}
      {showModal && (
        <AddAttendeeModal
          lessonId={lessonId}
          existingStudentIds={existingStudentIds}
          onAdd={handleAdd}
          onClose={() => setShowModal(false)}
        />
      )}
    </div>
  );
}
