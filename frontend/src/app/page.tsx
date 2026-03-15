'use client';

import { useEffect, useRef, useState } from 'react';
import {
  DndContext,
  DragEndEvent,
  PointerSensor,
  closestCenter,
  useSensor,
  useSensors,
} from '@dnd-kit/core';
import {
  SortableContext,
  arrayMove,
  rectSortingStrategy,
  useSortable,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { Student, getStudents } from './lib/api';
import StudentCard, { StudentCardHandle } from './components/StudentCard';
import AddStudentModal from './components/AddStudentModal';

interface SortableStudentCardProps {
  student: Student;
  index: number;
  cardRefs: React.MutableRefObject<(StudentCardHandle | null)[]>;
  onUpdate: (student: Student) => void;
  onDelete: (id: number) => void;
  onNavigate: (direction: 'prev' | 'next' | 'up' | 'down') => void;
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

const COLUMN_STORAGE_KEY = 'studentGridColumns';
const MIN_COLUMNS = 1;
const MAX_COLUMNS = 6;

export default function Home() {
  const [students, setStudents] = useState<Student[]>([]);
  const [showModal, setShowModal] = useState(false);
  const [loading, setLoading] = useState(true);
  const [columnCount, setColumnCount] = useState(4);
  const cardRefs = useRef<(StudentCardHandle | null)[]>([]);

  useEffect(() => {
    const saved = localStorage.getItem(COLUMN_STORAGE_KEY);
    if (saved) setColumnCount(Number(saved));
  }, []);

  const handleColumnCountChange = (next: number) => {
    const clamped = Math.min(MAX_COLUMNS, Math.max(MIN_COLUMNS, next));
    setColumnCount(clamped);
    localStorage.setItem(COLUMN_STORAGE_KEY, String(clamped));
  };

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 8 } })
  );

  useEffect(() => {
    getStudents()
      .then(setStudents)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const handleAdd = (student: Student) => {
    setStudents(prev => [...prev, student]);
    setShowModal(false);
  };

  const handleUpdate = (updated: Student) => {
    setStudents(prev => prev.map(s => (s.id === updated.id ? updated : s)));
  };

  const handleDelete = (id: number) => {
    setStudents(prev => prev.filter(s => s.id !== id));
  };

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    if (!over || active.id === over.id) return;
    setStudents(prev => {
      const oldIndex = prev.findIndex(s => s.id === active.id);
      const newIndex = prev.findIndex(s => s.id === over.id);
      return arrayMove(prev, oldIndex, newIndex);
    });
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-orange-50">
      <div className="mx-auto px-6 py-10" style={{ maxWidth: `${columnCount * 18}rem` }}>
        {/* Header */}
        <header className="mb-10">
          <h1 className="text-4xl font-bold text-purple-500">🍎 학생 관리</h1>
          <p className="text-gray-400 mt-2">나의 소중한 학생들을 관리해요</p>
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
            <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
              <SortableContext items={students.map(s => s.id)} strategy={rectSortingStrategy}>
                <div
                  className="grid gap-5"
                  style={{ gridTemplateColumns: `repeat(${columnCount}, minmax(0, 1fr))` }}
                >
                  {students.map((student, index) => (
                    <SortableStudentCard
                      key={student.id}
                      student={student}
                      index={index}
                      cardRefs={cardRefs}
                      onUpdate={handleUpdate}
                      onDelete={handleDelete}
                      onNavigate={direction => {
                        const targetIndex =
                          direction === 'prev' ? index - 1 :
                          direction === 'next' ? index + 1 :
                          direction === 'up' ? index - columnCount :
                          index + columnCount;
                        cardRefs.current[targetIndex]?.focusKeywordInput();
                      }}
                    />
                  ))}
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
