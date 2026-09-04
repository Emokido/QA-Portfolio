# TripFriend 댓글·입력 중심 Postman API 테스트 계획

- 문서 상태: `Executed — assertion 교정·사용자 재실행 완료`
- 기준일: 2026-09-02
- 기준 제품 커밋: `e2431223ce2a18c8f944d544cba76b951ce0356d`
- Collection: `TripFriend-Stage8.postman_collection.json`
- Environment Template: `TripFriend-Local.postman_environment.template.json`
- 실행 주체: `User`; 제한 진단: `Codex with user authorization`

## 1. 목적과 범위

| 5W1H | 내용 |
|---|---|
| Why | JUnit의 내부 규칙·경계 검증과 중복되지 않게 실제 HTTP 계약, 인증 흐름, 댓글 CRUD와 대표 결함을 확인한다. |
| What | Stage 7의 공개·인증 리뷰 흐름을 사전조건으로 사용하고 댓글 4개, 검증·Security 2개, 결함 확인 1개 요청을 추가한다. |
| Who | Codex가 승인된 Collection·assertion 초안을 작성하고, 사용자가 요청별 목적을 확인한 뒤 Postman에서 직접 실행한다. |
| When | 교정 `ReviewControllerTest` 사용자 재현 후, 수동 프론트 탐색 전에 실행한다. |
| Where | 로컬 QA 실행본의 H2 백엔드와 격리된 임시 Redis를 대상으로 하는 비공개 Postman Workspace다. |
| How | 빈 비밀값 Template, 고유 데이터, 요청 간 ID 전달과 Postman Tests assertion을 사용한다. |

> 파일명과 Collection의 `Stage8` 표기는 기존 실행 ID와의 추적성을 위한 내부 이력명이다.

## 2. 선별 원칙

- JUnit P0 49개의 모든 경계값을 Postman으로 반복하지 않는다.
- Postman은 실제 HTTP 상태·본문 코드·인증·데이터 연결을 보여주는 대표 흐름에 집중한다.
- 정상 댓글 CRUD는 하나의 생성 데이터로 연결하고 `createdReviewId`·`createdCommentId`를 전달한다.
- 현재 Open 결함은 실제 HTTP에서 확인 가치가 높은 TF-BUG-003·004·006만 이번 단위에 연결한다.
- 기대 결과를 현재 제품 동작에 맞춰 낮추지 않는다. 불일치는 Fail 후보로 기록한다.
- 제품 수정 후 재검증이 아니므로 `Defect Verification` 폴더에서 Open 결함의 실제 서버 재현·영향 확인으로 구분한다.

## 3. 사전조건과 데이터

- Java 17 기반 QA 실행본 백엔드가 `http://localhost:8080`에서 준비돼 있어야 한다.
- H2와 Stage 7에서 사용한 격리 Redis 구성을 사용하고 실제 외부 DB·운영 Redis는 사용하지 않는다.
- `02 Authenticated Review`의 로그인·리뷰 생성·저장 확인을 먼저 실행한다.
- Environment의 `createdReviewId`와 `accessToken`이 준비돼 있어야 한다.
- `testPassword`·`accessToken`은 Postman 비공개 로컬 값으로만 사용하고 공개 파일·스크린샷에 남기지 않는다.
- `stage8CommentContent`, `stage8UpdatedCommentContent`, `createdCommentId`는 실행 중 자동 저장되는 임시 값이다.

## 4. 요청과 오라클

| 순서 | 요청 ID | 목적 | 핵심 기대 결과 | 연결 |
|---|---|---|---|---|
| 1 | TF-S8-POSTMAN-001 | 인증 댓글 생성 | HTTP 201, code `201-1`, ID·내용·reviewId 일치 | TF-TC-014·TF-BUG-004 |
| 2 | TF-S8-POSTMAN-002 | 리뷰별 댓글 조회 | HTTP 200, code `200-2`, 생성 댓글 존재 | TF-TC-003 |
| 3 | TF-S8-POSTMAN-003 | 본인 댓글 수정 | HTTP 200, code `200-4`, ID·수정 내용 일치 | TF-TC-015·026 |
| 4 | TF-S8-POSTMAN-004 | 본인 댓글 삭제 | HTTP 200, code `200-5` | TF-TC-016 |
| 5 | TF-S8-POSTMAN-005 | 비로그인 댓글 생성 차단 | HTTP 401, 성공 코드 미반환 | TF-TC-023 |
| 6 | TF-S8-POSTMAN-006 | `reviewId=null` 입력 검증 | 유효한 Bearer 인증 전제, HTTP 400, JSON 오류 본문, 401·403 미발생 | TF-TC-043-B·TF-BUG-006 |
| 7 | TF-S8-POSTMAN-007 | 평점 범위 밖 오류 코드 | HTTP 400, code `400-2` | TF-TC-038·TF-BUG-003 |

