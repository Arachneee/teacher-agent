import type { SchoolGrade } from '../types/api';

export const SCHOOL_GRADE_LABELS: Record<SchoolGrade, string> = {
  ELEMENTARY_1: '초1', ELEMENTARY_2: '초2', ELEMENTARY_3: '초3',
  ELEMENTARY_4: '초4', ELEMENTARY_5: '초5', ELEMENTARY_6: '초6',
  MIDDLE_1: '중1', MIDDLE_2: '중2', MIDDLE_3: '중3',
  HIGH_1: '고1', HIGH_2: '고2', HIGH_3: '고3',
};

export const SCHOOL_GRADE_GROUPS: { label: string; grades: SchoolGrade[] }[] = [
  { label: '초등학교', grades: ['ELEMENTARY_1', 'ELEMENTARY_2', 'ELEMENTARY_3', 'ELEMENTARY_4', 'ELEMENTARY_5', 'ELEMENTARY_6'] },
  { label: '중학교', grades: ['MIDDLE_1', 'MIDDLE_2', 'MIDDLE_3'] },
  { label: '고등학교', grades: ['HIGH_1', 'HIGH_2', 'HIGH_3'] },
];

export const AVATAR_COLORS = [
  'bg-pink-200 text-pink-600',
  'bg-purple-200 text-purple-600',
  'bg-blue-200 text-blue-600',
  'bg-green-200 text-green-600',
  'bg-yellow-200 text-yellow-600',
  'bg-orange-200 text-orange-600',
  'bg-rose-200 text-rose-600',
  'bg-teal-200 text-teal-600',
];

export function getAvatarColor(id: number): string {
  return AVATAR_COLORS[id % AVATAR_COLORS.length];
}
