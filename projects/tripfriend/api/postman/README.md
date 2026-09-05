# TripFriend Postman API 실행 안내

## 목적

이 폴더는 TripFriend의 실제 HTTP API 흐름을 사용자가 Postman에서 직접 재현하기 위한 공개 실행 자산입니다. 초기 API 검증 자산인 `Stage7` Collection은 비로그인 공개 리뷰와 인증 리뷰 생성 흐름을 보존하며 이후 확장한 `Stage8` Collection은 댓글 CRUD, 인증, 입력 검증, 기존 결함 확인 7개 요청을 추가했습니다.


Collection은 실행 도구이며 [최신 결과 요약](../../reports/tripfriend-stage-8-results-summary.md)과 [댓글 Controller 교정 결과](../../reports/portfolio-review-correction-report.md)가 공개 판정 근거다.

## 가져오기

1. Postman에서 본인의 비공개 Workspace를 만들거나 연다. Workspace 이름은 자유롭게 정한다.
2. 현재 실행은 `Import`에서 `TripFriend-Stage8.postman_collection.json`을 선택한다. `TripFriend-Stage7.postman_collection.json`은 Stage 7 당시 실행 자산을 보존한 역사 파일이다.
3. 다시 `Import`에서 `TripFriend-Local.postman_environment.template.json`을 선택한다.
4. 우측 상단 Environment를 `TripFriend Local Template`로 선택한다.
5. `baseUrl`이 `http://localhost:8080`인지 확인한다.

기존 Stage 7 Collection을 가져왔다면 Stage 8 Collection을 별도로 Import하고 `TripFriend Stage 8 API Portfolio`를 실행 대상으로 선택한다. Environment는 최신 Template으로 교체하거나 `createdCommentId`, `stage8CommentContent`, `stage8UpdatedCommentContent` 세 빈 변수를 추가한다.

## 공개 리뷰 API 실행 (Stage 7 이력)

백엔드가 8080 포트에서 실행 중일 때 다음 순서로 보낸다.

1. `01 Public Review / TF-TC-001 공개 리뷰 목록 조회`에서 `Send`를 누른다.
2. Status `200`, 응답 `code`가 `200-5`, `data`가 1개 이상인 배열인지 확인한다.
3. `Test Results`의 4개 assertion 결과를 확인한다.
4. `TF-TC-049 검색 결과 없음`을 보내고 `data`가 빈 배열인지 확인한다.

## 인증 리뷰 API 실행 (Stage 7 이력)

사전조건은 H2 백엔드 8080과 격리된 임시 Redis다. 브라우저 흐름을 함께 확인할 때만 프런트 3000이 필요하다. 기존 Windows Redis 6379를 사용하지 않고 QA 검증용 설정의 임시 Redis 6380을 사용한다. 실제 계정 대신 `BaseInitData`의 로컬 테스트 회원을 사용한다.

1. Environment의 `testUsername`과 `testPassword`에 직접 준비한 로컬 테스트 값만 입력한다. 공개 파일이나 스크린샷에는 남기지 않는다.
2. `TF-S7-EXEC-004 비로그인 리뷰 생성 차단`을 보내고 2/2 Passed와 HTTP 401을 확인한다.
3. `TF-S7-PREP-007 로컬 테스트 회원 로그인`을 보내고 4/4 Passed를 확인한다. Tests가 `accessToken`을 Environment 로컬 값에 임시 저장한다.
4. `TF-S7-EXEC-005 인증 리뷰 생성`을 보낸다. 계약 기대는 HTTP 201·code `201-1`이다. 실제 HTTP 200이면 HTTP assertion 1개는 실패하지만 나머지 생성·본문 assertion은 통과할 수 있으며, assertion을 200으로 낮추지 않는다.
5. `TF-S7-EXEC-006 내 리뷰 저장 확인`을 보내고 방금 만든 reviewId·제목이 배열에 존재하는지 확인한다.
6. 결과 공유 후 `testPassword`와 `accessToken`의 로컬 값을 지운다. H2 데이터는 테스트 서버 종료 시 폐기한다.

## 댓글·입력 검증 API 실행 (Stage 8)

Stage 8의 상세 목적·기대 결과 기준·중단 조건은 [Postman 테스트 계획](../../test-plan/postman-stage-8-test-plan.md)를 사용한다. JUnit P0 경계를 모두 반복하지 않고 실제 HTTP 계약과 요청 간 데이터 연결을 보여주는 대표 7개만 실행한다.

