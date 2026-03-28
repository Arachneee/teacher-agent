'use client';

import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import type { TopKeywordResponse } from '../lib/api';

interface Props {
  data: TopKeywordResponse[];
}

export default function TopKeywordsChart({ data }: Props) {
  const topTen = data.slice(0, 10);

  return (
    <div className="bg-white rounded-3xl p-6 shadow-sm">
      <h3 className="text-lg font-bold text-gray-800 mb-6">인기 키워드 Top 10</h3>

      {topTen.length === 0 ? (
        <div className="flex items-center justify-center h-64 text-gray-400">
          아직 키워드 데이터가 없어요
        </div>
      ) : (
        <ResponsiveContainer width="100%" height={400}>
          <BarChart data={topTen} layout="vertical">
            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
            <XAxis type="number" stroke="#9ca3af" style={{ fontSize: '12px' }} />
            <YAxis
              type="category"
              dataKey="keyword"
              width={120}
              stroke="#9ca3af"
              style={{ fontSize: '12px' }}
            />
            <Tooltip
              contentStyle={{
                backgroundColor: 'white',
                border: '1px solid #e5e7eb',
                borderRadius: '12px',
                fontSize: '12px',
              }}
            />
            <Bar dataKey="count" fill="#f472b6" radius={[0, 8, 8, 0]} />
          </BarChart>
        </ResponsiveContainer>
      )}
    </div>
  );
}
