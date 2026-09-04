import { expect, test } from "@playwright/test";
import {
  cleanupCommentScenario,
  createReviewFixture,
  deleteReviewFixture,
  findCommentId,
  findReviewIdByTitle,
  requirePlaceName,
  uniqueText,
} from "../../helpers/api-fixtures";
import { loginThroughUi } from "../../helpers/auth";

test("TF-E2E-003-A 화면에서 리뷰를 작성하고 작성 결과를 확인한다", async ({
  page,
  request,
}, testInfo) => {
  // 목적: 핵심 리뷰 작성 흐름을 UI로 검증한다. 핵심 assertion은 생성 응답과 목록·상세의 고유 제목 표시다.
  const token = await loginThroughUi(page);
  const title = uniqueText("pw-create", testInfo.workerIndex);
  const content = `Playwright UI에서 작성한 ${title} 리뷰 내용입니다.`;
  const placeName = requirePlaceName();
  let reviewId: number | undefined;

  try {
    await page.goto("/community/write");
    await page.getByLabel("제목").fill(title);
    await page.getByRole("combobox").click();
    await page.getByRole("option", { name: placeName, exact: true }).click();
    await page.getByRole("button", { name: "4점", exact: true }).click();
    await page.getByLabel("내용").fill(content);

    const createResponsePromise = page.waitForResponse(
      (response) =>
        response.url().endsWith("/api/reviews") &&
        response.request().method() === "POST",
    );
    page.once("dialog", async (dialog) => {
      expect(dialog.message()).toContain("성공적으로 등록");
      await dialog.accept();
    });
    await page.getByRole("button", { name: "등록하기", exact: true }).click();

    const createResponse = await createResponsePromise;
    expect(createResponse.status()).toBe(201);
    const responseBody = await createResponse.json();
    reviewId = responseBody?.data?.reviewId;
    expect(reviewId, "리뷰 생성 응답에 reviewId가 있어야 한다").toBeTruthy();

    await expect(page).toHaveURL(/\/community$/);
    await page.getByPlaceholder("리뷰 제목 검색").fill(title);
    await page.getByRole("button", { name: "검색", exact: true }).click();
    await expect(page.getByRole("heading", { name: title, exact: true })).toBeVisible();
    await page.getByRole("heading", { name: title, exact: true }).click();
    await expect(page).toHaveURL(new RegExp(`/community/${reviewId}$`));
    await expect(page.getByRole("heading", { name: title, exact: true })).toBeVisible();
    await expect(page.getByText(content, { exact: true })).toBeVisible();
  } finally {
    reviewId ??= await findReviewIdByTitle(request, title);
    await deleteReviewFixture(request, token, reviewId);
  }
});

test("TF-E2E-003-B API로 준비된 리뷰를 화면에서 조회·수정·삭제한다", async ({
  page,
  request,
}, testInfo) => {
  // 목적: 리뷰의 핵심 UI 회귀 흐름을 확인한다. 핵심 assertion은 수정 반영과 삭제 후 목록 복귀다.
  const token = await loginThroughUi(page);
  const originalTitle = uniqueText("e2e-review", testInfo.workerIndex);
  const updatedTitle = `${originalTitle}-updated`;
  const placeName = requirePlaceName();
  let reviewId: number | undefined;
  let deletedThroughUi = false;

  try {
    const review = await createReviewFixture(request, token, originalTitle);
    reviewId = review.reviewId;

    await page.goto(`/community/${reviewId}`);
    await expect(page.getByRole("heading", { name: originalTitle })).toBeVisible();
    await page.getByRole("button", { name: "수정", exact: true }).click();

    // 수정 폼의 비동기 초기화가 끝난 뒤 입력해야 기존 여행지·평점 상태를 잃지 않는다.
    // 핵심 assertion: API fixture의 제목·내용·여행지·평점이 수정 폼에 모두 보존된다.
    await expect(page).toHaveURL(new RegExp(`/community/edit/${reviewId}$`));
    await expect(page.getByLabel("제목")).toHaveValue(originalTitle);
    await expect(page.getByLabel("내용")).toHaveValue(review.content);
    await expect(page.getByRole("combobox")).toContainText(placeName);
    await expect(
      page.getByRole("button", { name: "4점", exact: true }),
    ).toHaveAttribute("aria-pressed", "true");

    await page.getByLabel("제목").fill(updatedTitle);
    await page.getByLabel("내용").fill(`${review.content} 수정 완료.`);

    const updateResponsePromise = page.waitForResponse(
      (response) =>
        response.url().endsWith(`/api/reviews/${reviewId}`) &&
        response.request().method() === "PUT",
    );
    page.once("dialog", async (dialog) => {
      expect(dialog.message()).toContain("성공적으로 수정");
      await dialog.accept();
    });
    await page.getByRole("button", { name: "수정하기", exact: true }).click();
    const updateResponse = await updateResponsePromise;
    expect(updateResponse.status()).toBe(200);
    await expect(page).toHaveURL(/\/community$/);

    await page.goto(`/community/${reviewId}`);
    await expect(page.getByRole("heading", { name: updatedTitle })).toBeVisible();
    await page.getByRole("button", { name: "삭제", exact: true }).click();
    const deleteDialog = page.getByRole("alertdialog", { name: "리뷰 삭제" });

    page.once("dialog", async (dialog) => {
      expect(dialog.message()).toContain("리뷰가 삭제");
      await dialog.accept();
    });
    await deleteDialog.getByRole("button", { name: "삭제", exact: true }).click();
    await expect(page).toHaveURL(/\/community$/);
    deletedThroughUi = true;
  } finally {
    if (!deletedThroughUi) {
      await deleteReviewFixture(request, token, reviewId);
    }
  }
});

test("TF-E2E-004 준비된 리뷰에 댓글을 등록하고 표시한다", async ({
  page,
  request,
}, testInfo) => {
  // 목적: 인증 사용자의 댓글 등록 연결을 확인한다. 핵심 assertion은 새 댓글의 화면 표시다.
  const token = await loginThroughUi(page);
  const title = uniqueText("e2e-comment-review", testInfo.workerIndex);
  const comment = uniqueText("e2e-comment", testInfo.workerIndex);
  let reviewId: number | undefined;
  let commentId: number | undefined;

  try {
    const review = await createReviewFixture(request, token, title);
    reviewId = review.reviewId;
    await page.goto(`/community/${reviewId}`);

    await page.getByPlaceholder("댓글을 작성해주세요...").fill(comment);
    await page.getByRole("button", { name: "댓글 등록", exact: true }).click();
    await expect(page.getByText(comment, { exact: true })).toBeVisible();
    commentId = await findCommentId(request, reviewId, comment);
  } finally {
    await cleanupCommentScenario(request, token, commentId, reviewId);
  }
});
