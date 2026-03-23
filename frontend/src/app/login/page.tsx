'use client';

import { useState, FormEvent, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { login } from '../lib/api';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const [userId, setUserId] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const router = useRouter();
  const { user, loading, setUser } = useAuth();

  useEffect(() => {
    if (!loading && user) {
      router.replace('/calendar');
    }
  }, [loading, user, router]);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    setSubmitting(true);

    try {
      const authResponse = await login(userId, password);
      setUser(authResponse);
      router.push('/calendar');
    } catch {
      setError('아이디 또는 비밀번호가 올바르지 않아요');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-orange-50 flex items-center justify-center px-4">
      <div className="w-full max-w-sm">
        <div className="text-center mb-8">
          <div className="text-6xl mb-4">🍎</div>
          <h1 className="text-3xl font-bold text-purple-500">학생 관리</h1>
          <p className="text-gray-400 mt-2">로그인하고 시작해요</p>
        </div>

        <form onSubmit={handleSubmit} className="bg-white rounded-3xl shadow-md p-8 flex flex-col gap-5">
          <div className="flex flex-col gap-2">
            <label htmlFor="userId" className="text-sm font-medium text-gray-600">
              아이디
            </label>
            <input
              id="userId"
              type="text"
              value={userId}
              onChange={event => setUserId(event.target.value)}
              placeholder="아이디를 입력하세요"
              required
              autoFocus
              className="bg-purple-50 rounded-2xl px-4 py-3 text-sm outline-none focus:ring-2 focus:ring-purple-300 transition-shadow"
            />
          </div>

          <div className="flex flex-col gap-2">
            <label htmlFor="password" className="text-sm font-medium text-gray-600">
              비밀번호
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={event => setPassword(event.target.value)}
              placeholder="비밀번호를 입력하세요"
              required
              className="bg-pink-50 rounded-2xl px-4 py-3 text-sm outline-none focus:ring-2 focus:ring-pink-300 transition-shadow"
            />
          </div>

          {error && (
            <p className="text-sm text-red-400 text-center">{error}</p>
          )}

          <button
            type="submit"
            disabled={submitting}
            className="bg-purple-400 hover:bg-purple-500 disabled:opacity-50 text-white font-semibold rounded-2xl px-4 py-3 text-sm transition-colors"
          >
            {submitting ? '로그인 중...' : '로그인'}
          </button>
        </form>
      </div>
    </div>
  );
}
