# TripFriend API·프런트 흐름 실행 보고서

- 문서 상태: `Stage 7 Complete`
- 기준일: 2026-09-01
- 완료 승인일: 2026-09-01
- 현재 실행 결과: `7 Pass · 1 Fail · 0 Not Run · 0 Product-test Blocked`
- Stage 6 결과와 별도 집계한다.

## 1. 현재 범위

| 실행 ID | 연결 케이스 | 계층 | 상태 | 증거 | 비고 |
|---|---|---|---|---|---|
| TF-S7-EXEC-001 | TF-TC-001 | Postman/API | Pass | `evidence/stage-07/TF-S7-EXEC-001-postman-public-review-list-pass.png` | Desktop App, HTTP 200·4/4 Passed |
| TF-S7-EXEC-002 | TF-TC-049 | Postman/API | Pass | `evidence/stage-07/TF-S7-EXEC-002-postman-no-result-pass.png` | Desktop App, HTTP 200·4/4 Passed |
| TF-S7-EXEC-003 | TF-TC-001·049 | Browser/API/UI | Fail | 내부 원본 보존·공개 제외(로컬 경로 노출) | Review CORS error·preflight 403·Place 403·빈 결과 UI |
| TF-S7-RETEST-001 | TF-BUG-007 | Browser/API/UI Retest | Pass | 내부 원본 보존·공개 제외(로컬 경로 노출) | QA execution patch 후 Place·Review 200, 리뷰 목록·빈 검색 결과 정상 표시, CORS 오류 없음 |
| TF-S7-EXEC-004 | TF-TC-020 | Postman/API Security | Pass | 사용자 보고, 공식 증거 미보존 | Desktop App, HTTP 401·2/2 Passed |
| TF-S7-EXEC-005 | TF-TC-004·TF-BUG-002 | Postman/API Authenticated Create | Pass | `evidence/stage-07/TF-S7-EXEC-005-postman-authenticated-review-create-pass.png` | HTTP 201·code `201-1`·생성 ID/제목, 4/4 Passed |
| TF-S7-EXEC-006 | TF-TC-004 | Postman/API Authenticated Read | Pass | 사용자 보고, 별도 증거 미보존 | HTTP 200·code `200-7`·생성 ID/제목 존재, 4/4 Passed |
| TF-S7-EXEC-007 | TF-TC-004·TF-BUG-002·008 | Browser/API/UI Authenticated Flow | Pass | 내부 원본 보존·공개 제외(로컬 경로 노출) | 로그인·작성·목록 반영, POST `/api/reviews` HTTP 201·후속 목록 GET 200 |

## 2. 준비된 실행 자산

- `api/postman/TripFriend-Stage7.postman_collection.json`
- `api/postman/TripFriend-Local.postman_environment.template.json`
- `api/postman/README.md`
- `test-plan/api-frontend-flow-test-plan.md`

Collection은 TF-TC-001과 TF-TC-049 요청 및 각 4개 assertion을 포함한다. 사용자가 비공개 Postman Workspace로 가져와 직접 실행한다.

## 3. 환경 준비 결과

