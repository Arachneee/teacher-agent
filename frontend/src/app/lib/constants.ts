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
