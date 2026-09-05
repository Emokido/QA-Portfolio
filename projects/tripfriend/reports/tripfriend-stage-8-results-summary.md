# TripFriend QA 최종 결과·결함·증거 요약

- 문서 상태: `Stage 8 완료 이력 + 2026-09-05 제출본 교정 반영; 로컬 검토본`
- 최초 결과 기준일: 2026-09-04
- 최신 판정 교정일: 2026-09-05
- 기준 제품 커밋: `e2431223ce2a18c8f944d544cba76b951ce0356d`
- 최신 P0 판정(2026-09-05 교정): **45 Pass · 4 Fail · 0 Blocked · 0 Not Run**
- 실제 API·프런트 통합 검증: **7 Pass · 1 Fail · 0 Blocked · 0 Not Run**
- 댓글·입력 중심 Postman 추가 검증: **5 Pass · 2 Fail · 0 Blocked**
- 수동 프런트 탐색: **사용자 관찰 결함 6건 정식 등록 · 전체 실행 케이스 분모 미확인으로 별도 Pass/Fail 집계 없음**
- Playwright E2E: **QA 사본 교정 후 정상 회귀 5/5 Pass · 등록 결함 2건의 3개 시나리오 재현**
- 실행 주체: `Codex with user authorization · User`; 사용자 IntelliJ·Postman·수동 프런트 탐색·Playwright 실행 완료

> 이 문서의 Stage 6·7·8 표기는 QA 작업 순서와 실행 ID의 추적성을 보존하기 위한 내부 이력명이다. 공개 성과는 위의 업무 목적별 결과를 기준으로 읽는다.


## 최신 교정과 집계 해석

TF-REVIEW-EXEC-001에서 댓글 Controller 경계 4개를 실행해 모두 Pass했다. TF-TC-040·041은 실제 ResponseAspect 누락에 따른 오탐이어서 Pass로 정정했고 TF-BUG-004를 Closed 처리했다. [교정 보고서](portfolio-review-correction-report.md)에 실행 근거와 한계를 기록했다.

현재 P0는 케이스별 최신 결과의 누적 집계다. 아래 Stage 8 실행 이력의 43 Pass·6 Fail과 완료 당시 결함 수는 역사 기록이다. Playwright 5/5는 CORS·접근성 속성·수정 폼 초기화 등을 교정한 QA 실행 사본에서 얻은 기존 결과이며 원본 제품 5/5를 뜻하지 않는다.

## 1. 최종 결과 정리의 목적과 범위

| 5W1H | 내용 |
|---|---|
| Why | 계층별 테스트와 실제 서버 실행 결과, 결함, 증거를 현재 판정과 일치시키고 테스트 구성 오탐을 제품 결함과 분리한다. |
| What | TF-BUG-002 연결 5개 케이스 교정, 현재 P0 집계, 기존 결함과 수동 탐색 신규 결함 6건, 대표 증거 22개와 공개 주장 경계를 정리한다. |
| Who | Codex가 승인된 테스트 구성 교정·실행·문서화를 수행하고, 사용자가 IntelliJ 대표 재현과 단계 완료를 최종 승인한다. |
| When | 2026-09-02 시작, 2026-09-04 완료 승인. |
| Where | QA 실행본의 `ReviewControllerTest`와 QA-Portfolio 공개 후보 보고서·자동화 사본에서 작업한다. |
| How | 실제 런타임 `ResponseAspect`를 포함한 Web MVC slice로 같은 기대 assertion을 실행하고, 과거 이력을 보존한 채 현재 판정을 덧붙인다. |

최종 결과를 정리하는 동안 원본 팀 저장소는 변경하지 않았다. 사용자 승인 후 별도 QA 프런트의 `review-form.tsx` 별점 버튼에 `aria-label`·`aria-pressed`를 추가했고, TF-E2E-003-B가 드러낸 수정 폼 초기화 결함을 재검증하기 위해 동적 route params 처리와 리뷰 초기 로딩 제어를 교정했다. API·백엔드 비즈니스 로직, 실제 외부 DB·운영 Redis·Docker·비밀 파일·Git은 변경하지 않았다.

### 1.1 포트폴리오에서 보여 주는 핵심 QA 판단

