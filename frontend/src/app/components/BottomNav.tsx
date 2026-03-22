'use client';

import React from 'react';
import { usePathname, useRouter } from 'next/navigation';

type Tab = 'calendar' | 'students';

function CalendarIcon({ active }: { active: boolean }) {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={active ? 2.2 : 1.8} strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
      <line x1="16" y1="2" x2="16" y2="6" />
      <line x1="8" y1="2" x2="8" y2="6" />
      <line x1="3" y1="10" x2="21" y2="10" />
    </svg>
  );
}

function StudentsIcon({ active }: { active: boolean }) {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={active ? 2.2 : 1.8} strokeLinecap="round" strokeLinejoin="round">
      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
      <path d="M16 3.13a4 4 0 0 1 0 7.75" />
    </svg>
  );
}

const TABS: { id: Tab; label: string; href: string }[] = [
  { id: 'calendar', label: '캘린더', href: '/' },
  { id: 'students', label: '학생 관리', href: '/students' },
];

export default function BottomNav() {
  const pathname = usePathname();
  const router = useRouter();

  const activeTab: Tab = pathname.startsWith('/students') ? 'students' : 'calendar';

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-30 flex md:hidden bg-white/90 backdrop-blur-md border-t border-gray-100 shadow-[0_-1px_0_0_rgba(0,0,0,0.04)]">
      {TABS.map(({ id, label, href }) => {
        const isActive = activeTab === id;
        return (
          <button
            key={id}
            onClick={() => router.push(href)}
            className={`flex-1 flex flex-col items-center justify-center py-2.5 gap-1 transition-colors ${
              isActive ? 'text-purple-600' : 'text-gray-400'
            }`}
            aria-label={label}
            aria-current={isActive ? 'page' : undefined}
          >
            {id === 'calendar' ? (
              <CalendarIcon active={isActive} />
            ) : (
              <StudentsIcon active={isActive} />
            )}
            <span className={`text-[10px] font-medium leading-tight ${isActive ? 'text-purple-600' : 'text-gray-400'}`}>
              {label}
            </span>
            {isActive && (
              <span className="absolute bottom-0 w-8 h-0.5 bg-purple-500 rounded-full" />
            )}
          </button>
        );
      })}
    </nav>
  );
}
