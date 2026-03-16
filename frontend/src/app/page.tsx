'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  DndContext,
  DragCancelEvent,
  DragEndEvent,
  DragStartEvent,
  PointerSensor,
  closestCenter,
  useSensor,
  useSensors,
} from '@dnd-kit/core';
import {
  SortableContext,
  rectSortingStrategy,
  useSortable,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { Student, getStudents } from './lib/api';
import StudentCard, { StudentCardHandle } from './components/StudentCard';
import AddStudentModal from './components/AddStudentModal';
import { useAuth } from './context/AuthContext';
import { MAX_COLUMNS, MIN_COLUMNS, useGridLayout } from './hooks/useGridLayout';

interface SortableStudentCardProps {
  student: Student;
  index: number;
  cardRefs: React.MutableRefObject<(StudentCardHandle | null)[]>;
  onUpdate: () => void;
  onDelete: (id: number) => void;
  onNavigate: (direction: 'prev' | 'next' | 'up' | 'down') => void;
}

const EMPTY_SLOT_PREFIX = 'empty-slot-';

function SortableEmptySlot({ id, isDragActive }: { id: string; isDragActive: boolean }) {
  const { setNodeRef, isOver, transform, transition } = useSortable({
    id,
    disabled: { draggable: true },
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
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

function SortableStudentCard({
  student,
  index,
  cardRefs,
  onUpdate,
  onDelete,
  onNavigate,
}: SortableStudentCardProps) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: student.id,
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
    zIndex: isDragging ? 10 : undefined,
  };

  return (
    <div ref={setNodeRef} style={style} className="h-full">
      <StudentCard
        ref={el => {
          cardRefs.current[index] = el;
        }}
        student={student}
        onUpdate={onUpdate}
        onDelete={onDelete}
        onNavigate={onNavigate}
        dragHandleProps={{ ...attributes, ...listeners }}
      />
    </div>
  );
}

export default function Home() {
  const { user, loading: authLoading, logout } = useAuth();
  const router = useRouter();
  const [students, setStudents] = useState<Student[]>([]);
  const [showModal, setShowModal] = useState(false);
  const [loading, setLoading] = useState(true);
  const [isDragActive, setIsDragActive] = useState(false);
  const cardRefs = useRef<(StudentCardHandle | null)[]>([]);

  const {
    gridSlots,
    setGridSlots,
    columnCount,
    handleColumnCountChange,
    initializeGridSlots,
    addStudentToGrid,
    removeStudentFromGrid,
  } = useGridLayout();

  const studentMap = useMemo(() => {
    const map = new Map<number, Student>();
    students.forEach(student => map.set(student.id, student));
    return map;
  }, [students]);

  useEffect(() => {
    if (!authLoading && !user) {
      router.replace('/login');
    }
  }, [authLoading, user, router]);

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 8 } })
  );

  useEffect(() => {
    getStudents()
      .then(data => {
        setStudents(data);
        initializeGridSlots(data);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [initializeGridSlots]);

  const handleAdd = async () => {
    const data = await getStudents();
    const newStudents = data.filter(student => !students.some(existing => existing.id === student.id));
    newStudents.forEach(student => addStudentToGrid(student.id));
    setStudents(data);
    setShowModal(false);
  };

  const handleUpdate = async () => {
    const data = await getStudents();
    setStudents(data);
  };

  const handleDelete = (id: number) => {
    setStudents(prev => prev.filter(student => student.id !== id));
    removeStudentFromGrid(id);
  };

  const handleDragStart = (_event: DragStartEvent) => {
    setIsDragActive(true);
  };

  const handleDragCancel = (_event: DragCancelEvent) => {
    setIsDragActive(false);
  };

  const handleDragEnd = (event: DragEndEvent) => {
    setIsDragActive(false);
    const { active, over } = event;
    if (!over || active.id === over.id) return;

    const activeId = active.id as number;

    if (String(over.id).startsWith(EMPTY_SLOT_PREFIX)) {
      // Dropped on an empty slot — move card to that slot position
      const targetSlotIndex = parseInt(String(over.id).replace(EMPTY_SLOT_PREFIX, ''), 10);
      setGridSlots(prev => {
        const result = [...prev];
        // Clear old position
        const oldIndex = result.indexOf(activeId);
        if (oldIndex !== -1) result[oldIndex] = null;
        // Place at target
        while (result.length <= targetSlotIndex) result.push(null);
        result[targetSlotIndex] = activeId;
        // Trim trailing nulls
        while (result.length > 0 && result[result.length - 1] === null) {
          result.pop();
        }
        return result;
      });
      return;
    }

    // Dropped on another card — swap positions
    const overId = over.id as number;
    setGridSlots(prev => {
      const result = [...prev];
      const activeIndex = result.indexOf(activeId);
      const overIndex = result.indexOf(overId);
      if (activeIndex !== -1 && overIndex !== -1) {
        result[activeIndex] = overId;
        result[overIndex] = activeId;
      }
      return result;
    });
  };

  // Build display slots: gridSlots + trailing empty slots to fill the last row + 1 extra row
  const displaySlots = useMemo(() => {
    const slots = [...gridSlots];
    const remainder = slots.length % columnCount;
    if (remainder !== 0) {
      const padding = columnCount - remainder;
      for (let i = 0; i < padding; i++) slots.push(null);
    }
    // Add one extra row of empty slots below
    for (let i = 0; i < columnCount; i++) slots.push(null);
    return slots;
  }, [gridSlots, columnCount]);

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
        <header className="mb-10 flex items-start justify-between">
          <div>
            <h1 className="text-4xl font-bold text-purple-500">🍎 학생 관리</h1>
            <p className="text-gray-400 mt-2">나의 소중한 학생들을 관리해요</p>
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
        {loading ? (
          <div className="flex flex-col items-center justify-center h-64 gap-3">
            <div className="w-10 h-10 border-4 border-purple-200 border-t-purple-400 rounded-full animate-spin" />
            <p className="text-gray-400 text-sm">불러오는 중...</p>
          </div>
        ) : students.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-64 gap-3 text-gray-300">
            <div className="text-7xl">👨‍🎓</div>
            <p className="text-lg font-medium">아직 학생이 없어요</p>
            <p className="text-sm">오른쪽 아래 + 버튼으로 추가해보세요!</p>
          </div>
        ) : (
          <>
            <div className="flex items-center justify-between mb-6">
              <p className="text-sm text-gray-400">
                총{' '}
                <span className="font-semibold text-purple-400">{students.length}</span>명의 학생
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
              onDragStart={handleDragStart}
              onDragEnd={handleDragEnd}
              onDragCancel={handleDragCancel}
              autoScroll={{ threshold: { x: 0.2, y: 0.2 }, interval: 5 }}
            >
              <SortableContext
                items={displaySlots.map((slotId, i) => slotId !== null ? slotId : `${EMPTY_SLOT_PREFIX}${i}`)}
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
                    const student = studentMap.get(slotId);
                    if (!student) return null;
                    const studentIndex = students.findIndex(student => student.id === slotId);
                    return (
                      <SortableStudentCard
                        key={student.id}
                        student={student}
                        index={studentIndex}
                        cardRefs={cardRefs}
                        onUpdate={handleUpdate}
                        onDelete={handleDelete}
                        onNavigate={direction => {
                          const targetSlotIndex =
                            direction === 'prev' ? slotIndex - 1 :
                            direction === 'next' ? slotIndex + 1 :
                            direction === 'up' ? slotIndex - columnCount :
                            slotIndex + columnCount;
                          const targetSlotId = displaySlots[targetSlotIndex];
                          if (targetSlotId != null) {
                            const targetStudentIndex = students.findIndex(student => student.id === targetSlotId);
                            cardRefs.current[targetStudentIndex]?.focusKeywordInput();
                          }
                        }}
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
        aria-label="학생 추가"
      >
        +
      </button>

      {/* Modal */}
      {showModal && (
        <AddStudentModal onAdd={handleAdd} onClose={() => setShowModal(false)} />
      )}
    </div>
  );
}
