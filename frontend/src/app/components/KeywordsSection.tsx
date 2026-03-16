'use client';

import type { RefObject } from 'react';
import { FeedbackKeyword } from '../lib/api';

interface Props {
  keywords: FeedbackKeyword[];
  keywordInput: string;
  onKeywordInputChange: (value: string) => void;
  onAddKeyword: () => void;
  onRemoveKeyword: (keywordId: number) => void;
  onNavigate?: (direction: 'prev' | 'next' | 'up' | 'down') => void;
  inputRef?: RefObject<HTMLInputElement | null>;
}

export default function KeywordsSection({
  keywords,
  keywordInput,
  onKeywordInputChange,
  onAddKeyword,
  onRemoveKeyword,
  onNavigate,
  inputRef,
}: Props) {
  return (
    <div className="flex-1 flex flex-col gap-2">
      <p className="text-xs font-semibold text-gray-400 tracking-wide">수업 키워드</p>
      {keywords.length > 0 && (
        <div className="flex flex-wrap gap-1.5">
          {keywords.map(keyword => (
            <span
              key={keyword.id}
              className="inline-flex items-center gap-1 bg-pink-50 text-pink-500 text-xs font-medium px-2.5 py-1 rounded-full"
            >
              {keyword.keyword}
              <button
                onClick={() => onRemoveKeyword(keyword.id)}
                className="w-4 h-4 flex items-center justify-center rounded-full hover:bg-pink-200 transition-colors text-sm leading-none"
                aria-label={`${keyword.keyword} 삭제`}
              >
                ×
              </button>
            </span>
          ))}
        </div>
      )}
      <div className="mt-auto flex items-center bg-pink-50 rounded-2xl focus-within:ring-2 focus-within:ring-pink-200 transition-colors">
        <input
          ref={inputRef}
          value={keywordInput}
          onChange={event => onKeywordInputChange(event.target.value)}
          onKeyDown={event => {
            if (event.key === 'Enter' && !event.nativeEvent.isComposing) {
              onAddKeyword();
            } else if (keywordInput === '' && event.key === 'ArrowLeft') {
              event.preventDefault();
              onNavigate?.('prev');
            } else if (keywordInput === '' && event.key === 'ArrowRight') {
              event.preventDefault();
              onNavigate?.('next');
            } else if (keywordInput === '' && event.key === 'ArrowUp') {
              event.preventDefault();
              onNavigate?.('up');
            } else if (keywordInput === '' && event.key === 'ArrowDown') {
              event.preventDefault();
              onNavigate?.('down');
            }
          }}
          className="flex-1 min-w-0 text-sm text-gray-700 bg-transparent px-3 py-2 outline-none placeholder:text-gray-300"
          placeholder="키워드 입력"
          maxLength={100}
        />
        <button
          onClick={onAddKeyword}
          disabled={!keywordInput.trim()}
          className="shrink-0 w-8 h-8 mr-1 flex items-center justify-center rounded-xl bg-pink-100 hover:bg-pink-200 disabled:opacity-30 text-pink-400 transition-colors duration-150"
          aria-label="키워드 추가"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="9 10 4 15 9 20" />
            <path d="M20 4v7a4 4 0 0 1-4 4H4" />
          </svg>
        </button>
      </div>
    </div>
  );
}
