'use client';

import { useEffect, useRef, useState } from 'react';
import type { SchoolGrade } from '../lib/api';
import { SCHOOL_GRADE_GROUPS, SCHOOL_GRADE_LABELS } from '../lib/constants';

interface Props {
  value: SchoolGrade;
  onChange: (grade: SchoolGrade) => void;
}

export default function GradeSelect({ value, onChange }: Props) {
  const [open, setOpen] = useState(false);
  const [dropdownStyle, setDropdownStyle] = useState<React.CSSProperties>({});
  const triggerRef = useRef<HTMLButtonElement>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const updateDropdownPosition = () => {
    if (!triggerRef.current) return;
    const rect = triggerRef.current.getBoundingClientRect();
    const spaceBelow = window.innerHeight - rect.bottom;
    const dropdownHeight = 260;

    if (spaceBelow >= dropdownHeight) {
      setDropdownStyle({ top: rect.bottom + 6, left: rect.left, width: rect.width });
    } else {
      setDropdownStyle({ bottom: window.innerHeight - rect.top + 6, left: rect.left, width: rect.width });
    }
  };

  const handleOpen = () => {
    updateDropdownPosition();
    setOpen(prev => !prev);
  };

  useEffect(() => {
    if (!open) return;
    const handleClose = (event: MouseEvent) => {
      if (triggerRef.current && !triggerRef.current.contains(event.target as Node)) {
        setOpen(false);
      }
    };
    const handleScroll = (event: Event) => {
      if (dropdownRef.current?.contains(event.target as Node)) return;
      setOpen(false);
    };
    document.addEventListener('mousedown', handleClose);
    window.addEventListener('scroll', handleScroll, true);
    return () => {
      document.removeEventListener('mousedown', handleClose);
      window.removeEventListener('scroll', handleScroll, true);
    };
  }, [open]);

  return (
    <>
      <button
        ref={triggerRef}
        type="button"
        onClick={handleOpen}
        className="w-full bg-purple-50 rounded-2xl px-4 py-3 text-left text-gray-800 flex items-center justify-between outline-none focus:ring-2 focus:ring-purple-300 hover:bg-purple-100 transition-colors duration-150"
      >
        <span className="text-sm font-medium">{SCHOOL_GRADE_LABELS[value]}</span>
        <svg
          width="16" height="16" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"
          className={`text-gray-400 transition-transform duration-200 ${open ? 'rotate-180' : ''}`}
        >
          <polyline points="6 9 12 15 18 9" />
        </svg>
      </button>

      {open && (
        <div
          ref={dropdownRef}
          style={{ ...dropdownStyle, position: 'fixed' }}
          className="z-[9999] bg-white rounded-2xl shadow-lg border border-gray-100 py-2 overflow-y-auto max-h-64"
        >
          {SCHOOL_GRADE_GROUPS.map((group, groupIndex) => (
            <div key={group.label}>
              {groupIndex > 0 && <div className="mx-3 my-1 border-t border-gray-100" />}
              <p className="px-4 pt-1 pb-0.5 text-xs font-semibold text-gray-400">
                {group.label}
              </p>
              {group.grades.map(gradeOption => (
                <button
                  key={gradeOption}
                  type="button"
                  onClick={() => { onChange(gradeOption); setOpen(false); }}
                  className={`w-full text-left px-4 py-2 text-sm transition-colors duration-100 ${
                    value === gradeOption
                      ? 'bg-purple-50 text-purple-600 font-semibold'
                      : 'text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  {SCHOOL_GRADE_LABELS[gradeOption]}
                </button>
              ))}
            </div>
          ))}
        </div>
      )}
    </>
  );
}
