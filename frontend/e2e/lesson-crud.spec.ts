import { test, expect, Page } from '@playwright/test';

const LESSON_TITLE = `테스트수업_${Date.now()}`;

async function login(page: Page) {
  await page.goto('/login');
  await page.fill('#userId', 'admin');
  await page.fill('#password', '123');
  await page.click('button[type="submit"]');
  await expect(page.locator('h1:has-text("내 수업")')).toBeVisible({ timeout: 10000 });
}

test.describe('Lesson CRUD', () => {
  test('create lesson, view detail, navigate back', async ({ page }) => {
    await login(page);

    await page.click('button[aria-label="수업 추가"]');
    await expect(page.locator('h2:has-text("새 수업 추가")')).toBeVisible();

    await page.fill('input[placeholder="수업 제목을 입력하세요"]', LESSON_TITLE);
    await page.click('button:has-text("다음 →")');

    await expect(page.locator('h2:has-text("수강생 선택")')).toBeVisible({ timeout: 5000 });
    await page.click('button:has-text("나중에 추가하기")');

    await page.waitForTimeout(1000);
    await expect(page.locator(`text=${LESSON_TITLE}`)).toBeVisible({ timeout: 5000 });

    await page.locator(`text=${LESSON_TITLE}`).first().click({ force: true });
    await expect(page.locator(`h1:has-text("${LESSON_TITLE}")`)).toBeVisible({ timeout: 10000 });
    await expect(page.locator('text=수강생을 관리해요')).toBeVisible();

    await page.click('text=수업 목록으로');
    await expect(page.locator('h1:has-text("내 수업")')).toBeVisible({ timeout: 10000 });
  });
});
