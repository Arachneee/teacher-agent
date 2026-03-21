'use client';

import { useState, useRef, useEffect, useCallback } from 'react';

interface Option {
  value: string;
  label: string;
}

interface CustomSelectProps {
  value: string;
  options: Option[];
  onChange: (value: string) => void;
  className?: string;
}

export default function CustomSelect({ value, options, onChange, className = '' }: CustomSelectProps) {
  const [isOpen, setIsOpen] = useState(false);
  const triggerRef = useRef<HTMLButtonElement>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const [dropdownPos, setDropdownPos] = useState({ top: 0, left: 0, width: 0 });

  const DROPDOWN_HEIGHT = 200;

  const updatePosition = useCallback(() => {
    if (!triggerRef.current) return;
    const rect = triggerRef.current.getBoundingClientRect();
    const spaceBelow = window.innerHeight - rect.bottom;
    const openUp = spaceBelow < DROPDOWN_HEIGHT && rect.top > spaceBelow;
    setDropdownPos({
      top: openUp ? rect.top - Math.min(options.length * 36 + 8, DROPDOWN_HEIGHT) - 8 : rect.bottom + 8,
      left: rect.left,
      width: rect.width,
    });
  }, [options.length]);

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

  const selectedLabel = options.find(opt => opt.value === value)?.label ?? '';

  return (
    <div className={`relative ${className}`}>
      <button
        ref={triggerRef}
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        className="w-full bg-white rounded-xl px-3 py-2 text-gray-700 text-sm text-left hover:bg-purple-50 transition-colors duration-200 flex items-center justify-between gap-2"
      >
        <span>{selectedLabel}</span>
        <span className={`text-purple-300 text-xs transition-transform duration-200 ${isOpen ? 'rotate-180' : ''}`}>▼</span>
      </button>

      {isOpen && (
        <div
          ref={dropdownRef}
          className="fixed bg-white rounded-2xl shadow-xl border border-purple-100 py-1 z-[9999] overflow-y-auto"
          style={{ top: dropdownPos.top, left: dropdownPos.left, width: dropdownPos.width, maxHeight: `${DROPDOWN_HEIGHT}px` }}
        >
          {options.map((option) => (
            <div
              key={option.value}
              onClick={() => { onChange(option.value); setIsOpen(false); }}
              className={`px-3 py-2 text-sm cursor-pointer transition-colors duration-150 ${
                option.value === value
                  ? 'bg-pink-400 text-white font-semibold'
                  : 'text-gray-700 hover:bg-purple-50'
              }`}
            >
              {option.label}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
