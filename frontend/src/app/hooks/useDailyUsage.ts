'use client';

import { useEffect, useState } from 'react';
import { getDailyUsage, type DailyUsageResponse } from '../lib/api';

export function useDailyUsage(days: number) {
  const [data, setData] = useState<DailyUsageResponse[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;

    async function fetchData() {
      try {
        setLoading(true);
        setError(null);
        const result = await getDailyUsage(days);
        if (mounted) {
          setData(result);
        }
      } catch (err) {
        if (mounted) {
          setError(err instanceof Error ? err.message : '일별 통계를 불러오지 못했어요');
        }
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    }

    fetchData();

    return () => {
      mounted = false;
    };
  }, [days]);

  return { data, loading, error };
}