- 자동화 대상은 수동 Fail 전체가 아니라 비즈니스 중요도, 기능 연결성, 반복 비용, 회귀 위험, 안정적인 오라클을 기준으로 정상 회귀 5개와 Known Defect 3개 실행 변형을 선별했다.
- Playwright 원시 `failed` 표시는 곧바로 제품 Fail로 계산하지 않았다. FFmpeg·환경변수·fixture·오라클 문제는 Blocked 또는 테스트 구성 문제로 분리하고, 제품 검증 지점에 도달한 불일치만 Fail과 결함 근거로 사용했다.
- TF-BUG-015는 기준 제품 반복 Fail → 동적 route 가설 교정 후 동일 Fail로 가설 반증 → 리뷰 폼 비동기 초기화 문제 격리 → QA 실행 사본 수정 후 단일 Pass → 정상 회귀 5/5 Pass 순서로 검증했다.
- QA 실행 사본의 수정은 원인 분리와 재검증을 위한 patch다. 원본 팀 저장소와 기준 커밋은 수정하지 않았고, QA 사본 Pass를 제품 결함 Closed로 표현하지 않는다.
- 요구사항·테스트케이스·실행 ID·증거·결함·재검증을 연결하고, 정상 회귀와 Known Defect 결과를 서로 다른 분모로 관리했다.

## 2. 테스트 구성 오탐 교정

Stage 7 실제 서버 실행은 리뷰 생성 성공 응답이 HTTP 201임을 확인했다. 이에 따라 TF-BUG-002는 제품 결함이 아니라 기존 독립형 MockMvc가 `ResponseAspect`를 포함하지 않아 발생한 테스트 구성 오탐으로 확정했다.

교정한 `ReviewControllerTest`는 실제 `ReviewController`·`GlobalExceptionHandler`·`ResponseAspect`를 사용한다. `ReviewService`·`AuthService`·JPA metamodel은 Mock하고 Security 필터는 비활성화하므로, 이 결과는 Controller 입력 경계와 HTTP·본문 계약에 한정된다.

| 케이스 | 교정 전 현재 상태 | TF-S8-EXEC-002 | 교정 후 현재 상태 |
|---|---|---|---|
| TF-TC-030 제목 2자 | Blocked | HTTP 201·`201-1` 일치 | Pass |
| TF-TC-031 제목 30자 | Blocked | HTTP 201·`201-1` 일치 | Pass |
| TF-TC-034 내용 10자 | Blocked | HTTP 201·`201-1` 일치 | Pass |
| TF-TC-035 내용 2,000자 | Blocked | HTTP 201·`201-1` 일치 | Pass |
| TF-TC-037 평점 1.0·5.0 | Blocked | 두 변형 모두 HTTP 201·`201-1` 일치 | Pass |
| TF-TC-038 평점 0·6 | Fail | HTTP 400은 일치, 기대 `400-2` 대비 실제 `400-1` | Fail · TF-BUG-003 |

TF-BUG-002와 Stage 6의 원본 실행 기록은 삭제하지 않는다. 연결 5개 케이스의 당시 Fail 근거가 왜 무효였는지와 `Blocked → Pass` 전환 근거를 함께 보존한다.

## 3. Stage 8 실행 이력

| 실행 ID | 검증 지점 | 결과 | 판정 |
|---|---|---|---|
| TF-S8-PREP-001 | 도달 전 | 샌드박스 Gradle 사용자 홈 잠금 경로 오류 | 제품 결과 아님 |
| TF-S8-EXEC-001 | 도달 전 | Web MVC context에서 `DeletedMemberFilter`의 `JwtUtil` Bean 누락, 12개 context 실패 | Blocked 구성 시도; 제품 Fail 아님 |
| TF-S8-EXEC-002 | 도달 | 12 invocations, 10 Pass·2 Fail, errors 0·skipped 0 | 5 Blocked 해소; TF-TC-038만 기존 Fail 유지 |
| TF-S8-EXEC-003 | 도달 | 사용자 IntelliJ 전체 클래스 실행, 12 tests 중 10 Pass·2 Fail, 522ms | TF-S8-EXEC-002와 동일; TF-TC-038 평점 0·6만 Fail |

