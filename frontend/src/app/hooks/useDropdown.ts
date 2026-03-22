'use client';

import { useState, useRef, useEffect, useCallback } from 'react';

interface DropdownPosition {
  top: number;
  left: number;
  width?: number;
  openUp?: boolean;
}

interface UseDropdownOptions {
  dropdownHeight: number;
  includeWidth?: boolean;
  includeOpenUp?: boolean;
}

interface UseDropdownReturn<T extends HTMLElement, D extends HTMLElement> {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  triggerRef: React.RefObject<T | null>;
  dropdownRef: React.RefObject<D | null>;
  dropdownPos: DropdownPosition;
}

export function useDropdown<
  T extends HTMLElement = HTMLButtonElement,
  D extends HTMLElement = HTMLDivElement
>(options: UseDropdownOptions): UseDropdownReturn<T, D> {
  const { dropdownHeight, includeWidth = false, includeOpenUp = false } = options;

  const [isOpen, setIsOpen] = useState(false);
  const [dropdownPos, setDropdownPos] = useState<DropdownPosition>({ top: 0, left: 0 });
  const triggerRef = useRef<T>(null);
  const dropdownRef = useRef<D>(null);

  const updatePosition = useCallback(() => {
    if (!triggerRef.current) return;
    const rect = triggerRef.current.getBoundingClientRect();
    const spaceBelow = window.innerHeight - rect.bottom;
    const openUp = spaceBelow < dropdownHeight && rect.top > spaceBelow;
    const left = Math.min(rect.left, window.innerWidth - (includeWidth ? rect.width : 328));

    const newPos: DropdownPosition = {
      top: openUp ? rect.top - dropdownHeight - 8 : rect.bottom + 8,
      left: Math.max(8, left),
    };

    if (includeWidth) {
      newPos.width = rect.width;
    }
    if (includeOpenUp) {
      newPos.openUp = openUp;
    }

    setDropdownPos(newPos);
  }, [dropdownHeight, includeWidth, includeOpenUp]);

  useEffect(() => {
    if (!isOpen) return;
    updatePosition();

    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current && !dropdownRef.current.contains(event.target as Node) &&
        triggerRef.current && !triggerRef.current.contains(event.target as Node)
      ) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    window.addEventListener('scroll', updatePosition, true);
    window.addEventListener('resize', updatePosition);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      window.removeEventListener('scroll', updatePosition, true);
      window.removeEventListener('resize', updatePosition);
    };
  }, [isOpen, updatePosition]);

  return {
    isOpen,
    setIsOpen,
    triggerRef,
    dropdownRef,
    dropdownPos,
  };
}
