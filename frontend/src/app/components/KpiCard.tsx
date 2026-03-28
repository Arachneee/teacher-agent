'use client';

interface Props {
  label: string;
  value: string | number;
  subInfo?: string;
  icon?: string;
}

export default function KpiCard({ label, value, subInfo, icon }: Props) {
  return (
    <div className="bg-white rounded-3xl p-6 shadow-sm">
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <p className="text-sm text-gray-400 mb-2">{label}</p>
          <p className="text-3xl font-bold text-gray-800">{value}</p>
          {subInfo && (
            <p className="text-xs text-gray-400 mt-2">{subInfo}</p>
          )}
        </div>
        {icon && (
          <span className="text-2xl">{icon}</span>
        )}
      </div>
    </div>
  );
}
