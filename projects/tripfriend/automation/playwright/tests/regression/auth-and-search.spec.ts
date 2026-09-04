import { expect, test } from "@playwright/test";
import { loginThroughUi } from "../../helpers/auth";
import { uniqueText } from "../../helpers/api-fixtures";

test("TF-E2E-001 정상 로그인 후 홈페이지로 이동한다", async ({ page }) => {
  // 목적: 핵심 인증 진입이 동작하는지 확인한다. 핵심 assertion은 홈 이동과 helper 내부의 accessToken 존재 확인이다.
  const token = await loginThroughUi(page);
  await expect(page).toHaveURL(/\/$/);
  expect(token).toBeTruthy();
});

test("TF-E2E-002 공개 리뷰 검색에서 결과 없음 상태를 표시한다", async ({
  page,
}, testInfo) => {
  // 목적: 공개 목록의 상태 기반 검색 흐름을 검증한다. 핵심 assertion은 검색 API 200과 빈 결과 안내다.
  const keyword = uniqueText("no-result", testInfo.workerIndex);

  await page.goto("/community");
  await page.getByPlaceholder("리뷰 제목 검색").fill(keyword);
  const searchResponsePromise = page.waitForResponse((response) => {
    const url = new URL(response.url());
    return (
      response.request().method() === "GET" &&
      url.pathname === "/api/reviews" &&
      url.searchParams.get("keyword") === keyword
    );
  });
  await page.getByRole("button", { name: "검색", exact: true }).click();

  const searchResponse = await searchResponsePromise;
  expect(searchResponse.status()).toBe(200);
  await expect(page).toHaveURL(/\/community$/);
  await expect(page.getByText("검색 결과가 없습니다.", { exact: true })).toBeVisible();
});
