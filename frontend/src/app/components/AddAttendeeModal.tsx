'use client';

import { useEffect, useState } from 'react';
import { Student, addAttendee, createStudent, getStudents } from '../lib/api';

interface Props {
  lessonId: number;
  existingStudentIds: Set<number>;
  onAdd: () => void;
  onClose: () => void;
}

export default function AddAttendeeModal({ lessonId, existingStudentIds, onAdd, onClose }: Props) {
  const [students, setStudents] = useState<Student[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [showNewStudentForm, setShowNewStudentForm] = useState(false);
  const [newName, setNewName] = useState('');
  const [newMemo, setNewMemo] = useState('');

  useEffect(() => {
    getStudents()
      .then(setStudents)
      .catch(() => setErrorMessage('학생 목록을 불러오지 못했어요'))
      .finally(() => setLoading(false));
  }, []);

  const availableStudents = students.filter(
    student =>
      !existingStudentIds.has(student.id) &&
      student.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleSelectStudent = async (student: Student) => {
    setSubmitting(true);
    setErrorMessage(null);
    try {
      await addAttendee(lessonId, student.id);
      onAdd();
    } catch {
      setErrorMessage('수강생을 추가하지 못했어요');
      setSubmitting(false);
    }
  };

  const handleCreateAndAdd = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!newName.trim()) return;
    setSubmitting(true);
    setErrorMessage(null);
    try {
      const created = await createStudent(newName.trim(), newMemo.trim());
      await addAttendee(lessonId, created.id);
      onAdd();
    } catch {
      setErrorMessage('학생을 추가하지 못했어요');
      setSubmitting(false);
    }
  };

  return (
    <div
      className="fixed inset-0 bg-black/30 backdrop-blur-sm flex items-center justify-center z-50 p-4"
      onClick={event => event.target === event.currentTarget && onClose()}
    >
      <div className="bg-white rounded-3xl p-8 w-full max-w-md shadow-2xl max-h-[80vh] flex flex-col">
        <div className="text-center mb-6 shrink-0">
          <div className="text-4xl mb-2">👨‍🎓</div>
          <h2 className="text-2xl font-bold text-gray-800">수강생 추가</h2>
          <p className="text-sm text-gray-400 mt-1">이 수업에 수강생을 추가해요</p>
        </div>

        {errorMessage && (
          <p className="text-xs text-rose-400 bg-rose-50 rounded-xl px-3 py-2 mb-4 shrink-0">
            {errorMessage}
          </p>
        )}

        {!showNewStudentForm ? (
          <>
            {/* 검색 */}
            <div className="shrink-0 mb-3">
              <input
                value={searchQuery}
                onChange={event => setSearchQuery(event.target.value)}
                className="w-full bg-purple-50 rounded-2xl px-4 py-3 text-gray-800 outline-none focus:ring-2 focus:ring-purple-300 placeholder-gray-300"
                placeholder="학생 이름으로 검색"
                autoFocus
              />
            </div>

            {/* 학생 목록 */}
            <div className="flex-1 overflow-y-auto min-h-0">
              {loading ? (
                <div className="flex justify-center py-8">
                  <div className="w-8 h-8 border-4 border-purple-200 border-t-purple-400 rounded-full animate-spin" />
                </div>
              ) : availableStudents.length === 0 ? (
                <div className="text-center text-gray-300 py-8">
                  <p className="text-sm">
                    {searchQuery ? '검색 결과가 없어요' : '추가할 수 있는 학생이 없어요'}
                  </p>
                </div>
              ) : (
                <div className="flex flex-col gap-2">
                  {availableStudents.map(student => (
                    <button
                      key={student.id}
                      onClick={() => handleSelectStudent(student)}
                      disabled={submitting}
                      className="flex items-center gap-3 w-full text-left px-4 py-3 rounded-2xl hover:bg-purple-50 transition-colors duration-150 disabled:opacity-50"
                    >
                      <div className="w-9 h-9 rounded-xl bg-purple-100 text-purple-500 flex items-center justify-center font-semibold shrink-0">
                        {student.name.charAt(0)}
                      </div>
                      <div className="min-w-0">
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

            {/* 새 학생 추가 버튼 */}
            <div className="shrink-0 mt-4 flex gap-3">
              <button
                onClick={onClose}
                className="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-500 font-medium py-3 rounded-2xl transition-colors duration-150"
              >
                취소
              </button>
              <button
                onClick={() => setShowNewStudentForm(true)}
                className="flex-1 bg-pink-400 hover:bg-pink-500 text-white font-medium py-3 rounded-2xl transition-colors duration-150"
              >
                새 학생 추가 ✨
              </button>
            </div>
          </>
        ) : (
          /* 새 학생 인라인 폼 */
          <form onSubmit={handleCreateAndAdd} className="flex flex-col gap-4 flex-1">
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1 ml-1">
                이름 <span className="text-rose-400">*</span>
              </label>
              <input
                value={newName}
                onChange={event => setNewName(event.target.value)}
                className="w-full bg-purple-50 rounded-2xl px-4 py-3 text-gray-800 outline-none focus:ring-2 focus:ring-purple-300 placeholder-gray-300"
                placeholder="학생 이름을 입력하세요"
                autoFocus
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1 ml-1">메모</label>
              <textarea
                value={newMemo}
                onChange={event => setNewMemo(event.target.value)}
                className="w-full bg-purple-50 rounded-2xl px-4 py-3 text-gray-800 outline-none focus:ring-2 focus:ring-purple-300 placeholder-gray-300 resize-none"
                placeholder="학생에 대한 메모 (선택)"
                rows={3}
                maxLength={500}
              />
              <p className="text-xs text-gray-300 text-right mt-1">{newMemo.length}/500</p>
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
                disabled={submitting || !newName.trim()}
                className="flex-1 bg-pink-400 hover:bg-pink-500 disabled:bg-pink-200 text-white font-medium py-3 rounded-2xl transition-colors duration-150"
              >
                {submitting ? '추가 중...' : '추가하기 ✨'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