TF-S8-EXEC-002의 `BUILD FAILED`는 TF-TC-038 두 변형의 실제 assertion 불일치 때문에 발생했다. 개별 판정은 10 Pass·2 Fail이며, 테스트케이스 집계에서 TF-TC-038 두 변형은 한 케이스 Fail로 계산한다.

### 3.1 Postman 보강·사용자 실행

- Stage 7 Collection은 당시 실행 자산으로 보존하고 `TripFriend-Stage8.postman_collection.json`을 새로 만들었다.
- 선별 범위는 댓글 CRUD 4개, Validation·Security 2개, 결함 확인 1개로 총 7개 요청이다. Stage 7 보존 흐름 6개를 포함한 Runner 전체는 13개 요청이다.
- 사용자 `TF-S8-POSTMAN-EXEC-001`은 13개 요청·42개 assertion을 완료했다. Postman 원시 표시는 41 Passed·1 Failed·0 Errors였다.
- TF-S8-POSTMAN-007은 HTTP 400·실제 `400-1`로 기존 TF-BUG-003을 재현해 Fail이다.
- TF-S8-POSTMAN-006은 로그인 직후에도 HTTP 401·빈 본문을 반환했다. 최초 assertion이 400~499 전체를 허용해 3/3 Passed로 표시한 것은 테스트 오탐이다.
- 사용자 승인 제한 진단에서 Codex가 로컬 로그인 1회와 동일 null 요청 1회를 실행했고, 유효 토큰 발급 뒤 `CommentController.createComment` 경로의 서버 예외를 확인했다. 소스의 `reviewId` null 제약 부재와 `findById(null)` 전달, 처리되지 않은 오류 경로를 함께 대조해 기존 TF-BUG-006 재현 Fail로 판정했다.
- 요청 단위 최종 판정은 전체 13개 11 Pass·2 Fail·0 Blocked, Stage 8 신규 7개 5 Pass·2 Fail·0 Blocked다. 기존 P0 케이스 대표 재현이므로 P0 43 Pass·6 Fail에는 중복 합산하지 않는다.
- 006 Collection assertion은 빈 accessToken 사전 중단, HTTP 400, 401·403 미발생, JSON 오류 본문으로 교정했다. 사용자 TF-S8-POSTMAN-RETEST-001에서 HTTP 401·빈 본문을 0/3 Failed로 정상 탐지해 assertion 교정 재검증은 Pass했다.

### 3.2 수동 프런트 탐색·결함 정제

- 사용자가 Windows 11·Chrome·Frontend localhost:3000·Backend localhost:8080 환경에서 자유 탐색을 수행하고 내부 원본에 6건을 기록했다.
- Codex는 2026-09-03 공개 페이지의 재현 절차·Expected·Actual·스크린샷·일부 Console 내용을 읽기 전용으로 검토하고 기준 코드와 대조했다. 제품을 재실행하지 않았다.
- 원본 `BUG-001~006`은 기존 공식 결함 번호와 충돌하므로 발견 당시 번호로만 보존하고, 공식 결함 `TF-BUG-009~014`로 등록했다.
- 확인된 범위는 홈페이지 검색 무응답, 회원가입 CTA 잘못된 경로, 초장문 모집 검색 오류, 모집 인원 필터 검증 부재, 리뷰 최초 조회수 중복 증가, 댓글 길이 오류 안내 손실이다.
- 사전에 번호가 부여된 전체 수동 시나리오와 총 실행 수가 확인되지 않아 `6 Fail`이나 Pass 비율을 만들지 않는다. P0 49개 및 Stage 7·Postman 집계에도 중복 합산하지 않는다.
- 상세 판정과 자동화 후보는 [수동 프런트 탐색 보고서](manual-frontend-exploration-report.md)에 기록했다.

### 3.3 Playwright E2E 준비

