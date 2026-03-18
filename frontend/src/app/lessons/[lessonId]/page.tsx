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
import { Attendee, Lesson, LessonDetailAttendee, getLessonDetail, removeAttendee } from '../../lib/api';
import { useAuth } from '../../context/AuthContext';
import AttendeeCard from '../../components/AttendeeCard';
import AddAttendeeModal from '../../components/AddAttendeeModal';
import { MAX_COLUMNS, MIN_COLUMNS, useGridLayout } from '../../hooks/useGridLayout';

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
            <div>
              {loading ? (
                <div className="h-10 w-48 bg-purple-100 rounded-2xl animate-pulse" />
              ) : (
                <h1 className="text-4xl font-bold text-purple-500">
                  {lesson?.title ?? '수업'}
                </h1>
              )}
              <p className="text-gray-400 mt-2">수강생을 관리해요</p>
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
