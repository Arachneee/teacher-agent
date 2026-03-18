'use client';

import { useEffect, useState } from 'react';
import { Lesson, Student, addAttendee, createLesson, createStudent, getStudents, updateLesson } from '../lib/api';

interface Props {
  lesson?: Lesson;
  initialStartTime?: string;
  initialEndTime?: string;
  onSave: () => void;
  onClose: () => void;
}

const HOURS = Array.from({ length: 24 }, (_, i) => i);
const MINUTES = [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55];

function pad(n: number): string {
  return String(n).padStart(2, '0');
}

function parseDateTime(iso: string): { date: string; hour: number; minute: number } {
  const [datePart, timePart] = iso.slice(0, 16).split('T');
  const [hour, minute] = timePart.split(':').map(Number);
  return { date: datePart, hour, minute };
}

function TimePicker({
  label,
  hour,
  minute,
  onHourChange,
  onMinuteChange,
}: {
  label: string;
  hour: number;
  minute: number;
  onHourChange: (h: number) => void;
  onMinuteChange: (m: number) => void;
}) {
  const minuteOptions = MINUTES.includes(minute) ? MINUTES : [...MINUTES, minute].sort((a, b) => a - b);
  return (
    <div className="flex-1">
      <label className="block text-xs font-medium text-gray-500 mb-1 ml-1">{label}</label>
      <div className="flex items-center gap-1.5">
        <select
          value={hour}
          onChange={e => onHourChange(parseInt(e.target.value, 10))}
          className="flex-1 bg-white rounded-xl px-2 py-2 text-gray-700 text-sm outline-none focus:ring-2 focus:ring-purple-300 cursor-pointer appearance-none text-center"
        >
          {HOURS.map(h => (
            <option key={h} value={h}>{pad(h)}시</option>
          ))}
        </select>
        <span className="text-purple-300 font-bold select-none">:</span>
        <select
          value={minute}
          onChange={e => onMinuteChange(parseInt(e.target.value, 10))}
          className="flex-1 bg-white rounded-xl px-2 py-2 text-gray-700 text-sm outline-none focus:ring-2 focus:ring-purple-300 cursor-pointer appearance-none text-center"
        >
          {minuteOptions.map(m => (
            <option key={m} value={m}>{pad(m)}분</option>
          ))}
        </select>
      </div>
    </div>
  );
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
  const todayStr = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
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

  // 수강생 선택 (수업 생성 모드 2단계)
  const [step, setStep] = useState<'details' | 'students'>('details');
  const [createdLessonId, setCreatedLessonId] = useState<number | null>(null);
  const [allStudents, setAllStudents] = useState<Student[]>([]);
  const [studentsLoading, setStudentsLoading] = useState(false);
  const [studentSearchQuery, setStudentSearchQuery] = useState('');
  const [selectedStudentIds, setSelectedStudentIds] = useState<Set<number>>(new Set());
  const [showNewStudentForm, setShowNewStudentForm] = useState(false);
  const [newStudentName, setNewStudentName] = useState('');
  const [newStudentMemo, setNewStudentMemo] = useState('');

  // 수업 생성 모드일 때 학생 목록을 백그라운드로 미리 fetch
  useEffect(() => {
    if (!isEditMode) {
      setStudentsLoading(true);
      getStudents()
        .then(setAllStudents)
        .catch(() => {}) // 2단계에서 재시도 안내
        .finally(() => setStudentsLoading(false));
    }
  }, [isEditMode]);

  const buildIso = (hour: number, minute: number) =>
    `${date}T${pad(hour)}:${pad(minute)}:00`;

  const handleDetailsSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!title.trim() || !date) return;
    setLoading(true);
    setErrorMessage(null);
    try {
      if (isEditMode) {
        await updateLesson(lesson.id, title.trim(), buildIso(startHour, startMinute), buildIso(endHour, endMinute));
        onSave();
      } else {
        const created = await createLesson(title.trim(), buildIso(startHour, startMinute), buildIso(endHour, endMinute));
        setCreatedLessonId(created.id);
        setStep('students');
      }
    } catch {
      setErrorMessage(isEditMode ? '수업을 수정하지 못했어요.' : '수업을 추가하지 못했어요.');
    } finally {
      setLoading(false);
    }
  };

  const toggleStudent = (id: number) => {
    setSelectedStudentIds(prev => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  };

  const handleAddAttendeesAndFinish = async () => {
    if (!createdLessonId) return;
    if (selectedStudentIds.size === 0) {
      onSave();
      return;
    }
    setLoading(true);
    setErrorMessage(null);
    try {
      await Promise.all([...selectedStudentIds].map(id => addAttendee(createdLessonId, id)));
      onSave();
    } catch {
      setErrorMessage('일부 수강생을 추가하지 못했어요. 수업 상세에서 다시 시도해주세요.');
      setLoading(false);
    }
  };

  const handleSkipAttendees = () => {
    onSave();
  };

  const handleCreateAndSelectStudent = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!newStudentName.trim() || !createdLessonId) return;
    setLoading(true);
    setErrorMessage(null);
    try {
      const created = await createStudent(newStudentName.trim(), newStudentMemo.trim());
      await addAttendee(createdLessonId, created.id);
      setAllStudents(prev => [...prev, created]);
      setSelectedStudentIds(prev => new Set([...prev, created.id]));
      setShowNewStudentForm(false);
      setNewStudentName('');
      setNewStudentMemo('');
    } catch {
      setErrorMessage('학생을 등록하지 못했어요');
    } finally {
      setLoading(false);
    }
  };

  const selectedStudentsInStep2 = allStudents.filter(student => selectedStudentIds.has(student.id));
  const unselectedStudentsInStep2 = allStudents.filter(
    student =>
      !selectedStudentIds.has(student.id) &&
      student.name.toLowerCase().includes(studentSearchQuery.toLowerCase())
  );

  return (
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
              {!isEditMode && (
                <div className="flex justify-center gap-1.5 mt-3">
                  <span className="w-2 h-2 rounded-full bg-purple-400" />
                  <span className="w-2 h-2 rounded-full bg-gray-200" />
                </div>
              )}
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

              <div>
                <label className="block text-sm font-medium text-gray-600 mb-1 ml-1">
                  날짜 <span className="text-rose-400">*</span>
                </label>
                <input
                  type="date"
                  value={date}
                  onChange={event => setDate(event.target.value)}
                  className="w-full bg-purple-50 rounded-2xl px-4 py-3 text-gray-700 text-sm outline-none focus:ring-2 focus:ring-purple-300 cursor-pointer"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-600 mb-2 ml-1">
                  시간 <span className="text-rose-400">*</span>
                </label>
                <div className="bg-purple-50 rounded-2xl px-4 py-3 flex items-end gap-3">
                  <TimePicker
                    label="시작"
                    hour={startHour}
                    minute={startMinute}
                    onHourChange={setStartHour}
                    onMinuteChange={setStartMinute}
                  />
                  <span className="text-gray-300 font-medium pb-2">–</span>
                  <TimePicker
                    label="종료"
                    hour={endHour}
                    minute={endMinute}
                    onHourChange={setEndHour}
                    onMinuteChange={setEndMinute}
                  />
                </div>
              </div>

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
                  {loading
                    ? (isEditMode ? '저장 중...' : '생성 중...')
                    : (isEditMode ? '저장하기' : '다음 →')}
                </button>
              </div>
            </form>
          </>
        )}

        {/* Step 2: 수강생 선택 */}
        {step === 'students' && (
          <>
            {/* 헤더 */}
            <div className="text-center pt-6 px-6 pb-4 shrink-0">
              <div className="text-4xl mb-2">👨‍🎓</div>
              <h2 className="text-2xl font-bold text-gray-800">수강생 선택</h2>
              <p className="text-sm text-gray-400 mt-1">수업에 참여할 수강생을 선택해요 (선택사항)</p>
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
                {/* 검색 */}
                <div className="shrink-0 px-6 mb-3">
                  <input
                    value={studentSearchQuery}
                    onChange={event => setStudentSearchQuery(event.target.value)}
                    className="w-full bg-purple-50 rounded-2xl px-4 py-3 text-gray-800 outline-none focus:ring-2 focus:ring-purple-300 placeholder-gray-300"
                    placeholder="등록된 학생 이름으로 검색"
                    autoFocus
                  />
                </div>

                {/* 미선택 학생 목록 (스크롤) */}
                <div className="flex-1 overflow-y-auto min-h-0 px-6">
                  {studentsLoading ? (
                    <div className="flex justify-center py-8">
                      <div className="w-8 h-8 border-4 border-purple-200 border-t-purple-400 rounded-full animate-spin" />
                    </div>
                  ) : unselectedStudentsInStep2.length === 0 ? (
                    <div className="text-center text-gray-300 py-8">
                      <p className="text-sm">
                        {studentSearchQuery
                          ? '검색 결과가 없어요'
                          : allStudents.length === 0
                            ? '등록된 학생이 없어요'
                            : '모두 선택됐어요'}
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
                  )}
                </div>

                {/* 선택된 수강생 고정 섹션 */}
                {selectedStudentsInStep2.length > 0 && (
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
                  {selectedStudentIds.size > 0 && (
                    <button
                      onClick={handleAddAttendeesAndFinish}
                      disabled={loading}
                      className="w-full bg-purple-500 hover:bg-purple-600 disabled:bg-purple-200 text-white font-semibold py-3 rounded-2xl transition-colors duration-150"
                    >
                      {loading ? '추가 중...' : `수강생 ${selectedStudentIds.size}명 추가하고 완료`}
                    </button>
                  )}
                  <div className="flex gap-3">
                    <button
                      onClick={handleSkipAttendees}
                      disabled={loading}
                      className="flex-1 bg-gray-100 hover:bg-gray-200 disabled:opacity-50 text-gray-500 font-medium py-3 rounded-2xl transition-colors duration-150"
                    >
                      {selectedStudentIds.size === 0 ? '나중에 추가하기' : '취소'}
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
  );
}
