import { test, expect, Page } from '@playwright/test';

const STUDENT_NAME = `테스트학생_${Date.now()}`;
const EDITED_NAME = `수정학생_${Date.now()}`;

async function login(page: Page) {
  await page.goto('/login');
  await page.fill('#userId', 'admin');
  await page.fill('#password', '123');
  await page.click('button[type="submit"]');
  await page.waitForURL('/', { timeout: 10000 });
}

async function goToStudents(page: Page) {
  await page.click('button[aria-label="학생 관리"]');
  await expect(page.locator('h1:has-text("학생 관리")')).toBeVisible({ timeout: 10000 });
}

test.describe.serial('Student Management', () => {
  test('학생을 추가한다', async ({ page }) => {
    await login(page);
    await goToStudents(page);

    await page.click('button[aria-label="학생 추가"]');
    await expect(page.locator('h2:has-text("새 학생 추가")')).toBeVisible();

    await page.fill('input[placeholder="학생 이름을 입력하세요"]', STUDENT_NAME);
    await page.click('button:has-text("추가하기 ✨")');
    await page.waitForTimeout(1000);

    await expect(page.locator(`text=${STUDENT_NAME}`).first()).toBeVisible({ timeout: 5000 });
  });

  test('학생 이름을 수정한다', async ({ page }) => {
    await login(page);
    await goToStudents(page);
    await page.waitForTimeout(500);

    const card = page.locator(`text=${STUDENT_NAME}`).first().locator('xpath=ancestor::div[contains(@class,"rounded-3xl")]');
    await card.locator('button[aria-label="수정"]').click();
    await page.waitForTimeout(300);

    const nameInput = page.locator('input[placeholder="이름"]');
    await nameInput.clear();
    await nameInput.fill(EDITED_NAME);

    await page.locator('button:has-text("저장")').click();
    await page.waitForTimeout(1000);

    await expect(page.locator(`text=${EDITED_NAME}`).first()).toBeVisible({ timeout: 5000 });
  });

  test('학생을 삭제한다', async ({ page }) => {
    await login(page);
    await goToStudents(page);
    await page.waitForTimeout(500);

    page.on('dialog', dialog => dialog.accept());

    const card = page.locator(`text=${EDITED_NAME}`).first().locator('xpath=ancestor::div[contains(@class,"rounded-3xl")]');
    await card.locator('button[aria-label="삭제"]').click();
    await page.waitForTimeout(1000);

    await expect(page.locator(`text=${EDITED_NAME}`)).not.toBeVisible({ timeout: 5000 });
  });
});
