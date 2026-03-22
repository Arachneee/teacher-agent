'use client';

import type { SchoolGrade } from '../../lib/api';
import GradeSelect from '../GradeSelect';

interface NewStudentFormProps {
  newStudentName: string;
  newStudentMemo: string;
  newStudentGrade: SchoolGrade;
  loading: boolean;
  onNameChange: (value: string) => void;
  onMemoChange: (value: string) => void;
  onGradeChange: (value: SchoolGrade) => void;
  onSubmit: (event: React.FormEvent) => void;
  onCancel: () => void;
}

export default function NewStudentForm({
  newStudentName,
  newStudentMemo,
  newStudentGrade,
  loading,
  onNameChange,
  onMemoChange,
  onGradeChange,
  onSubmit,
  onCancel,
}: NewStudentFormProps) {
  return (
    <form onSubmit={onSubmit} className="flex flex-col gap-4 flex-1 px-6 pb-6 overflow-y-auto">
      <div className="bg-amber-50 border border-amber-100 rounded-2xl px-4 py-3 shrink-0">
        <p className="text-xs text-amber-700 leading-relaxed">
          💡 <strong>학생 등록</strong>은 시스템 전체에 학생 정보를 추가해요.
          등록 후 이 수업 <strong>수강생으로 자동 추가</strong>돼요.
        </p>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-600 mb-1 ml-1">
          이름 <span className="text-rose-400">*</span>
        </label>
        <input
          value={newStudentName}
          onChange={event => onNameChange(event.target.value)}
          className="w-full bg-purple-50 rounded-2xl px-4 py-3 text-gray-800 outline-none focus:ring-2 focus:ring-purple-300 placeholder-gray-300"
          placeholder="학생 이름을 입력하세요"
          autoFocus
          required
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-600 mb-2 ml-1">
          학년 <span className="text-rose-400">*</span>
        </label>
        <GradeSelect value={newStudentGrade} onChange={onGradeChange} />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-600 mb-1 ml-1">메모</label>
        <textarea
          value={newStudentMemo}
          onChange={event => onMemoChange(event.target.value)}
          className="w-full bg-purple-50 rounded-2xl px-4 py-3 text-gray-800 outline-none focus:ring-2 focus:ring-purple-300 placeholder-gray-300 resize-none"
          placeholder="학생에 대한 메모 (선택)"
          rows={3}
          maxLength={500}
        />
        <p className="text-xs text-gray-300 text-right mt-1">{newStudentMemo.length}/500</p>
      </div>

      <div className="flex gap-3 mt-auto">
        <button
          type="button"
          onClick={onCancel}
          className="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-500 font-medium py-3 rounded-2xl transition-colors duration-150"
        >
          뒤로
        </button>
        <button
          type="submit"
          disabled={loading || !newStudentName.trim()}
          className="flex-1 bg-pink-400 hover:bg-pink-500 disabled:bg-pink-200 text-white font-medium py-3 rounded-2xl transition-colors duration-150"
        >
          {loading ? '등록 중...' : '등록하고 추가하기 ✨'}
        </button>
      </div>
    </form>
  );
}