- 자동화 선정 기준을 비즈니스 중요도·기능 연결성·반복 비용·회귀 위험·자동화 안정성으로 명시했다.
- 정상 회귀 5개 실행 케이스와 Known Defect 3개 실행 변형을 별도 디렉터리·명령으로 구현했다. 댓글은 CRUD 전체가 아니라 등록·표시만 정상 회귀 범위다.
- 리뷰·댓글 시나리오는 고유 데이터를 API로 준비하고 `finally` cleanup을 수행한다. 핵심 검증은 UI에서 수행한다.
- 사용자 관점 locator를 우선하고 CSS·순번 locator는 사용하지 않았다. 승인된 별점 접근성 속성으로 UI 리뷰 작성·결과 확인을 추가했으며, UI 댓글 수정·삭제는 현재 semantic 구조로 안정적인 locator를 만들 수 없어 보류했다.
- TF-BUG-009는 최초 Known Defect 후보였지만 성공 동작 정책이 미정이라 제외하고 명확한 목적지 오라클이 있는 TF-BUG-010을 대체 대상으로 선택했다. 무응답을 성공으로 assertion하거나 임의 목적지를 요구하지 않는다.
- TF-E2E-006은 1자·101자 경계를 각각 실행하고 두 변형 모두 2~100자 범위의 구체적 안내를 기대한다.
- `loginThroughUi`가 로그인 뒤 `localStorage.accessToken` 존재를 assertion하고 TF-E2E-001도 반환 토큰을 명시적으로 확인한다.
- `npm install`은 의존성 3개 설치·취약점 0건으로 완료했다. UI 작성 분리 후 `npm run test:list`에서 정상 회귀 5개·Known Defect 3개, 총 8개 실행 변형을 확인했다.
- Playwright 실제 실행 전 준비 단계까지 완료한 뒤 사용자와 Codex가 승인 범위에서 정상 회귀 및 단일 케이스를 실행했다. 결과는 아래 3.4절에 별도로 기록하며 기존 P0·Stage 7·Postman 집계에 합산하지 않는다. Known Defect 결과도 정상 회귀 성공률과 분리한다.

### 3.4 Playwright 사용자 실행과 오라클 교정

| 실행 ID | 사용자 명령 | Playwright 원시 표시 | QA 판정 | 근거 |
|---|---|---|---|---|
| TF-S8-PW-EXEC-001 | `npm run test:regression` | 5 failed | 0 Pass·0 Fail·5 Blocked | `browserContext.newPage` 전에 Playwright FFmpeg 실행 파일이 없어 제품 검증 미도달 |
| TF-S8-PW-EXEC-002 | `npm run test:regression` | 5 failed | 0 Pass·0 Fail·5 Blocked | 001·003-A·003-B·004는 필수 계정 환경변수 누락, 002는 실제 상태 기반 검색과 다른 URL 오라클 사용 |
| TF-S8-PW-EXEC-003 | `npm run test:regression` | 4 passed·1 failed | 4 Pass·0 Fail·1 Blocked | 001·002·003-B·004 Pass; 003-A는 환경변수 `경복궁`과 실제 옵션 `경복궁 (서울)` 불일치로 선택 전 timeout |
| TF-S8-PW-EXEC-004 | `npm run test:regression` | 4 passed·1 failed | 4 Pass·0 Fail·1 Blocked | 001·002·003-A·004 Pass; 003-B는 수정 폼에서 여행지·평점 필수 검증에 걸려 PUT 전 중단, 원인은 후속 실행에서 판별 |
| TF-S8-PW-EXEC-005 | `npx playwright test … -g 'TF-E2E-003-B'` | 1 failed | 0 Pass·1 Fail·0 Blocked | 초기값 대기 assertion에서도 GET 200 이후 제목 입력값이 7초 동안 비어 있어 제품 검증 지점의 수정 폼 초기화 결함 확인 |
| TF-S8-PW-EXEC-006 | 동일 단일 케이스, Codex 시도 1 | 1 failed | 0 Pass·1 Fail·0 Blocked | 사용자 결과와 동일하게 빈 제목을 재현해 우발적 실행 실패 배제 |
| TF-S8-PW-EXEC-007 | 동일 단일 케이스, Codex 시도 2 | 1 failed | 0 Pass·1 Fail·0 Blocked | QA 실행 사본의 동적 route params 비동기 처리를 교정했으나 빈 제목 지속; 직접 원인이 아님을 확인 |
| TF-S8-PW-RETEST-001 | 동일 단일 케이스, Codex 시도 3 | 1 passed | 1 Pass·0 Fail·0 Blocked | `ReviewForm`이 상세 데이터 로딩 완료 후 폼을 렌더하도록 교정한 QA 실행 사본에서 조회·수정 PUT 200·삭제·cleanup까지 4.2초에 Pass |
| TF-S8-PW-EXEC-008 | `npm run test:regression` | 5 passed | 5 Pass·0 Fail·0 Blocked | 사용자 수정 후 전체 회귀 실행; 001·002·003-A·003-B·004 모두 Pass, 총 14.3초 |
| TF-S8-PW-KD-EXEC-001 | `npm run test:known-defects` | 3 failed | 0 Pass·3 Fail·0 Blocked | 005는 `/signup` 404로 TF-BUG-010 재현, 006 1자·101자는 HTTP 400 뒤 일반 오류 문구로 TF-BUG-014 재현; 101자는 dialog 처리 지연으로 timeout·cleanup 실패도 동반 |
| TF-S8-PW-KD-RETEST-001 | 101자 변형만 단일 실행 | 1 failed | 0 Pass·1 Fail·0 Blocked | dialog 즉시 처리 교정 후 4.3초에 일반 오류 문구를 명확히 검출; review ID 36 cleanup DELETE 200 |

