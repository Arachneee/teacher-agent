'use client';

import React from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { CalendarIcon, StudentsIcon } from './icons/NavIcons';

type Tab = 'calendar' | 'students';

const TABS: { id: Tab; label: string; href: string; Icon: typeof CalendarIcon }[] = [
  { id: 'calendar', label: '캘린더', href: '/' , Icon: CalendarIcon },
  { id: 'students', label: '학생 관리', href: '/students', Icon: StudentsIcon },
];

export default function BottomNav() {
  const pathname = usePathname();
  const router = useRouter();

  const activeTab: Tab = pathname.startsWith('/students') ? 'students' : 'calendar';

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-30 flex md:hidden bg-white/90 backdrop-blur-md border-t border-gray-100 shadow-[0_-1px_0_0_rgba(0,0,0,0.04)]">
      {TABS.map(({ id, label, href, Icon }) => {
        const isActive = activeTab === id;
        return (
          <button
            key={id}
            onClick={() => router.push(href)}
            className={`relative flex-1 flex flex-col items-center justify-center py-2.5 gap-1 transition-colors ${
              isActive ? 'text-purple-600' : 'text-gray-400'
            }`}
            aria-label={label}
            aria-current={isActive ? 'page' : undefined}
          >
            <Icon active={isActive} />
            <span className="text-[10px] font-medium leading-tight">
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