| 준비 ID | 작업 | 결과 | 판정 경계 |
|---|---|---|---|
| TF-S7-PREP-001 | `npm ci` 1회 | 성공, 396 packages 설치 | 제품 기능 Pass가 아님 |
| TF-S7-PREP-002 | `npm run build` 1회 | 성공, `/community` 포함 28개 정적 페이지 생성 | 설정상 type validation·lint 생략 |
| TF-S7-PREP-003 | 격리 H2 백엔드 기동 | 두 번째 허용 시도 성공, Java 17·8080·리뷰 18개·댓글 28개 | 첫 시도는 샌드박스의 `C:\.gradle` lock 경로 생성 실패 |
| TF-S7-PREP-004 | curl 공개 리뷰 목록 | HTTP 200, JSON, code `200-5`, data 18개 | 사용자 Postman 실행 전이므로 TF-S7-EXEC-001은 Not Run 유지 |
| TF-S7-PREP-005 | curl 미존재 키워드 | HTTP 200, JSON, code `200-5`, 빈 data 배열 | 사용자 Postman 실행 전이므로 TF-S7-EXEC-002는 Not Run 유지 |
| TF-S7-PREP-006 | Next.js dev 서버 | `http://localhost:3000`, Ready | 브라우저 흐름은 Not Run |
| TF-S7-PREP-008 | 별도 비저장 Redis 6379 첫 기동 | 실패, TCP bind 단계에서 즉시 종료 | 제품 요청 전 환경 준비 실패 |
| TF-S7-PREP-009 | 동일 Redis 마지막 허용 기동 | 실패, 동일 TCP bind 오류 | 허용 2회 소진 후 읽기 전용 진단 |
| TF-S7-PREP-010 | QA 실행 Redis 포트 6380 격리 | 성공, `application-stage7.yml` 한 줄 변경 | 기존 6379 서비스 미사용·미변경 |
| TF-S7-PREP-011 | 비저장 Redis 6380 기동·PING 1회 | 성공, 127.0.0.1:6380 Ready·`PONG` | RDB save·AOF 비활성, PID 1772 |
| TF-S7-PREP-012 | Java 17·stage7 백엔드 재기동 1회 | 애플리케이션 시작 전 실패 | 기본 `application.yml`의 필수 `application-secret.yml` import 누락; 비밀 파일 미열람 |
| TF-S7-PREP-013 | optional import 명령행 교정 재시도 1회 | 동일 Config Data 단계 실패 | 명령행 옵션이 기본 파일의 필수 import를 덮어쓰지 못함; 8080 미기동 |
| TF-S7-PREP-014 | QA 실행 사본 import 수정·Java 17 재기동 1회 | 성공, stage7·H2·Redis 6380·8080 Ready | import 한 줄만 optional로 변경; 원본·감사본·비밀 파일 미열람·미변경 |
| TF-S7-PREP-015 | 공개 리뷰 readiness curl 1회 | HTTP 200, code `200-5`, data 18개 | 인증·쓰기 없이 성공 조건 확인; 추가 요청 없음 |
| TF-S7-PREP-016 | QA 실행 JWT 대체 키 교정·Java 17 재기동 | 최대 3회 중 1차 성공, 528-bit·8080 Ready | 기존 448-bit HS512 부적합 해소; 2·3차 미사용 |
| TF-S7-PREP-017 | 교정 후 공개 readiness GET 1회 | HTTP 200, code `200-5`, data 18개 | 인증·쓰기 없이 Ready 확인 |

`npm ci` 감사 요약은 15 vulnerabilities(2 low·2 moderate·9 high·2 critical)를 표시했다. 승인 범위 밖인 `npm audit fix`·의존성 업그레이드는 수행하지 않았다.

## 4. 사용자 Postman 실행 결과

| 실행 ID | 실행 주체·도구 | HTTP | Tests | 판정 |
|---|---|---:|---:|---|
| TF-S7-EXEC-001 | 사용자 · Postman Desktop App | 200 | 4/4 Passed | Pass |
| TF-S7-EXEC-002 | 사용자 · Postman Desktop App | 200 | 4/4 Passed | Pass |
| TF-S7-EXEC-004 | 사용자 · Postman Desktop App | 401 | 2/2 Passed | Pass |
| TF-S7-EXEC-005 | 사용자 · Postman Desktop App | 201 | 4/4 Passed | Pass |
| TF-S7-EXEC-006 | 사용자 · Postman Desktop App | 200 | 4/4 Passed | Pass |

