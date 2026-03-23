import { type ReactNode } from 'react';

/**
 * 텍스트에서 키워드와 일치하는 부분을 <mark>로 감싸 하이라이트된 ReactNode 배열을 반환한다.
 * 대소문자를 구분하지 않으며, 키워드가 겹치는 경우 긴 키워드를 우선 매칭한다.
 */
export function highlightKeywords(text: string, keywords: string[]): ReactNode[] {
  if (!text || keywords.length === 0) return [text];

  const validKeywords = keywords.filter(keyword => keyword.trim().length > 0);
  if (validKeywords.length === 0) return [text];

  const sortedKeywords = [...validKeywords].sort((a, b) => b.length - a.length);
  const escaped = sortedKeywords.map(keyword =>
    keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  );
  const pattern = new RegExp(`(${escaped.join('|')})`, 'gi');

  const parts = text.split(pattern);
  if (parts.length === 1) return [text];

  return parts.map((part, index) => {
    const isMatch = validKeywords.some(
      keyword => keyword.toLowerCase() === part.toLowerCase()
    );
    if (isMatch) {
      return (
        <mark
          key={index}
          className="bg-yellow-200/70 text-inherit rounded-sm px-px"
        >
          {part}
        </mark>
      );
    }
    return part;
  });
}
