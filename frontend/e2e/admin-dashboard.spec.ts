import { test, expect, type Page } from '@playwright/test';

async function login(page: Page) {
  await page.goto('/login');
  await page.fill('#userId', 'admin');
  await page.fill('#password', '123');
  await page.click('button[type="submit"]');
  await page.waitForURL('**/calendar', { timeout: 10000 });
}

test.describe('어드민 대시보드', () => {
  let page: Page;

  test.beforeAll(async ({ browser }) => {
    page = await browser.newPage();
    await login(page);
  });

  test.afterAll(async () => {
    await page.close();
  });

  test('/admin 페이지 접근 → KPI 카드 6개 렌더링 확인', async () => {
    await page.goto('/admin');
    await page.waitForSelector('h1:has-text("통계")', { timeout: 10000 });

    // KPI 카드 6개 확인 (AI 생성 횟수, 좋아요율, 복사 전환율, 재생성율, 평균 생성 시간, 최근 7일 활성일)
    const kpiCards = page.locator('.grid.grid-cols-2 > div');
    await expect(kpiCards).toHaveCount(6);

    // 각 KPI 라벨 확인
    await expect(page.getByText('AI 생성 횟수')).toBeVisible();
    await expect(page.getByText('좋아요율')).toBeVisible();
    await expect(page.getByText('복사 전환율')).toBeVisible();
    await expect(page.getByText('재생성율')).toBeVisible();
    await expect(page.getByText('평균 생성 시간')).toBeVisible();
    await expect(page.getByText('최근 7일 활성일')).toBeVisible();
  });

  test('일별 추이 차트 영역 렌더링 확인', async () => {
    await page.goto('/admin');
    await page.waitForSelector('h1:has-text("통계")', { timeout: 10000 });

    // 일별 사용 추이 차트 제목 확인
    await expect(page.getByText('일별 사용 추이')).toBeVisible();

    // 차트 컨테이너 확인 (Recharts ResponsiveContainer)
    const chartContainer = page.locator('.recharts-responsive-container').first();
    await expect(chartContainer).toBeVisible();
  });

  test('인기 키워드 차트 영역 렌더링 확인', async () => {
    await page.goto('/admin');
    await page.waitForSelector('h1:has-text("통계")', { timeout: 10000 });

    // 인기 키워드 차트 제목 확인
    await expect(page.getByText('인기 키워드')).toBeVisible();

    // 두 번째 차트 컨테이너 확인
    const chartContainers = page.locator('.recharts-responsive-container');
    await expect(chartContainers).toHaveCount(2);
  });

  test('기간 토글 버튼 (7일/14일/30일) 클릭 동작 확인', async () => {
    await page.goto('/admin');
    await page.waitForSelector('h1:has-text("통계")', { timeout: 10000 });

    // 기간 토글 버튼들 확인
    const button7 = page.getByRole('button', { name: '7일' });
    const button14 = page.getByRole('button', { name: '14일' });
    const button30 = page.getByRole('button', { name: '30일' });

    await expect(button7).toBeVisible();
    await expect(button14).toBeVisible();
    await expect(button30).toBeVisible();

    // 기본 선택 상태 확인 (7일이 활성화)
    await expect(button7).toHaveClass(/bg-pink-400/);

    // 14일 클릭
    await button14.click();
    await expect(button14).toHaveClass(/bg-pink-400/);
    await expect(button7).not.toHaveClass(/bg-pink-400/);

    // 30일 클릭
    await button30.click();
    await expect(button30).toHaveClass(/bg-pink-400/);
    await expect(button14).not.toHaveClass(/bg-pink-400/);

    // 7일 다시 클릭
    await button7.click();
    await expect(button7).toHaveClass(/bg-pink-400/);
  });

  test('Sidebar에서 통계 탭 클릭 → /admin 이동 확인', async () => {
    // 캘린더 페이지에서 시작
    await page.goto('/calendar');
    await page.waitForSelector('h1:has-text("내 수업")', { timeout: 10000 });

    // Sidebar의 통계 탭 클릭 (데스크톱 뷰)
    // 📊 아이콘 또는 '통계' 텍스트로 찾기
    const statsTab = page.locator('a[href="/admin"]').first();
    
    if (await statsTab.isVisible()) {
      await statsTab.click();
      await page.waitForURL('**/admin', { timeout: 10000 });
      await expect(page.getByText('통계').first()).toBeVisible();
    } else {
      // 모바일 뷰에서는 BottomNav 확인
      const bottomNavStats = page.locator('nav a[href="/admin"]');
      if (await bottomNavStats.isVisible()) {
        await bottomNavStats.click();
        await page.waitForURL('**/admin', { timeout: 10000 });
        await expect(page.getByText('통계').first()).toBeVisible();
      }
    }
  });
});
