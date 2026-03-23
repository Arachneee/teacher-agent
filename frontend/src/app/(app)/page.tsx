'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { highlightKeywords } from '../lib/highlightKeywords';

const DEMO_KEYWORDS: { text: string; required: boolean }[] = [
  { text: '집중력 향상', required: false },
  { text: '수학 개념 이해', required: false },
  { text: '100점 맞았어요', required: true },
];
const DEMO_MESSAGE =
  '안녕하세요, 어머님! 오늘 수업에서 민준이가 집중력이 한층 높아졌고 수학 개념을 잘 이해하는 모습을 보였어요. 특히 시험에서 100점 맞았어요! 정말 기특했습니다 😊 앞으로도 함께 열심히 하겠습니다!';

type PhaseId = 'empty' | 'kw1' | 'kw2' | 'kw3' | 'generating' | 'done';

const PHASES: { id: PhaseId; duration: number }[] = [
  { id: 'empty', duration: 1000 },
  { id: 'kw1', duration: 900 },
  { id: 'kw2', duration: 900 },
  { id: 'kw3', duration: 1100 },
  { id: 'generating', duration: 1500 },
  { id: 'done', duration: 3000 },
];

function useAnimationPhase(): PhaseId {
  const [index, setIndex] = useState(0);

  useEffect(() => {
    const timer = setTimeout(() => {
      setIndex(prev => (prev + 1) % PHASES.length);
    }, PHASES[index].duration);
    return () => clearTimeout(timer);
  }, [index]);

  return PHASES[index].id;
}

function AiDemoCard() {
  const phase = useAnimationPhase();

  const visibleKeywords =
    phase === 'empty'
      ? []
      : phase === 'kw1'
        ? DEMO_KEYWORDS.slice(0, 1)
        : phase === 'kw2'
          ? DEMO_KEYWORDS.slice(0, 2)
          : DEMO_KEYWORDS;

  const isGenerating = phase === 'generating';
  const showMessage = phase === 'done';
  const isButtonHighlighted = phase === 'kw3';

  return (
    <div className="bg-white rounded-3xl shadow-md p-5 w-full max-w-sm mx-auto select-none pointer-events-none">
      <div className="flex justify-center mb-3">
        <svg width="24" height="10" viewBox="0 0 24 10" fill="none">
          {([4, 12, 20] as number[]).flatMap(x =>
            ([2, 8] as number[]).map(y => (
              <circle key={`${x}-${y}`} cx={x} cy={y} r="1.5" fill="#d1d5db" />
            ))
          )}
        </svg>
      </div>

      <div className="flex items-center gap-3 mb-4">
        <div className="w-12 h-12 rounded-2xl bg-purple-100 text-purple-600 flex items-center justify-center text-xl font-bold shrink-0">
          민
        </div>
        <div className="flex-1">
          <div className="flex items-center gap-2 flex-wrap">
            <span className="font-semibold text-gray-800">김민준</span>
            <span className="text-xs bg-purple-100 text-purple-600 rounded-lg px-2 py-0.5 font-medium">
              초등 3학년
            </span>
          </div>
          <p className="text-xs text-gray-400 mt-0.5">2026.01.15 등록</p>
        </div>
        <div className="w-7 h-7 bg-purple-50 rounded-xl flex items-center justify-center shrink-0">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="#a78bfa" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <polyline points="14 2 14 8 20 8" />
            <line x1="16" y1="13" x2="8" y2="13" />
            <line x1="16" y1="17" x2="8" y2="17" />
          </svg>
        </div>
      </div>

      <div className="mb-4 px-3 py-2 rounded-2xl">
        <p className="text-sm text-gray-300 italic">메모를 추가하려면 클릭하세요</p>
      </div>

      <div className="flex flex-col gap-2 mb-3">
        <p className="text-xs font-semibold text-gray-400 tracking-wide">수업 키워드</p>
        {visibleKeywords.length > 0 && (
          <div className="flex flex-wrap gap-1.5">
            {visibleKeywords.map((keyword, i) => (
              <span
                key={i}
                className={`inline-flex items-center gap-1 text-xs font-medium px-2.5 py-1 rounded-full ${
                  keyword.required
                    ? 'bg-purple-100 text-purple-600 ring-1 ring-purple-300'
                    : 'bg-pink-50 text-pink-500'
                }`}
              >
                {keyword.required ? `「${keyword.text}」` : keyword.text}
                <span className="text-sm leading-none opacity-60">×</span>
              </span>
            ))}
          </div>
        )}
        <div className="flex flex-col gap-1">
          <div className="flex items-center rounded-2xl bg-pink-50">
            <div className="flex items-center ml-1.5 mr-0.5 shrink-0 gap-0.5">
              <span className="text-xs px-2 py-0.5 rounded-lg font-medium bg-pink-100 text-pink-500">자연스럽게</span>
              <span className="text-xs px-2 py-0.5 rounded-lg font-medium text-gray-300">그대로</span>
            </div>
            <span className="flex-1 text-xs text-gray-300 px-3 py-2">키워드 입력</span>
            <div className="w-8 h-8 mr-1 flex items-center justify-center rounded-xl bg-pink-100">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#f472b6" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <polyline points="9 10 4 15 9 20" />
                <path d="M20 4v7a4 4 0 0 1-4 4H4" />
              </svg>
            </div>
          </div>
          <p className="text-[11px] text-gray-500 ml-2">AI가 자연스럽게 문장에 녹여요</p>
        </div>
      </div>

      {showMessage && (
        <div className="relative bg-indigo-50 rounded-2xl p-3 mb-2">
          <p className="text-xs text-gray-700 leading-relaxed pr-8">
            {highlightKeywords(DEMO_MESSAGE, DEMO_KEYWORDS)}
          </p>
          <div className="absolute top-2 right-2 w-7 h-7 flex items-center justify-center rounded-xl bg-white">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#818cf8" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <rect x="9" y="9" width="13" height="13" rx="2" ry="2" />
              <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
            </svg>
          </div>
          <div className="flex items-center justify-end gap-1.5 mt-1.5">
            <span className="text-xs text-indigo-300">{DEMO_MESSAGE.length}자</span>
            <div className="w-7 h-7 flex items-center justify-center rounded-xl bg-indigo-100">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#818cf8" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14z" />
                <path d="M7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3" />
              </svg>
            </div>
          </div>
        </div>
      )}

      <button
        className={`w-full text-sm font-medium py-2.5 rounded-2xl flex items-center justify-center gap-2 transition-all ${
          isGenerating
            ? 'bg-gradient-to-r from-blue-100 to-indigo-100 text-indigo-500'
            : 'bg-gradient-to-r from-blue-50 to-indigo-50 text-indigo-500'
        } ${isButtonHighlighted ? 'ring-2 ring-indigo-300 shadow-md' : ''}`}
      >
        {isGenerating ? (
          <>
            <div className="w-3.5 h-3.5 border-2 border-indigo-300 border-t-indigo-500 rounded-full animate-spin" />
            생성 중...
          </>
        ) : (
          <>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
              <path d="M9 9h.01M12 9h.01M15 9h.01" strokeWidth="2.5" />
            </svg>
            {showMessage ? '다시 생성' : 'AI 학부모 문자 생성'}
          </>
        )}
      </button>
    </div>
  );
}

