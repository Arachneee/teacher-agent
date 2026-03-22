'use client';

import { useAuth } from '../../context/AuthContext';
import StudentsView from '../../components/StudentsView';

export default function StudentsPage() {
  const { user, logout } = useAuth();

  return (
    <div className="flex flex-col flex-1 px-6 py-8">
      <header className="mb-6 flex items-center justify-between">
        <h1 className="text-3xl font-bold text-purple-500">학생 관리</h1>
        <div className="flex items-center gap-3">
          <span className="text-sm text-gray-400">{user?.userId}</span>
          <button
            onClick={logout}
            className="text-sm text-gray-400 hover:text-purple-500 transition-colors"
          >
            로그아웃
          </button>
        </div>
      </header>
      <StudentsView />
    </div>
  );
}
