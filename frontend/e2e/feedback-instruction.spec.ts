import { test, expect, Page } from '@playwright/test';

async function login(page: Page) {
  await page.goto('/login');
  await page.fill('#userId', 'admin');
  await page.fill('#password', '123');
  await page.click('button[type="submit"]');
  await page.waitForURL('**/calendar', { timeout: 10000 });
}

async function createStudentViaAPI(page: Page, name: string) {
  return await page.evaluate(
    async ({ name }) => {
      const res = await fetch('/api/students', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ name, grade: 'ELEMENTARY_1' }),
      });
      return res.json();
    },
    { name },
  );
}

async function createLessonViaAPI(page: Page, title: string, studentIds: number[]) {
  const now = new Date();
  const y = now.getFullYear();
  const m = String(now.getMonth() + 1).padStart(2, '0');
  const d = String(now.getDate()).padStart(2, '0');
  const startTime = `${y}-${m}-${d}T10:00:00`;
  const endTime = `${y}-${m}-${d}T11:00:00`;
  return await page.evaluate(
    async ({ title, startTime, endTime, studentIds }) => {
      const res = await fetch('/api/lessons', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ title, startTime, endTime, recurrence: null, studentIds }),
      });
      return res.json();
    },
    { title, startTime, endTime, studentIds },
  );
}

test.describe('Feedback instruction (regeneration direction)', () => {
  let lessonId: number;
  let studentId: number;

  test.beforeAll(async ({ browser }) => {
    const page = await browser.newPage();
    await login(page);
    const student = await createStudentViaAPI(page, 'E2E방향테스트');
    studentId = student.id;
    const lesson = await createLessonViaAPI(page, 'E2E방향수업', [studentId]);
    lessonId = lesson.id;
    await page.close();
  });

  test('최초 생성 시 방향 입력란 없음 -> 생성 후 노출 -> 재생성 후 초기화', async ({ page }) => {
    await login(page);
    await page.goto(`/lessons/${lessonId}`);
    await page.waitForTimeout(2000);

    // Find the student card area and add a keyword
    const studentCard = page.locator('text=E2E방향테스트').first();
    await expect(studentCard).toBeVisible({ timeout: 10000 });
    await studentCard.click();
    await page.waitForTimeout(1000);

    // Add a keyword
    const keywordInput = page.locator('input[placeholder*="키워드"]').first();
    await expect(keywordInput).toBeVisible({ timeout: 5000 });
    await keywordInput.fill('성실함');
    await keywordInput.press('Enter');
    await page.waitForTimeout(1000);

    // Verify instruction input is NOT visible before first generation (no aiContent)
    const instructionInput = page.locator('input[placeholder*="수정 방향"]');
    await expect(instructionInput).not.toBeVisible();

    // Click generate button
    const generateButton = page.locator('button:has-text("AI 학부모 문자 생성")');
    await expect(generateButton).toBeEnabled({ timeout: 5000 });
    await generateButton.click();

    // Wait for generation to complete (button changes from "생성 중..." back)
    await expect(page.locator('button:has-text("다시 생성")')).toBeVisible({ timeout: 60000 });
    await page.waitForTimeout(1000);

    // After generation, instruction input should be visible (aiContent exists now)
    await expect(instructionInput).toBeVisible({ timeout: 5000 });

    // Type instruction
    await instructionInput.fill('더 짧게');
    await expect(instructionInput).toHaveValue('더 짧게');

    // Click regenerate button
    const regenerateButton = page.locator('button:has-text("다시 생성")');
    await regenerateButton.click();

    // Wait for regeneration to complete
    await expect(page.locator('button:has-text("생성 중...")')).toBeVisible({ timeout: 5000 });
    await expect(page.locator('button:has-text("다시 생성")')).toBeVisible({ timeout: 60000 });
    await page.waitForTimeout(500);

    // Instruction input should be cleared after generation completes
    await expect(instructionInput).toHaveValue('');
  });
});
