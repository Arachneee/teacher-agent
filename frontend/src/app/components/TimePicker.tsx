'use client';

import { useMemo } from 'react';
import { HOURS, MINUTES, padTwoDigits } from '../lib/dateTimeUtils';

interface Props {
  hour: number;
  minute: number;
  onHourChange: (hour: number) => void;
  onMinuteChange: (minute: number) => void;
}

export default function TimePicker({ hour, minute, onHourChange, onMinuteChange }: Props) {
  const minuteOptions = useMemo(
    () => (MINUTES.includes(minute) ? MINUTES : [...MINUTES, minute].sort((a, b) => a - b)),
    [minute]
  );

  return (
    <div className="flex-1">
      <div className="flex items-center gap-1.5">
        <select
          value={hour}
          onChange={event => onHourChange(parseInt(event.target.value, 10))}
          className="flex-1 bg-white rounded-xl px-2 py-2 text-gray-700 text-sm outline-none focus:ring-2 focus:ring-purple-300 cursor-pointer appearance-none text-center"
        >
          {HOURS.map(hourValue => (
            <option key={hourValue} value={hourValue}>{padTwoDigits(hourValue)}시</option>
          ))}
        </select>
        <span className="text-purple-300 font-bold select-none">:</span>
        <select
          value={minute}
          onChange={event => onMinuteChange(parseInt(event.target.value, 10))}
          className="flex-1 bg-white rounded-xl px-2 py-2 text-gray-700 text-sm outline-none focus:ring-2 focus:ring-purple-300 cursor-pointer appearance-none text-center"
        >
          {minuteOptions.map(minuteValue => (
            <option key={minuteValue} value={minuteValue}>{padTwoDigits(minuteValue)}분</option>
          ))}
        </select>
      </div>
    </div>
  );
}