- 두 번째 실행에서 screenshot·video·trace가 생성돼 FFmpeg 차단은 해소된 것으로 확인했다. 설치 명령 자체의 출력은 제공되지 않아 실행 세부사항을 추정하지 않는다.
- TF-E2E-002의 제품 구현은 검색어를 `activeSearchQuery` 상태에 반영하고 같은 `/community` 화면에서 `/api/reviews?…&keyword=…`를 다시 호출한다.
- 기존 URL 변경 assertion은 테스트 구성 오탐이므로 제품 Fail로 판정하지 않았다. 교정 테스트는 keyword가 일치하는 API GET의 HTTP 200, `/community` 유지, 빈 결과 안내를 확인한다.
- 세 번째 실행에서는 `TF_E2E_PLACE_NAME`을 실제 접근 가능한 옵션 이름 `경복궁 (서울)`과 다르게 지정해 TF-E2E-003-A만 Blocked됐고, 값을 바로잡은 네 번째 실행에서 Pass했다.
- 네 번째 실행에서는 PUT이 전송되지 않아 우선 동기화 가능성을 의심했다. 그러나 초기 제목·내용·여행지·평점 대기 assertion을 추가한 TF-S8-PW-EXEC-005·006에서도 GET 200 이후 제목이 계속 빈 값이어서 Blocked가 아니라 제품 검증 지점에 도달한 Fail로 재분류했다.
- QA 실행 사본의 Next 동적 route params를 비동기로 교정한 시도 2에서도 같은 Fail이 지속됐다. 시도 3에서 `ReviewForm`이 상세 데이터 로딩을 마친 뒤 폼을 렌더하고 함수형 상태 갱신을 사용하도록 수정하자 TF-E2E-003-B의 조회·초기값·수정 PUT 200·삭제·cleanup이 모두 Pass했다.
- TF-S8-PW-EXEC-008에서 수정 후 정상 회귀 5개 전체가 14.3초에 Pass했다. 현재 정상 회귀 판정은 5 Pass·0 Fail·0 Blocked다.
- TF-S8-PW-KD-EXEC-001에서 TF-E2E-005는 CTA가 `/signup` 404로 이동해 TF-BUG-010을 재현했고, TF-E2E-006 1자는 기대 `2~100자` 대신 일반 오류 문구를 표시해 TF-BUG-014를 재현했다.
- TF-E2E-006 101자는 trace에서 댓글 POST 400과 기대와 다른 일반 오류 dialog를 확인해 제품 검증 지점의 Fail로 판정했다. dialog를 발생 즉시 dismiss하면서 메시지를 보존하도록 handler를 교정한 TF-S8-PW-KD-RETEST-001에서는 timeout 없이 4.3초에 같은 Fail을 재현했고 review ID 36 cleanup도 DELETE 200으로 완료했다.
- 최초 timeout에서 남은 review ID 35는 제목 `e2e-validation-…`과 작성자 `user1`을 읽기 전용으로 확인한 뒤 승인된 조건부 cleanup으로 DELETE 200, 후속 GET 404를 확인했다.
- 현재 Known Defect 실행 변형 집계는 0 Pass·3 Fail·0 Blocked다. TF-BUG-010과 TF-BUG-014가 재현됐으며 정상 회귀 성공률과 합산하지 않는다.
- TF-E2E-003-B가 발견한 수정 폼 초기화 문제는 재현·가설 반증·QA 실행 사본 수정·단일 및 전체 회귀 Pass 근거로 TF-BUG-015에 등록했다. 원본 제품 반영은 수행하지 않았다.
- 사용자 제공 터미널 출력과 생성된 `test-results` 경로를 근거로 판정했으며 이번 범위에서 별도 공개 evidence 파일로 복제하지 않았다.

