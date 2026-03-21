'use client';

import { useEffect, useRef, type RefObject } from 'react';
import { FeedbackKeyword } from '../lib/api';

interface Props {
  keywords: FeedbackKeyword[];
  keywordInput: string;
  editingKeywordId: number | null;
  onKeywordInputChange: (value: string) => void;
  onSubmitKeyword: () => void;
  onStartEditKeyword: (keyword: FeedbackKeyword) => void;
  onCancelEditKeyword: () => void;
  onRemoveKeyword: (keywordId: number) => void;
  onNavigate?: (direction: 'prev' | 'next' | 'up' | 'down') => void;
  inputRef?: RefObject<HTMLInputElement | null>;
}

export default function KeywordsSection({
  keywords,
  keywordInput,
  editingKeywordId,
  onKeywordInputChange,
  onSubmitKeyword,
  onStartEditKeyword,
  onCancelEditKeyword,
  onRemoveKeyword,
  onNavigate,
  inputRef,
}: Props) {
  const isEditing = editingKeywordId !== null;
  const blurTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    return () => {
      if (blurTimeoutRef.current) clearTimeout(blurTimeoutRef.current);
    };
  }, []);

  return (
    <div className="flex-1 flex flex-col gap-2">
      <p className="text-xs font-semibold text-gray-400 tracking-wide">수업 키워드</p>
      {keywords.length > 0 && (
        <div className="flex flex-wrap gap-1.5">
          {keywords.map(keyword => {
            const isBeingEdited = keyword.id === editingKeywordId;
            return (
              <span
                key={keyword.id}
                onClick={() => onStartEditKeyword(keyword)}
                className={`inline-flex items-center gap-1 text-xs font-medium px-2.5 py-1 rounded-full cursor-pointer transition-colors ${
                  isBeingEdited
                    ? 'bg-amber-100 text-amber-600 ring-2 ring-amber-300'
                    : 'bg-pink-50 text-pink-500 hover:bg-pink-100'
                }`}
              >
                {keyword.keyword}
                <button
                  onClick={event => {
                    event.stopPropagation();
                    onRemoveKeyword(keyword.id);
                  }}
                  className="w-4 h-4 flex items-center justify-center rounded-full hover:bg-pink-200 transition-colors text-sm leading-none"
                  aria-label={`${keyword.keyword} 삭제`}
                >
                  ×
                </button>
              </span>
            );
          })}
        </div>
      )}
      <div className={`mt-auto flex items-center rounded-2xl transition-all ${
        isEditing
          ? 'bg-amber-50 ring-2 ring-amber-200'
          : 'bg-pink-50 focus-within:ring-2 focus-within:ring-pink-200'
      }`}>
        <input
          ref={inputRef}
          value={keywordInput}
          onChange={event => onKeywordInputChange(event.target.value)}
          onBlur={() => {
            blurTimeoutRef.current = setTimeout(() => {
              onCancelEditKeyword();
            }, 150);
          }}
          onKeyDown={event => {
            if (event.key === 'Enter' && !event.nativeEvent.isComposing) {
              onSubmitKeyword();
            } else if (event.key === 'Escape') {
              onCancelEditKeyword();
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
          className={`flex-1 min-w-0 text-sm bg-transparent px-3 py-2 outline-none placeholder:text-gray-300 ${
            isEditing ? 'text-amber-700' : 'text-gray-700'
          }`}
          placeholder={isEditing ? '키워드 수정' : '키워드 입력'}
          maxLength={100}
        />
        <button
          onMouseDown={() => {
            if (blurTimeoutRef.current) {
              clearTimeout(blurTimeoutRef.current);
              blurTimeoutRef.current = null;
            }
          }}
          onClick={() => {
            if (blurTimeoutRef.current) {
              clearTimeout(blurTimeoutRef.current);
              blurTimeoutRef.current = null;
            }
            onSubmitKeyword();
          }}
          disabled={!isEditing && !keywordInput.trim()}
          className={`shrink-0 w-8 h-8 mr-1 flex items-center justify-center rounded-xl transition-colors duration-150 ${
            isEditing
              ? 'bg-amber-100 hover:bg-amber-200 text-amber-500'
              : 'bg-pink-100 hover:bg-pink-200 disabled:opacity-30 text-pink-400'
          }`}
          aria-label={isEditing ? '키워드 수정 완료' : '키워드 추가'}
        >
          {isEditing ? (
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <polyline points="20 6 9 17 4 12" />
            </svg>
          ) : (
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <polyline points="9 10 4 15 9 20" />
              <path d="M20 4v7a4 4 0 0 1-4 4H4" />
            </svg>
          )}
        </button>
      </div>
    </div>
  );
}
