'use client';

import type { LessonDetailAttendee, Student } from '../../lib/api';
import NewStudentForm from './NewStudentForm';

interface StudentSelectionStepProps {
  isEditMode: boolean;
  studentsLoading: boolean;
  loading: boolean;
  errorMessage: string | null;
  studentSearchQuery: string;
  showNewStudentForm: boolean;
  newStudentName: string;
  newStudentMemo: string;
  currentAttendees: LessonDetailAttendee[];
  selectedStudentIds: Set<number>;
  allStudents: Student[];
  onSearchQueryChange: (value: string) => void;
  onShowNewStudentForm: (value: boolean) => void;
  onNewStudentNameChange: (value: string) => void;
  onNewStudentMemoChange: (value: string) => void;
  onEditModeAdd: (student: Student) => void;
  onEditModeRemove: (studentId: number) => void;
  onToggleStudent: (studentId: number) => void;
  onClearAllSelected: () => void;
  onCreateAndSelectStudent: (event: React.FormEvent) => void;
  onCreateAndFinish: (studentIds: number[]) => void;
  onEditFinish: () => void;
  onBackToDetails: () => void;
}

export default function StudentSelectionStep({
  isEditMode,
  studentsLoading,
  loading,
  errorMessage,
  studentSearchQuery,
  showNewStudentForm,
  newStudentName,
  newStudentMemo,
  currentAttendees,
  selectedStudentIds,
  allStudents,
  onSearchQueryChange,
  onShowNewStudentForm,
  onNewStudentNameChange,
  onNewStudentMemoChange,
  onEditModeAdd,
  onEditModeRemove,
  onToggleStudent,
  onClearAllSelected,
  onCreateAndSelectStudent,
  onCreateAndFinish,
  onEditFinish,
  onBackToDetails,
}: StudentSelectionStepProps) {
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
                        onClick={() => onEditModeRemove(attendee.student.id)}
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
              onChange={event => onSearchQueryChange(event.target.value)}
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
                      onClick={() => onEditModeAdd(student)}
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
                      onClick={() => onToggleStudent(student.id)}
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
                  onClick={onClearAllSelected}
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
                      onClick={() => onToggleStudent(student.id)}
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
                onClick={() => onCreateAndFinish([...selectedStudentIds])}
                disabled={loading}
                className="w-full bg-purple-500 hover:bg-purple-600 disabled:bg-purple-200 text-white font-semibold py-3 rounded-2xl transition-colors duration-150"
              >
                {loading ? '추가 중...' : `수강생 ${selectedStudentIds.size}명 추가하고 완료`}
              </button>
            )}
            <div className="flex gap-3">
              <button
                type="button"
                onClick={onBackToDetails}
                className="bg-gray-100 hover:bg-gray-200 text-gray-500 font-medium py-3 px-4 rounded-2xl transition-colors duration-150"
              >
                ←
              </button>
              <button
                onClick={isEditMode ? onEditFinish : () => onCreateAndFinish([])}
                disabled={loading}
                className="flex-1 bg-pink-400 hover:bg-pink-500 disabled:bg-pink-200 text-white font-medium py-3 rounded-2xl transition-colors duration-150"
              >
                {loading ? '저장 중...' : isEditMode ? '완료' : selectedStudentIds.size === 0 ? '나중에 추가하기' : '취소'}
              </button>
              <button
                onClick={() => onShowNewStudentForm(true)}
                disabled={loading}
                className="flex-1 bg-gray-100 hover:bg-gray-200 disabled:opacity-50 text-gray-500 font-medium py-3 rounded-2xl transition-colors duration-150"
              >
                새 학생 등록
              </button>
            </div>
          </div>
        </>
      ) : (
        <NewStudentForm
          newStudentName={newStudentName}
          newStudentMemo={newStudentMemo}
          loading={loading}
          onNameChange={onNewStudentNameChange}
          onMemoChange={onNewStudentMemoChange}
          onSubmit={onCreateAndSelectStudent}
          onCancel={() => onShowNewStudentForm(false)}
        />
      )}
    </>
  );
}
