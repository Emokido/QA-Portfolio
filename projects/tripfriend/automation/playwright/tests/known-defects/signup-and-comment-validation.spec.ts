import { expect, test } from "@playwright/test";
import {
  createReviewFixture,
  deleteReviewFixture,
  uniqueText,
} from "../../helpers/api-fixtures";
import { loginThroughUi } from "../../helpers/auth";

test("TF-E2E-005 [TF-BUG-010] 홈 회원가입 CTA가 실제 회원가입 화면으로 이동한다", async ({
  page,
}) => {
  // 목적: 과거 경로 결함의 회귀를 탐지한다. 핵심 assertion은 /member/signup 도착이다.
  await page.goto("/");
  await page.getByRole("link", { name: "무료 회원가입", exact: true }).click();
  await expect(page).toHaveURL(/\/member\/signup$/);
  await expect(page.getByRole("heading", { name: "회원가입", exact: true })).toBeVisible();
});

for (const boundary of [
  { label: "1자", content: "한" },
  { label: "101자", content: "가".repeat(101) },
]) {
  test(`TF-E2E-006 [TF-BUG-014] ${boundary.label} 댓글에 구체적인 길이 안내를 표시한다`, async ({
    page,
    request,
  }, testInfo) => {
    // 목적: 최소·최대 댓글 경계 오류가 사용자의 수정 방법을 알려주는지 확인한다. 핵심 assertion은 2~100자 안내다.
    const token = await loginThroughUi(page);
    const title = uniqueText("e2e-validation", testInfo.workerIndex);
    let reviewId: number | undefined;

    try {
      const review = await createReviewFixture(request, token, title);
      reviewId = review.reviewId;
      await page.goto(`/community/${reviewId}`);
      await page.getByPlaceholder("댓글을 작성해주세요...").fill(boundary.content);

      // dialog listener 안에서 즉시 dismiss해 click과 테스트 timeout이 서로 기다리지 않게 한다.
      const dialogMessagePromise = new Promise<string>((resolve, reject) => {
        page.once("dialog", async (dialog) => {
          const message = dialog.message();
          try {
            await dialog.dismiss();
            resolve(message);
          } catch (error) {
            reject(error);
          }
        });
      });
      await page.getByRole("button", { name: "댓글 등록", exact: true }).click();
      const message = await dialogMessagePromise;
      expect(message).toMatch(/2.*100.*자/);
    } finally {
      await deleteReviewFixture(request, token, reviewId);
    }
  });
}
