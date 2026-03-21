'use client';

import type { UpdateScope } from '../lib/api';

interface Props {
  mode: 'edit' | 'delete' | 'add_attendee' | 'remove_attendee';
  lessonTitle: string;
  onSelect: (scope: UpdateScope) => void;
  onClose: () => void;
}

const getModeConfig = (mode: Props['mode']) => {
  switch (mode) {
    case 'delete':
      return {
        emoji: '🗑️',
        title: '반복 수업 삭제',
        descriptions: {
          SINGLE: '선택한 수업 하나만 삭제해요',
          THIS_AND_FOLLOWING: '이 수업과 이후 반복 수업을 모두 삭제해요',
          ALL: '이 시리즈의 모든 수업을 삭제해요',
        },
      };
    case 'add_attendee':
      return {
        emoji: '👨‍🎓',
        title: '반복 수업 수강생 추가',
        descriptions: {
          SINGLE: '선택한 수업에만 수강생을 추가해요',
          THIS_AND_FOLLOWING: '이 수업과 이후 반복 수업에 수강생을 추가해요',
          ALL: '이 시리즈의 모든 수업에 수강생을 추가해요',
        },
      };
    case 'remove_attendee':
      return {
        emoji: '👨‍🎓',
        title: '반복 수업 수강생 삭제',
        descriptions: {
          SINGLE: '선택한 수업에서만 수강생을 삭제해요',
          THIS_AND_FOLLOWING: '이 수업과 이후 반복 수업에서 수강생을 삭제해요',
          ALL: '이 시리즈의 모든 수업에서 수강생을 삭제해요',
        },
      };
    case 'edit':
    default:
      return {
        emoji: '✏️',
        title: '반복 수업 수정',
        descriptions: {
          SINGLE: '선택한 수업 하나만 변경해요',
          THIS_AND_FOLLOWING: '이 수업과 이후 반복 수업을 모두 변경해요',
          ALL: '이 시리즈의 모든 수업을 변경해요',
        },
      };
  }
};

export default function RecurringScopeModal({ mode, lessonTitle, onSelect, onClose }: Props) {
  const isDelete = mode === 'delete';
  const config = getModeConfig(mode);
  
  const scopeOptions: { value: UpdateScope; label: string }[] = [
    { value: 'SINGLE', label: '이 수업만' },
    { value: 'THIS_AND_FOLLOWING', label: '이 수업 및 이후 수업' },
    { value: 'ALL', label: '모든 반복 수업' },
  ];

  return (
    <div
      className="fixed inset-0 bg-black/30 backdrop-blur-sm flex items-center justify-center z-50 p-4"
      onClick={event => event.target === event.currentTarget && onClose()}
    >
      <div className="bg-white rounded-3xl w-full max-w-sm shadow-2xl overflow-hidden">
        <div className="text-center pt-6 px-6 pb-4">
          <div className="text-4xl mb-2">{config.emoji}</div>
          <h2 className="text-xl font-bold text-gray-800">
            {config.title}
          </h2>
          <p className="text-sm text-gray-400 mt-1 truncate">
            &ldquo;{lessonTitle}&rdquo;
          </p>
        </div>

        <div className="flex flex-col gap-2 px-6 pb-4">
          {scopeOptions.map(option => (
            <button
              key={option.value}
              onClick={() => onSelect(option.value)}
              className={`w-full text-left px-4 py-3 rounded-2xl transition-colors duration-150 ${
                isDelete && option.value !== 'SINGLE'
                  ? 'hover:bg-rose-50 active:bg-rose-100'
                  : 'hover:bg-purple-50 active:bg-purple-100'
              }`}
            >
              <p className={`text-sm font-semibold ${
                isDelete && option.value !== 'SINGLE' ? 'text-rose-500' : 'text-gray-800'
              }`}>
                {option.label}
              </p>
              <p className="text-xs text-gray-400 mt-0.5">{config.descriptions[option.value]}</p>
            </button>
          ))}
        </div>

        <div className="px-6 pb-6">
          <button
            onClick={onClose}
            className="w-full bg-gray-100 hover:bg-gray-200 text-gray-500 font-medium py-3 rounded-2xl transition-colors duration-150"
          >
            취소
          </button>
        </div>
      </div>
    </div>
  );
}