- TF-S7-EXEC-001 증거는 요청명, `GET {{baseUrl}}/api/reviews?sort=newest&page=0`, HTTP 200과 `HTTP·JSON·code 200-5·1개 이상 data 배열` assertion 4개 Passed를 보여 준다.
- TF-S7-EXEC-002 증거는 요청명, 미존재 keyword, HTTP 200과 `HTTP·JSON·code 200-5·빈 data 배열` assertion 4개 Passed를 보여 준다.
- 증거 화면에 Body 원문은 표시되지 않지만, body의 `code`와 `data` 구조를 직접 파싱하는 assertion 결과가 보이고 동일 서버에 대한 준비용 curl 본문도 확인했으므로 판정 근거로 충분하다.
- Web의 Cloud Agent 오류는 로컬 `localhost:8080`에 접근하지 못하는 실행 Agent 제약이며 제품 Fail이나 최종 Blocked로 집계하지 않는다. Desktop App 실행이 대체 경로로 성공했다.
- 증거 1: 73,293바이트, SHA-256 `BFCBACE57F7027F984F24D15F25842BFECF72FF8DC69CC23DFDF620B794A8C31`.
- 증거 2: 74,360바이트, SHA-256 `F5E509A798061F2B53450CC7FABDA847F58F427D6E41A26D37B1970AD9A16A6B`.
- TF-S7-EXEC-005 증거는 요청명, `POST {{baseUrl}}/api/reviews`, HTTP 201과 `HTTP·JSON·code 201-1·생성 ID/제목` assertion 4개 Passed를 보여 준다. 요청 Body에는 로컬 테스트 데이터만 있고 토큰·비밀번호·Authorization·Cookie 값은 보이지 않는다.
- TF-S7-EXEC-005 증거: 80,959바이트, SHA-256 `34F08B83ACD8BB902D35AB24EF7B227E195943E8FB9FD9DD04B369B106B91B24`.

## 5. 정적 관찰과 판정 경계

- 목록 API의 계약은 HTTP 200·code `200-5`·`data` 배열이다.
- Stage 7 H2 기동 시 `BaseInitData`가 예시 리뷰를 만들므로 TF-TC-001은 비어 있지 않은 배열을 기대한다.
- TF-TC-049는 고유한 미존재 키워드로 빈 배열을 기대한다.
- 프런트가 전달하는 `page`는 현재 백엔드에서 소비하지 않는다.
- CORS 설정과 프런트 오류의 빈 목록 변환은 실제 브라우저에서 반드시 Network·Console과 함께 확인한다.

## 6. CORS 수정 후 재검증 결과

- 환경 준비·curl 계약 확인과 사용자 Postman 실행 2건은 완료했다.
- 원본 브라우저 흐름은 CORS preflight 403으로 Fail했고 TF-BUG-007을 등록했다.
- QA 실행 작업본의 CORS Origin 목록을 최소 수정하고 `compileJava`·백엔드 재기동에 성공했다.
- 사용자가 Chrome에서 강력 새로고침한 뒤 `/place`와 `/api/reviews?sort=newest&page=1`의 HTTP 200, 리뷰 카드 표시를 확인했다.
- 미존재 키워드 `stage7_no_result_20260901` 검색 요청도 HTTP 200으로 완료됐고 화면은 정상적으로 `검색 결과가 없습니다.`를 표시했다.
- 사용자는 Console에 CORS 오류가 없음을 확인했다. 저장 증거는 목록·Network 200이 함께 보이는 수정 후 화면 1개이며, 검색 후 화면은 채팅에서 판정에 사용했지만 승인된 추가 증거 한도에 따라 별도 파일로 복제하지 않았다.
- 수정 후 증거: 193,158바이트, SHA-256 `50B0A56347BA73F8490EBEA285C626B8CB6396638D9CDDAF25E98D67BD47B0AE`.
- TF-S7-RETEST-001은 `Pass`다. TF-S7-EXEC-003의 원본 `Fail`은 유지하며, TF-BUG-007도 원본 제품 수정 전까지 `Open`으로 유지한다.

## 7. 현재 제외·다음 경계

