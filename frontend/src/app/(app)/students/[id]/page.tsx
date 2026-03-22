'use client';

import Link from 'next/link';
import { useParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import FeedbackHistoryCard from '../../../components/FeedbackHistoryCard';
import { getFeedbacks, getStudent } from '../../../lib/api';
import { getAvatarColor } from '../../../lib/constants';
import type { Feedback, Student } from '../../../types/api';

type FilterTab = 'all' | 'liked';

export default function StudentHistoryPage() {
  const params = useParams();
  const studentId = Number(params.id);

  const [student, setStudent] = useState<Student | null>(null);
  const [feedbacks, setFeedbacks] = useState<Feedback[]>([]);
  const [activeTab, setActiveTab] = useState<FilterTab>('all');
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    async function load() {
      setLoading(true);
      setErrorMessage(null);
      try {
        const [loadedStudent, loadedFeedbacks] = await Promise.all([
          getStudent(studentId),
          getFeedbacks(studentId),
        ]);
        setStudent(loadedStudent);
        setFeedbacks(loadedFeedbacks);
      } catch {
        setErrorMessage('학생 정보를 불러오지 못했어요');
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [studentId]);

  const displayedFeedbacks = activeTab === 'liked'
    ? feedbacks.filter(feedback => feedback.liked)
    : feedbacks;

  const likedCount = feedbacks.filter(feedback => feedback.liked).length;
  const avatarColor = student ? getAvatarColor(student.id) : 'bg-purple-200 text-purple-600';

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-orange-50">
      <div className="max-w-2xl mx-auto px-4 py-8">
        {/* 헤더 */}
        <div className="flex items-center gap-3 mb-6">
          <Link
            href="/students"
            className="w-9 h-9 flex items-center justify-center rounded-2xl bg-white shadow-sm hover:shadow-md text-gray-400 hover:text-gray-600 transition-all duration-150 shrink-0"
            aria-label="학생 목록으로"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <polyline points="15 18 9 12 15 6" />
            </svg>
          </Link>
          <h1 className="text-lg font-bold text-gray-800">피드백 기록</h1>
        </div>

        {/* 학생 프로필 */}
        {student && (
          <div className="bg-white rounded-3xl p-5 shadow-sm mb-5 flex items-center gap-4">
            <div className={`w-12 h-12 rounded-2xl flex items-center justify-center text-xl font-bold shrink-0 ${avatarColor}`}>
              {student.name.charAt(0)}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-base font-semibold text-gray-800">{student.name}</p>
              {student.memo && (
                <p className="text-sm text-gray-400 truncate">{student.memo}</p>
              )}
            </div>
            <div className="shrink-0 text-right">
              <p className="text-2xl font-bold text-pink-400">{feedbacks.length}</p>
              <p className="text-xs text-gray-400">총 수업</p>
            </div>
          </div>
        )}

        {/* 필터 탭 */}
        <div className="flex gap-2 mb-4">
          <button
            onClick={() => setActiveTab('all')}
            className={`px-4 py-2 rounded-2xl text-sm font-medium transition-colors duration-150 ${
              activeTab === 'all'
                ? 'bg-purple-400 text-white'
                : 'bg-white text-gray-500 hover:bg-purple-50'
            }`}
          >
            전체 {feedbacks.length}
          </button>
          <button
            onClick={() => setActiveTab('liked')}
            className={`px-4 py-2 rounded-2xl text-sm font-medium transition-colors duration-150 ${
              activeTab === 'liked'
                ? 'bg-pink-400 text-white'
                : 'bg-white text-gray-500 hover:bg-pink-50'
            }`}
          >
            ♥ 보관 {likedCount}
          </button>
        </div>

        {/* 에러 */}
        {errorMessage && (
          <div className="bg-rose-50 text-rose-400 text-sm rounded-2xl px-4 py-3 mb-4">
            {errorMessage}
          </div>
        )}

        {/* 목록 */}
        {loading ? (
          <div className="flex flex-col gap-3">
            {[1, 2, 3].map(index => (
              <div key={index} className="bg-white rounded-3xl p-5 shadow-sm animate-pulse">
                <div className="h-4 bg-gray-100 rounded-full w-1/3 mb-3" />
                <div className="h-3 bg-gray-100 rounded-full w-1/2 mb-3" />
                <div className="h-16 bg-gray-100 rounded-2xl" />
              </div>
            ))}
          </div>
        ) : displayedFeedbacks.length === 0 ? (
          <div className="text-center py-16 text-gray-400">
            {activeTab === 'liked'
              ? '보관한 문자가 없어요. 마음에 드는 AI 문자에 ♥를 눌러보세요.'
              : '아직 피드백 기록이 없어요. 수업에서 키워드를 입력해보세요.'}
          </div>
        ) : (
          <div className="flex flex-col gap-3">
            {displayedFeedbacks.map(feedback => (
              <FeedbackHistoryCard key={feedback.id} feedback={feedback} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
