import { type ReactNode } from 'react';

export interface HighlightKeyword {
  text: string;
  required: boolean;
}

/**
 * 한국어 조사 목록 (긴 것부터 정렬하여 greedy 스트리핑/매칭)
 */
const KOREAN_PARTICLES = [
  '에서는', '에서도', '으로는', '으로도', '이라고', '이라는',
  '에게는', '에게도', '한테는', '한테도', '부터는', '까지는', '까지도',
  '에서', '으로', '이라', '에게', '한테', '부터', '까지', '처럼', '보다',
  '이나', '에는', '에도',
  '이', '가', '을', '를', '은', '는', '에', '로', '와', '과',
  '도', '만', '의', '나', '께',
];

const PARTICLE_GROUP = `(?:${KOREAN_PARTICLES.join('|')})`;

function escapeRegex(text: string): string {
  return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

/**
 * 단어 끝에 붙은 한국어 조사를 제거하여 어근을 반환한다.
 * 긴 조사부터 시도하여 "에서는" → "에서" + "는"이 아닌 통째로 제거.
 */
function stripParticle(word: string): string {
  for (const particle of KOREAN_PARTICLES) {
    if (word.length > particle.length && word.endsWith(particle)) {
      return word.slice(0, -particle.length);
    }
  }
  return word;
}

/**
 * 텍스트에서 키워드와 일치하는 부분을 <mark>로 감싸 하이라이트된 ReactNode 배열을 반환한다.
 *
 * - required(그대로) 키워드: 전체 문자열을 정확히 매칭
 * - 자연스럽게 키워드: 공백으로 분리한 각 단어에서 끝에 붙은 조사를 제거하여 어근을 추출한 뒤,
 *   콘텐츠에서 해당 어근 + 조사(선택) 형태를 찾아 하이라이트
 *   예) 키워드 "집중력이" → 어근 "집중력" → 콘텐츠에서 "집중력", "집중력이", "집중력을" 등 매칭
 */
export function highlightKeywords(text: string, keywords: HighlightKeyword[]): ReactNode[] {
  if (!text || keywords.length === 0) return [text];

  const patterns: string[] = [];

  for (const keyword of keywords) {
    const trimmed = keyword.text.trim();
    if (trimmed.length === 0) continue;

    if (keyword.required) {
      patterns.push(escapeRegex(trimmed));
    } else {
      const words = trimmed.split(/\s+/);
      for (const word of words) {
        if (word.length === 0) continue;
        const stem = stripParticle(word);
        patterns.push(escapeRegex(stem));
      }
    }
  }

  if (patterns.length === 0) return [text];

  // 긴 패턴을 먼저 시도하여 greedy 매칭
  patterns.sort((a, b) => b.length - a.length);

  const combined = new RegExp(`(${patterns.join('|')})`, 'gi');
  const result: ReactNode[] = [];
  let lastIndex = 0;

  for (const match of text.matchAll(combined)) {
    const matchStart = match.index!;
    const matchText = match[0];

    // 이미 처리된 범위와 겹치면 건너뛰기
    if (matchStart < lastIndex) continue;

    if (matchStart > lastIndex) {
      result.push(text.slice(lastIndex, matchStart));
    }

    result.push(
      <mark
        key={matchStart}
        className="bg-yellow-200/70 text-inherit rounded-sm px-px"
      >
        {matchText}
      </mark>
    );
    lastIndex = matchStart + matchText.length;
  }

  if (lastIndex < text.length) {
    result.push(text.slice(lastIndex));
  }

  return result.length > 0 ? result : [text];
}
