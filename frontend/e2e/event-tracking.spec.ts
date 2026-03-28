import { test, expect, type Page } from '@playwright/test';

async function login(page: Page) {
  await page.goto('/login');
  await page.fill('input[type="text"]', 'admin');
  await page.fill('input[type="password"]', '123');
  await page.click('button[type="submit"]');
  await page.waitForURL('**/calendar', { timeout: 10000 });
}

test.describe.serial('Event Tracking', () => {
  let page: Page;

  test.beforeAll(async ({ browser }) => {
    page = await browser.newPage();
    await login(page);
  });

  test.afterAll(async () => {
    await page.close();
  });

  test('POST /events API가 정상 동작한다', async () => {
    const response = await page.evaluate(async () => {
      const res = await fetch('/api/events', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          eventType: 'feedback_copy',
          metadata: JSON.stringify({ feedbackId: 999 }),
        }),
      });
      return { status: res.status, body: await res.json() };
    });

    expect(response.status).toBe(200);
    expect(response.body.id).toBeDefined();
  });

  test('feedback_edit 이벤트가 저장된다', async () => {
    const response = await page.evaluate(async () => {
      const res = await fetch('/api/events', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          eventType: 'feedback_edit',
          metadata: JSON.stringify({ feedbackId: 888 }),
        }),
      });
      return { status: res.status, body: await res.json() };
    });

    expect(response.status).toBe(200);
    expect(response.body.id).toBeDefined();
  });

  test('feedback_regenerate 이벤트가 저장된다', async () => {
    const response = await page.evaluate(async () => {
      const res = await fetch('/api/events', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          eventType: 'feedback_regenerate',
          metadata: JSON.stringify({ feedbackId: 777 }),
        }),
      });
      return { status: res.status, body: await res.json() };
    });

    expect(response.status).toBe(200);
    expect(response.body.id).toBeDefined();
  });

  test('metadata가 null이어도 저장된다', async () => {
    const response = await page.evaluate(async () => {
      const res = await fetch('/api/events', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          eventType: 'feedback_copy',
          metadata: null,
        }),
      });
      return { status: res.status, body: await res.json() };
    });

    expect(response.status).toBe(200);
    expect(response.body.id).toBeDefined();
  });

  test('eventType이 빈 문자열이면 400 에러를 반환한다', async () => {
    const response = await page.evaluate(async () => {
      const res = await fetch('/api/events', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          eventType: '',
          metadata: null,
        }),
      });
      return { status: res.status };
    });

    expect(response.status).toBe(400);
  });
});