## 4. 현재 P0 판정

- Pass 45개: `TF-TC-001~007`, `009~026`, `028~037`, `039~042`, `044~049`.
- Fail 4개: `TF-TC-008`, `027`, `038`, `043`.
- Blocked 0개.
- Not Run 0개.

## 5. 현재 결함 상태

| 결함 | 현재 상태 | 핵심 연결 |
|---|---|---|
| TF-BUG-001 | Open | TF-TC-008 |
| TF-BUG-002 | Closed — Not a Product Defect / Test Setup False Positive | TF-TC-030·031·034·035·037의 과거 오탐 기록 보존 |
| TF-BUG-003 | Open | TF-TC-038 |
| TF-BUG-004 | Closed — Test Setup False Positive | TF-TC-040·041, TF-REVIEW-EXEC-001에서 Pass |
| TF-BUG-005 | Open | TF-TC-027 |
| TF-BUG-006 | Open | TF-TC-043 |
| TF-BUG-007 | Open; QA 실행 사본 재검증 Pass | Stage 7 CORS 흐름 |
| TF-BUG-008 | Open | Stage 7 소수 평점 표시·입력 계약 |
| TF-BUG-009 | Open | 홈페이지 검색 submit 무응답·구현 미완성 |
| TF-BUG-010 | Open | 홈페이지 하단 회원가입 CTA `/signup` 404 · TF-E2E-005 재현 |
| TF-BUG-011 | Open | 초장문 동행 모집 검색의 통제되지 않은 조회 오류 |
| TF-BUG-012 | Open; product policy confirmation required | 모집 인원 필터의 값·관계 검증 부재 |
| TF-BUG-013 | Open; environment-limited observation, follow-up deferred | 리뷰 첫 상세 조회수 +2 사용자 관찰 · 자동화·대표 성과 제외 |
| TF-BUG-014 | Open | 댓글 길이 오류의 구체적 안내 손실 · TF-E2E-006 1자·101자 재현 |
| TF-BUG-015 | Open; QA execution patch retest passed | 리뷰 수정 폼 기존 값 비동기 초기화 실패 |

현재 Open 결함·관찰 기록은 TF-BUG-001·003·005~015 총 13건이다. TF-BUG-002·004는 테스트 구성 오탐으로 Closed했다. 이 중 TF-BUG-012는 제품 인원 정책 확인이 필요하고, TF-BUG-013은 환경 제한 관찰로 후속 재검증을 보류해 자동화 성공률과 대표 결함 성과에서 제외한다. TF-BUG-015는 QA 실행 사본 재검증만 Pass했으며 원본 제품에는 반영하지 않았다.

## 6. 증거와 주장 경계

