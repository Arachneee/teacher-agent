'use client';

import type { DayOfWeek, RecurrenceType } from '../../lib/api';
import CustomSelect from '../CustomSelect';
import DatePicker from '../DatePicker';
import TimePicker from '../TimePicker';

interface LessonDetailsStepProps {
  isEditMode: boolean;
  isRecurringLesson: boolean;
  title: string;
  date: string;
  startHour: number;
  startMinute: number;
  endHour: number;
  endMinute: number;
  recurrenceEnabled: boolean;
  recurrenceType: RecurrenceType;
  intervalValue: number;
  daysOfWeek: Set<DayOfWeek>;
  recurrenceEndDate: string;
  loading: boolean;
  errorMessage: string | null;
  onTitleChange: (value: string) => void;
  onDateChange: (value: string) => void;
  onStartHourChange: (value: number) => void;
  onStartMinuteChange: (value: number) => void;
  onEndHourChange: (value: number) => void;
  onEndMinuteChange: (value: number) => void;
  onRecurrenceEnabledChange: (value: boolean) => void;
  onRecurrenceTypeChange: (value: RecurrenceType) => void;
  onIntervalValueChange: (value: number) => void;
  onDaysOfWeekChange: (value: Set<DayOfWeek>) => void;
  onRecurrenceEndDateChange: (value: string) => void;
  onSubmit: (event: React.FormEvent) => void;
  onClose: () => void;
}

