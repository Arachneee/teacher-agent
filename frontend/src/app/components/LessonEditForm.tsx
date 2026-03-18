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
    <form onSubmit={onSubmit}>
      <input
        value={editForm.title}
        onChange={e => setEditForm(prev => ({ ...prev, title: e.target.value }))}
        className="text-4xl font-bold text-purple-500 bg-transparent border-b-2 border-purple-200 focus:border-purple-400 outline-none w-full leading-tight"
        required
        autoFocus
      />

      <div className="flex flex-wrap items-end gap-3 mt-4">
        <div>
          <p className="text-xs font-medium text-gray-400 mb-1">날짜</p>
          <input
            type="date"
            value={editForm.date}
            onChange={e => setEditForm(prev => ({ ...prev, date: e.target.value }))}
            className="bg-purple-50 rounded-xl px-3 py-2 text-gray-700 text-sm outline-none focus:ring-2 focus:ring-purple-200 cursor-pointer"
            required
          />
        </div>

        <div className="flex items-end gap-2">
          <TimePicker
            label="시작"
            hour={editForm.startHour}
            minute={editForm.startMinute}
            onHourChange={startHour => setEditForm(prev => ({ ...prev, startHour }))}
            onMinuteChange={startMinute => setEditForm(prev => ({ ...prev, startMinute }))}
          />
          <span className="text-gray-300 font-medium pb-2.5">–</span>
          <TimePicker
            label="종료"
            hour={editForm.endHour}
            minute={editForm.endMinute}
            onHourChange={endHour => setEditForm(prev => ({ ...prev, endHour }))}
            onMinuteChange={endMinute => setEditForm(prev => ({ ...prev, endMinute }))}
          />
        </div>

        <div className="flex items-center gap-2 pb-0.5">
          <button
            type="button"
            onClick={onClose}
            className="text-sm text-gray-400 hover:text-gray-600 transition-colors px-1"
          >
            취소
          </button>
          <button
            type="submit"
            disabled={isSaving || !editForm.title.trim() || !editForm.date}
            className="text-sm font-semibold text-purple-500 hover:text-purple-700 disabled:text-purple-300 transition-colors px-1"
          >
            {isSaving ? '저장 중...' : '저장'}
          </button>
        </div>
      </div>

      {saveError && (
        <p className="text-xs text-rose-400 mt-2">{saveError}</p>
      )}
    </form>
  );
}