- 외부 서비스·실계정·비밀값·제품 수정은 현재 범위에서 제외한다. 인증·쓰기는 로컬 fixture와 격리 Redis/H2를 사용하는 두 번째 단위에만 제한한다.
- Stage 7의 첫 API·프런트 검증 단위는 완료했다.
- 두 번째 단위의 Collection·Environment Template·README·계획 확장은 완료했지만 실행은 시작하지 않았다.
- Windows `Redis` 서비스가 이미 6379에서 자동 시작·디스크 저장 설정으로 실행 중이고 Stage 7 설정에는 별도 DB 번호가 없다. 기존 데이터 충돌·토큰 저장 위험 때문에 이 서비스를 사용하지 않았다.
- 프런트 3000은 계속 리스닝 중이고, Java 17·stage7 백엔드는 QA 실행 사본의 H2·Redis 6380 설정으로 8080에서 Ready 상태다. 두 번째 단위 제품 요청은 아직 보내지 않았다.
- QA 실행 설정은 6380으로 바뀌었고 비저장 Redis는 6380에서 `PONG` 상태다. 기존 6379 서비스는 사용·변경하지 않았다.
- 앞선 Java 17 재기동과 명령행 optional import 교정 재시도는 필수 secret import 때문에 제품 시작 전 종료됐다. 이후 사용자 승인으로 QA 실행 사본 `application.yml`의 해당 한 줄만 optional로 변경했다.
- 최종 허용 재기동은 Java 17·stage7·H2·Redis 6380으로 성공했다. 8080 수신 PID는 38760이며 공개 readiness curl 1회는 HTTP 200·code `200-5`·data 18개였다.
- 원본·감사본과 `application-secret.yml`은 읽거나 변경·복원하지 않았다. 기존 Redis 6379 서비스도 사용·변경하지 않았다.
- 정확한 중단 지점은 TF-S7-EXEC-004 Pass 후 로그인 토큰 생성이 QA JWT 키 길이 오류로 중단된 지점이다. TF-S7-EXEC-005·006은 Blocked, 007은 Not Run이며 설정 교정 전 추가 사용자 실행을 중단한다.

## 8. 인증 리뷰 사용자 실행·JWT 환경 Blocked

- 사용자 Postman Desktop App 결과에서 TF-S7-EXEC-004는 HTTP 401·2/2 Passed로 Pass했다.
- 로그인 준비 요청은 로컬 fixture `user1` 조회와 비밀번호 검증을 통과했지만 `JwtUtil.generateToken()`의 HS512 서명 단계에서 `WeakKeyException`이 발생했다.
- QA 실행 전용 `application-stage7.yml`의 로컬 JWT 대체 키는 448-bit이고 HS512가 요구하는 최소 길이는 512-bit다. 이는 제품 계약 불일치가 아니라 QA 실행 설정 오류다.
- 로그인 응답은 HTTP 401·빈 Body가 되어 Postman Tests의 JSON 파싱도 함께 실패했다. 이 `JSONError`는 원인이 아니라 빈 오류 응답에 따른 2차 현상이다.
- `accessToken`이 저장되지 않아 TF-S7-EXEC-005·006은 동일 401 흐름으로 제품 검증 지점에 도달하지 못했다. 두 건은 Fail이 아니라 Blocked다.
- TF-S7-EXEC-007 브라우저 흐름은 시작하지 않아 Not Run이다. 누적은 4 Pass·1 Fail·1 Not Run·2 product-test Blocked다.
- 사용자가 제공한 진단 화면에는 로컬 테스트 비밀번호가 표시되어 공식 증거로 복제하지 않는다.
- 다음 안전 해소안은 QA 실행 사본 `application-stage7.yml`의 JWT 대체 키 한 줄만 512-bit 이상 로컬 전용 값으로 바꾸고 백엔드를 정상 종료·1회 재기동하는 것이다.

## 9. JWT 환경 교정 결과

- QA 실행 사본 `application-stage7.yml`의 로컬 JWT 대체 키 한 줄만 448-bit에서 528-bit로 교정했다.
- 현재 Java 17·stage7 백엔드를 정상 종료한 뒤 승인된 최대 3회 중 1차 재기동에서 성공했다. 2·3차 재기동은 사용하지 않았다.
- Redis 6380과 프런트 3000은 재기동하지 않았고 기존 Redis 6379도 사용·변경하지 않았다.
- 교정 후 공개 readiness GET 1회는 HTTP 200·code `200-5`·data 18개였다.
- TF-S7-EXEC-005·006의 기존 Blocked 판정은 사용자 재검증 전까지 유지한다. 다음 시작점은 Postman 로그인 준비 요청부터의 재실행이다.

