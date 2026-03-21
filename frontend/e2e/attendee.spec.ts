import { test, expect, Page } from '@playwright/test';

async function login(page: Page) {
  await page.goto('/login');
  await page.fill('#userId', 'admin');
  await page.fill('#password', '123');
  await page.click('button[type="submit"]');
  await page.waitForURL('/', { timeout: 10000 });
}

async function createStudentViaAPI(page: Page, name: string, memo: string) {
  return await page.evaluate(
    async ({ name, memo }) => {
      const res = await fetch('/api/students', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ name, memo }),
      });
      return res.json();
    },
    { name, memo },
  );
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

test.describe.serial('Attendee Management', () => {
  let lessonId: number;
  let studentName: string;

  test('수업과 학생을 생성하고 수업 상세로 이동한다', async ({ page }) => {
    await login(page);
    studentName = `수강생_${Date.now()}`;
    const lesson = await createLessonViaAPI(page, '출석수업');
    lessonId = lesson.id;
    await createStudentViaAPI(page, studentName, '테스트');

    await page.goto(`/lessons/${lessonId}`);
    await page.waitForTimeout(1000);
    await expect(page.locator('h1:has-text("출석수업")')).toBeVisible();
  });

  test('수강생을 추가한다', async ({ page }) => {
    await login(page);
    await page.goto(`/lessons/${lessonId}`);
    await page.waitForTimeout(1000);

    await page.click('button[aria-label="수강생 추가"]');
    await page.waitForTimeout(500);

    const studentButton = page.locator(`button:has-text("${studentName}")`).first();
    await studentButton.click();
    await page.waitForTimeout(500);

    await page.click('button:has-text("수강생 1명 추가하기")');
    await page.waitForTimeout(1000);

    await expect(page.locator(`text=${studentName}`).first()).toBeVisible();
  });

  test('수강생을 삭제한다', async ({ page }) => {
    await login(page);
    await page.goto(`/lessons/${lessonId}`);
    await page.waitForTimeout(1000);

    await expect(page.locator(`text=${studentName}`).first()).toBeVisible();

    page.on('dialog', dialog => dialog.accept());

    await page.click('button[aria-label="수업에서 제거"]');
    await page.waitForTimeout(1000);

    await expect(page.locator(`text=${studentName}`)).not.toBeVisible({ timeout: 5000 });
  });
});
