'use client';

import { useState } from 'react';
import { Student, createStudent } from '../lib/api';

interface Props {
  onAdd: (student: Student) => void;
  onClose: () => void;
}

export default function AddStudentModal({ onAdd, onClose }: Props) {
  const [name, setName] = useState('');
  const [memo, setMemo] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;
    setLoading(true);
    try {
      const student = await createStudent(name.trim(), memo.trim());
      onAdd(student);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="fixed inset-0 bg-black/30 backdrop-blur-sm flex items-center justify-center z-50 p-4"
      onClick={e => e.target === e.currentTarget && onClose()}
    >
      <div className="bg-white rounded-3xl p-8 w-full max-w-md shadow-2xl">
        <div className="text-center mb-6">
          <div className="text-4xl mb-2">🌟</div>
          <h2 className="text-2xl font-bold text-gray-800">새 학생 추가</h2>
          <p className="text-sm text-gray-400 mt-1">새로운 학생을 등록해요</p>
        </div>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-600 mb-1 ml-1">
              이름 <span className="text-rose-400">*</span>
            </label>
            <input
              value={name}
              onChange={e => setName(e.target.value)}
              className="w-full bg-purple-50 rounded-2xl px-4 py-3 text-gray-800 outline-none focus:ring-2 focus:ring-purple-300 placeholder-gray-300"
              placeholder="학생 이름을 입력하세요"
              autoFocus
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-600 mb-1 ml-1">
              메모
            </label>
            <textarea
              value={memo}
              onChange={e => setMemo(e.target.value)}
              className="w-full bg-purple-50 rounded-2xl px-4 py-3 text-gray-800 outline-none focus:ring-2 focus:ring-purple-300 placeholder-gray-300 resize-none"
              placeholder="학생에 대한 메모를 남겨요 (선택)"
              rows={4}
              maxLength={500}
            />
            <p className="text-xs text-gray-300 text-right mt-1">{memo.length}/500</p>
          </div>

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
              disabled={loading || !name.trim()}
              className="flex-1 bg-pink-400 hover:bg-pink-500 disabled:bg-pink-200 text-white font-medium py-3 rounded-2xl transition-colors duration-150"
            >
              {loading ? '추가 중...' : '추가하기 ✨'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
