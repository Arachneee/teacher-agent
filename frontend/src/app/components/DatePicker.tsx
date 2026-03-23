'use client';

import { useState, useEffect } from 'react';
import { useDropdown } from '../hooks/useDropdown';

interface DatePickerProps {
  value: string;
  onChange: (date: string) => void;
  required?: boolean;
  className?: string;
  showWeekRange?: boolean;
}

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'];
const CALENDAR_HEIGHT = 380;

function getWeekStartMonday(date: Date): Date {
  const result = new Date(date);
  const dayOfWeek = result.getDay();
  const daysToMonday = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;
  result.setDate(result.getDate() + daysToMonday);
  result.setHours(0, 0, 0, 0);
  return result;
}

export default function DatePicker({ value, onChange, required, className = '', showWeekRange = false }: DatePickerProps) {
  const { isOpen, setIsOpen, triggerRef, dropdownRef, dropdownPos } = useDropdown({
    dropdownHeight: CALENDAR_HEIGHT,
    includeOpenUp: true,
  });

  const [viewYear, setViewYear] = useState(new Date().getFullYear());
  const [viewMonth, setViewMonth] = useState(new Date().getMonth());

  const selectedDate = value ? new Date(value) : null;
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  useEffect(() => {
    if (selectedDate) {
      setViewYear(selectedDate.getFullYear());
      setViewMonth(selectedDate.getMonth());
    }
  }, []);

  const formatDisplayDate = (date: Date | null) => {
    if (!date) return '날짜 선택';
    if (showWeekRange) {
      const weekStart = getWeekStartMonday(date);
      const weekEnd = new Date(weekStart);
      weekEnd.setDate(weekStart.getDate() + 6);
      const startMonth = weekStart.getMonth() + 1;
      const endMonth = weekEnd.getMonth() + 1;
      if (startMonth === endMonth) {
        return `${startMonth}월 ${weekStart.getDate()}일 ~ ${weekEnd.getDate()}일`;
      }
      return `${startMonth}월 ${weekStart.getDate()}일 ~ ${endMonth}월 ${weekEnd.getDate()}일`;
    }
    const weekday = WEEKDAYS[date.getDay()];
    return `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일 (${weekday})`;
  };

  const generateCalendarDays = () => {
    const firstDay = new Date(viewYear, viewMonth, 1);
    const lastDay = new Date(viewYear, viewMonth + 1, 0);
    const prevLastDay = new Date(viewYear, viewMonth, 0);

    const firstDayOfWeek = firstDay.getDay();
    const daysInMonth = lastDay.getDate();
    const daysInPrevMonth = prevLastDay.getDate();

    const days: Array<{ date: number; month: 'prev' | 'current' | 'next'; fullDate: Date }> = [];

    for (let i = firstDayOfWeek - 1; i >= 0; i--) {
      days.push({
        date: daysInPrevMonth - i,
        month: 'prev',
        fullDate: new Date(viewYear, viewMonth - 1, daysInPrevMonth - i)
      });
    }

    for (let i = 1; i <= daysInMonth; i++) {
      days.push({
        date: i,
        month: 'current',
        fullDate: new Date(viewYear, viewMonth, i)
      });
    }

    const remainingDays = 42 - days.length;
    for (let i = 1; i <= remainingDays; i++) {
      days.push({
        date: i,
        month: 'next',
        fullDate: new Date(viewYear, viewMonth + 1, i)
      });
    }

    return days;
  };

  const handleDateSelect = (date: Date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    onChange(`${year}-${month}-${day}`);
    setIsOpen(false);
  };

  const handlePrevMonth = () => {
    if (viewMonth === 0) {
      setViewMonth(11);
      setViewYear(viewYear - 1);
    } else {
      setViewMonth(viewMonth - 1);
    }
  };

  const handleNextMonth = () => {
    if (viewMonth === 11) {
      setViewMonth(0);
      setViewYear(viewYear + 1);
    } else {
      setViewMonth(viewMonth + 1);
    }
  };

  const isSameDay = (date1: Date, date2: Date | null) => {
    if (!date2) return false;
    return (
      date1.getFullYear() === date2.getFullYear() &&
      date1.getMonth() === date2.getMonth() &&
      date1.getDate() === date2.getDate()
    );
  };

  const calendarDays = generateCalendarDays();

  return (
    <div className={`relative ${className}`}>
      <button
        ref={triggerRef}
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        className="w-full px-4 py-2.5 bg-purple-50 rounded-xl text-gray-700 text-sm text-left focus:outline-none focus:ring-2 focus:ring-purple-300 transition-all hover:bg-purple-100"
        aria-label="날짜 선택"
        aria-expanded={isOpen}
        aria-required={required}
      >
        {formatDisplayDate(selectedDate)}
      </button>

      {isOpen && (
        <div
          ref={dropdownRef}
          className="fixed bg-white rounded-2xl shadow-xl border border-purple-100 p-4 z-[9999]"
          style={{ top: dropdownPos.top, left: dropdownPos.left, width: '320px' }}
        >
          <div className="flex items-center justify-between mb-4">
            <button
              type="button"
              onClick={handlePrevMonth}
              className="p-2 text-purple-400 hover:bg-purple-50 rounded-xl transition-colors"
              aria-label="이전 달"
            >
              ◀
            </button>
            <div className="text-gray-800 font-medium">
              {viewYear}년 {viewMonth + 1}월
            </div>
            <button
              type="button"
              onClick={handleNextMonth}
              className="p-2 text-purple-400 hover:bg-purple-50 rounded-xl transition-colors"
              aria-label="다음 달"
            >
              ▶
            </button>
          </div>

          <div className="grid grid-cols-7 gap-1 mb-2">
            {WEEKDAYS.map((day, idx) => (
              <div
                key={day}
                className={`text-center text-xs font-medium py-2 ${
                  idx === 0 ? 'text-rose-300' : idx === 6 ? 'text-purple-300' : 'text-gray-400'
                }`}
              >
                {day}
              </div>
            ))}
          </div>

          <div className="grid grid-cols-7 gap-1">
            {calendarDays.map((day, idx) => {
              const isToday = isSameDay(day.fullDate, today);
              const isSelected = isSameDay(day.fullDate, selectedDate);
              const isCurrentMonth = day.month === 'current';
              const isSunday = day.fullDate.getDay() === 0;
              const isSaturday = day.fullDate.getDay() === 6;

              return (
                <button
                  key={idx}
                  type="button"
                  onClick={() => handleDateSelect(day.fullDate)}
                  className={`
                    relative p-2 text-sm rounded-xl transition-all
                    ${isSelected ? 'bg-pink-400 text-white font-medium' : ''}
                    ${!isSelected && isCurrentMonth ? 'text-gray-800 hover:bg-purple-50' : ''}
                    ${!isSelected && !isCurrentMonth ? 'text-gray-300' : ''}
                    ${!isSelected && isSunday && isCurrentMonth ? 'text-rose-300' : ''}
                    ${!isSelected && isSaturday && isCurrentMonth ? 'text-purple-300' : ''}
                    ${isToday && !isSelected ? 'ring-2 ring-purple-300' : ''}
                  `}
                  aria-label={`${day.fullDate.getFullYear()}년 ${day.fullDate.getMonth() + 1}월 ${day.date}일`}
                >
                  {day.date}
                </button>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}
