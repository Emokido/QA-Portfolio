# TripFriend Playwright E2E

이 폴더는 위험도와 반복 비용을 기준으로 선별한 프런트 E2E를 제품 코드와 분리해 보관한다. 정상 회귀 5개와 현재 실패가 예상되는 등록 결함 재현 3개는 디렉터리와 실행 명령을 분리했다. TF-E2E-003은 작성·확인과 수정·삭제로, TF-E2E-006은 1자·101자 경계로 나뉘어 실행 단위는 총 8개다.

기존 실행 결과는 별도 QA 검증용 사본의 CORS·접근성 속성·수정 폼 교정 후 정상 회귀 `5/5 Pass`, 등록 결함 재현 `3/3 재현`이다. 정상 회귀에서는 001·002·003-A·003-B·004가 모두 Pass했다. TF-E2E-003-B는 기준 제품에서 TF-BUG-015를 발견했고 별도 QA 환경 교정 후 단일·전체 회귀에서 Pass했다. 등록 결함 재현에서는 TF-BUG-010과 TF-BUG-014의 1자·101자 경계가 모두 예상대로 검출됐다.

후속 보강에서는 정상 회귀 5개를 GitHub Actions의 필수 CI 대상으로 연결한다. 등록 결함 재현은 예상된 실패가 정상 회귀 성공률이나 정상 회귀 성공률이나 필수 CI 결과에 영향을 주지 않도록 별도 수동 또는 비차단 job 후보로 유지한다.

## 범위

- 정상 회귀: 로그인, 공개 리뷰 검색, UI 리뷰 작성·결과 확인, API로 준비한 리뷰의 UI 조회·수정·삭제, 댓글 UI 등록·표시
- 등록 결함 재현(Known Defect): TF-BUG-010 회원가입 CTA 경로, TF-BUG-014 댓글 1자·101자 길이 안내
- 보류: 댓글별로 안정적으로 식별할 수 있는 요소 구조가 없는 UI 댓글 수정·삭제, 기대 이동 정책이 미정인 TF-BUG-009 홈 검색

API 기반 테스트 데이터 준비 코드(fixture)는 사전 데이터 생성과 실패 시 데이터 정리에만 사용합니다. 핵심 동작과 assertion은 브라우저 UI에서 수행한다.

## 사전조건

[재현 조건과 공개 범위](../README.md#재현-조건과-공개-범위)를 먼저 확인한다. 아래는 기존 로컬 QA 환경의 실행 절차이며 기준 제품만 내려받아 동일 결과를 보장하는 설치 가이드는 아니다. 기존 Chrome과 Playwright 브라우저 실행 의존성(영상 기록용 FFmpeg 포함)이 준비돼 있어야 한다.

1. 비저장 Redis `6380`, H2 백엔드 `8080`, 프런트엔드 `3000`을 순서대로 실행한다.
2. 로컬 테스트 회원과 기존 여행지 ID 하나를 준비한다.
3. 이 폴더에서 의존성을 한 번 설치한다: `npm install`
4. 실제 값은 파일에 저장하지 않고 실행할 PowerShell 세션에만 입력한다.

```powershell
$env:TF_E2E_WEB_URL='http://localhost:3000'
$env:TF_E2E_API_URL='http://localhost:8080'
$env:TF_E2E_USERNAME='로컬 테스트 회원 아이디'
$env:TF_E2E_PASSWORD='로컬 테스트 회원 비밀번호'
$env:TF_E2E_PLACE_ID='실제로 존재하는 여행지 ID'
$env:TF_E2E_PLACE_NAME='위 ID와 일치하는 전체 옵션 이름(예: 경복궁 (서울))'
```

## 실행

정상 회귀만 실행:

```powershell
npm run test:regression
```

Known Defect만 실행:

```powershell
npm run test:known-defects
```

Known Defect는 제품 수정 전에는 실패가 기대된다. 정상 회귀의 성공률·집계에 포함하지 않으며, 실패를 통과시키기 위해 assertion을 완화하지 않는다. 전체 테스트 검색만 확인하려면 `npm run test:list`를 사용한다.

## 데이터 격리와 정리(cleanup)

- 리뷰·댓글 데이터에는 실행 시각과 worker 번호를 포함한 고유 문자열을 사용한다.
- TF-E2E-003-A는 리뷰를 UI로 작성한다. 나머지 리뷰 의존 시나리오는 로그인 UI에서 얻은 토큰으로 API 준비한 뒤 UI에서 검증한다.
- TF-E2E-002는 URL query 이동이 아니라 같은 `/community` 화면에서 발생하는 keyword 포함 리뷰 API 요청과 빈 결과 안내를 검증한다.
- 각 테스트는 `finally`에서 댓글과 리뷰를 정리한다. UI 삭제가 성공한 리뷰는 API로 다시 삭제하지 않는다.
- cleanup이 404가 아닌 응답으로 실패하면 오류를 숨기지 않는다. 남은 고유 제목으로 데이터를 추적할 수 있다.
- `workers: 1`로 시작해 데이터 경쟁을 줄였다. 병렬화는 격리성을 실제로 확인한 뒤 검토한다.

## 요소 선택(locator) 원칙과 한계

- `getByRole`, `getByLabel`, `getByPlaceholder`, 보이는 고유 텍스트를 우선한다.
- CSS 경로와 순번 기반 `nth()`는 사용하지 않는다.
- 실행용 프런트 리뷰 별점 버튼에는 QA 검증용으로 추가한 접근성 속성 `aria-label`과 `aria-pressed`를 추가해 `getByRole`로 선택한다.
- 댓글별 semantic container처럼 아직 안정적인 locator를 만들 수 없는 흐름은 보류했다.
- 첫 단계에서는 인증 helper와 API fixture helper만 공통화했고 Page Object Model은 도입하지 않았다.


## 검증 깊이

TF-E2E-003-B는 수정 제목의 재표시와 삭제 후 목록 복귀를 확인한다. 삭제 후 GET 404나 자원 미존재를 별도로 검증하지 않으므로 영속 삭제까지 확인했다고 확대 해석하지 않는다. [삭제 검증 보강안](../../reports/portfolio-review-correction-report.md#삭제-검증-검토--미실행-보강안)은 후속 계획이며 이번 판정 교정에서는 E2E 전체를 재실행하지 않았습니다.
