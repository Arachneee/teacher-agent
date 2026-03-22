'use client';

import { useEffect, useState } from 'react';
import type { DayOfWeek, RecurrenceCreateRequest, RecurrenceType, SchoolGrade, UpdateScope } from '../lib/api';
import {
  Lesson,
  LessonDetailAttendee,
  Student,
  createLesson,
  createStudent,
  getLessonDetail,
  getStudents,
  updateLesson,
} from '../lib/api';
import { padTwoDigits, parseDateTime } from '../lib/dateTimeUtils';
import RecurringScopeModal from './RecurringScopeModal';
import LessonDetailsStep from './AddLessonModal/LessonDetailsStep';
import StudentSelectionStep from './AddLessonModal/StudentSelectionStep';

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
  const isRecurringLesson = isEditMode && lesson.recurrenceGroupId !== null;
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

  useEffect(() => {
    setStep('details');
  }, [lesson?.id]);

  const [pendingCreateData, setPendingCreateData] = useState<{
    title: string;
    startIso: string;
    endIso: string;
    recurrence?: RecurrenceCreateRequest;
  } | null>(null);

  const [pendingEditData, setPendingEditData] = useState<{
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
  const [newStudentGrade, setNewStudentGrade] = useState<SchoolGrade>('ELEMENTARY_1');
  const [showEditScopeModal, setShowEditScopeModal] = useState(false);

  const [selectedStudentIds, setSelectedStudentIds] = useState<Set<number>>(new Set());
  const [initialAttendeeStudentIds, setInitialAttendeeStudentIds] = useState<Set<number>>(new Set());
  const [currentAttendees, setCurrentAttendees] = useState<LessonDetailAttendee[]>([]);

  useEffect(() => {
    setStudentsLoading(true);
    const promises: Promise<unknown>[] = [getStudents().then(setAllStudents)];
    if (isEditMode) {
      promises.push(getLessonDetail(lesson.id).then(detail => {
        setCurrentAttendees(detail.attendees);
        const ids = new Set(detail.attendees.map(a => a.student.id));
        setInitialAttendeeStudentIds(ids);
        setSelectedStudentIds(ids);
      }));
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
    setErrorMessage(null);

    const recurrence: RecurrenceCreateRequest | undefined = recurrenceEnabled
      ? {
          recurrenceType,
          intervalValue,
          ...(recurrenceType === 'WEEKLY' ? { daysOfWeek: [...daysOfWeek] } : {}),
          endDate: recurrenceEndDate,
        }
      : undefined;

    if (isEditMode) {
      setPendingEditData({
        title: title.trim(),
        startIso: buildIso(startHour, startMinute),
        endIso: buildIso(endHour, endMinute),
        recurrence,
      });
    } else {
      setPendingCreateData({
        title: title.trim(),
        startIso: buildIso(startHour, startMinute),
        endIso: buildIso(endHour, endMinute),
        recurrence,
      });
    }
    setStep('students');
  };

  const toggleStudent = (id: number) => {
    setSelectedStudentIds(prev => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const getAttendeeChanges = () => {
    const addStudentIds = [...selectedStudentIds].filter(id => !initialAttendeeStudentIds.has(id));
    const removeStudentIds = [...initialAttendeeStudentIds].filter(id => !selectedStudentIds.has(id));
    return { addStudentIds, removeStudentIds };
  };

  const handleEditFinish = () => {
    if (!lesson || !pendingEditData) return;
    if (isRecurringLesson) {
      setShowEditScopeModal(true);
    } else {
      executeEditUpdate('SINGLE');
    }
  };

  const handleEditWithScope = async (scope: UpdateScope) => {
    setShowEditScopeModal(false);
    await executeEditUpdate(scope);
  };

  const executeEditUpdate = async (scope: UpdateScope) => {
    if (!lesson || !pendingEditData) return;
    setLoading(true);
    setErrorMessage(null);
    try {
      const { addStudentIds, removeStudentIds } = getAttendeeChanges();
      await updateLesson(
        lesson.id,
        pendingEditData.title,
        pendingEditData.startIso,
        pendingEditData.endIso,
        scope,
        pendingEditData.recurrence,
        addStudentIds.length > 0 ? addStudentIds : undefined,
        removeStudentIds.length > 0 ? removeStudentIds : undefined
      );
      onSave();
    } catch {
      setErrorMessage('수업을 수정하지 못했어요.');
    } finally {
      setLoading(false);
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

  const handleEditModeAdd = (student: Student) => {
    if (selectedStudentIds.has(student.id)) return;
    setSelectedStudentIds(prev => new Set([...prev, student.id]));
    setCurrentAttendees(prev => [...prev, { attendeeId: -student.id, student, feedback: null }]);
  };

  const handleEditModeRemove = (studentId: number) => {
    setSelectedStudentIds(prev => {
      const next = new Set(prev);
      next.delete(studentId);
      return next;
    });
    setCurrentAttendees(prev => prev.filter(a => a.student.id !== studentId));
  };

  const handleCreateAndSelectStudent = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!newStudentName.trim()) { setErrorMessage('학생 이름을 입력해주세요.'); return; }
    setLoading(true);
    setErrorMessage(null);
    try {
      const created = await createStudent(newStudentName.trim(), newStudentMemo.trim(), newStudentGrade);
      setAllStudents(prev => [...prev, created]);
      setSelectedStudentIds(prev => new Set([...prev, created.id]));
      
      if (isEditMode) {
        setCurrentAttendees(prev => [...prev, { attendeeId: -created.id, student: created, feedback: null }]);
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
          {step === 'details' && (
            <LessonDetailsStep
              isEditMode={isEditMode}
              isRecurringLesson={isRecurringLesson}
              title={title}
              date={date}
              startHour={startHour}
              startMinute={startMinute}
              endHour={endHour}
              endMinute={endMinute}
              recurrenceEnabled={recurrenceEnabled}
              recurrenceType={recurrenceType}
              intervalValue={intervalValue}
              daysOfWeek={daysOfWeek}
              recurrenceEndDate={recurrenceEndDate}
              loading={loading}
              errorMessage={errorMessage}
              onTitleChange={setTitle}
              onDateChange={setDate}
              onStartHourChange={setStartHour}
              onStartMinuteChange={setStartMinute}
              onEndHourChange={setEndHour}
              onEndMinuteChange={setEndMinute}
              onRecurrenceEnabledChange={setRecurrenceEnabled}
              onRecurrenceTypeChange={setRecurrenceType}
              onIntervalValueChange={setIntervalValue}
              onDaysOfWeekChange={setDaysOfWeek}
              onRecurrenceEndDateChange={setRecurrenceEndDate}
              onSubmit={handleDetailsSubmit}
              onClose={onClose}
            />
          )}

          {step === 'students' && (
            <StudentSelectionStep
              isEditMode={isEditMode}
              studentsLoading={studentsLoading}
              loading={loading}
              errorMessage={errorMessage}
              studentSearchQuery={studentSearchQuery}
              showNewStudentForm={showNewStudentForm}
              newStudentName={newStudentName}
              newStudentMemo={newStudentMemo}
              newStudentGrade={newStudentGrade}
              currentAttendees={currentAttendees}
              selectedStudentIds={selectedStudentIds}
              allStudents={allStudents}
              onSearchQueryChange={setStudentSearchQuery}
              onShowNewStudentForm={setShowNewStudentForm}
              onNewStudentNameChange={setNewStudentName}
              onNewStudentMemoChange={setNewStudentMemo}
              onNewStudentGradeChange={setNewStudentGrade}
              onEditModeAdd={handleEditModeAdd}
              onEditModeRemove={handleEditModeRemove}
              onToggleStudent={toggleStudent}
              onClearAllSelected={() => setSelectedStudentIds(new Set())}
              onCreateAndSelectStudent={handleCreateAndSelectStudent}
              onCreateAndFinish={handleCreateAndFinish}
              onEditFinish={handleEditFinish}
              onBackToDetails={() => setStep('details')}
            />
          )}
        </div>
      </div>
    </>
  );
}
