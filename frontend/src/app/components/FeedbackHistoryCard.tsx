'use client';

import type { Feedback } from '../types/api';
import { highlightKeywords } from '../lib/highlightKeywords';

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
            <span className="text-indigo-500 shrink-0" aria-label="좋아요">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14z" />
                <path d="M7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3" />
              </svg>
            </span>
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
              className={`inline-flex items-center text-xs rounded-full px-2 py-1 ${
                keyword.required
                  ? 'bg-purple-100 text-purple-600 ring-1 ring-purple-300'
                  : 'bg-purple-50 text-purple-500'
              }`}
            >
              {keyword.required ? `「${keyword.keyword}」` : keyword.keyword}
            </span>
          ))}
        </div>
      )}

      {/* AI 문자 */}
      <div className={`rounded-2xl px-4 py-3 text-sm leading-relaxed ${
        hasAiContent ? 'bg-pink-50 text-gray-700' : 'bg-gray-50 text-gray-300 italic'
      }`}>
        {hasAiContent
          ? highlightKeywords(feedback.aiContent!, feedback.keywords.map(keyword => keyword.keyword))
          : 'AI 문자 없음'}
      </div>
    </div>
  );
}
