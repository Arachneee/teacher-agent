'use client';

import { useEffect } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { useAuth } from '../context/AuthContext';
import Sidebar from '../components/Sidebar';
import BottomNav from '../components/BottomNav';

export default function AppLayout({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth();
  const router = useRouter();
  const pathname = usePathname();

  const isIntroPage = pathname === '/';

  useEffect(() => {
    if (!loading && !user && !isIntroPage) {
      router.replace('/login');
    }
  }, [loading, user, router, isIntroPage]);

  // Auth-required pages: show loading screen, then redirect if not authenticated
  if (!isIntroPage) {
    if (loading) {
      return (
        <div className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-orange-50 flex items-center justify-center">
          <div className="text-purple-400 text-lg">로딩 중...</div>
        </div>
      );
    }
    if (!user) {
      return null;
    }
  }

  // Authenticated users get full app layout (sidebar + bottom nav)
  if (!loading && user) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-orange-50 flex">
        <Sidebar />
        <div className="flex-1 min-w-0 pb-16 md:pb-0">
          {children}
        </div>
        <BottomNav />
      </div>
    );
  }

  // Intro page for unauthenticated users: plain background only, no sidebar/bottom nav
  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-orange-50">
      {children}
    </div>
  );
}