- 대표 사용자 실행 PNG는 22개(Stage 6 11개·Stage 7 6개·Stage 8 5개)이며 파일 메타데이터와 연결 문서가 존재한다.
- Stage 8 사용자 증거 `TF-S8-EXEC-003-review-controller-user-run.png`는 109,409바이트, SHA-256 `F31B4A6B9BA2B05C4C1DBBF7B42F8E6C7F161F7B10C9BF5D2294842B14B969B4`다.
- 화면에서 `ReviewControllerTest` 12개 중 10 Pass·2 Fail, 522ms와 TF-TC-038 평점 0·6 두 실패를 확인했다. 실행 시각·프로젝트 절대경로는 화면에 없으므로 임의로 기록하지 않는다.
- Postman 증거는 Runner 요약, TF-S8-POSTMAN-007의 `400-1` 본문, TF-S8-POSTMAN-006의 401·빈 본문과 잘못된 3/3 Passed 화면 3개다. 비밀번호·토큰 값은 포함하지 않는다.
- `TF-S8-POSTMAN-EXEC-001-run-summary-user.png`: 130,193바이트, SHA-256 `A01B2B211E0A3F3AA4EB8321448AC528D5DA824381F73E97AA24C9FFFC08E873`.
- `TF-S8-POSTMAN-007-rating-error-code-user.png`: 20,985바이트, SHA-256 `B9AB430072764421E193FED87C1E5DF5BCE7E49BC572701E1F926833E00DC05F`.
- `TF-S8-POSTMAN-006-null-review-id-user.png`: 73,112바이트, SHA-256 `89727D304CF27C1D4DCF54596F6C11E499FA9CCF89EE7E6A6FBB27F207AB7296`.
- `TF-S8-POSTMAN-RETEST-001-null-review-id-assertion-user.png`: 78,792바이트, SHA-256 `BBD29B7B4B52603E837F22DD62D2B6775281C4D2AE478EE63C6228C87C3AB5B8`.
- 수동 탐색 6건의 화면 증거는 사용자 내부 원본에서 확인했다. 원본에는 Severity·Priority 미기입, 초안 문구와 오탈자가 남아 있어 공개 범위에서 제외하고 정제된 결함 보고서와 수동 탐색 보고서를 공식 산출물로 사용한다.
- TF-BUG-011의 최대 길이, TF-BUG-012의 모집 인원 정책, TF-BUG-013의 중복 방지 단위는 임의로 확정하지 않는다.
- 대표 증거 22개의 파일 존재·이미지 가독성·문서 참조·상대 링크를 확인했고 깨진 상대 링크는 0개였다. 19개는 현재 공개 후보로 유지한다.
- Stage 7 브라우저 증거 3개(`TF-S7-EXEC-003`, `TF-S7-EXEC-007`, `TF-S7-RETEST-001`)는 DevTools Initiator에 로컬 사용자 경로가 보여 원본은 보존하되 현재 공개 대상에서 제외한다. 해당 이미지 없이 공개했으며 추가 이미지는 제출 필수 조건이 아니다.
- 따라서 P0 49건 전체 정상, 전체 애플리케이션 정상, 실제 외부 DB·Redis·Security 전체 정상이라고 주장하지 않는다.
- 현재 근거 있는 표현은 `P0 49개 케이스의 최신 누적 판정은 45 Pass·4 Fail이며, 테스트 구성 때문에 남은 Blocked는 없다`이다.

## 7. Stage 8 완료 당시 판단과 후속 계획

1. TF-BUG-013은 추가 재검증 없이 환경 제한 관찰·후속 보류로 유지하고 Stage 8 blocker, 자동화 성공률, 대표 결함 성과에서 제외했다.
2. 대표 증거 22개와 수동 탐색 원본의 공개 적합성을 검토하고 공개 제외 자료와 공식 산출물 경계를 정했다.
3. Open 14건·Closed 오탐 1건의 테스트·실행·증거·재검증 연결을 대조했다.
4. 사용자 완료 승인에 따라 Stage 8을 완료로 전환했다.
5. 다음 진행 대상은 `Stage 8A — TripFriend Playwright GitHub Actions CI 보강`이다. 정상 회귀 5개를 필수 CI 대상으로 삼고 Known Defect는 별도 비차단 실행으로 분리한다. 워크플로 설계·실행·Git 반영은 별도 승인 후 진행한다.
6. Public 제출본에서는 로컬 경로 노출 증거 3개를 제외했다. TF-BUG-015 추가 이미지와 원본 trace 공개는 이번 범위에 포함하지 않는다.

상세 Stage 6 실행 역사는 [P0 백엔드 테스트 실행 보고서](p0-backend-test-execution-report.md), Stage 7 실행은 [API·프런트 흐름 실행 보고서](api-frontend-flow-execution-report.md), 개별 결함은 [결함 폴더](../defects/)를 사용한다.
