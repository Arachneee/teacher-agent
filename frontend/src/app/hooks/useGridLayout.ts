'use client';

import { useCallback, useEffect, useState } from 'react';
import { Student } from '../lib/api';

const COLUMN_STORAGE_KEY = 'studentGridColumns';
const GRID_ORDER_STORAGE_KEY = 'studentGridOrder';
export const MIN_COLUMNS = 1;
export const MAX_COLUMNS = 6;

export function useGridLayout() {
  const [gridSlots, setGridSlots] = useState<(number | null)[]>([]);
  const [columnCount, setColumnCount] = useState(4);
  const [isInitialized, setIsInitialized] = useState(false);

  useEffect(() => {
    const saved = localStorage.getItem(COLUMN_STORAGE_KEY);
    if (saved) setColumnCount(Number(saved));
  }, []);

  const handleColumnCountChange = (next: number) => {
    const clamped = Math.min(MAX_COLUMNS, Math.max(MIN_COLUMNS, next));
    setColumnCount(clamped);
    localStorage.setItem(COLUMN_STORAGE_KEY, String(clamped));
  };

  const buildGridSlots = useCallback((studentList: Student[]): (number | null)[] => {
    const savedOrder = localStorage.getItem(GRID_ORDER_STORAGE_KEY);
    if (savedOrder) {
      try {
        const parsed: (number | null)[] = JSON.parse(savedOrder);
        const studentIds = new Set(studentList.map(student => student.id));
        // Filter out deleted students but keep nulls
        const filtered = parsed.filter(id => id === null || studentIds.has(id));
        // Add any new students not in the saved order
        const existingIds = new Set(filtered.filter((id): id is number => id !== null));
        const newStudents = studentList.filter(student => !existingIds.has(student.id));
        const result = [...filtered, ...newStudents.map(student => student.id)];
        // Trim trailing nulls
        while (result.length > 0 && result[result.length - 1] === null) {
          result.pop();
        }
        return result;
      } catch {
        // Fall through to default
      }
    }
    return studentList.map(student => student.id);
  }, []);

  const initializeGridSlots = useCallback((studentList: Student[]) => {
    setGridSlots(buildGridSlots(studentList));
    setIsInitialized(true);
  }, [buildGridSlots]);

  // Persist gridSlots to localStorage only after initialization to avoid overwriting
  // saved data before the initial fetch completes. Clears storage when all students
  // are removed so stale order data doesn't persist.
  useEffect(() => {
    if (!isInitialized) return;
    if (gridSlots.length > 0) {
      localStorage.setItem(GRID_ORDER_STORAGE_KEY, JSON.stringify(gridSlots));
    } else {
      localStorage.removeItem(GRID_ORDER_STORAGE_KEY);
    }
  }, [gridSlots, isInitialized]);

  const addStudentToGrid = useCallback((studentId: number) => {
    setGridSlots(prev => [...prev, studentId]);
  }, []);

  const removeStudentFromGrid = useCallback((studentId: number) => {
    setGridSlots(prev => {
      const result = prev.map(slotId => (slotId === studentId ? null : slotId));
      // Trim trailing nulls
      while (result.length > 0 && result[result.length - 1] === null) {
        result.pop();
      }
      return result;
    });
  }, []);

  return {
    gridSlots,
    setGridSlots,
    columnCount,
    handleColumnCountChange,
    initializeGridSlots,
    addStudentToGrid,
    removeStudentFromGrid,
  };
}