function CalendarMockup() {
  const grid = [
    [null, null, { label: '영어', color: 'bg-pink-100 text-pink-500' }, null, null],
    [{ label: '수학', color: 'bg-purple-100 text-purple-600' }, null, null, null, { label: '과학', color: 'bg-blue-100 text-blue-500' }],
    [null, { label: '국어', color: 'bg-amber-100 text-amber-600' }, null, null, null],
    [null, null, null, { label: '수학', color: 'bg-purple-100 text-purple-600' }, null],
  ] as ({ label: string; color: string } | null)[][];

  return (
    <div className="bg-white/80 rounded-2xl p-3 shadow-sm select-none">
      <div className="grid grid-cols-5 gap-1 mb-2">
        {['월', '화', '수', '목', '금'].map(day => (
          <div key={day} className="text-center text-xs font-medium text-gray-300">{day}</div>
        ))}
      </div>
      <div className="flex flex-col gap-1">
        {grid.map((row, rowIndex) => (
          <div key={rowIndex} className="grid grid-cols-5 gap-1">
            {row.map((cell, colIndex) => (
              <div
                key={colIndex}
                className={`h-7 rounded-lg flex items-center justify-center text-xs font-medium ${cell ? cell.color : 'bg-gray-50'}`}
              >
                {cell?.label}
              </div>
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}

function StudentsMockup() {
  const cards = [
    { name: '김민준', grade: '초3', initial: '민', color: 'bg-purple-100 text-purple-600' },
    { name: '이서연', grade: '초4', initial: '서', color: 'bg-pink-100 text-pink-600' },
    { name: '박준호', grade: '중1', initial: '준', color: 'bg-amber-100 text-amber-600' },
    { name: '최예린', grade: '초3', initial: '예', color: 'bg-teal-100 text-teal-600' },
  ];

  return (
    <div className="grid grid-cols-2 gap-2 select-none">
      {cards.map(card => (
        <div key={card.name} className="bg-white rounded-2xl p-3 shadow-sm flex flex-col gap-2">
          <div className={`w-10 h-10 rounded-xl flex items-center justify-center text-base font-bold ${card.color}`}>
            {card.initial}
          </div>
          <div>
            <p className="text-xs font-semibold text-gray-700">{card.name}</p>
            <span className="text-xs text-purple-500 bg-purple-50 rounded-full px-1.5 py-0.5">{card.grade}</span>
          </div>
        </div>
      ))}
    </div>
  );
}

const HOW_IT_WORKS_STEPS = [
  { number: '1', title: '수업에 학생을 등록해요', description: '캘린더에서 수업을 만들고 수강생을 추가해요' },
  { number: '2', title: '수업 키워드를 입력해요', description: 'AI가 자연스럽게 녹이거나 그대로 포함할 수 있어요' },
  { number: '3', title: 'AI가 학부모 문자를 작성해요', description: '버튼 한 번으로 개인화된 문자가 완성돼요' },
];

export default function IntroPage() {
  const router = useRouter();

  return (
    <div className="overflow-y-auto">
      <div className="max-w-3xl mx-auto px-4 md:px-8 py-8 md:py-12 flex flex-col gap-14 md:gap-20">

        {/* HERO */}
        <section className="text-center pt-2 md:pt-6">
          <div className="text-5xl md:text-7xl mb-5 leading-none">🍎</div>
          <h1 className="text-2xl md:text-4xl font-bold text-gray-800 mb-4 leading-snug">
            선생님을 위한<br />
            스마트 수업 관리
          </h1>
          <p className="text-gray-500 text-base md:text-lg mb-8 leading-relaxed">
            수업 일정 관리부터 AI 학부모 문자까지<br />
            선생님의 소중한 시간을 아껴드려요
          </p>
          <div className="flex items-center justify-center gap-3 flex-wrap">
            <button
              onClick={() => router.push('/login')}
              className="inline-flex items-center gap-2 px-6 py-3 bg-purple-500 text-white font-semibold rounded-2xl hover:bg-purple-600 active:scale-95 transition-all shadow-md hover:shadow-lg"
            >
              시작하기
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <polyline points="9 18 15 12 9 6" />
              </svg>
            </button>
            <button
              onClick={() => router.push('/calendar')}
              className="inline-flex items-center gap-2 px-6 py-3 bg-white text-purple-500 font-semibold rounded-2xl hover:bg-purple-50 active:scale-95 transition-all shadow-sm border border-purple-100"
            >
              캘린더 보기
            </button>
          </div>
        </section>

        {/* FEATURES */}
        <section>
          <h2 className="text-lg font-bold text-gray-700 mb-6 text-center">주요 기능</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="bg-white/70 backdrop-blur-sm rounded-3xl p-5 shadow-sm border border-white/80">
              <div className="flex items-center gap-2 mb-2">
                <span className="text-xl">📅</span>
                <span className="font-semibold text-gray-700">수업 캘린더</span>
              </div>
              <p className="text-sm text-gray-400 mb-4 leading-relaxed">
                주간 수업 일정을 한눈에 파악하고 반복 수업도 쉽게 관리해요
              </p>
              <CalendarMockup />
            </div>

            <div className="bg-white/70 backdrop-blur-sm rounded-3xl p-5 shadow-sm border border-white/80">
              <div className="flex items-center gap-2 mb-2">
                <span className="text-xl">👨‍🎓</span>
                <span className="font-semibold text-gray-700">학생 관리</span>
              </div>
              <p className="text-sm text-gray-400 mb-4 leading-relaxed">
                학년별로 학생을 정리하고 메모와 피드백 기록을 남겨요
              </p>
              <StudentsMockup />
            </div>

            <div className="bg-gradient-to-br from-indigo-50 to-purple-50 rounded-3xl p-5 shadow-sm border border-indigo-100/60">
              <div className="flex items-center gap-2 mb-2">
                <span className="text-xl">✨</span>
                <span className="font-semibold text-gray-700">AI 학부모 문자</span>
                <span className="text-xs bg-indigo-500 text-white rounded-full px-2 py-0.5 font-medium">핵심</span>
              </div>
              <p className="text-sm text-gray-500 mb-5 leading-relaxed">
                수업 키워드만 입력하면 AI가 개인화된 학부모 문자를 자동으로 작성해드려요
              </p>
              <div className="flex flex-col gap-2.5">
                <div className="flex flex-wrap gap-1.5">
                  <span className="text-xs bg-pink-50 text-pink-500 border border-pink-100 px-2.5 py-1 rounded-full font-medium">
                    집중력 향상
                  </span>
                  <span className="text-xs bg-pink-50 text-pink-500 border border-pink-100 px-2.5 py-1 rounded-full font-medium">
                    수학 이해
                  </span>
                  <span className="text-xs bg-purple-100 text-purple-600 border border-purple-200 px-2.5 py-1 rounded-full font-medium">
                    「100점 맞았어요」
                  </span>
                </div>
                <div className="flex items-center gap-1.5 text-indigo-400 text-xs font-medium">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <polyline points="6 9 12 15 18 9" />
                  </svg>
                  AI가 자동 생성
                </div>
                <div className="bg-white rounded-2xl p-2.5 shadow-sm">
                  <p className="text-xs text-gray-600 leading-relaxed line-clamp-3">
                    안녕하세요! 오늘 수업에서 민준이가 <mark className="bg-yellow-200/70 rounded-sm px-px">집중력</mark>이 높아졌고 <mark className="bg-yellow-200/70 rounded-sm px-px">수학</mark> 개념을 잘 이해하는 모습을 보였어요. 특히 <mark className="bg-yellow-200/70 rounded-sm px-px">100점 맞았어요</mark>!...
                  </p>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* AI SHOWCASE */}
        <section>
          <div className="text-center mb-10">
            <span className="text-sm font-semibold text-indigo-500 bg-indigo-50 rounded-full px-4 py-1.5 inline-block mb-4">
              ✨ 이렇게 작동해요
            </span>
            <h2 className="text-xl md:text-2xl font-bold text-gray-800 mb-3">
              키워드 입력 → AI 문자 완성
            </h2>
            <p className="text-gray-500 text-sm md:text-base leading-relaxed">
              수업에서 관찰한 내용을 짧은 키워드로 남기면<br />
              AI가 각 학생에게 맞는 학부모 문자를 만들어드려요
            </p>
          </div>

          <div className="flex flex-col md:flex-row items-start gap-8 md:gap-12">
            <div className="flex flex-col gap-6 md:w-52 shrink-0 w-full">
              {HOW_IT_WORKS_STEPS.map(step => (
                <div key={step.number} className="flex items-start gap-3">
                  <div className="w-8 h-8 rounded-full bg-purple-500 text-white text-sm font-bold flex items-center justify-center shrink-0 mt-0.5 shadow-sm">
                    {step.number}
                  </div>
                  <div>
                    <p className="font-semibold text-gray-700 text-sm">{step.title}</p>
                    <p className="text-xs text-gray-400 mt-0.5 leading-relaxed">{step.description}</p>
                  </div>
                </div>
              ))}
              <div className="mt-2 bg-indigo-50 rounded-2xl p-3 border border-indigo-100/60">
                <p className="text-xs text-indigo-600 font-medium mb-1.5">💡 이런 키워드 어때요?</p>
                <div className="flex flex-wrap gap-1">
                  <span className="text-xs text-pink-500 bg-pink-50 border border-pink-100 rounded-full px-2 py-0.5">발음 교정 필요</span>
                  <span className="text-xs text-pink-500 bg-pink-50 border border-pink-100 rounded-full px-2 py-0.5">적극적 참여</span>
                  <span className="text-xs text-purple-600 bg-purple-50 border border-purple-200 rounded-full px-2 py-0.5">「숙제 완료」</span>
                </div>
                <p className="text-[10px] text-indigo-400 mt-1.5 leading-relaxed">
                  <span className="text-pink-400">자연스럽게</span> — AI가 문장에 녹여요 · <span className="text-purple-500">「그대로」</span> — 원문 그대로 포함
                </p>
              </div>
            </div>

            <div className="flex-1 w-full">
              <AiDemoCard />
            </div>
          </div>
        </section>

        {/* CTA */}
        <section className="pb-4">
          <div className="bg-gradient-to-br from-purple-500 to-pink-500 rounded-3xl p-8 text-white text-center shadow-lg">
            <div className="text-4xl mb-4">🍎</div>
            <h3 className="text-xl md:text-2xl font-bold mb-2">지금 바로 시작해보세요!</h3>
            <p className="text-purple-100 text-sm md:text-base mb-6 leading-relaxed">
              학생 관리부터 AI 학부모 문자까지<br />
              선생님의 시간을 아껴드릴게요
            </p>
            <div className="flex items-center justify-center gap-3 flex-wrap">
              <button
                onClick={() => router.push('/login')}
                className="bg-white text-purple-600 font-semibold px-6 py-2.5 rounded-2xl hover:bg-purple-50 active:scale-95 transition-all shadow-sm"
              >
                로그인하고 시작하기 →
              </button>
            </div>
          </div>
        </section>

      </div>
    </div>
  );
}
