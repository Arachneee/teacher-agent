'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
import { Student, getStudents } from '../lib/api';
import StudentManagementCard from './StudentManagementCard';
import AddStudentModal from './AddStudentModal';

export default function StudentsView() {
  const [students, setStudents] = useState<Student[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  const fetchStudents = useCallback(() => {
    setLoading(true);
    getStudents()
      .then(setStudents)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    fetchStudents();
  }, [fetchStudents]);

  const handleAdd = () => {
    setShowAddModal(false);
    fetchStudents();
  };

  const handleDelete = (id: number) => {
    setStudents(prev => prev.filter(student => student.id !== id));
  };

  const filteredStudents = useMemo(() => {
    const trimmed = searchQuery.trim();
    if (!trimmed) return students;
    return students.filter(student =>
      student.name.toLowerCase().includes(trimmed.toLowerCase())
    );
  }, [students, searchQuery]);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center h-96 gap-3 bg-white rounded-2xl shadow-sm border border-gray-100">
        <div className="w-10 h-10 border-4 border-purple-200 border-t-purple-400 rounded-full animate-spin" />
        <p className="text-gray-400 text-sm">학생 목록을 불러오는 중...</p>
      </div>
    );
  }

  return (
    <>
      {/* Search bar */}
      <div className="mb-6 flex items-center gap-3">
        <div className="relative flex-1 max-w-sm">
          <svg
            className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-300 pointer-events-none"
            width="16" height="16" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"
          >
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input
            type="text"
            value={searchQuery}
            onChange={event => setSearchQuery(event.target.value)}
            placeholder="이름으로 검색"
            className="w-full bg-white rounded-2xl pl-9 pr-4 py-2.5 text-sm text-gray-700 shadow-sm border border-gray-100 outline-none focus:ring-2 focus:ring-purple-300 placeholder-gray-300"
          />
          {searchQuery && (
            <button
              onClick={() => setSearchQuery('')}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-300 hover:text-gray-400 transition-colors"
              aria-label="검색어 지우기"
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          )}
        </div>
        <p className="text-sm text-gray-400 shrink-0">
          {searchQuery ? (
            <>
              <span className="font-semibold text-purple-400">{filteredStudents.length}</span>명 검색됨
            </>
          ) : (
            <>총 <span className="font-semibold text-purple-400">{students.length}</span>명</>
          )}
        </p>
      </div>

      {/* Student grid */}
      {students.length === 0 ? (
        <div className="flex flex-col items-center justify-center h-80 gap-3 bg-white rounded-2xl shadow-sm border border-gray-100">
          <div className="text-6xl">👨‍🎓</div>
          <p className="text-base font-medium text-gray-400">아직 등록된 학생이 없어요</p>
          <p className="text-sm text-gray-300">아래 + 버튼으로 첫 학생을 추가해보세요!</p>
        </div>
      ) : filteredStudents.length === 0 ? (
        <div className="flex flex-col items-center justify-center h-80 gap-3 bg-white rounded-2xl shadow-sm border border-gray-100">
          <div className="text-6xl">🔍</div>
          <p className="text-base font-medium text-gray-400">검색 결과가 없어요</p>
          <p className="text-sm text-gray-300">다른 이름으로 검색해보세요</p>
        </div>
      ) : (
        <div className="grid gap-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {filteredStudents.map(student => (
            <StudentManagementCard
              key={student.id}
              student={student}
              onUpdate={fetchStudents}
              onDelete={handleDelete}
            />
          ))}
        </div>
      )}

      {/* FAB */}
      <button
        onClick={() => setShowAddModal(true)}
        className="fixed bottom-8 right-8 w-16 h-16 bg-pink-400 hover:bg-pink-500 active:scale-95 text-white text-3xl rounded-full shadow-lg hover:shadow-xl transition-all duration-200 flex items-center justify-center"
        aria-label="학생 추가"
      >
        +
      </button>

      {showAddModal && (
        <AddStudentModal
          onAdd={handleAdd}
          onClose={() => setShowAddModal(false)}
        />
      )}
    </>
  );
}
