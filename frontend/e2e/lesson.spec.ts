import { test, expect, Page } from '@playwright/test';

async function login(page: Page) {
  await page.goto('/login');
  await page.fill('#userId', 'admin');
  await page.fill('#password', '123');
  await page.click('button[type="submit"]');
  await page.waitForURL('**/calendar', { timeout: 10000 });
}

async function createLessonViaAPI(page: Page, title: string) {
  const now = new Date();
  const y = now.getFullYear();
  const m = String(now.getMonth() + 1).padStart(2, '0');
  const d = String(now.getDate()).padStart(2, '0');
  const startTime = `${y}-${m}-${d}T10:00:00`;
  const endTime = `${y}-${m}-${d}T11:00:00`;
  return await page.evaluate(
    async ({ title, startTime, endTime }) => {
      const res = await fetch('/api/lessons', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ title, startTime, endTime, recurrence: null, studentIds: null }),
      });
      return res.json();
    },
    { title, startTime, endTime },
  );
}

test.describe.serial('Lesson CRUD', () => {
  let lessonId: number;

  test('수업을 생성하고 캘린더에 표시된다', async ({ page }) => {
    await login(page);
    const lesson = await createLessonViaAPI(page, '수학');
    lessonId = lesson.id;
    await page.reload();
    await page.waitForTimeout(1000);
    await expect(page.locator('text=수학').first()).toBeVisible();
  });

  test('수업 상세 페이지로 이동한다', async ({ page }) => {
    await login(page);
    await page.goto(`/lessons/${lessonId}`);
    await page.waitForTimeout(1000);
    await expect(page.locator('h1:has-text("수학")')).toBeVisible();
  });

  test('수업 목록으로 돌아간다', async ({ page }) => {
    await login(page);
    await page.goto(`/lessons/${lessonId}`);
    await page.waitForTimeout(1000);

    await page.click('text=수업 목록으로');
    await page.waitForTimeout(500);

    await expect(page.locator('h1:has-text("내 수업")')).toBeVisible();
  });
});
