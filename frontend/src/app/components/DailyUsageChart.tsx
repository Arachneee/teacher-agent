'use client';

import { useState } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import type { DailyUsageResponse } from '../lib/api';

interface Props {
  data: DailyUsageResponse[];
}

export default function DailyUsageChart({ data }: Props) {
  const [period, setPeriod] = useState<7 | 14 | 30>(7);

  const filteredData = data.slice(-period);

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return `${date.getMonth() + 1}/${date.getDate()}`;
  };

  return (
    <div className="bg-white rounded-3xl p-6 shadow-sm">
      <div className="flex items-center justify-between mb-6">
        <h3 className="text-lg font-bold text-gray-800">일별 사용 추이</h3>
        <div className="flex gap-2">
          {([7, 14, 30] as const).map(days => (
            <button
              key={days}
              onClick={() => setPeriod(days)}
              className={`px-3 py-1 rounded-2xl text-sm transition-colors ${
                period === days
                  ? 'bg-pink-400 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              {days}일
            </button>
          ))}
        </div>
      </div>

      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={filteredData}>
          <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
          <XAxis
            dataKey="date"
            tickFormatter={formatDate}
            stroke="#9ca3af"
            style={{ fontSize: '12px' }}
          />
          <YAxis stroke="#9ca3af" style={{ fontSize: '12px' }} />
          <Tooltip
            contentStyle={{
              backgroundColor: 'white',
              border: '1px solid #e5e7eb',
              borderRadius: '12px',
              fontSize: '12px',
            }}
            labelFormatter={(label) => formatDate(String(label))}
          />
          <Legend
            wrapperStyle={{ fontSize: '12px' }}
            iconType="line"
          />
          <Line
            type="monotone"
            dataKey="generations"
            stroke="#f472b6"
            strokeWidth={2}
            name="AI 생성"
            dot={{ fill: '#f472b6', r: 3 }}
          />
          <Line
            type="monotone"
            dataKey="copies"
            stroke="#a78bfa"
            strokeWidth={2}
            name="복사"
            dot={{ fill: '#a78bfa', r: 3 }}
          />
          <Line
            type="monotone"
            dataKey="likes"
            stroke="#fb923c"
            strokeWidth={2}
            name="좋아요"
            dot={{ fill: '#fb923c', r: 3 }}
          />
          <Line
            type="monotone"
            dataKey="regenerations"
            stroke="#60a5fa"
            strokeWidth={2}
            name="재생성"
            dot={{ fill: '#60a5fa', r: 3 }}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
