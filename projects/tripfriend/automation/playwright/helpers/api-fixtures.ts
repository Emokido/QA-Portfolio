import type { APIRequestContext } from "@playwright/test";

type RsData<T> = { data?: T } & Partial<T>;

export interface ReviewFixture {
  reviewId: number;
  title: string;
  content: string;
  rating: number;
  placeId: number;
}

function apiUrl(path: string): string {
  const base = process.env.TF_E2E_API_URL ?? "http://localhost:8080";
  return `${base.replace(/\/$/, "")}${path}`;
}

function requiredPlaceId(): number {
  const value = Number(process.env.TF_E2E_PLACE_ID);
  if (!Number.isInteger(value) || value < 1) {
    throw new Error("TF_E2E_PLACE_ID must be an existing positive integer place ID.");
  }
  return value;
}

function unwrap<T>(body: RsData<T>): T {
  return (body.data ?? body) as T;
}

export function uniqueText(prefix: string, workerIndex: number): string {
  return `${prefix}-${Date.now().toString(36)}-${workerIndex}`;
}

export function requirePlaceName(): string {
  const placeName = process.env.TF_E2E_PLACE_NAME;
  if (!placeName) {
    throw new Error(
      "TF_E2E_PLACE_NAME is required and must match TF_E2E_PLACE_ID.",
    );
  }
  return placeName;
}

export async function createReviewFixture(
  request: APIRequestContext,
  token: string,
  title: string,
): Promise<ReviewFixture> {
  const payload = {
    title,
    content: `Playwright 격리 데이터 ${title} 내용입니다.`,
    rating: 4,
    placeId: requiredPlaceId(),
  };
  const response = await request.post(apiUrl("/api/reviews"), {
    headers: { Authorization: `Bearer ${token}` },
    data: payload,
  });

  if (!response.ok()) {
    throw new Error(`Review fixture creation failed: HTTP ${response.status()}`);
  }

  const created = unwrap<ReviewFixture>(await response.json());
  if (!created.reviewId) {
    throw new Error("Review fixture response did not contain reviewId.");
  }
  return { ...payload, reviewId: created.reviewId };
}

export async function deleteCommentFixture(
  request: APIRequestContext,
  token: string,
  commentId: number | undefined,
): Promise<void> {
  if (!commentId) return;
  const response = await request.delete(apiUrl(`/api/comments/${commentId}`), {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!response.ok() && response.status() !== 404) {
    throw new Error(`Comment cleanup failed: HTTP ${response.status()}`);
  }
}

export async function deleteReviewFixture(
  request: APIRequestContext,
  token: string,
  reviewId: number | undefined,
): Promise<void> {
  if (!reviewId) return;
  const response = await request.delete(apiUrl(`/api/reviews/${reviewId}`), {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!response.ok() && response.status() !== 404) {
    throw new Error(`Review cleanup failed: HTTP ${response.status()}`);
  }
}

export async function cleanupCommentScenario(
  request: APIRequestContext,
  token: string,
  commentId: number | undefined,
  reviewId: number | undefined,
): Promise<void> {
  const failures: string[] = [];
  try {
    await deleteCommentFixture(request, token, commentId);
  } catch (error) {
    failures.push(String(error));
  }
  try {
    await deleteReviewFixture(request, token, reviewId);
  } catch (error) {
    failures.push(String(error));
  }
  if (failures.length > 0) {
    throw new Error(`Fixture cleanup failed: ${failures.join(" | ")}`);
  }
}

export async function findCommentId(
  request: APIRequestContext,
  reviewId: number,
  content: string,
): Promise<number | undefined> {
  const response = await request.get(apiUrl(`/api/comments/review/${reviewId}`));
  if (!response.ok()) return undefined;
  const comments = unwrap<Array<{ commentId: number; content: string }>>(
    await response.json(),
  );
  return comments.find((comment) => comment.content === content)?.commentId;
}

export async function findReviewIdByTitle(
  request: APIRequestContext,
  title: string,
): Promise<number | undefined> {
  const response = await request.get(
    apiUrl(`/api/reviews?sort=newest&page=1&keyword=${encodeURIComponent(title)}`),
  );
  if (!response.ok()) return undefined;
  const reviews = unwrap<Array<{ reviewId: number; title: string }>>(
    await response.json(),
  );
  return reviews.find((review) => review.title === title)?.reviewId;
}
