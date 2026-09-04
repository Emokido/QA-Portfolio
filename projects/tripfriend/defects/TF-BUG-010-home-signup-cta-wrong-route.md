# TF-BUG-010 — 홈페이지 하단 무료 회원가입 링크가 존재하지 않는 경로를 가리킴

- 결함 상태: `Open`
- 발견 경로: 사용자 수동 프런트 탐색
- 기준 프로젝트: Next.js 프런트
- 기준 커밋: `e2431223ce2a18c8f944d544cba76b951ce0356d`
- 영향 기능: 홈페이지 회원가입 CTA
- 테스트 수준: Browser UI · 정적 코드 상관 분석
- 심각도: `Minor`
- 수정 우선순위: 제품 담당자 결정 필요
- 실행 주체: `User`

## 1. 요약

로그아웃 사용자가 홈페이지 하단의 `무료 회원가입` 버튼을 누르면 실제 회원가입 페이지가 아니라 존재하지 않는 `/signup` 경로로 이동해 404가 발생한다.

## 2. 원본 기록과 추적

- 수동 탐색 원본 번호: `BUG-002`
- 원본 발견 기록: 사용자 내부 자료로 보존하며 공개 산출물은 아래 정제 보고서를 사용한다.
- 수동 탐색 보고서: [TripFriend 수동 프런트 탐색 보고서](../reports/manual-frontend-exploration-report.md)
- 자동화 재현: `TF-E2E-005 → TF-S8-PW-KD-EXEC-001` — 사용자 실행에서 `/signup` 404를 재현해 Fail 판정

## 3. 테스트 환경

- Windows 11, Chrome
- Frontend `localhost:3000`, Backend `localhost:8080`
- 로그아웃 상태

## 4. 재현 절차

1. TripFriend 홈페이지에 접속한다.
2. 페이지 하단의 가입 유도 영역으로 이동한다.
3. `무료 회원가입`을 누른다.
4. 이동 URL과 응답 화면을 확인한다.

## 5. 기대 결과

실제 회원가입 화면 `/member/signup`으로 이동한다.

## 6. 실제 결과

`/signup`으로 이동하며 사용자 기록에서 `GET /signup 404`가 확인됐다.

## 7. 코드 상관관계

- `frontend/src/app/page.tsx`의 하단 CTA는 `href="/signup"`이다.
- 실제 페이지는 `frontend/src/app/member/signup/ClientPage.tsx`에 있으며 Header와 로그인 화면의 회원가입 링크는 `/member/signup`을 사용한다.

## 8. 영향과 심각도 근거

- 홈페이지의 직접 회원가입 전환 경로가 실패한다.
- Header 등 다른 회원가입 진입 경로가 있어 전체 기능이 차단되지는 않으므로 `Minor`를 제안한다.

## 9. 수정 후 재검증 조건

- 홈페이지 CTA가 `/member/signup`으로 이동하고 HTTP 200 화면이 표시되는지 확인한다.
- Header·로그인 화면·홈페이지의 모든 회원가입 링크가 같은 유효 경로를 사용하는지 확인한다.
- 로그아웃 상태에서 뒤로 가기와 직접 URL 접근도 확인한다.

## 10. 확인 한계

- 최초 등록은 사용자 제공 화면과 기준 코드 대조를 근거로 했다.
- 이후 사용자가 Playwright Known Defect 실행으로 동일 `/signup` 404를 재현했다. 제품 수정 후 Pass 재검증은 아직 수행하지 않았다.