export default function LessonDetailsStep({
  isEditMode,
  isRecurringLesson,
  title,
  date,
  startHour,
  startMinute,
  endHour,
  endMinute,
  recurrenceEnabled,
  recurrenceType,
  intervalValue,
  daysOfWeek,
  recurrenceEndDate,
  loading,
  errorMessage,
  onTitleChange,
  onDateChange,
  onStartHourChange,
  onStartMinuteChange,
  onEndHourChange,
  onEndMinuteChange,
  onRecurrenceEnabledChange,
  onRecurrenceTypeChange,
  onIntervalValueChange,
  onDaysOfWeekChange,
  onRecurrenceEndDateChange,
  onSubmit,
  onClose,
}: LessonDetailsStepProps) {
  return (
    <>
      <div className="text-center pt-6 px-6 pb-4 shrink-0">
        <div className="text-4xl mb-2">{isEditMode ? '✏️' : '📚'}</div>
        <h2 className="text-2xl font-bold text-gray-800">
          {isEditMode ? '수업 수정' : '새 수업 추가'}
        </h2>
        <p className="text-sm text-gray-400 mt-1">
          {isEditMode ? '수업 정보를 수정해요' : '수업 정보를 입력해요'}
        </p>
        <div className="flex justify-center gap-1.5 mt-3">
          <span className="w-2 h-2 rounded-full bg-purple-400" />
          <span className="w-2 h-2 rounded-full bg-gray-200" />
        </div>
      </div>

      <form onSubmit={onSubmit} className="flex flex-col gap-4 px-6 pb-6 overflow-y-auto">
        <div>
          <label className="block text-sm font-medium text-gray-600 mb-1 ml-1">
            수업 제목 <span className="text-rose-400">*</span>
          </label>
          <input
            value={title}
            onChange={event => onTitleChange(event.target.value)}
            className="w-full bg-purple-50 rounded-2xl px-4 py-3 text-gray-800 outline-none focus:ring-2 focus:ring-purple-300 placeholder-gray-300"
            placeholder="수업 제목을 입력하세요"
            autoFocus
            required
          />
        </div>

        <div className="flex flex-col gap-3">
          <div>
            <label className="block text-sm font-medium text-gray-600 mb-1 ml-1">
              날짜 <span className="text-rose-400">*</span>
            </label>
            <DatePicker
              value={date}
              onChange={onDateChange}
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-600 mb-1 ml-1">
              시간 <span className="text-rose-400">*</span>
            </label>
            <div className="bg-purple-50 rounded-2xl px-3 py-2.5 flex items-center gap-2">
              <TimePicker
                hour={startHour}
                minute={startMinute}
                onHourChange={onStartHourChange}
                onMinuteChange={onStartMinuteChange}
              />
              <span className="text-gray-300 font-medium pb-2">–</span>
              <TimePicker
                hour={endHour}
                minute={endMinute}
                onHourChange={onEndHourChange}
                onMinuteChange={onEndMinuteChange}
              />
            </div>
          </div>
        </div>

        {(!isEditMode || (isEditMode && !isRecurringLesson)) && (
          <div>
            <label className="flex items-center gap-2 cursor-pointer ml-1">
              <button
                type="button"
                role="switch"
                aria-checked={recurrenceEnabled}
                onClick={() => onRecurrenceEnabledChange(!recurrenceEnabled)}
                className={`relative w-11 h-6 rounded-full transition-colors duration-200 ${recurrenceEnabled ? 'bg-purple-400' : 'bg-gray-200'}`}
              >
                <span className={`absolute top-0.5 left-0.5 w-5 h-5 bg-white rounded-full shadow transition-transform duration-200 ${recurrenceEnabled ? 'translate-x-5' : ''}`} />
              </button>
              <span className="text-sm font-medium text-gray-600 flex items-center gap-1">
                반복하기
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" className="text-purple-400">
                  <path d="M17 1l4 4-4 4" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M3 11V9a4 4 0 014-4h14" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M7 23l-4-4 4-4" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M21 13v2a4 4 0 01-4 4H3" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </span>
            </label>

            {recurrenceEnabled && (
              <div className="mt-3 flex flex-col gap-3 bg-purple-50 rounded-2xl px-4 py-4">
                <div className="flex gap-3">
                  <div className="flex-1">
                    <label className="block text-xs font-medium text-gray-500 mb-1 ml-1">반복 유형</label>
                    <CustomSelect
                      value={recurrenceType}
                      options={[
                        { value: 'DAILY', label: '매일' },
                        { value: 'WEEKLY', label: '매주' },
                        { value: 'MONTHLY', label: '매월' },
                      ]}
                      onChange={value => onRecurrenceTypeChange(value as RecurrenceType)}
                    />
                  </div>
                  <div className="w-24">
                    <label className="block text-xs font-medium text-gray-500 mb-1 ml-1">간격</label>
                    <CustomSelect
                      value={String(intervalValue)}
                      options={[1, 2, 3, 4].map(value => ({
                        value: String(value),
                        label: `${value}${recurrenceType === 'DAILY' ? '일' : recurrenceType === 'WEEKLY' ? '주' : '개월'}`,
                      }))}
                      onChange={value => onIntervalValueChange(parseInt(value, 10))}
                    />
                  </div>
                </div>

                {recurrenceType === 'WEEKLY' && (
                  <div>
                    <label className="block text-xs font-medium text-gray-500 mb-2 ml-1">요일 선택</label>
                    <div className="flex gap-1.5">
                      {([
                        ['월', 'MONDAY'],
                        ['화', 'TUESDAY'],
                        ['수', 'WEDNESDAY'],
                        ['목', 'THURSDAY'],
                        ['금', 'FRIDAY'],
                        ['토', 'SATURDAY'],
                        ['일', 'SUNDAY'],
                      ] as const).map(([label, day]) => (
                        <button
                          key={day}
                          type="button"
                          onClick={() => {
                            const next = new Set(daysOfWeek);
                            if (next.has(day)) next.delete(day);
                            else next.add(day);
                            onDaysOfWeekChange(next);
                          }}
                          className={`flex-1 py-2 rounded-xl text-xs font-semibold transition-colors duration-150 ${
                            daysOfWeek.has(day)
                              ? 'bg-purple-400 text-white'
                              : 'bg-white text-gray-500 hover:bg-purple-100'
                          }`}
                        >
                          {label}
                        </button>
                      ))}
                    </div>
                  </div>
                )}

                <div>
                  <label className="block text-xs font-medium text-gray-500 mb-1 ml-1">반복 종료일</label>
                  <DatePicker
                    value={recurrenceEndDate}
                    onChange={onRecurrenceEndDateChange}
                  />
                </div>

                <div className="bg-amber-50 border border-amber-100 rounded-xl px-3 py-2">
                  <p className="text-xs text-amber-700 leading-relaxed">
                    💡 반복 수업은 시작일로부터 최대 <strong>6개월</strong>까지 설정할 수 있어요.
                  </p>
                </div>
              </div>
            )}
          </div>
        )}

        {errorMessage && (
          <p className="text-xs text-rose-400 bg-rose-50 rounded-xl px-3 py-2">{errorMessage}</p>
        )}

        <div className="flex gap-3 mt-2">
          <button
            type="button"
            onClick={onClose}
            className="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-500 font-medium py-3 rounded-2xl transition-colors duration-150"
          >
            취소
          </button>
          <button
            type="submit"
            disabled={loading || !title.trim() || !date}
            className="flex-1 bg-pink-400 hover:bg-pink-500 disabled:bg-pink-200 text-white font-medium py-3 rounded-2xl transition-colors duration-150"
          >
            {loading ? '저장 중...' : '다음 →'}
          </button>
        </div>
      </form>
    </>
  );
}
