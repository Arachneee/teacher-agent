'use client';

import { useEffect, useState } from 'react';
import type { DayOfWeek, RecurrenceCreateRequest, RecurrenceType, UpdateScope } from '../lib/api';
import {
  Lesson,
  LessonDetailAttendee,
  Student,
  addAttendee,
  createLesson,
  createStudent,
  getLessonDetail,
  getStudents,
  removeAttendee,
  updateLesson,
} from '../lib/api';
import { padTwoDigits, parseDateTime } from '../lib/dateTimeUtils';
import CustomSelect from './CustomSelect';
import DatePicker from './DatePicker';
import RecurringScopeModal from './RecurringScopeModal';
import TimePicker from './TimePicker';

interface Props {
  lesson?: Lesson;
  initialStartTime?: string;
  initialEndTime?: string;
  onSave: () => void;
  onClose: () => void;
}

function getInitialValues(lesson?: Lesson, initialStartTime?: string, initialEndTime?: string) {
  if (lesson) {
    const start = parseDateTime(lesson.startTime);
    const end = parseDateTime(lesson.endTime);
    return { date: start.date, startHour: start.hour, startMinute: start.minute, endHour: end.hour, endMinute: end.minute };
  }
  if (initialStartTime && initialEndTime) {
    const start = parseDateTime(initialStartTime);
    const end = parseDateTime(initialEndTime);
    return { date: start.date, startHour: start.hour, startMinute: start.minute, endHour: end.hour, endMinute: end.minute };
  }
  const now = new Date();
  const roundedMinute = Math.round(now.getMinutes() / 5) * 5 % 60;
  const startHour = roundedMinute === 60 ? now.getHours() + 1 : now.getHours();
  const todayStr = `${now.getFullYear()}-${padTwoDigits(now.getMonth() + 1)}-${padTwoDigits(now.getDate())}`;
  return { date: todayStr, startHour, startMinute: roundedMinute % 60, endHour: (startHour + 1) % 24, endMinute: roundedMinute % 60 };
}

