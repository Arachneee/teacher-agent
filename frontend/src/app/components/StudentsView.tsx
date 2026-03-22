'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
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
import { Student, getStudents } from '../lib/api';
import { MAX_COLUMNS, MIN_COLUMNS, useGridLayout } from '../hooks/useGridLayout';
import { useIsMobile } from '../hooks/useIsMobile';
import StudentManagementCard from './StudentManagementCard';
import AddStudentModal from './AddStudentModal';

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
      className={`rounded-3xl border-2 border-dashed transition-colors min-h-[140px] ${
        isDragActive
          ? isOver
            ? 'border-purple-300 bg-purple-50/50'
            : 'border-gray-200'
          : 'border-transparent'
      }`}
    />
  );
}

interface SortableStudentCardProps {
  student: Student;
  onUpdate: () => void;
  onDelete: (id: number) => void;
}

function SortableStudentCard({ student, onUpdate, onDelete }: SortableStudentCardProps) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: student.id,
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
    >
      <StudentManagementCard
        student={student}
        onUpdate={onUpdate}
        onDelete={onDelete}
        dragHandleProps={{ ...attributes, ...listeners }}
      />
    </div>
  );
}

export default function StudentsView() {
  const [students, setStudents] = useState<Student[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [isDragActive, setIsDragActive] = useState(false);

  const {
    gridSlots,
    setGridSlots,
    columnCount,
    handleColumnCountChange,
    initializeGridSlots,
    removeFromGrid,
  } = useGridLayout('students');

  const isMobile = useIsMobile();
  const effectiveColumnCount = isMobile ? 1 : columnCount;

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 8 } })
  );

  const fetchStudents = useCallback(() => {
    setLoading(true);
    getStudents()
      .then(data => {
        setStudents(data);
        initializeGridSlots(data.map(student => student.id));
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [initializeGridSlots]);

  useEffect(() => {
    fetchStudents();
  }, [fetchStudents]);

  const handleAdd = () => {
    setShowAddModal(false);
    fetchStudents();
  };

  const handleDelete = (id: number) => {
    setStudents(prev => prev.filter(student => student.id !== id));
    removeFromGrid(id);
  };

  const filteredStudents = useMemo(() => {
    const trimmed = searchQuery.trim();
    if (!trimmed) return students;
    return students.filter(student =>
      student.name.toLowerCase().includes(trimmed.toLowerCase())
    );
  }, [students, searchQuery]);

  const studentMap = useMemo(() => {
    const map = new Map<number, Student>();
    students.forEach(student => map.set(student.id, student));
    return map;
  }, [students]);

  const displaySlots = useMemo(() => {
    const filledCount = gridSlots.filter(id => id !== null).length;
    const nextRowStart = Math.ceil(filledCount / effectiveColumnCount) * effectiveColumnCount;
    const padTo = Math.max(nextRowStart, gridSlots.length) + (isDragActive ? effectiveColumnCount : 0);
    const padded = [...gridSlots];
    while (padded.length < padTo) padded.push(null);
    return padded;
  }, [gridSlots, effectiveColumnCount, isDragActive]);

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

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center h-96 gap-3 bg-white rounded-2xl shadow-sm border border-gray-100">
        <div className="w-10 h-10 border-4 border-purple-200 border-t-purple-400 rounded-full animate-spin" />
        <p className="text-gray-400 text-sm">학생 목록을 불러오는 중...</p>
      </div>
    );
  }

  const isSearching = searchQuery.trim() !== '';

  return (
    <>
      {/* Search bar + column controls */}
      <div className="mb-6 flex items-center gap-3">
        <div className="relative flex-1 max-w-sm">
          <svg
            className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-300 pointer-events-none"
            width="16" height="16" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"
          >
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input
            type="text"
            value={searchQuery}
            onChange={event => setSearchQuery(event.target.value)}
            placeholder="이름으로 검색"
            className="w-full bg-white rounded-2xl pl-9 pr-4 py-2.5 text-sm text-gray-700 shadow-sm border border-gray-100 outline-none focus:ring-2 focus:ring-purple-300 placeholder-gray-300"
          />
          {searchQuery && (
            <button
              onClick={() => setSearchQuery('')}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-300 hover:text-gray-400 transition-colors"
              aria-label="검색어 지우기"
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          )}
        </div>
        <p className="text-sm text-gray-400 shrink-0">
          {isSearching ? (
            <>
              <span className="font-semibold text-purple-400">{filteredStudents.length}</span>명 검색됨
            </>
          ) : (
            <>총 <span className="font-semibold text-purple-400">{students.length}</span>명</>
          )}
        </p>
        {!isSearching && students.length > 0 && (
          <div className="hidden md:flex items-center gap-2 ml-auto">
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
        )}
      </div>

      {/* Student grid */}
      {students.length === 0 ? (
        <div className="flex flex-col items-center justify-center h-80 gap-3 bg-white rounded-2xl shadow-sm border border-gray-100">
          <div className="text-6xl">👨‍🎓</div>
          <p className="text-base font-medium text-gray-400">아직 등록된 학생이 없어요</p>
          <p className="text-sm text-gray-300">아래 + 버튼으로 첫 학생을 추가해보세요!</p>
        </div>
      ) : filteredStudents.length === 0 ? (
        <div className="flex flex-col items-center justify-center h-80 gap-3 bg-white rounded-2xl shadow-sm border border-gray-100">
          <div className="text-6xl">🔍</div>
          <p className="text-base font-medium text-gray-400">검색 결과가 없어요</p>
          <p className="text-sm text-gray-300">다른 이름으로 검색해보세요</p>
        </div>
      ) : isSearching ? (
        <div
          className="grid gap-4"
          style={{ gridTemplateColumns: `repeat(${effectiveColumnCount}, minmax(0, 1fr))` }}
        >
          {filteredStudents.map(student => (
            <StudentManagementCard
              key={student.id}
              student={student}
              onUpdate={fetchStudents}
              onDelete={handleDelete}
            />
          ))}
        </div>
      ) : (
        <DndContext
          sensors={sensors}
          collisionDetection={closestCenter}
          onDragStart={() => setIsDragActive(true)}
          onDragEnd={handleDragEnd}
          onDragCancel={() => setIsDragActive(false)}
          autoScroll={{ threshold: { x: 0.2, y: 0.2 }, interval: 5 }}
        >
          <SortableContext
            items={displaySlots.map((slotId, index) =>
              slotId !== null ? slotId : `${EMPTY_SLOT_PREFIX}${index}`
            )}
            strategy={rectSortingStrategy}
          >
            <div
              className="grid gap-4"
              style={{ gridTemplateColumns: `repeat(${effectiveColumnCount}, minmax(0, 1fr))` }}
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
                return (
                  <SortableStudentCard
                    key={student.id}
                    student={student}
                    onUpdate={fetchStudents}
                    onDelete={handleDelete}
                  />
                );
              })}
            </div>
          </SortableContext>
        </DndContext>
      )}

      {/* FAB */}
      <button
        onClick={() => setShowAddModal(true)}
        className="fixed bottom-20 right-5 md:bottom-8 md:right-8 w-14 h-14 md:w-16 md:h-16 bg-pink-400 hover:bg-pink-500 active:scale-95 text-white text-3xl rounded-full shadow-lg hover:shadow-xl transition-all duration-200 flex items-center justify-center"
        aria-label="학생 추가"
      >
        +
      </button>

      {showAddModal && (
        <AddStudentModal
          onAdd={handleAdd}
          onClose={() => setShowAddModal(false)}
        />
      )}
    </>
  );
}
