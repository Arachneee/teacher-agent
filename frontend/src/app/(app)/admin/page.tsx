'use client';

import KpiCard from '../../components/KpiCard';
import DailyUsageChart from '../../components/DailyUsageChart';
import TopKeywordsChart from '../../components/TopKeywordsChart';
import { useUsageSummary } from '../../hooks/useUsageSummary';
import { useDailyUsage } from '../../hooks/useDailyUsage';
import { useTopKeywords } from '../../hooks/useTopKeywords';

export default function AdminPage() {
  const { data: summary, loading: summaryLoading, error: summaryError } = useUsageSummary();
  const { data: dailyData, loading: dailyLoading, error: dailyError } = useDailyUsage(30);
  const { data: keywordsData, loading: keywordsLoading, error: keywordsError } = useTopKeywords(10);

  const formatDuration = (ms: number) => {
    return `${(ms / 1000).toFixed(1)}초`;
  };

  const formatPercentage = (rate: number) => {
    return `${(rate * 100).toFixed(0)}%`;
  };

  if (summaryLoading || dailyLoading || keywordsLoading) {
    return (
      <div className="flex-1 min-w-0 pb-16 md:pb-0 p-4 md:p-6">
        <div className="max-w-7xl mx-auto">
          <div className="flex items-center gap-3 mb-6">
            <span className="text-3xl">📊</span>
            <h1 className="text-2xl font-bold text-gray-800">통계</h1>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-3 gap-4 mb-6">
            {[1, 2, 3, 4, 5, 6].map(i => (
              <div key={i} className="bg-white rounded-3xl p-6 shadow-sm animate-pulse">
                <div className="h-4 bg-gray-200 rounded w-1/2 mb-4"></div>
                <div className="h-8 bg-gray-200 rounded w-3/4"></div>
              </div>
            ))}
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div className="bg-white rounded-3xl p-6 shadow-sm animate-pulse">
              <div className="h-6 bg-gray-200 rounded w-1/3 mb-6"></div>
              <div className="h-64 bg-gray-200 rounded"></div>
            </div>
            <div className="bg-white rounded-3xl p-6 shadow-sm animate-pulse">
              <div className="h-6 bg-gray-200 rounded w-1/3 mb-6"></div>
              <div className="h-64 bg-gray-200 rounded"></div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (summaryError || dailyError || keywordsError) {
    return (
      <div className="flex-1 min-w-0 pb-16 md:pb-0 p-4 md:p-6">
        <div className="max-w-7xl mx-auto">
          <div className="flex items-center gap-3 mb-6">
            <span className="text-3xl">📊</span>
            <h1 className="text-2xl font-bold text-gray-800">통계</h1>
          </div>
          <div className="bg-white rounded-3xl p-6 shadow-sm text-center text-gray-400">
            {summaryError || dailyError || keywordsError}
          </div>
        </div>
      </div>
    );
  }

  if (!summary || !dailyData || !keywordsData) {
    return null;
  }

  return (
    <div className="flex-1 min-w-0 pb-16 md:pb-0 p-4 md:p-6">
      <div className="max-w-7xl mx-auto">
        <div className="flex items-center gap-3 mb-6">
          <span className="text-3xl">📊</span>
          <h1 className="text-2xl font-bold text-gray-800">통계</h1>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-3 gap-4 mb-6">
          <KpiCard
            label="AI 생성 횟수"
            value={summary.totalAiGenerations}
            icon="✨"
          />
          <KpiCard
            label="좋아요율"
            value={formatPercentage(summary.likeRate)}
            subInfo={`${summary.totalLikes}개 좋아요`}
            icon="❤️"
          />
          <KpiCard
            label="복사 전환율"
            value={formatPercentage(summary.copyRate)}
            subInfo={`${summary.totalCopyClicks}회 복사`}
            icon="📋"
          />
          <KpiCard
            label="재생성율"
            value={formatPercentage(summary.regenerationRate)}
            subInfo={`${summary.totalRegenerations}회 재생성`}
            icon="🔄"
          />
          <KpiCard
            label="평균 생성 시간"
            value={formatDuration(summary.avgGenerationDurationMs)}
            icon="⏱️"
          />
          <KpiCard
            label="최근 7일 활성일"
            value={`${summary.activeDaysLast7}일`}
            subInfo={`최근 30일: ${summary.activeDaysLast30}일`}
            icon="📅"
          />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {dailyData.length > 0 ? (
            <DailyUsageChart data={dailyData} />
          ) : (
            <div className="bg-white rounded-3xl p-6 shadow-sm">
              <h3 className="text-lg font-bold text-gray-800 mb-6">일별 사용 추이</h3>
              <div className="flex items-center justify-center h-64 text-gray-400">
                아직 사용 데이터가 없어요
              </div>
            </div>
          )}

          <TopKeywordsChart data={keywordsData} />
        </div>
      </div>
    </div>
  );
}
