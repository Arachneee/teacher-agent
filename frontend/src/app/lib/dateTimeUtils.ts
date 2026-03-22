export const HOURS = Array.from({ length: 24 }, (_, i) => i);
export const MINUTES = [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55];

export function padTwoDigits(value: number): string {
  return String(value).padStart(2, '0');
}

export function parseDateTime(iso: string): { date: string; hour: number; minute: number } {
  const [datePart, timePart] = iso.slice(0, 16).split('T');
  const [hour, minute] = timePart.split(':').map(Number);
  return { date: datePart, hour, minute };
}

export function formatDateKorean(iso: string): string {
  return new Date(iso).toLocaleDateString('ko-KR', { month: 'long', day: 'numeric' });
}