TF-S8-POSTMAN-004는 응답 계약까지만 확인한다. 삭제 후 실제 목록 미존재를 별도 요청으로 추가하는 것은 이번 7개 제한 범위 밖이며, 결과 검토에서 필요성이 확인될 때만 제안한다.

## 5. 실행 순서

1. 최신 Collection과 Environment Template을 Postman에 Import한다.
2. `01 Public Review` 대표 2개 요청으로 백엔드 응답 준비 상태를 확인한다.
3. `02 Authenticated Review`에서 비로그인 차단, 로그인, 리뷰 생성, 내 리뷰 저장 확인을 순서대로 실행한다.
4. `03 Comment`의 네 요청을 생성 → 조회 → 수정 → 삭제 순서로 실행한다.
5. 댓글 삭제 후 `04 Validation & Security`의 비로그인 차단과 null 입력을 실행한다.
6. `05 Defect Verification`의 평점 오류 코드 요청을 실행한다.
7. 각 요청의 HTTP 상태와 Test Results를 기록하고, 기대 불일치를 수정하지 않는다.
8. 결과 공유 후 `testPassword`·`accessToken` 로컬 값을 지운다.

## 6. 판정과 중단 조건

- Pass: 요청이 제품 검증 지점에 도달하고 모든 근거 있는 assertion이 일치한다.
- Fail: 제품 검증 지점에 도달했지만 HTTP·본문·데이터 assertion이 기대와 다르다.
- Blocked: 서버·계정·토큰·환경·테스트 구성 때문에 제품 검증 지점에 도달하지 못한다.
- 요청 수, 변수, API 경로가 예상과 다르거나 인증·비밀값 노출 위험이 있으면 중단한다.
- 제품 코드, assertion, 운영 데이터, 외부 서비스 설정은 사용자 실행 중 변경하지 않는다.

## 7. 증거

- 대표 화면에는 Collection 요청명, 메서드·URL, HTTP 상태와 Test Results만 포함한다.
- Header·Cookie·Environment·로그인 Body·응답 토큰은 캡처하지 않는다.
- 정상 CRUD는 대표 1장, Fail은 결함별 1장을 우선하며 반복 Pass를 모두 저장하지 않는다.
- 실행 결과는 사용자 보고로 받은 뒤 실행 ID·시각·결과·증거·결함 연결을 별도 보고서와 Stage 8 요약에 기록한다.

## 8. 실행 결과와 assertion 교정

- 사용자 Collection Runner `TF-S8-POSTMAN-EXEC-001`은 13개 요청·42개 assertion을 실행해 도구 화면상 41 Passed·1 Failed·0 Errors였다.
- `TF-S8-POSTMAN-007`은 HTTP 400이지만 기대 `400-2` 대신 실제 `400-1`을 반환해 TF-BUG-003을 재현했다.
- `TF-S8-POSTMAN-006`은 로그인 후에도 HTTP 401과 빈 본문을 반환했다. 최초 assertion이 모든 4xx를 허용해 3/3 Passed로 표시했지만 입력 검증 성공을 입증하지 못하는 테스트 오탐이었다.
- 사용자 승인 제한 진단에서 Codex는 로컬 로그인 1회와 동일한 null 요청 1회를 실행했다. 유효 토큰 발급 후 `CommentController.createComment` 처리 중 서버 예외가 발생해 기존 TF-BUG-006의 실제 HTTP 경로를 확인했다.
- 요청 단위 최종 판정은 전체 13개 `11 Pass · 2 Fail · 0 Blocked`, Stage 8 신규 7개 `5 Pass · 2 Fail · 0 Blocked`다. 기존 P0 케이스의 대표 재현이므로 P0 43 Pass·6 Fail에는 중복 합산하지 않는다.
- 교정 Collection은 `accessToken` 사전조건을 검사하고, TF-S8-POSTMAN-006에 HTTP 400·401/403 미발생·JSON 오류 본문을 요구한다. 제품 동작은 변경하지 않았다.
- 사용자 `TF-S8-POSTMAN-RETEST-001`에서 최신 Collection의 로그인 사전조건을 충족한 뒤 006을 재실행했다. 실제 HTTP 401·빈 본문에 대해 HTTP 400, 401·403 미발생, JSON 오류 본문 assertion이 모두 실패해 0/3으로 결함을 정상 탐지했다.
- 제품 요청 TF-S8-POSTMAN-006의 판정은 Fail이며, assertion 교정 자체의 재검증은 Pass다. 제품 동작이나 기대값을 낮추지 않았다.