## 10. 인증 API 재검증·TF-BUG-002 재분류

- 사용자 보고 기준 로그인 준비 요청은 HTTP 200·4/4 Passed, TF-S7-EXEC-005는 HTTP 201·4/4 Passed, TF-S7-EXEC-006은 HTTP 200·4/4 Passed다.
- TF-S7-EXEC-005는 code `201-1`과 생성 ID·제목을 반환했고, TF-S7-EXEC-006은 code `200-7`과 동일 생성 ID·제목을 확인했다. 이전 2 Blocked는 모두 해소됐다.
- 실제 서버의 `ResponseAspect`는 `RsData.getStatusCode()`를 HTTP 응답 상태에 반영한다. 기존 독립형 MockMvc는 `standaloneSetup()`에 이 Aspect를 포함하지 않아 기본 HTTP 200을 관찰했다.
- TF-BUG-002는 제품 수정 없이 `Closed — Not a Product Defect / Test Setup False Positive`로 재분류했다. 역사적 MockMvc 실패 증거는 삭제하지 않는다.
- Stage 7 누적은 7 Pass·1 Fail·0 Not Run·0 product-test Blocked다.

## 11. 인증 브라우저 흐름 Pass·평점 정책 불일치

- 사용자가 Chrome에서 로그인 상태로 리뷰를 작성했고 커뮤니티 목록의 생성 카드 반영을 확인했다.
- DevTools Network에서 `POST http://localhost:8080/api/reviews` HTTP 201과 후속 리뷰 목록 GET HTTP 200을 확인했다. 따라서 TF-S7-EXEC-007은 `Pass`다.
- 별점 작성 UI는 1~5의 정수 버튼만 제공한다. 반면 TF-S7-EXEC-005는 `rating: 4.5`를 API가 HTTP 201로 허용·저장함을 확인했다.
- 목록·상세의 `i < rating` 렌더링은 4.5를 5개의 채운 별로 보여 준다. 명시된 평점 증분 정책은 없으므로 “0.5점 입력 미지원”만을 요구사항 위반으로 확정하지 않고, 입력·API·표시 계층의 불일치와 오표시를 `TF-BUG-008 Open · Minor`로 등록했다.
- 최초 사용자 제공 브라우저 화면에는 응답 헤더의 `JSESSIONID`가 보여 공식 증거로 복제하지 않았다. 사용자가 헤더 값을 접은 안전한 화면을 다시 제공했고, 생성 카드와 POST 요청의 HTTP 201을 함께 확인해 공식 증거로 보존했다.
- TF-S7-EXEC-007 증거: 201,524바이트, SHA-256 `377271059E12A7301A01F91A8A39202E3A5CDBD1B139B82D583B56DCF3249D81`.
- Stage 7의 계획된 실행 8건은 모두 판정됐고 대표 증거 정리도 완료했다. 사용자의 완료 승인에 따라 Stage 7을 완료로 확정했다.

## 12. Stage 7 완료 판정

- 사용자가 2026-09-01 Stage 7 완료를 명시적으로 승인했다.
- 계획 실행 8건은 `7 Pass · 1 Fail · 0 Blocked · 0 Not Run`으로 모두 판정했다.
- Postman Collection·Environment Template·실행 안내, 계획·보고서, 대표 증거 6개와 Stage 7 결함 2건을 정리했다.
- TF-S7-EXEC-003 Fail은 숨기거나 Pass로 덮어쓰지 않았다. QA 실행 사본의 CORS 수정 후 재검증만 별도 Pass로 기록했다.
- TF-BUG-002는 실제 서버 HTTP 201 반증에 따라 제품 결함이 아닌 테스트 구성 오탐으로 Closed 처리했고, 교정 자동화는 다음 결과 정리 단계의 Blocked 해소 대상으로 넘긴다.
- 외부 MySQL·OAuth·메일·배포 환경·성능·Kotlin 전체 회귀는 검증하지 않았다.
