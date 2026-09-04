import { expect, type Page } from "@playwright/test";

export function requireCredentials(): { username: string; password: string } {
  const username = process.env.TF_E2E_USERNAME;
  const password = process.env.TF_E2E_PASSWORD;

  if (!username || !password) {
    throw new Error(
      "TF_E2E_USERNAME and TF_E2E_PASSWORD are required. Keep them in the current shell only.",
    );
  }

  return { username, password };
}

export async function loginThroughUi(page: Page): Promise<string> {
  const { username, password } = requireCredentials();

  await page.goto("/member/login");
  await page.getByLabel("아이디").fill(username);
  await page.getByLabel("비밀번호").fill(password);
  await page.getByRole("button", { name: "로그인", exact: true }).click();

  await expect(page).toHaveURL(/\/$/);
  const token = await page.evaluate(() => localStorage.getItem("accessToken"));
  expect(token, "로그인 성공 후 accessToken이 저장되어야 한다").toBeTruthy();
  return token!;
}
