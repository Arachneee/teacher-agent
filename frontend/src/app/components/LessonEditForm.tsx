'use client';

import TimePicker from './TimePicker';

interface EditForm {
  title: string;
  date: string;
  startHour: number;
  startMinute: number;
  endHour: number;
  endMinute: number;
}

interface Props {
  editForm: EditForm;
  setEditForm: React.Dispatch<React.SetStateAction<EditForm>>;
  isSaving: boolean;
  saveError: string | null;
  onSubmit: (event: React.FormEvent) => void;
  onClose: () => void;
}

export default function LessonEditForm({ editForm, setEditForm, isSaving, saveError, onSubmit, onClose }: Props) {
  return (
    <form
      onSubmit={onSubmit}
      className="mt-4 bg-white rounded-3xl shadow-lg border border-purple-100 p-5 max-w-sm"
    >
      <div className="flex items-center justify-between mb-4">
        <p className="text-sm font-semibold text-gray-700">수업 시간 수정</p>
        <button
          type="button"
          onClick={onClose}
          className="w-7 h-7 flex items-center justify-center rounded-full text-gray-300 hover:text-gray-500 hover:bg-gray-100 transition-colors text-lg"
          aria-label="닫기"
        >
          ✕
        </button>
      </div>

      <div className="flex flex-col gap-3">
        <div>
          <p className="text-xs font-medium text-gray-400 mb-1 ml-0.5">제목</p>
          <input
            value={editForm.title}
            onChange={e => setEditForm(prev => ({ ...prev, title: e.target.value }))}
            className="w-full bg-purple-50 rounded-xl px-3 py-2 text-gray-800 text-sm outline-none focus:ring-2 focus:ring-purple-300"
            required
          />
        </div>

        <div>
          <p className="text-xs font-medium text-gray-400 mb-1 ml-0.5">날짜</p>
          <input
            type="date"
            value={editForm.date}
            onChange={e => setEditForm(prev => ({ ...prev, date: e.target.value }))}
            className="w-full bg-purple-50 rounded-xl px-3 py-2 text-gray-700 text-sm outline-none focus:ring-2 focus:ring-purple-300 cursor-pointer"
            required
          />
        </div>

        <div className="bg-purple-50 rounded-2xl px-4 py-3 flex items-end gap-3">
          <TimePicker
            label="시작"
            hour={editForm.startHour}
            minute={editForm.startMinute}
            onHourChange={startHour => setEditForm(prev => ({ ...prev, startHour }))}
            onMinuteChange={startMinute => setEditForm(prev => ({ ...prev, startMinute }))}
          />
          <span className="text-gray-300 font-medium pb-2">–</span>
          <TimePicker
            label="종료"
            hour={editForm.endHour}
            minute={editForm.endMinute}
            onHourChange={endHour => setEditForm(prev => ({ ...prev, endHour }))}
            onMinuteChange={endMinute => setEditForm(prev => ({ ...prev, endMinute }))}
          />
        </div>

        {saveError && (
          <p className="text-xs text-rose-400 bg-rose-50 rounded-xl px-3 py-2">{saveError}</p>
        )}

        <div className="flex gap-2 mt-1">
          <button
            type="button"
            onClick={onClose}
            className="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-500 text-sm font-medium py-2.5 rounded-xl transition-colors"
          >
            취소
          </button>
          <button
            type="submit"
            disabled={isSaving || !editForm.title.trim() || !editForm.date}
            className="flex-1 bg-purple-500 hover:bg-purple-600 disabled:bg-purple-200 text-white text-sm font-medium py-2.5 rounded-xl transition-colors"
          >
            {isSaving ? '저장 중...' : '저장'}
          </button>
        </div>
      </div>
    </form>
  );
}
