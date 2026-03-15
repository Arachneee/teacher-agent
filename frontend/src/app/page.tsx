'use client';

import { useEffect, useState } from 'react';
import { Student, getStudents } from './lib/api';
import StudentCard from './components/StudentCard';
import AddStudentModal from './components/AddStudentModal';

export default function Home() {
  const [students, setStudents] = useState<Student[]>([]);
  const [showModal, setShowModal] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getStudents()
      .then(setStudents)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const handleAdd = (student: Student) => {
    setStudents(prev => [...prev, student]);
    setShowModal(false);
  };

  const handleUpdate = (updated: Student) => {
    setStudents(prev => prev.map(s => (s.id === updated.id ? updated : s)));
  };

  const handleDelete = (id: number) => {
    setStudents(prev => prev.filter(s => s.id !== id));
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-orange-50">
      <div className="max-w-6xl mx-auto px-6 py-10">
        {/* Header */}
        <header className="mb-10">
          <h1 className="text-4xl font-bold text-purple-500">🍎 학생 관리</h1>
          <p className="text-gray-400 mt-2">나의 소중한 학생들을 관리해요</p>
        </header>

        {/* Content */}
        {loading ? (
          <div className="flex flex-col items-center justify-center h-64 gap-3">
            <div className="w-10 h-10 border-4 border-purple-200 border-t-purple-400 rounded-full animate-spin" />
            <p className="text-gray-400 text-sm">불러오는 중...</p>
          </div>
        ) : students.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-64 gap-3 text-gray-300">
            <div className="text-7xl">👨‍🎓</div>
            <p className="text-lg font-medium">아직 학생이 없어요</p>
            <p className="text-sm">오른쪽 아래 + 버튼으로 추가해보세요!</p>
          </div>
        ) : (
          <>
            <p className="text-sm text-gray-400 mb-6">
              총{' '}
              <span className="font-semibold text-purple-400">{students.length}</span>명의 학생
            </p>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
              {students.map(student => (
                <StudentCard
                  key={student.id}
                  student={student}
                  onUpdate={handleUpdate}
                  onDelete={handleDelete}
                />
              ))}
            </div>
          </>
        )}
      </div>

      {/* FAB */}
      <button
        onClick={() => setShowModal(true)}
        className="fixed bottom-8 right-8 w-16 h-16 bg-pink-400 hover:bg-pink-500 active:scale-95 text-white text-3xl rounded-full shadow-lg hover:shadow-xl transition-all duration-200 flex items-center justify-center"
        aria-label="학생 추가"
      >
        +
      </button>

      {/* Modal */}
      {showModal && (
        <AddStudentModal onAdd={handleAdd} onClose={() => setShowModal(false)} />
      )}
    </div>
  );
}
