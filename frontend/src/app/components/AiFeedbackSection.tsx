'use client';

import { useEffect, useRef, useState } from 'react';
import { Feedback } from '../lib/api';
import { highlightKeywords } from '../lib/highlightKeywords';

interface Props {
  feedback: Feedback | null;
  aiGenerating: boolean;
  isEditingAiContent: boolean;
  onGenerate: () => void;
  onUpdateAiContent: (content: string) => void;
  onLike: () => void;
}

export default function AiFeedbackSection({ feedback, aiGenerating, isEditingAiContent, onGenerate, onUpdateAiContent, onLike }: Props) {
  const [copied, setCopied] = useState(false);
  const [editing, setEditing] = useState(false);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    if (editing && textareaRef.current) {
      textareaRef.current.style.height = 'auto';
      textareaRef.current.style.height = `${textareaRef.current.scrollHeight}px`;
      textareaRef.current.focus();
      const length = textareaRef.current.value.length;
      textareaRef.current.setSelectionRange(length, length);
    }
  }, [editing, feedback?.aiContent]);

  const handleCopy = async () => {
    if (!feedback?.aiContent) return;
    await navigator.clipboard.writeText(feedback.aiContent);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const keywordTexts = feedback?.keywords.map(keyword => keyword.keyword) ?? [];

  return (
    <div className="flex flex-col gap-2">
      {feedback?.aiContent !== undefined && feedback?.aiContent !== null && (
        <div className="relative bg-indigo-50 rounded-2xl p-3">
          {editing ? (
            <textarea
              ref={textareaRef}
              value={feedback.aiContent}
              onChange={event => onUpdateAiContent(event.target.value)}
              onBlur={() => setEditing(false)}
              className="w-full text-sm text-gray-700 leading-relaxed bg-transparent outline-none resize-none pr-8 pb-8 overflow-hidden"
            />
          ) : (
            <div
              onClick={() => setEditing(true)}
              className="text-sm text-gray-700 leading-relaxed whitespace-pre-wrap break-words pr-8 pb-8 cursor-text"
            >
              {highlightKeywords(feedback.aiContent, keywordTexts)}
            </div>
          )}
          <div className="absolute bottom-2 right-2 flex items-center gap-1.5">
            <span className="text-xs text-indigo-300 select-none">
              {feedback.aiContent.length}자
            </span>
            <button
              onClick={onLike}
              disabled={feedback.liked}
              className={`w-7 h-7 flex items-center justify-center rounded-xl transition-colors duration-150 ${
                feedback.liked
                  ? 'bg-indigo-100 text-indigo-500'
                  : 'bg-white hover:bg-indigo-100 text-indigo-300'
              }`}
              aria-label={feedback.liked ? '좋아요 완료' : '좋아요'}
            >
              <svg width="13" height="13" viewBox="0 0 24 24" fill={feedback.liked ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14z" />
                <path d="M7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3" />
              </svg>
            </button>
          </div>
          <button
            onClick={handleCopy}
            className="absolute top-2 right-2 w-7 h-7 flex items-center justify-center rounded-xl bg-white hover:bg-indigo-100 text-indigo-400 transition-colors duration-150"
            aria-label="복사"
          >
            {copied ? (
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <polyline points="20 6 9 17 4 12" />
              </svg>
            ) : (
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2" />
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
              </svg>
            )}
          </button>
        </div>
      )}
      <button
        onClick={onGenerate}
        disabled={aiGenerating || isEditingAiContent || !feedback || feedback.keywords.length === 0}
        className="w-full bg-gradient-to-r from-blue-50 to-indigo-50 hover:from-blue-100 hover:to-indigo-100 disabled:opacity-50 text-indigo-500 text-sm font-medium py-2.5 rounded-2xl transition-colors duration-150 flex items-center justify-center gap-2"
      >
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
          <path d="M9 9h.01M12 9h.01M15 9h.01" strokeWidth="2.5" />
        </svg>
        {aiGenerating ? '생성 중...' : feedback?.aiContent ? '다시 생성' : 'AI 학부모 문자 생성'}
      </button>
    </div>
  );
}