export default function AddLessonModal({ lesson, initialStartTime, initialEndTime, onSave, onClose }: Props) {
  const isEditMode = lesson !== undefined;
  const initial = getInitialValues(lesson, initialStartTime, initialEndTime);

  const [title, setTitle] = useState(lesson?.title ?? '');
  const [date, setDate] = useState(initial.date);
  const [startHour, setStartHour] = useState(initial.startHour);
  const [startMinute, setStartMinute] = useState(initial.startMinute);
  const [endHour, setEndHour] = useState(initial.endHour);
  const [endMinute, setEndMinute] = useState(initial.endMinute);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const [recurrenceEnabled, setRecurrenceEnabled] = useState(false);
  const [recurrenceType, setRecurrenceType] = useState<RecurrenceType>('WEEKLY');
  const [intervalValue, setIntervalValue] = useState(1);
  const [daysOfWeek, setDaysOfWeek] = useState<Set<DayOfWeek>>(new Set());
  const [recurrenceEndDate, setRecurrenceEndDate] = useState('');

  const [step, setStep] = useState<'details' | 'students'>('details');
  // 생성 모드: step1에서 입력한 데이터를 보관 (step2 완료 시 createLesson 호출)
  const [pendingCreateData, setPendingCreateData] = useState<{
    title: string;
    startIso: string;
    endIso: string;
    recurrence?: RecurrenceCreateRequest;
  } | null>(null);
  const [allStudents, setAllStudents] = useState<Student[]>([]);
  const [studentsLoading, setStudentsLoading] = useState(false);
  const [studentSearchQuery, setStudentSearchQuery] = useState('');
  const [showNewStudentForm, setShowNewStudentForm] = useState(false);
  const [newStudentName, setNewStudentName] = useState('');
  const [newStudentMemo, setNewStudentMemo] = useState('');
  const [showEditScopeModal, setShowEditScopeModal] = useState(false);
  const [pendingEditData, setPendingEditData] = useState<{ title: string; startIso: string; endIso: string } | null>(null);

  // 생성 모드 전용: 선택된 학생 ID 목록
  const [selectedStudentIds, setSelectedStudentIds] = useState<Set<number>>(new Set());

  // 수정 모드 전용: 현재 수강생 목록 (즉시 반영)
  const [currentAttendees, setCurrentAttendees] = useState<LessonDetailAttendee[]>([]);

  useEffect(() => {
    setStudentsLoading(true);
    const promises: Promise<unknown>[] = [getStudents().then(setAllStudents)];
    if (isEditMode) {
      promises.push(getLessonDetail(lesson.id).then(detail => setCurrentAttendees(detail.attendees)));
    }
    Promise.allSettled(promises).finally(() => setStudentsLoading(false));
  }, [isEditMode, lesson?.id]);

  const buildIso = (hour: number, minute: number) =>
    `${date}T${padTwoDigits(hour)}:${padTwoDigits(minute)}:00`;

  const handleDetailsSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!title.trim()) { setErrorMessage('수업 제목을 입력해주세요.'); return; }
    if (!date) { setErrorMessage('날짜를 선택해주세요.'); return; }
    if (recurrenceEnabled && recurrenceType === 'WEEKLY' && daysOfWeek.size === 0) {
      setErrorMessage('반복할 요일을 하나 이상 선택해주세요.'); return;
    }
    if (recurrenceEnabled && !recurrenceEndDate) {
      setErrorMessage('반복 종료일을 선택해주세요.'); return;
    }
    if (recurrenceEnabled && recurrenceEndDate && recurrenceEndDate < date) {
      setErrorMessage('반복 종료일은 수업 시작일 이후여야 해요.'); return;
    }
    if (recurrenceEnabled && recurrenceType === 'WEEKLY' && daysOfWeek.size > 0 && recurrenceEndDate) {
      const dayMap: Record<string, number> = { SUNDAY: 0, MONDAY: 1, TUESDAY: 2, WEDNESDAY: 3, THURSDAY: 4, FRIDAY: 5, SATURDAY: 6 };
      const selectedJsDays = new Set([...daysOfWeek].map(d => dayMap[d]));
      const start = new Date(date);
      const end = new Date(recurrenceEndDate);
      let found = false;
      for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
        if (selectedJsDays.has(d.getDay())) { found = true; break; }
      }
      if (!found) {
        const dayNames: Record<string, string> = { MONDAY: '월', TUESDAY: '화', WEDNESDAY: '수', THURSDAY: '목', FRIDAY: '금', SATURDAY: '토', SUNDAY: '일' };
        const selected = [...daysOfWeek].map(d => dayNames[d]).join(', ');
        setErrorMessage(`선택한 기간 내에 ${selected}요일이 없어요. 종료일을 늘리거나 요일을 변경해주세요.`);
        return;
      }
    }
    setLoading(true);
    setErrorMessage(null);
    try {
      if (isEditMode) {
        const startIso = buildIso(startHour, startMinute);
        const endIso = buildIso(endHour, endMinute);
        
        if (lesson.recurrenceGroupId) {
          setPendingEditData({ title: title.trim(), startIso, endIso });
          setShowEditScopeModal(true);
          setLoading(false);
          return;
        }
        
        const recurrence: RecurrenceCreateRequest | undefined = recurrenceEnabled
          ? {
              recurrenceType,
              intervalValue,
              ...(recurrenceType === 'WEEKLY' ? { daysOfWeek: [...daysOfWeek] } : {}),
              endDate: recurrenceEndDate,
            }
          : undefined;
        
        await updateLesson(lesson.id, title.trim(), startIso, endIso, undefined, recurrence);
        setStep('students');
      } else {
        const recurrence: RecurrenceCreateRequest | undefined = recurrenceEnabled
          ? {
              recurrenceType,
              intervalValue,
              ...(recurrenceType === 'WEEKLY' ? { daysOfWeek: [...daysOfWeek] } : {}),
              endDate: recurrenceEndDate,
            }
          : undefined;
        setPendingCreateData({
          title: title.trim(),
          startIso: buildIso(startHour, startMinute),
          endIso: buildIso(endHour, endMinute),
          recurrence,
        });
        setStep('students');
      }
    } catch {
      setErrorMessage(isEditMode ? '수업을 수정하지 못했어요.' : '수업을 추가하지 못했어요.');
    } finally {
      setLoading(false);
    }
  };

  // 생성 모드: 학생 선택 토글
  const toggleStudent = (id: number) => {
    setSelectedStudentIds(prev => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  // 반복 수업 수정: scope 선택 후 실제 업데이트
  const handleEditWithScope = async (scope: UpdateScope) => {
    if (!lesson || !pendingEditData) return;
    setShowEditScopeModal(false);
    setLoading(true);
    setErrorMessage(null);
    try {
      await updateLesson(lesson.id, pendingEditData.title, pendingEditData.startIso, pendingEditData.endIso, scope);
      setStep('students');
    } catch {
      setErrorMessage('수업을 수정하지 못했어요.');
    } finally {
      setLoading(false);
      setPendingEditData(null);
    }
  };

  const handleCreateAndFinish = async (studentIds: number[]) => {
    if (!pendingCreateData) return;
    setLoading(true);
    setErrorMessage(null);
    try {
      await createLesson(
        pendingCreateData.title,
        pendingCreateData.startIso,
        pendingCreateData.endIso,
        pendingCreateData.recurrence,
        studentIds.length > 0 ? studentIds : undefined,
      );
      onSave();
    } catch {
      setErrorMessage('수업을 추가하지 못했어요.');
      setLoading(false);
    }
  };

  // 수정 모드: 수강생 즉시 추가
  const handleEditModeAdd = async (student: Student) => {
    if (!lesson) return;
    
    if (lesson.recurrenceGroupId) {
      setLoading(true);
      setErrorMessage(null);
      try {
        const attendee = await addAttendee(lesson.id, student.id);
        setCurrentAttendees(prev => [...prev, { attendeeId: attendee.id, student, feedback: null }]);
      } catch {
        setErrorMessage('수강생을 추가하지 못했어요.');
      } finally {
        setLoading(false);
      }
      return;
    }
    
    setLoading(true);
    setErrorMessage(null);
    try {
      const attendee = await addAttendee(lesson.id, student.id);
      setCurrentAttendees(prev => [...prev, { attendeeId: attendee.id, student, feedback: null }]);
    } catch {
      setErrorMessage('수강생을 추가하지 못했어요.');
    } finally {
      setLoading(false);
    }
  };

  // 수정 모드: 수강생 즉시 삭제
  const handleEditModeRemove = async (attendeeId: number) => {
    if (!lesson) return;
    
    if (lesson.recurrenceGroupId) {
      setLoading(true);
      setErrorMessage(null);
      try {
        await removeAttendee(lesson.id, attendeeId);
        setCurrentAttendees(prev => prev.filter(attendee => attendee.attendeeId !== attendeeId));
      } catch {
        setErrorMessage('수강생을 삭제하지 못했어요.');
      } finally {
        setLoading(false);
      }
      return;
    }
    
    setLoading(true);
    setErrorMessage(null);
    try {
      await removeAttendee(lesson.id, attendeeId);
      setCurrentAttendees(prev => prev.filter(attendee => attendee.attendeeId !== attendeeId));
    } catch {
      setErrorMessage('수강생을 삭제하지 못했어요.');
    } finally {
      setLoading(false);
    }
  };

  // 새 학생 등록 후 수강생으로 추가
  const handleCreateAndSelectStudent = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!newStudentName.trim()) { setErrorMessage('학생 이름을 입력해주세요.'); return; }
    if (isEditMode && !lesson?.id) return;
    setLoading(true);
    setErrorMessage(null);
    try {
      const created = await createStudent(newStudentName.trim(), newStudentMemo.trim());
      setAllStudents(prev => [...prev, created]);
      
      if (isEditMode && lesson) {
        if (lesson.recurrenceGroupId) {
          const attendee = await addAttendee(lesson.id, created.id);
          setCurrentAttendees(prev => [...prev, { attendeeId: attendee.id, student: created, feedback: null }]);
        } else {
          const attendee = await addAttendee(lesson.id, created.id);
          setCurrentAttendees(prev => [...prev, { attendeeId: attendee.id, student: created, feedback: null }]);
        }
      } else {
        setSelectedStudentIds(prev => new Set([...prev, created.id]));
      }
      
      setShowNewStudentForm(false);
      setNewStudentName('');
      setNewStudentMemo('');
    } catch {
      setErrorMessage('학생을 등록하지 못했어요');
    } finally {
      setLoading(false);
    }
  };

  const currentAttendeeStudentIds = new Set(currentAttendees.map(attendee => attendee.student.id));

  const selectedStudentsInStep2 = allStudents.filter(student => selectedStudentIds.has(student.id));
  const unselectedStudentsInStep2 = allStudents.filter(
    student =>
      !selectedStudentIds.has(student.id) &&
      student.name.toLowerCase().includes(studentSearchQuery.toLowerCase())
  );
  const addableStudents = allStudents.filter(
    student =>
      !currentAttendeeStudentIds.has(student.id) &&
      student.name.toLowerCase().includes(studentSearchQuery.toLowerCase())
  );

  return (
    <>
      {showEditScopeModal && lesson && (
        <RecurringScopeModal
          mode="edit"
          lessonTitle={lesson.title}
          onSelect={handleEditWithScope}
          onClose={() => {
            setShowEditScopeModal(false);
            setPendingEditData(null);
            setLoading(false);
          }}
        />
      )}

      <div
        className="fixed inset-0 bg-black/30 backdrop-blur-sm flex items-center justify-center z-50 p-4"
        onClick={event => event.target === event.currentTarget && onClose()}
      >
        <div className="bg-white rounded-3xl w-full max-w-md shadow-2xl max-h-[85vh] flex flex-col overflow-hidden">

        {/* Step 1: 수업 정보 */}
        {step === 'details' && (
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

            <form onSubmit={handleDetailsSubmit} className="flex flex-col gap-4 px-6 pb-6 overflow-y-auto">
              <div>
                <label className="block text-sm font-medium text-gray-600 mb-1 ml-1">
                  수업 제목 <span className="text-rose-400">*</span>
                </label>
                <input
                  value={title}
                  onChange={event => setTitle(event.target.value)}
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
                    onChange={setDate}
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
                      onHourChange={setStartHour}
                      onMinuteChange={setStartMinute}
                    />
                    <span className="text-gray-300 font-medium pb-2">–</span>
                    <TimePicker
                      hour={endHour}
                      minute={endMinute}
                      onHourChange={setEndHour}
                      onMinuteChange={setEndMinute}
                    />
                  </div>
                </div>
              </div>

              {(!isEditMode || (isEditMode && !lesson?.recurrenceGroupId)) && (
                <div>
                  <label className="flex items-center gap-2 cursor-pointer ml-1">
                    <button
                      type="button"
                      role="switch"
                      aria-checked={recurrenceEnabled}
                      onClick={() => setRecurrenceEnabled(prev => !prev)}
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
                            onChange={value => setRecurrenceType(value as RecurrenceType)}
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
                            onChange={value => setIntervalValue(parseInt(value, 10))}
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
                                onClick={() => setDaysOfWeek(prev => {
                                  const next = new Set(prev);
                                  if (next.has(day)) next.delete(day);
                                  else next.add(day);
                                  return next;
                                })}
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
                          onChange={setRecurrenceEndDate}
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
        )}

        {/* Step 2: 수강생 관리 */}
        {step === 'students' && (
          <>
            <div className="text-center pt-6 px-6 pb-4 shrink-0">
              <div className="text-4xl mb-2">👨‍🎓</div>
              <h2 className="text-2xl font-bold text-gray-800">
                {isEditMode ? '수강생 관리' : '수강생 선택'}
              </h2>
              <p className="text-sm text-gray-400 mt-1">
                {isEditMode ? '수강생을 추가하거나 삭제해요' : '수업에 참여할 수강생을 선택해요 (선택사항)'}
              </p>
              <div className="flex justify-center gap-1.5 mt-3">
                <span className="w-2 h-2 rounded-full bg-gray-200" />
                <span className="w-2 h-2 rounded-full bg-purple-400" />
              </div>
            </div>

            {errorMessage && (
              <p className="text-xs text-rose-400 bg-rose-50 rounded-xl px-3 py-2 mx-6 mb-3 shrink-0">
                {errorMessage}
              </p>
            )}

            {!showNewStudentForm ? (
              <>
                {/* 수정 모드: 현재 수강생 목록 */}
                {isEditMode && (
                  <div className="shrink-0 px-6 mb-3">
                    <p className="text-xs font-semibold text-gray-400 mb-2">
                      현재 수강생 {currentAttendees.length}명
                    </p>
                    {studentsLoading ? (
                      <div className="flex justify-center py-3">
                        <div className="w-6 h-6 border-4 border-purple-200 border-t-purple-400 rounded-full animate-spin" />
                      </div>
                    ) : currentAttendees.length === 0 ? (
                      <p className="text-xs text-gray-300 text-center py-2">아직 수강생이 없어요</p>
                    ) : (
                      <div className="flex flex-col gap-1 max-h-36 overflow-y-auto">
                        {currentAttendees.map(attendee => (
                          <div
                            key={attendee.attendeeId}
                            className="flex items-center gap-3 px-3 py-2 bg-purple-50 rounded-xl"
                          >
                            <div className="w-7 h-7 rounded-lg bg-purple-100 text-purple-500 flex items-center justify-center font-semibold text-xs shrink-0">
                              {attendee.student.name.charAt(0)}
                            </div>
                            <p className="text-sm font-medium text-gray-800 truncate flex-1">
                              {attendee.student.name}
                            </p>
                            <button
                              onClick={() => handleEditModeRemove(attendee.attendeeId)}
                              disabled={loading}
                              className="text-gray-300 hover:text-rose-400 transition-colors shrink-0 disabled:opacity-50"
                              aria-label="수강생 삭제"
                            >
                              ✕
                            </button>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                )}

                {/* 검색 */}
                <div className="shrink-0 px-6 mb-3">
                  <input
                    value={studentSearchQuery}
                    onChange={event => setStudentSearchQuery(event.target.value)}
                    className="w-full bg-purple-50 rounded-2xl px-4 py-3 text-gray-800 outline-none focus:ring-2 focus:ring-purple-300 placeholder-gray-300"
                    placeholder={isEditMode ? '추가할 학생 이름으로 검색' : '등록된 학생 이름으로 검색'}
                    autoFocus={!isEditMode}
                  />
                </div>

                {/* 학생 목록 */}
                <div className="flex-1 overflow-y-auto min-h-0 px-6">
                  {studentsLoading ? (
                    <div className="flex justify-center py-8">
                      <div className="w-8 h-8 border-4 border-purple-200 border-t-purple-400 rounded-full animate-spin" />
                    </div>
                  ) : isEditMode ? (
                    addableStudents.length === 0 ? (
                      <div className="text-center text-gray-300 py-8">
                        <p className="text-sm">
                          {studentSearchQuery ? '검색 결과가 없어요' : allStudents.length === 0 ? '등록된 학생이 없어요' : '모두 추가됐어요'}
                        </p>
                      </div>
                    ) : (
                      <div className="flex flex-col gap-1.5 pb-2">
                        {addableStudents.map(student => (
                          <button
                            key={student.id}
                            onClick={() => handleEditModeAdd(student)}
                            disabled={loading}
                            className="flex items-center gap-3 w-full text-left px-4 py-3 rounded-2xl hover:bg-purple-50 transition-colors duration-150 disabled:opacity-50"
                          >
                            <div className="w-9 h-9 rounded-xl bg-purple-100 text-purple-500 flex items-center justify-center font-semibold text-sm shrink-0">
                              {student.name.charAt(0)}
                            </div>
                            <div className="min-w-0 flex-1">
                              <p className="text-sm font-medium text-gray-800 truncate">{student.name}</p>
                              {student.memo && (
                                <p className="text-xs text-gray-400 truncate">{student.memo}</p>
                              )}
                            </div>
                            <span className="text-purple-300 text-lg shrink-0">+</span>
                          </button>
                        ))}
                      </div>
                    )
                  ) : (
                    unselectedStudentsInStep2.length === 0 ? (
                      <div className="text-center text-gray-300 py-8">
                        <p className="text-sm">
                          {studentSearchQuery ? '검색 결과가 없어요' : allStudents.length === 0 ? '등록된 학생이 없어요' : '모두 선택됐어요'}
                        </p>
                      </div>
                    ) : (
                      <div className="flex flex-col gap-1.5 pb-2">
                        {unselectedStudentsInStep2.map(student => (
                          <button
                            key={student.id}
                            onClick={() => toggleStudent(student.id)}
                            disabled={loading}
                            className="flex items-center gap-3 w-full text-left px-4 py-3 rounded-2xl hover:bg-purple-50 transition-colors duration-150 disabled:opacity-50"
                          >
                            <div className="w-9 h-9 rounded-xl bg-purple-100 text-purple-500 flex items-center justify-center font-semibold text-sm shrink-0">
                              {student.name.charAt(0)}
                            </div>
                            <div className="min-w-0 flex-1">
                              <p className="text-sm font-medium text-gray-800 truncate">{student.name}</p>
                              {student.memo && (
                                <p className="text-xs text-gray-400 truncate">{student.memo}</p>
                              )}
                            </div>
                          </button>
                        ))}
                      </div>
                    )
                  )}
                </div>

                {/* 생성 모드: 선택된 수강생 고정 섹션 */}
                {!isEditMode && selectedStudentsInStep2.length > 0 && (
                  <div className="shrink-0 border-t border-gray-100 px-6 pt-3 pb-2">
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-xs font-semibold text-purple-600">
                        선택된 수강생 {selectedStudentsInStep2.length}명
                      </span>
                      <button
                        onClick={() => setSelectedStudentIds(new Set())}
                        className="text-xs text-gray-400 hover:text-gray-600 transition-colors"
                      >
                        전체 해제
                      </button>
                    </div>
                    <div className="flex flex-col gap-1 max-h-36 overflow-y-auto">
                      {selectedStudentsInStep2.map(student => (
                        <div
                          key={student.id}
                          className="flex items-center gap-3 px-3 py-2 bg-purple-50 rounded-xl"
                        >
                          <div className="w-7 h-7 rounded-lg bg-purple-100 text-purple-500 flex items-center justify-center font-semibold text-xs shrink-0">
                            {student.name.charAt(0)}
                          </div>
                          <p className="text-sm font-medium text-gray-800 truncate flex-1">{student.name}</p>
                          <button
                            onClick={() => toggleStudent(student.id)}
                            disabled={loading}
                            className="text-gray-300 hover:text-rose-400 transition-colors shrink-0 disabled:opacity-50"
                          >
                            ✕
                          </button>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* 하단 버튼 */}
                <div className="shrink-0 px-6 pt-3 pb-6 flex flex-col gap-2">
                  {!isEditMode && selectedStudentIds.size > 0 && (
                    <button
                      onClick={() => handleCreateAndFinish([...selectedStudentIds])}
                      disabled={loading}
                      className="w-full bg-purple-500 hover:bg-purple-600 disabled:bg-purple-200 text-white font-semibold py-3 rounded-2xl transition-colors duration-150"
                    >
                      {loading ? '추가 중...' : `수강생 ${selectedStudentIds.size}명 추가하고 완료`}
                    </button>
                  )}
                  <div className="flex gap-3">
                    {!isEditMode && (
                      <button
                        type="button"
                        onClick={() => setStep('details')}
                        className="bg-gray-100 hover:bg-gray-200 text-gray-500 font-medium py-3 px-4 rounded-2xl transition-colors duration-150"
                      >
                        ←
                      </button>
                    )}
                    <button
                      onClick={isEditMode ? onSave : () => handleCreateAndFinish([])}
                      disabled={loading}
                      className="flex-1 bg-gray-100 hover:bg-gray-200 disabled:opacity-50 text-gray-500 font-medium py-3 rounded-2xl transition-colors duration-150"
                    >
                      {isEditMode ? '완료' : selectedStudentIds.size === 0 ? '나중에 추가하기' : '취소'}
                    </button>
                    <button
                      onClick={() => setShowNewStudentForm(true)}
                      disabled={loading}
                      className="flex-1 bg-pink-400 hover:bg-pink-500 disabled:opacity-50 text-white font-medium py-3 rounded-2xl transition-colors duration-150"
                    >
                      새 학생 등록 ✨
                    </button>
                  </div>
                </div>
              </>
            ) : (
              /* 새 학생 등록 인라인 폼 */
              <form onSubmit={handleCreateAndSelectStudent} className="flex flex-col gap-4 flex-1 px-6 pb-6 overflow-y-auto">
                <div className="bg-amber-50 border border-amber-100 rounded-2xl px-4 py-3 shrink-0">
                  <p className="text-xs text-amber-700 leading-relaxed">
                    💡 <strong>학생 등록</strong>은 시스템 전체에 학생 정보를 추가해요.
                    등록 후 이 수업 <strong>수강생으로 자동 추가</strong>돼요.
                  </p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-600 mb-1 ml-1">
                    이름 <span className="text-rose-400">*</span>
                  </label>
                  <input
                    value={newStudentName}
                    onChange={event => setNewStudentName(event.target.value)}
                    className="w-full bg-purple-50 rounded-2xl px-4 py-3 text-gray-800 outline-none focus:ring-2 focus:ring-purple-300 placeholder-gray-300"
                    placeholder="학생 이름을 입력하세요"
                    autoFocus
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-600 mb-1 ml-1">메모</label>
                  <textarea
                    value={newStudentMemo}
                    onChange={event => setNewStudentMemo(event.target.value)}
                    className="w-full bg-purple-50 rounded-2xl px-4 py-3 text-gray-800 outline-none focus:ring-2 focus:ring-purple-300 placeholder-gray-300 resize-none"
                    placeholder="학생에 대한 메모 (선택)"
                    rows={3}
                    maxLength={500}
                  />
                  <p className="text-xs text-gray-300 text-right mt-1">{newStudentMemo.length}/500</p>
                </div>

                <div className="flex gap-3 mt-auto">
                  <button
                    type="button"
                    onClick={() => setShowNewStudentForm(false)}
                    className="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-500 font-medium py-3 rounded-2xl transition-colors duration-150"
                  >
                    뒤로
                  </button>
                  <button
                    type="submit"
                    disabled={loading || !newStudentName.trim()}
                    className="flex-1 bg-pink-400 hover:bg-pink-500 disabled:bg-pink-200 text-white font-medium py-3 rounded-2xl transition-colors duration-150"
                  >
                    {loading ? '등록 중...' : '등록하고 추가하기 ✨'}
                  </button>
                </div>
              </form>
            )}
          </>
        )}
      </div>
    </div>
    </>
  );
}
