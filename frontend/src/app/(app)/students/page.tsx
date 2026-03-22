'use client';

import { useAuth } from '../../context/AuthContext';
import StudentsView from '../../components/StudentsView';

export default function StudentsPage() {
  const { user, logout } = useAuth();

  return (
    <div className="flex flex-col flex-1 px-4 md:px-6 py-5 md:py-8">
      <header className="mb-4 md:mb-6 flex items-center justify-between">
        <h1 className="text-2xl md:text-3xl font-bold text-purple-500">학생 관리</h1>
        <div className="flex items-center gap-2 md:gap-3">
          <span className="hidden md:inline text-sm text-gray-400">{user?.userId}</span>
          <button
            onClick={logout}
            className="text-xs md:text-sm text-gray-400 hover:text-purple-500 transition-colors"
          >
            로그아웃
          </button>
        </div>
      </header>
      <StudentsView />
    </div>
  );
}
