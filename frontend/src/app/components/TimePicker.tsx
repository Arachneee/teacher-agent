'use client';

import { useMemo, useRef, useState, useEffect, useCallback } from 'react';
import { HOURS, MINUTES, padTwoDigits } from '../lib/dateTimeUtils';

interface Props {
  hour: number;
  minute: number;
  onHourChange: (hour: number) => void;
  onMinuteChange: (minute: number) => void;
}

export default function TimePicker({ hour, minute, onHourChange, onMinuteChange }: Props) {
  const [isOpen, setIsOpen] = useState(false);
  const triggerRef = useRef<HTMLButtonElement>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const hourRefs = useRef<{ [key: number]: HTMLDivElement | null }>({});
  const minuteRefs = useRef<{ [key: number]: HTMLDivElement | null }>({});
  const [dropdownPos, setDropdownPos] = useState({ top: 0, left: 0 });

  const minuteOptions = useMemo(
    () => (MINUTES.includes(minute) ? MINUTES : [...MINUTES, minute].sort((a, b) => a - b)),
    [minute]
  );

  const DROPDOWN_HEIGHT = 240;

  const updatePosition = useCallback(() => {
    if (!triggerRef.current) return;
    const rect = triggerRef.current.getBoundingClientRect();
    const spaceBelow = window.innerHeight - rect.bottom;
    const openUp = spaceBelow < DROPDOWN_HEIGHT && rect.top > spaceBelow;
    const left = Math.min(rect.left, window.innerWidth - 228);
    setDropdownPos({
      top: openUp ? rect.top - DROPDOWN_HEIGHT - 8 : rect.bottom + 8,
      left: Math.max(8, left),
    });
  }, []);

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

  useEffect(() => {
    if (isOpen) {
      setTimeout(() => {
        hourRefs.current[hour]?.scrollIntoView({ block: 'center', behavior: 'smooth' });
        minuteRefs.current[minute]?.scrollIntoView({ block: 'center', behavior: 'smooth' });
      }, 50);
    }
  }, [isOpen, hour, minute]);

  return (
    <div className="flex-1 relative">
      <button
        ref={triggerRef}
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        className="w-full bg-purple-50 rounded-xl px-3 py-2 text-gray-700 text-sm hover:bg-purple-100 transition-colors duration-200 flex items-center justify-center gap-1.5"
      >
        <span>🕐</span>
        <span>{padTwoDigits(hour)}:{padTwoDigits(minute)}</span>
      </button>

      {isOpen && (
        <div
          ref={dropdownRef}
          className="fixed bg-white rounded-2xl shadow-xl border border-purple-100 z-[9999]"
          style={{ top: dropdownPos.top, left: dropdownPos.left, width: '220px' }}
        >
          <div className="flex">
            <div className="flex-1 border-r border-purple-100">
              <div className="text-xs text-gray-400 font-medium text-center py-2 border-b border-purple-100">
                시
              </div>
              <div className="overflow-y-auto max-h-48 py-1 px-2">
                {HOURS.map((hourValue) => (
                  <div
                    key={hourValue}
                    ref={(el) => { hourRefs.current[hourValue] = el; }}
                    onClick={() => onHourChange(hourValue)}
                    className={`rounded-lg px-3 py-1.5 text-sm cursor-pointer text-center transition-colors duration-150 ${
                      hourValue === hour
                        ? 'bg-pink-400 text-white font-semibold'
                        : 'hover:bg-purple-50'
                    }`}
                  >
                    {padTwoDigits(hourValue)}
                  </div>
                ))}
              </div>
            </div>

            <div className="flex-1">
              <div className="text-xs text-gray-400 font-medium text-center py-2 border-b border-purple-100">
                분
              </div>
              <div className="overflow-y-auto max-h-48 py-1 px-2">
                {minuteOptions.map((minuteValue) => (
                  <div
                    key={minuteValue}
                    ref={(el) => { minuteRefs.current[minuteValue] = el; }}
                    onClick={() => onMinuteChange(minuteValue)}
                    className={`rounded-lg px-3 py-1.5 text-sm cursor-pointer text-center transition-colors duration-150 ${
                      minuteValue === minute
                        ? 'bg-pink-400 text-white font-semibold'
                        : 'hover:bg-purple-50'
                    }`}
                  >
                    {padTwoDigits(minuteValue)}
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
