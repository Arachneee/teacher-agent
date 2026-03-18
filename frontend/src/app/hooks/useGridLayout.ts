'use client';

import { useCallback, useEffect, useState } from 'react';

export const MIN_COLUMNS = 1;
export const MAX_COLUMNS = 6;

export function useGridLayout(storageSuffix = '') {
  const columnStorageKey = `gridColumns_${storageSuffix}`;
  const gridOrderStorageKey = `gridOrder_${storageSuffix}`;

  const [gridSlots, setGridSlots] = useState<(number | null)[]>([]);
  const [columnCount, setColumnCount] = useState(3);
  const [isInitialized, setIsInitialized] = useState(false);

  useEffect(() => {
    const saved = localStorage.getItem(columnStorageKey);
    if (saved) setColumnCount(Number(saved));
  }, [columnStorageKey]);

  const handleColumnCountChange = (next: number) => {
    const clamped = Math.min(MAX_COLUMNS, Math.max(MIN_COLUMNS, next));
    setColumnCount(clamped);
    localStorage.setItem(columnStorageKey, String(clamped));
  };

  const initializeGridSlots = useCallback((ids: number[]) => {
    const savedOrder = localStorage.getItem(gridOrderStorageKey);
    if (savedOrder) {
      try {
        const parsed: (number | null)[] = JSON.parse(savedOrder);
        const idSet = new Set(ids);
        const filtered = parsed.filter(id => id === null || idSet.has(id));
        const existingIds = new Set(filtered.filter((id): id is number => id !== null));
        const newIds = ids.filter(id => !existingIds.has(id));
        const result = [...filtered, ...newIds];
        while (result.length > 0 && result[result.length - 1] === null) result.pop();
        setGridSlots(result);
        setIsInitialized(true);
        return;
      } catch {
        // 파싱 실패 시 기본값으로 초기화
      }
    }
    setGridSlots(ids);
    setIsInitialized(true);
  }, [gridOrderStorageKey]);

  useEffect(() => {
    if (!isInitialized) return;
    if (gridSlots.length > 0) {
      localStorage.setItem(gridOrderStorageKey, JSON.stringify(gridSlots));
    } else {
      localStorage.removeItem(gridOrderStorageKey);
    }
  }, [gridSlots, isInitialized, gridOrderStorageKey]);

  const addToGrid = useCallback((id: number) => {
    setGridSlots(prev => [...prev, id]);
  }, []);

  const removeFromGrid = useCallback((id: number) => {
    setGridSlots(prev => {
      const result = prev.map(slotId => (slotId === id ? null : slotId));
      while (result.length > 0 && result[result.length - 1] === null) result.pop();
      return result;
    });
  }, []);

  return {
    gridSlots,
    setGridSlots,
    columnCount,
    handleColumnCountChange,
    initializeGridSlots,
    addToGrid,
    removeFromGrid,
  };
}
