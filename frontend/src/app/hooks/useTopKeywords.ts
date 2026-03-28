'use client';

import { useEffect, useState } from 'react';
import { getTopKeywords, type TopKeywordResponse } from '../lib/api';

export function useTopKeywords(limit: number) {
  const [data, setData] = useState<TopKeywordResponse[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;

    async function fetchData() {
      try {
        setLoading(true);
        setError(null);
        const result = await getTopKeywords(limit);
        if (mounted) {
          setData(result);
        }
      } catch (err) {
        if (mounted) {
          setError(err instanceof Error ? err.message : '인기 키워드를 불러오지 못했어요');
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
  }, [limit]);

  return { data, loading, error };
}
