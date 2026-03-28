const BASE_URL = '/api';

export function trackEvent(eventType: string, metadata?: Record<string, unknown>) {
  fetch(`${BASE_URL}/events`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      eventType,
      metadata: metadata ? JSON.stringify(metadata) : null,
    }),
  }).catch(() => {});
}
