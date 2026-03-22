'use client';

import React from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { CalendarIcon, StudentsIcon } from './icons/NavIcons';

type Tab = 'calendar' | 'students';

const TABS: { id: Tab; label: string; href: string; Icon: typeof CalendarIcon }[] = [
  { id: 'calendar', label: '캘린더', href: '/', Icon: CalendarIcon },
  { id: 'students', label: '학생 관리', href: '/students', Icon: StudentsIcon },
];

export default function Sidebar() {
  const pathname = usePathname();
  const router = useRouter();

  const activeTab: Tab = pathname.startsWith('/students') ? 'students' : 'calendar';

  return (
    <aside className="hidden md:flex w-[72px] shrink-0 bg-white/70 backdrop-blur-sm border-r border-white/80 flex-col items-center pt-6 pb-6 gap-1 shadow-[inset_-1px_0_0_0_rgba(0,0,0,0.04)]">
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
          <Icon active={activeTab === id} />
          <span className="text-[10px] font-medium leading-tight text-center">{label}</span>
        </button>
      ))}
    </aside>
  );
}
