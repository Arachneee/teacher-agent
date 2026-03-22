'use client';

import type { Feedback } from '../types/api';

interface Props {
  feedback: Feedback;
}

function formatLessonDate(isoString: string): string {
  return new Date(isoString).toLocaleDateString('ko-KR', {
    month: 'long',
    day: 'numeric',
    weekday: 'short',
  });
}

export default function FeedbackHistoryCard({ feedback }: Props) {
  const hasAiContent = feedback.aiContent != null && feedback.aiContent.trim() !== '';

  return (
    <div className="bg-white rounded-3xl p-5 shadow-sm flex flex-col gap-3">
      {/* 수업 정보 헤더 */}
      <div className="flex items-center justify-between gap-2">
        <div className="flex items-center gap-2 min-w-0">
          <span className="text-sm font-semibold text-gray-700 truncate">
            {feedback.lessonTitle}
          </span>
          {feedback.liked && (
            <span className="text-pink-400 text-base shrink-0" aria-label="보관됨">♥</span>
          )}
        </div>
        {feedback.lessonStartTime && (
          <span className="text-xs text-gray-400 shrink-0">
            {formatLessonDate(feedback.lessonStartTime)}
          </span>
        )}
      </div>

      {/* 키워드 */}
      {feedback.keywords.length > 0 && (
        <div className="flex flex-wrap gap-1.5">
          {feedback.keywords.map(keyword => (
            <span
              key={keyword.id}
              className="text-xs bg-purple-50 text-purple-500 rounded-full px-2.5 py-1"
            >
              {keyword.keyword}
            </span>
          ))}
        </div>
      )}

      {/* AI 문자 */}
      <div className={`rounded-2xl px-4 py-3 text-sm leading-relaxed ${
        hasAiContent ? 'bg-pink-50 text-gray-700' : 'bg-gray-50 text-gray-300 italic'
      }`}>
        {hasAiContent ? feedback.aiContent : 'AI 문자 없음'}
      </div>
    </div>
  );
}