1. 먼저 `02 Authenticated Review`의 로그인·리뷰 생성·내 리뷰 저장 확인을 완료해 `accessToken`과 `createdReviewId`를 준비한다.
2. `03 Comment / TF-S8-POSTMAN-001 인증 댓글 생성`을 보낸다. 기대 HTTP 201·code `201-1`이며, TF-BUG-004는 테스트 구성 오탐으로 Closed됐다. 실제 서버에서 200이 관찰되면 환경·구성과 원시 응답을 새로 확인하며 과거 결함 재현으로 자동 분류하지 않는다.
3. `TF-S8-POSTMAN-002 리뷰별 댓글 조회`에서 생성 댓글이 보이는지 확인한다.
4. `TF-S8-POSTMAN-003 본인 댓글 수정`과 `004 본인 댓글 삭제`를 순서대로 실행한다.
5. `04 Validation & Security / TF-S8-POSTMAN-005`에서 Authorization·Cookie 없이 댓글 생성이 HTTP 401로 차단되는지 확인한다.
6. `TF-S8-POSTMAN-006 reviewId null 입력 검증`에서 유효한 Bearer 인증을 전제로 HTTP 400과 JSON 오류 본문이 반환되고 401·403으로 가려지지 않는지 확인한다. accessToken이 비어 있으면 요청 전 스크립트가 사전조건 오류로 중단한다.
7. `05 Defect Verification / TF-S8-POSTMAN-007`에서 평점 0이 HTTP 400·code `400-2`인지 확인한다. 실제 `400-1`이면 TF-BUG-003 재현 Fail 후보입니다.
8. 결과 공유 후 `testPassword`·`accessToken`을 지우고, 실행 중 생성된 ID·내용 변수도 필요하지 않으면 비운다.

요청별 Test Results와 실제 결과를 보기 전에는 Pass·Fail을 확정하지 않는다. `Defect Verification` 폴더는 현재 Open 결함의 실제 서버 재현·영향 확인이며 제품 수정 후 Pass 재검증으로 표현하지 않는다.

### Stage 8 최초 실행 후 교정

- `TF-S8-POSTMAN-EXEC-001`은 13개 요청·42개 assertion을 완료했고 Postman 화면에는 41 Passed·1 Failed·0 Errors로 표시됐다.
- TF-S8-POSTMAN-007은 실제 `400-1`로 TF-BUG-003을 재현했다.
- TF-S8-POSTMAN-006은 HTTP 401·빈 본문인데도 최초의 넓은 4xx assertion 때문에 통과한 테스트 오탐이었다. 추가 재현과 제한된 진단에서 기존 TF-BUG-006 경로로 판정했다.
- 최신 Collection에서는 006의 assertion을 교정했다. TF-S8-POSTMAN-RETEST-001 재실행에서 로그인 후 006을 실행해 HTTP 401·빈 본문을 3개 검증 조건 모두 실패로 검출했다. 제품 요청은 TF-BUG-006 연결 Fail, assertion 교정 재검증은 Pass다.

브라우저에서는 같은 로컬 테스트 회원으로 로그인한 뒤 `/community/write`에서 이미지 없이 리뷰를 작성한다. Network의 로그인·리뷰 생성·목록 요청 상태와 최종 리뷰 카드만 확인하며, 비밀번호·Authorization·Cookie·응답 토큰이 보이는 화면은 캡처하지 않는다.

## 판정과 증거

- 모든 assertion이 통과하면 해당 요청은 `Pass` 후보이다.
- 기대 결과 불일치는 `Fail` 후보이며 응답을 임의로 수정해 통과시키지 않는다.
- 서버 미기동·준비되지 않은 외부 의존성 등으로 실행할 수 없으면 `Blocked` 후보이다.
- 대표 증거에는 요청명, URL, HTTP 상태와 Test Results가 보이게 한다. 인증 실행은 Body·Headers·Cookies·Environment 값을 가리고 토큰이 없는 화면만 사용한다.
- 토큰·쿠키·비밀번호·개인정보가 보이면 캡처하거나 공유하지 않는다.

## 비밀정보 경계

- 이 Template에는 로컬 URL과 비어 있는 변수 정의만 둔다.
- 로컬 테스트 비밀번호와 토큰은 Postman의 비공개 로컬 값으로만 관리하고 공개 파일에 저장하지 않는다.
- 실제 값이 든 Environment와 계정 정보를 Git에 포함하지 않는다.

## 재현 환경의 경계

이 Collection만으로 서버가 준비되지는 않는다. [자동화 재현 안내](../../automation/README.md#재현-조건과-공개-범위)의 기준 제품·테스트 설정·QA 변경 조건을 먼저 확인한다. 공개 템플릿에는 실제 비밀번호·토큰이 없고, 기존 로컬 계정 값을 제3자가 사용할 수 있다고 가정하지 않는다.
