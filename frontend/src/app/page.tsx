'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Lesson, getLessons } from './lib/api';
import { useAuth } from './context/AuthContext';
import LessonCard from './components/LessonCard';
import AddLessonModal from './components/AddLessonModal';

export default function Home() {
  const { user, loading: authLoading, logout } = useAuth();
  const router = useRouter();
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingLesson, setEditingLesson] = useState<Lesson | undefined>(undefined);

  useEffect(() => {
    if (!authLoading && !user) {
      router.replace('/login');
    }
  }, [authLoading, user, router]);

  const fetchLessons = () => {
    getLessons()
      .then(setLessons)
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchLessons();
  }, []);

  const handleSave = async () => {
    setShowModal(false);
    setEditingLesson(undefined);
    setLoading(true);
    fetchLessons();
  };

  const handleDelete = (id: number) => {
    setLessons(prev => prev.filter(lesson => lesson.id !== id));
  };

  const handleEditClick = (lesson: Lesson) => {
    setEditingLesson(lesson);
    setShowModal(true);
  };

  const handleModalClose = () => {
    setShowModal(false);
    setEditingLesson(undefined);
  };

  if (authLoading || !user) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-orange-50 flex items-center justify-center">
        <div className="w-10 h-10 border-4 border-purple-200 border-t-purple-400 rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-orange-50">
      <div className="mx-auto px-6 py-10 max-w-5xl">
        {/* Header */}
        <header className="mb-10 flex items-start justify-between">
          <div>
            <h1 className="text-4xl font-bold text-purple-500">🍎 내 수업</h1>
            <p className="text-gray-400 mt-2">수업을 선택해서 수강생을 관리해요</p>
          </div>
          <div className="flex items-center gap-3">
            <span className="text-sm text-gray-400">{user.userId}</span>
            <button
              onClick={logout}
              className="text-sm text-gray-400 hover:text-purple-500 transition-colors"
            >
              로그아웃
            </button>
          </div>
        </header>

        {/* Content */}
        {loading ? (
          <div className="flex flex-col items-center justify-center h-64 gap-3">
            <div className="w-10 h-10 border-4 border-purple-200 border-t-purple-400 rounded-full animate-spin" />
            <p className="text-gray-400 text-sm">불러오는 중...</p>
          </div>
        ) : lessons.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-64 gap-3 text-gray-300">
            <div className="text-7xl">📚</div>
            <p className="text-lg font-medium">아직 수업이 없어요</p>
            <p className="text-sm">오른쪽 아래 + 버튼으로 수업을 추가해보세요!</p>
          </div>
        ) : (
          <>
            <p className="text-sm text-gray-400 mb-6">
              총 <span className="font-semibold text-purple-400">{lessons.length}</span>개의 수업
            </p>
            <div className="grid gap-6" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))' }}>
              {lessons.map(lesson => (
                <LessonCard
                  key={lesson.id}
                  lesson={lesson}
                  onEdit={handleEditClick}
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
        aria-label="수업 추가"
      >
        +
      </button>

      {/* Modal */}
      {showModal && (
        <AddLessonModal
          lesson={editingLesson}
          onSave={handleSave}
          onClose={handleModalClose}
        />
      )}
    </div>
  );
}
