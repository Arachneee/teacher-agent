import { test, expect, Page } from '@playwright/test';

async function login(page: Page) {
  await page.goto('/login');
  await page.fill('#userId', 'admin');
  await page.fill('#password', '123');
  await page.click('button[type="submit"]');
  await page.waitForURL('/', { timeout: 10000 });
}

test.describe('Recurring Lesson', () => {
  test('반복 수업 생성 모달에서 반복하기 토글이 동작한다', async ({ page }) => {
    await login(page);
    await page.click('button[aria-label="수업 추가"]');
    await page.waitForTimeout(500);

    await page.fill('input[placeholder="수업 제목을 입력하세요"]', '반복수학');

    const toggle = page.locator('button[role="switch"]');
    await expect(toggle).toBeVisible();
    await expect(toggle).toHaveAttribute('aria-checked', 'false');

    await toggle.click();
    await page.waitForTimeout(300);

    await expect(toggle).toHaveAttribute('aria-checked', 'true');
    await expect(page.locator('text=반복 유형')).toBeVisible();
    await expect(page.locator('text=반복 종료일')).toBeVisible();
  });
});
