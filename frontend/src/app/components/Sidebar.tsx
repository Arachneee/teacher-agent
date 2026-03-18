'use client';

import React from 'react';
import { usePathname, useRouter } from 'next/navigation';

type Tab = 'calendar' | 'students';

function CalendarIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
      <line x1="16" y1="2" x2="16" y2="6" />
      <line x1="8" y1="2" x2="8" y2="6" />
      <line x1="3" y1="10" x2="21" y2="10" />
    </svg>
  );
}

function StudentsIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
      <path d="M16 3.13a4 4 0 0 1 0 7.75" />
    </svg>
  );
}

const TABS: { id: Tab; label: string; href: string; Icon: () => React.ReactElement }[] = [
  { id: 'calendar', label: '캘린더', href: '/', Icon: CalendarIcon },
  { id: 'students', label: '학생 관리', href: '/students', Icon: StudentsIcon },
];

export default function Sidebar() {
  const pathname = usePathname();
  const router = useRouter();

  const activeTab: Tab = pathname.startsWith('/students') ? 'students' : 'calendar';

  return (
    <aside className="w-[72px] shrink-0 bg-white/70 backdrop-blur-sm border-r border-white/80 flex flex-col items-center pt-6 pb-6 gap-1 shadow-[inset_-1px_0_0_0_rgba(0,0,0,0.04)]">
      <div className="mb-5 text-2xl select-none" aria-hidden>🍎</div>
      {TABS.map(({ id, label, href, Icon }) => (
        <button
          key={id}
          onClick={() => router.push(href)}
          className={`w-14 py-3 px-1 rounded-2xl flex flex-col items-center gap-1.5 transition-all duration-150 ${
            activeTab === id
              ? 'bg-purple-100 text-purple-600'
              : 'text-gray-400 hover:text-gray-600 hover:bg-gray-50/80'
          }`}
          aria-label={label}
          aria-current={activeTab === id ? 'page' : undefined}
        >
          <Icon />
          <span className="text-[10px] font-medium leading-tight text-center">{label}</span>
        </button>
      ))}
    </aside>
  );
}
