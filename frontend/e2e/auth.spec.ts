import { test, expect } from '@playwright/test';

test.describe('Auth flow', () => {
  test('로그인, 홈 확인, 로그아웃, 재로그인', async ({ page }) => {
    await page.goto('/login');
    await page.fill('#userId', 'admin');
    await page.fill('#password', '123');
    await page.click('button[type="submit"]');

    await expect(page.locator('h1:has-text("내 수업")')).toBeVisible({ timeout: 10000 });
    await expect(page.locator('text=admin')).toBeVisible();

    await page.click('text=로그아웃');
    await expect(page).toHaveURL(/\/login/, { timeout: 10000 });

    await page.fill('#userId', 'admin');
    await page.fill('#password', '123');
    await page.click('button[type="submit"]');

    await expect(page.locator('h1:has-text("내 수업")')).toBeVisible({ timeout: 10000 });
  });
});
