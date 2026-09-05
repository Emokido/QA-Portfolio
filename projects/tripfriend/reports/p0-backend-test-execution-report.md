# TripFriend P0 백엔드 테스트 실행 보고서

## 최신 판정 — 2026-09-05 제출본 교정

**누적 P0 45 Pass·4 Fail·0 Blocked·0 Not Run.** TF-REVIEW-EXEC-001에서 댓글 경계 4개가 모두 Pass해 TF-TC-040·041의 테스트 구성 오탐을 정정했다. TF-BUG-004는 제품 결함이 아닌 테스트 구성 오탐으로 Closed했다. 이번에 P0 전체를 재실행한 것은 아니다.

[교정 구성·4개 실행 결과·판정 범위](portfolio-review-correction-report.md)

## 과거 실행 기록 읽는 방법

아래는 Stage 6·8 당시의 실행·학습 기록이다. 당시의 `현재`, `Open`, `Not Run`과 43 Pass·6 Fail은 해당 기록 시점의 상태이며 위 최신 판정을 대체하지 않는다. 38 Pass·11 Fail → 43 Pass·6 Fail → 45 Pass·4 Fail의 교정 이력을 보존한다.


<details>
<summary>Stage 6·8 실행 이력과 당시 판정 펼치기</summary>

- 문서 상태: `Stage 6 Complete — P0 49/49 실행·Kotlin 제한적 정적 감사·공개 후보 검토와 사용자 단계 완료 승인`
- 기준 프로젝트: 스프린트 1 Java/Spring 완성본
- 기준 커밋: `e2431223ce2a18c8f944d544cba76b951ce0356d`
- 실행 기간: 2026-08-28~2026-09-01
- 실행 주체: `Codex with user authorization · User`
- 현재 실행 범위: `TF-TC-001~049`
- Stage 6 당시 결과: `38 Pass / 11 Fail / 0 Not Run / 0 product-test Blocked`
- Stage 6 당시 등록 결함: `TF-BUG-001~006 Open`

> 이 문서는 Stage 6의 누적 실행 역사를 보존한다. 당시 P0 TF-TC-001~049 전체를 실행해 38 Pass·11 Fail을 기록했다. 이후 실제 서버 반증과 Stage 8 테스트 구성 교정으로 TF-TC-030·031·034·035·037의 당시 Fail 근거가 무효화됐고, 현재 판정은 43 Pass·6 Fail·0 Blocked·0 Not Run이다. 상세 정정은 8절을 사용한다. P0 전체 실행은 실제 서버·외부 Redis·DB·프론트 정상 동작이나 49건 전체 Pass를 뜻하지 않는다.

## 1. 용어와 판정

| 한글 용어 | 영어 | 적용 기준 |
|---|---|---|
| 통과 | Pass | 검증 지점까지 실행한 실제 결과가 근거 있는 기대 결과와 일치 |
| 실패 | Fail | 검증 지점까지 실행한 실제 결과가 근거 있는 기대 결과와 불일치 |
| 차단 | Blocked | 환경·도구·테스트 코드 등으로 제품 검증 지점에 도달하지 못함 |
| 위험 재현 | Reproduced risk | 실행 전 위험 가설과 연결된 오라클 불일치가 실행에서 관찰됨 |
| 결함 후보 | Defect candidate | 불일치는 확인했지만 추가 재현·영향 검토 전이라 결함 ID를 확정하지 않은 상태 |

`BUILD FAILED`는 Gradle 테스트 태스크의 종합 결과다. 이 보고서는 개별 테스트케이스의 실제 검증 결과를 별도로 판정한다.

## 2. 실행 목표와 범위

- 목적: 리뷰·댓글 생성·수정·삭제의 서비스 규칙, 생성 입력 경계와 미존재 상세 조회의 HTTP 응답 계약, 댓글 `reviewId` 누락·null 처리, 리뷰 검색·정렬 Repository 동작과 변경 요청의 Security 경계를 검증한다.
- 테스트 수준: 서비스 단위 테스트(Service unit test), 독립형 MockMvc Controller 단위 테스트, Repository slice test, Controller·Service·Repository/H2 통합 테스트, Spring Security Web MVC slice test.
- 테스트 도구: JUnit 5, Mockito, AssertJ, Spring MockMvc, Spring Data JPA `@DataJpaTest`, 내장 H2.
- Mock: 테스트 목적에 따라 `ReviewRepository`, `CommentRepository`, `ReviewViewCountRepository`, `PlaceRepository`를 사용한다.
- 실제 객체: `Member`, `Place`, `Review`, `ReviewRequestDto`.
- Controller 범위: `ReviewController.createReview`의 제목·내용·평점 경계와 `CommentController.createComment`의 내용 경계는 Service·`AuthService`를 Mock하고 실제 `GlobalExceptionHandler`를 사용했다. TF-TC-043·047·048은 실제 Service·Repository/H2와 독립형 MockMvc·실제 `GlobalExceptionHandler`를 사용하고 `AuthService`만 Mock했다.
- Security 범위: TF-TC-020~028은 실제 `SecurityConfig`·JWT·탈퇴회원 필터를 사용했다. TF-TC-020~025는 Mock Controller 의존성으로 비로그인 HTTP 401·미호출을, TF-TC-026~028은 실제 `AuthService`와 Mock JWT·Redis·회원·댓글 협력 객체로 Bearer·쿠키·유효하지 않은 JWT 전달을 검증했다.
- 제외: 실제 토큰·Redis·서버·외부 DB·Docker·프론트·Kotlin 실행. TF-TC-001~003은 Authorization 없는 Controller·Service·H2 통합 범위이며 실제 Spring Security 필터 체인은 포함하지 않았다.
- 제품 코드 변경: 없음.

## 3. 실행 이력

| 실행 ID | 명령 | 검증 지점 도달 | 결과 | 처리 |
|---|---|---|---|---|
| TF-EXEC-001 | `./gradlew.bat test --tests "com.tripfriend.domain.review.service.ReviewServiceTest" --no-daemon` | 아니요 | 샌드박스 Gradle 사용자 홈의 잠금 파일 부모 경로 생성 실패 | 환경 차단으로 분리. 동일 명령을 승인된 외부 실행으로 재시도해 해소 |
| TF-EXEC-002 | 동일 명령 | 예 | 4 tests, 3 Pass, 1 Fail, `BUILD FAILED in 21s` | 개별 케이스 결과 기록 |
| TF-EXEC-003 | IntelliJ에서 `ReviewServiceTest` 클래스 실행 | 예 | 사용자 화면 기준 3 Pass, TF-TC-008 1 Fail, 전체 1초 243ms | 사용자 직접 재현. 첨부 화면과 실패 로그 대조 |
| TF-EXEC-004 | 지정 `ReviewServiceTest` Gradle 실행 | 예 | 7 tests, 6 Pass, 1 Fail, `BUILD FAILED in 12s` | TF-TC-004~006 신규 Pass, 기존 TF-TC-008 동일 Fail |
| TF-EXEC-005 | IntelliJ에서 최신 `ReviewServiceTest` 클래스 실행 | 예 | 사용자 화면 기준 7 tests, 6 Pass, TF-TC-008 1 Fail, 전체 1초 285ms | 신규 TF-TC-004~006 포함 사용자 직접 재현 |
| TF-EXEC-006 | `.\gradlew.bat test --tests "com.tripfriend.domain.review.service.ReviewServiceTest.deleteReview_*" --no-daemon` | 예 | 3 tests, 3 Pass, `BUILD SUCCESSFUL in 12s` | TF-TC-011~013만 필터 실행; TF-TC-008 제외 |
| TF-EXEC-007 | IntelliJ에서 최신 `ReviewServiceTest` 클래스 실행 | 예 | 사용자 화면 기준 10 tests, 9 Pass, TF-TC-008 1 Fail, 전체 1초 219ms | TF-TC-011~013 Pass 사용자 직접 재현, TF-TC-008 동일 Fail |
| TF-EXEC-008 | `.\gradlew.bat test --tests "com.tripfriend.domain.review.service.CommentServiceTest" --no-daemon --offline` | 아니요 | 샌드박스 Gradle 사용자 홈의 잠금 파일 부모 경로 생성 실패 | 샌드박스 환경 차단으로 분리; 동일 명령의 승인된 외부 실행으로 재시도 |
| TF-EXEC-009 | 동일 오프라인 명령 | 예 | 6 tests, 6 Pass, `BUILD SUCCESSFUL in 15s` | TF-TC-014~019 개별 결과 기록 |
| TF-EXEC-010 | IntelliJ에서 `CommentServiceTest` 클래스 실행 | 예 | 사용자 화면 기준 6 tests, 6 Pass, 전체 1초 168ms; `BUILD SUCCESSFUL in 9s` | 사용자 직접 재현 확인; 첨부 화면 검토 후 별도 파일로 보존하지 않음 |
| TF-EXEC-011 | `.\gradlew.bat test --tests "com.tripfriend.domain.review.controller.ReviewControllerTest" --no-daemon --offline` | 예 | 4 tests, 2 Pass, 2 Fail, `BUILD FAILED in 13s` | TF-TC-029·032 Pass, TF-TC-030·031은 기대 HTTP 201 대비 실제 200으로 Fail; assertion 유지, 재실행 안 함 |
| TF-EXEC-012 | IntelliJ에서 `ReviewControllerTest` 클래스 실행 | 예 | 사용자 화면 기준 4 tests, 2 Pass, 2 Fail, 전체 2초 401ms; `BUILD FAILED in 4s` | Codex 결과와 동일 재현; TF-TC-030·031 기대 201·실제 200 로그 확인, 중요 Fail 증거 보존 |
| TF-EXEC-013 | `.\gradlew.bat test --tests "com.tripfriend.domain.review.controller.ReviewControllerTest.content_*" --tests "com.tripfriend.domain.review.controller.ReviewControllerTest.rating_*" --no-daemon --offline` | 예 | 8 test invocations, 2 Pass, 6 Fail, `BUILD FAILED in 14s` | 신규 TF-TC-033~038만 실행; 유효 4개 변형은 기대 201·실제 200, 평점 0·6은 기대 `400-2`·실제 `400-1`; 재실행 안 함 |
| TF-EXEC-014 | IntelliJ에서 최신 `ReviewControllerTest` 클래스 전체 실행 | 예 | 사용자 화면 기준 12 tests, 4 Pass, 8 Fail, 전체 2초 631ms; `BUILD FAILED in 4s` | TF-EXEC-011~013 결과와 일치; TF-TC-038 두 변형 기대 `400-2`·실제 `400-1` 재현, 중요 Fail 증거 보존 |
| TF-EXEC-015 | `CommentControllerTest` 지정 오프라인 명령 | 아니요 | 샌드박스 Gradle 사용자 홈의 잠금 파일 부모 경로 생성 실패 | 샌드박스 환경 차단으로 분리; 승인된 두 번째 실행으로 재시도 |
| TF-EXEC-016 | 동일 오프라인 명령 | 예 | 4 tests, 2 Pass, 2 Fail, `BUILD FAILED in 19s` | TF-TC-039·042 Pass, TF-TC-040·041은 기대 HTTP 201 대비 실제 200으로 Fail; 재실행 안 함 |
| TF-EXEC-017 | IntelliJ에서 `CommentControllerTest` 클래스 전체 실행 | 예 | 사용자 화면 기준 4 tests, 2 Pass, 2 Fail, 전체 2초 366ms | TF-EXEC-016과 동일 재현; TF-TC-040·041 기대 201·실제 200 로그 확인, 중요 Fail 증거 보존 |
| TF-EXEC-018 | `.\gradlew.bat test --tests "com.tripfriend.domain.review.repository.ReviewRepositoryTest" --no-daemon --offline` | 아니요 | 샌드박스 Gradle 사용자 홈의 잠금 파일 부모 경로 생성 실패 | 샌드박스 환경 차단으로 분리; 승인된 외부 실행으로 재시도 |
| TF-EXEC-019 | 동일 오프라인 명령 | 아니요 | 컴파일 후 3 tests 모두 ApplicationContext 로딩 실패, `BUILD FAILED in 11s` | 필수 `application-secret.yml` 누락. 세 케이스 Blocked; 최대 2회 소진, 추가 실행 안 함 |
| TF-EXEC-020 | 테스트 전용 `spring.config.name=application-test` 적용 후 동일 오프라인 명령 | 아니요 | H2 초기화 후 3 tests 모두 ApplicationContext 로딩 실패, `BUILD FAILED in 16s` | secret import 차단 해소; `RecruitRepositoryCustomImpl`의 `JPAQueryFactory` Bean 누락으로 3 Blocked 유지 |
| TF-EXEC-021 | 기존 `QueryDslConfig` test import 후 동일 오프라인 명령 | 예 | 3 tests, 3 Pass, `BUILD SUCCESSFUL in 17s` | TF-TC-044·045·049 Pass; 이전 3 Blocked 해소 |
| TF-EXEC-022 | IntelliJ에서 `ReviewRepositoryTest` 클래스 전체 실행 | 예 | 사용자 화면 기준 3 tests, 3 Pass, 전체 873ms | TF-EXEC-021과 동일 재현; 대표 Repository/H2 사용자 증거 보존 |
| TF-EXEC-023 | `.\gradlew.bat test --tests "com.tripfriend.domain.review.service.ReviewSortingIntegrationTest" --no-daemon --offline` | 예 | 4 tests, 4 Pass, `BUILD SUCCESSFUL in 17s` | TF-TC-046 네 정렬 변형 Pass; 테스트케이스 기준 1 Pass 추가 |
| TF-EXEC-024 | IntelliJ에서 `ReviewSortingIntegrationTest` 클래스 전체 실행 | 예 | 사용자 화면 기준 4 tests, 4 Pass, 전체 1초 20ms | TF-EXEC-023과 동일 재현; 대표 정렬 통합 사용자 증거 보존 |
| TF-EXEC-025 | `.\gradlew.bat test --tests "com.tripfriend.domain.review.controller.ReviewCommentNotFoundIntegrationTest" --no-daemon --offline` | 예 | 2 tests, 2 Pass, `BUILD SUCCESSFUL in 18s` | TF-TC-047·048 HTTP 404·본문 코드·데이터 불변 Pass; 첫 실행 성공 |
| TF-EXEC-026 | IntelliJ에서 `ReviewCommentNotFoundIntegrationTest` 클래스 전체 실행 | 예 | 사용자 화면 기준 2 tests, 2 Pass, 전체 1초 576ms | TF-EXEC-025와 동일 재현; 대표 미존재 상세 조회 사용자 증거 보존 |
| TF-EXEC-027 | `.\gradlew.bat test --tests "com.tripfriend.domain.review.controller.ReviewCommentUnauthenticatedSecurityTest" --no-daemon --offline` | 아니요 | 샌드박스 Gradle 사용자 홈의 잠금 파일 부모 경로 생성 실패 | 샌드박스 환경 차단으로 분리; 승인된 외부 실행으로 재시도 |
| TF-EXEC-028 | 동일 오프라인 명령의 승인된 외부 실행 | 아니요 | 테스트 컴파일 후 6 tests 모두 ApplicationContext 로딩 실패, `BUILD FAILED in 15s` | `@EnableJpaAuditing`이 Web MVC slice에서 빈 JPA metamodel을 요구해 TF-TC-020~025 모두 Blocked; 최대 2회 소진 |
| TF-EXEC-029 | JPA metamodel Mock 추가 후 동일 오프라인 명령 1회 | 예 | 6 tests, 6 Pass, `BUILD SUCCESSFUL in 16s` | TF-TC-020~025 HTTP 401·Controller 의존성 미호출 Pass; 이전 6 Blocked 해소 |
| TF-EXEC-030 | IntelliJ에서 `ReviewCommentUnauthenticatedSecurityTest` 클래스 전체 실행 | 예 | 사용자 화면 기준 6 tests, 6 Pass, 전체 158ms | TF-EXEC-029와 동일 재현; 대표 Security 사용자 증거 보존, 누적 테스트케이스 수 중복 합산 안 함 |
| TF-EXEC-031 | `ReviewCommentAuthenticatedSecurityTest` 지정 오프라인 명령 1회 | 예 | 3 tests, 2 Pass·1 Fail, `BUILD FAILED in 19s` | TF-TC-026 Bearer 성공·028 invalid JWT 401 Pass; TF-TC-027 쿠키 전용은 Controller에서 null 토큰 NPE로 Fail; 두 번째 실행 안 함 |
| TF-EXEC-032 | IntelliJ에서 `ReviewCommentAuthenticatedSecurityTest` 클래스 전체 실행 | 예 | 사용자 화면 기준 3 tests, 2 Pass·1 Fail, 전체 512ms | TF-EXEC-031과 동일 재현; TF-TC-027 null 토큰 NPE 로그 확인, 중요 Fail 증거 보존·TF-BUG-005 등록 |
| TF-EXEC-033 | `CommentReviewIdValidationIntegrationTest` 지정 오프라인 명령의 샌드박스 실행 | 아니요 | 샌드박스 Gradle 사용자 홈의 잠금 파일 부모 경로 생성 실패 | 제품 검증 전 환경 차단으로 분리; 동일 명령의 승인된 외부 실행으로 해소 |
| TF-EXEC-034 | 동일 오프라인 명령의 승인된 외부 실행 | 예 | 2 tests, 2 Fail, `BUILD FAILED in 21s` | TF-TC-043 누락·null 모두 `findById(null)` 요청 처리 예외; 제품 불일치이므로 두 번째 제품 실행 안 함 |
| TF-EXEC-035 | IntelliJ에서 `CommentReviewIdValidationIntegrationTest` 클래스 전체 실행 | 예 | 사용자 화면 기준 2 tests, 2 Fail, 전체 1초 428ms | TF-EXEC-034와 동일 재현; 누락·null 모두 `The given id must not be null`, 중요 Fail 증거 보존·TF-BUG-006 등록 |
| TF-EXEC-036 | `PublicReviewCommentQueryIntegrationTest` 지정 오프라인 명령 1회 | 예 | 3 tests, 3 Pass, `BUILD SUCCESSFUL in 22s` | TF-TC-001~003 HTTP 200·본문 코드·SORT-BASE 데이터·AuthService 미호출 Pass; 두 번째 실행 안 함 |
| TF-EXEC-037 | IntelliJ에서 `PublicReviewCommentQueryIntegrationTest` 클래스 전체 실행 | 예 | 사용자 화면 기준 3 tests, 3 Pass, 전체 1초 908ms | TF-EXEC-036과 동일 재현; P0 49/49 실행 완료 대표 사용자 증거 보존, 누적 수치 중복 합산 안 함 |

TF-EXEC-001·008·015·018·027·033의 경로 오류는 제품 테스트 결과가 아니다. TF-EXEC-001·008·015·033은 이후 승인 실행이 제품 검증 지점에 도달해 해소됐고, TF-EXEC-019·020의 Repository 설정 차단도 TF-EXEC-021에서 해소됐다. TF-EXEC-028의 JPA auditing 컨텍스트 차단은 `JpaMetamodelMappingContext` Mock을 추가한 TF-EXEC-029에서 해소됐으므로 남아 있는 제품 테스트 `Blocked`는 없다. TF-EXEC-003의 정확한 프로젝트 절대경로와 실행 시각은 첨부 증거에 표시되지 않아 임의로 기록하지 않는다.

## 4. 테스트케이스별 결과

| 케이스 | 검증 목적 | 기대 결과 | 실제 결과 | 상태 |
|---|---|---|---|---|
| TF-TC-001 | 비로그인 리뷰 목록 조회 | HTTP 200, `200-5`, 전체 리뷰와 댓글 수·조회수 | Authorization 없이 실제 Controller·Service·H2에서 네 리뷰 최신순과 댓글 수·조회수 assertion 일치, AuthService 미호출 | Pass |
| TF-TC-002 | 비로그인 리뷰 상세 조회 | HTTP 200, `200-1`, 대상 ID·제목·평점·댓글 수 | Authorization 없이 대상 리뷰 필드·댓글 수 assertion 일치, AuthService 미호출 | Pass |
| TF-TC-003 | 비로그인 리뷰별 댓글 조회 | HTTP 200, `200-2`, 댓글 4개 생성일 오름차순 | Authorization 없이 R-SORT-02 댓글 네 개의 ID·내용 순서 assertion 일치, AuthService 미호출 | Pass |
| TF-TC-004 | 유효한 리뷰 생성 | 리뷰와 조회수 0 레코드 함께 저장 | 저장 객체·관계·응답 assertion 일치 | Pass |
| TF-TC-005 | `placeId=null` 생성 거부 | `400-1`, 리뷰·조회수 부분 저장 없음 | 예외 코드·무호출 verify 일치 | Pass |
| TF-TC-006 | 미존재 장소 생성 거부 | `404-2`, 리뷰·조회수 부분 저장 없음 | 예외 코드·무호출 verify 일치 | Pass |
| TF-TC-007 | 작성자의 기본 필드 수정 | 제목·내용·평점 변경, 작성자·ID 유지 | assertion 일치 | Pass |
| TF-TC-008 | 작성자의 여행지 변경 | 요청 `placeId`의 제주 장소로 실제 관계 변경 | 기존 부산 장소 관계가 유지됨 | Fail |
| TF-TC-009 | 비작성자 수정 차단 | `403-1`, 모든 필드와 장소 관계 불변 | assertion 일치 | Pass |
| TF-TC-010 | 미존재 리뷰 수정 | `404-1`, 신규 저장 없음 | assertion·verify 일치 | Pass |
| TF-TC-011 | 댓글 없는 본인 리뷰 삭제 | 조회수 삭제 후 리뷰 삭제 | Mockito 순서 검증 일치 | Pass |
| TF-TC-012 | 비작성자 리뷰 삭제 차단 | `403-1`, 조회수·리뷰 삭제 없음 | 예외 코드·무호출 verify 일치 | Pass |
| TF-TC-013 | 미존재 리뷰 삭제 | `404-1`, 삭제 작업 없음 | 예외 코드·무호출 verify 일치 | Pass |
| TF-TC-014 | 유효한 댓글 생성 | 대상 리뷰·작성자 연결과 저장 응답 일치 | 저장 객체·관계·응답 assertion 일치 | Pass |
| TF-TC-015 | 작성자 댓글 수정 | 내용 변경, 작성자·리뷰 연결 유지 | 상태·응답 assertion 일치 | Pass |
| TF-TC-016 | 작성자 댓글 삭제 | 대상 댓글 삭제, 리뷰 유지 | 삭제 verify와 관계 assertion 일치 | Pass |
| TF-TC-017 | 비작성자 댓글 수정 차단 | `403-2`, 내용·관계 불변 | 예외 코드·불변 assertion·무호출 verify 일치 | Pass |
| TF-TC-018 | 비작성자 댓글 삭제 차단 | `403-2`, 댓글 유지 | 예외 코드·무호출 verify 일치 | Pass |
| TF-TC-019 | 미존재 리뷰 댓글 생성 | `404-3`, 댓글 미저장 | 예외 코드·무호출 verify 일치 | Pass |
| TF-TC-020 | 비로그인 리뷰 생성 차단 | 요청 거부·리뷰/조회수 미생성, 정확한 401/403은 현재 계약 관찰 | HTTP 401, Auth·Review·Comment Service 미호출 | Pass |
| TF-TC-021 | 비로그인 리뷰 수정 차단 | 요청 거부·리뷰 불변, 정확한 401/403은 현재 계약 관찰 | HTTP 401, Auth·Review·Comment Service 미호출 | Pass |
| TF-TC-022 | 비로그인 리뷰 삭제 차단 | 요청 거부·리뷰/조회수 유지, 정확한 401/403은 현재 계약 관찰 | HTTP 401, Auth·Review·Comment Service 미호출 | Pass |
| TF-TC-023 | 비로그인 댓글 생성 차단 | 요청 거부·댓글 미생성, 정확한 401/403은 현재 계약 관찰 | HTTP 401, Auth·Review·Comment Service 미호출 | Pass |
| TF-TC-024 | 비로그인 댓글 수정 차단 | 요청 거부·댓글 불변, 정확한 401/403은 현재 계약 관찰 | HTTP 401, Auth·Review·Comment Service 미호출 | Pass |
| TF-TC-025 | 비로그인 댓글 삭제 차단 | 요청 거부·댓글 유지, 정확한 401/403은 현재 계약 관찰 | HTTP 401, Auth·Review·Comment Service 미호출 | Pass |
| TF-TC-026 | Bearer 헤더 인증 대표 성공 | 인증·작성자 판정 후 HTTP 200, `200-4`; 댓글 변경 경로 전달 | 실제 Security 필터·AuthService를 거쳐 HTTP 200·`200-4`, 댓글 수정 Service 호출 | Pass |
| TF-TC-027 | 쿠키 전용 인증 전달 | 인증·Controller 전달 결과 관찰; 5xx·정상 사용자 차단은 위험 근거 | 유효 쿠키는 Security 필터를 통과했지만 Controller가 null 헤더를 AuthService에 전달해 `token.replace()` NPE, 정상 응답 미완료 | Fail |
| TF-TC-028 | 유효하지 않은 JWT 차단 | 요청 거부·데이터 불변, 정확한 오류 형식 관찰 | HTTP 401, 사용자 조회·댓글 수정 Service 미호출 | Pass |
| TF-TC-029 | 리뷰 제목 1자 생성 거부 | HTTP 400, `400-1`, 서비스 미호출 | 상태·코드 assertion과 무호출 verify 일치 | Pass |
| TF-TC-030 | 리뷰 제목 2자 생성 허용 | HTTP 201, `201-1` | HTTP 200 반환으로 상태 assertion 불일치 | Fail |
| TF-TC-031 | 리뷰 제목 30자 생성 허용 | HTTP 201, `201-1` | HTTP 200 반환으로 상태 assertion 불일치 | Fail |
| TF-TC-032 | 리뷰 제목 31자 생성 거부 | HTTP 400, `400-1`, 서비스 미호출 | 상태·코드 assertion과 무호출 verify 일치 | Pass |
| TF-TC-033 | 리뷰 내용 9자 생성 거부 | HTTP 400, `400-1`, 서비스 미호출 | 상태·코드 assertion과 무호출 verify 일치 | Pass |
| TF-TC-034 | 리뷰 내용 10자 생성 허용 | HTTP 201, `201-1` | HTTP 200 반환으로 상태 assertion 불일치 | Fail |
| TF-TC-035 | 리뷰 내용 2000자 생성 허용 | HTTP 201, `201-1` | HTTP 200 반환으로 상태 assertion 불일치 | Fail |
| TF-TC-036 | 리뷰 내용 2001자 생성 거부 | HTTP 400, `400-1`, 서비스 미호출 | 상태·코드 assertion과 무호출 verify 일치 | Pass |
| TF-TC-037 | 평점 1·5 생성 허용 | 각 변형 HTTP 201, `201-1` | 두 변형 모두 HTTP 200 반환으로 상태 assertion 불일치 | Fail |
| TF-TC-038 | 평점 0·6 생성 거부 | 각 변형 HTTP 400, `400-2`, 서비스 미호출 | HTTP 400이나 두 변형 모두 본문 코드 `400-1` | Fail |
| TF-TC-039 | 댓글 1자 생성 거부 | HTTP 400, `400-1`, 서비스 미호출 | 상태·코드 assertion과 무호출 verify 일치 | Pass |
| TF-TC-040 | 댓글 2자 생성 허용 | HTTP 201, `201-1` | HTTP 200 반환으로 상태 assertion 불일치 | Fail |
| TF-TC-041 | 댓글 100자 생성 허용 | HTTP 201, `201-1` | HTTP 200 반환으로 상태 assertion 불일치 | Fail |
| TF-TC-042 | 댓글 101자 생성 거부 | HTTP 400, `400-1`, 서비스 미호출 | 상태·코드 assertion과 무호출 verify 일치 | Pass |
| TF-TC-043 | 댓글 `reviewId` 누락·null | 정확한 400 계약은 미명시; 응답 상태·형식 관찰, 5xx 없이 거부·댓글 미저장 | 두 변형 모두 응답 생성 전 `findById(null)`에서 요청 처리 예외; 4xx 응답·요청 후 count assertion 미도달, SQL에 댓글 INSERT 없음 | Fail |
| TF-TC-044 | 제목 키워드 `부산` 검색 | 제목에 부산을 포함한 리뷰만 최신순 반환 | 대상 두 리뷰의 ID·제목·최신순 assertion 일치 | Pass |
| TF-TC-045 | 부산 장소 필터 | 부산 장소 리뷰만 최신순 반환 | 대상 두 리뷰의 ID·장소·최신순 assertion 일치 | Pass |
| TF-TC-049 | 미존재 제목 검색 | 빈 목록 반환 | 빈 목록 assertion 일치 | Pass |
| TF-TC-046 | 단일 정렬 4종 | newest·highest_rating·comments·most_viewed가 SORT-BASE 순서 반환 | 네 변형의 리뷰 ID 순서와 댓글·조회수 값 assertion 일치 | Pass |
| TF-TC-047 | 미존재 리뷰 상세 조회 | HTTP 404, `404-1`, 리뷰 데이터 변경 없음 | 실제 H2 미존재 판정·공통 예외 처리·Repository 개수 불변 assertion 일치 | Pass |
| TF-TC-048 | 미존재 댓글 상세 조회 | HTTP 404, `404-4`, 데이터 변경 없음 | 실제 H2 미존재 판정·공통 예외 처리·Repository 개수 불변 assertion 일치 | Pass |

누적 판정은 P0 49건 중 38 Pass·11 Fail·0 Not Run·product-test Blocked 0이다. TF-EXEC-013 XML은 `tests="8"`, `failures="6"`, `errors="0"`, `skipped="0"`을 기록했다. 6개 테스트케이스 중 TF-TC-033·036은 Pass, TF-TC-034·035·037·038은 Fail이다. TF-EXEC-016 XML은 `tests="4"`, `failures="2"`, `errors="0"`, `skipped="0"`을 기록했다. TF-EXEC-021 XML은 `tests="3"`, `failures="0"`, `errors="0"`, `skipped="0"`을 기록했다. TF-EXEC-023 XML은 `tests="4"`, `failures="0"`, `errors="0"`, `skipped="0"`, 타임스탬프 `2026-08-31T12:00:50`, suite time `0.959`를 기록했다. TF-EXEC-025 XML은 `tests="2"`, `failures="0"`, `errors="0"`, `skipped="0"`, 타임스탬프 `2026-08-31T12:30:58`, suite time `1.619`를 기록했다. TF-EXEC-029 XML은 `tests="6"`, `failures="0"`, `errors="0"`, `skipped="0"`, 타임스탬프 `2026-08-31T13:24:08`, suite time `0.162`를 기록했다. TF-EXEC-031 XML은 `tests="3"`, `failures="1"`, `errors="0"`, `skipped="0"`, 타임스탬프 `2026-08-31T14:27:29`, suite time `0.64`를 기록했다. TF-EXEC-034 XML은 `tests="2"`, `failures="2"`, `errors="0"`, `skipped="0"`, 타임스탬프 `2026-09-01T02:09:11`, suite time `1.414`를 기록했다. TF-EXEC-036 XML은 `tests="3"`, `failures="0"`, `errors="0"`, `skipped="0"`, 타임스탬프 `2026-09-01T05:10:07`, suite time `1.918`를 기록했다. XML 타임스탬프에는 시간대 정보가 없다.

## 5. TF-TC-008 실패 분석

### 기대 결과

사용자가 리뷰 수정 요청에서 선택한 여행지 ID가 실제 `Review-Place` 관계에 반영돼야 한다. 근거는 `TF-REQ-004` 사용자 확인 오라클과 `TF-RISK-001`이다.

### 실제 결과

제주 장소 ID를 포함한 수정 요청을 실행한 뒤에도 리뷰의 장소 객체는 기존 부산 장소였다. assertion은 서로 다른 장소 객체를 확인해 실패했다.

### 코드 상관관계

기준 코드의 `ReviewService.updateReview`는 리뷰를 조회하고 작성자·평점을 확인한 뒤 `review.update(title, content, rating)`만 호출한다. 수정 요청의 `placeId`로 `PlaceRepository`를 조회하거나 리뷰의 장소 관계를 변경하는 경로는 해당 메서드에 없다.

### 현재 판정

- TF-TC-008: `Fail`.
- TF-RISK-001: `Reproduced`.
- 제품 결함: `TF-BUG-001 — 리뷰 수정 시 선택한 여행지가 실제 관계에 반영되지 않음`, 상태 `Open`.
- assertion을 약화하거나 제품 코드를 수정하지 않았다.

### TF-TC-030·031 실패 분석

- 기대 결과: 유효한 제목 2자·30자의 리뷰 생성 성공은 HTTP 201과 본문 코드 `201-1`을 반환한다.
- 실제 결과: 두 케이스 모두 기대 HTTP 201 대비 실제 HTTP 200으로 상태 assertion이 실패했다.
- 코드 상관관계: `ReviewController.createReview`는 본문 코드 `201-1`의 `RsData`를 직접 반환하지만 HTTP 상태를 201로 지정하는 `ResponseEntity` 또는 `@ResponseStatus`가 없다.
- 현재 판정: TF-TC-030·031·034·035·037 `Fail`. `TF-BUG-002 — 리뷰 생성 성공 응답이 HTTP 201 대신 200을 반환함`, 상태 `Open`, 심각도 `Minor`, 재현 실행 4/4다.
- 처리 경계: assertion을 약화하지 않았고 제품 코드·기존 테스트를 수정하지 않았다. 실제 클라이언트 흐름 중단은 아직 확인하지 않았으며 확인 시 심각도를 재평가한다.

### TF-TC-034·035·037·038 실패 분석

- TF-TC-034·035·037 기대 결과: 유효한 내용 10·2000자와 평점 1·5는 HTTP 201과 본문 코드 `201-1`을 반환한다.
- 실제 결과: 네 실행 변형 모두 기대 HTTP 201 대비 실제 200으로 실패했다. TF-BUG-002와 같은 `ReviewController.createReview` 성공 상태 지정 누락 현상으로 연결하며 새 결함으로 중복 등록하지 않는다.
- TF-TC-038 기대 결과: 평점 0·6은 HTTP 400과 본문 코드 `400-2`로 거부되고 서비스가 호출되지 않는다.
- 실제 결과: HTTP 400은 일치했지만 두 변형 모두 본문 코드가 `400-1`이었다.
- 코드 상관관계: DTO의 `@Min/@Max` 검증이 Controller 진입 전에 실패하고 `GlobalExceptionHandler`의 Bean Validation 처리기가 모든 필드 검증 실패를 `400-1`로 반환한다. 따라서 Service의 평점 전용 `400-2` 분기에는 도달하지 않는다.
- 현재 판정: TF-TC-038 `Fail`. `TF-BUG-003 — 범위 밖 리뷰 평점이 전용 코드 400-2 대신 공통 코드 400-1을 반환함`, 상태 `Open`, 심각도 `Minor`, 재현 실행 2/2로 등록했다.
- 기존 결함 갱신: TF-TC-034·035·037을 TF-BUG-002 영향 케이스로 추가하고 TF-EXEC-013·014까지 동일 HTTP 201·200 실패가 확인돼 재현 실행을 4/4로 갱신했다.

### TF-TC-039~042 실행 분석

- TF-TC-039·042 기대 결과: 댓글 내용 1자·101자는 HTTP 400과 본문 코드 `400-1`로 거부되고 인증·댓글 서비스가 호출되지 않는다.
- 실제 결과: 두 케이스 모두 상태·본문 코드·무호출 검증이 일치해 Pass했다.
- TF-TC-040·041 기대 결과: 댓글 내용 2자·100자는 HTTP 201과 본문 코드 `201-1`로 생성 요청이 처리된다.
- 실제 결과: 두 케이스 모두 기대 HTTP 201 대비 실제 200으로 상태 assertion이 실패했다.
- 코드 상관관계: `CommentController.createComment`는 본문 코드 `201-1`의 `RsData`를 반환하지만 HTTP 상태를 201로 지정하지 않는다. Review Controller의 TF-BUG-002와 유사한 기술 패턴이지만 별도 댓글 생성 API의 불일치이므로 영향 범위와 결함 묶음 여부는 사용자 재현 후 판단한다.
- 현재 판정: TF-TC-040·041 `Fail`. TF-EXEC-017 사용자 직접 재현 후 `TF-BUG-004 — 댓글 생성 성공 응답이 HTTP 201 대신 200을 반환함`, 상태 `Open`, 심각도 `Minor`, 재현 실행 2/2로 등록했다.

### TF-TC-026~028 인증 Security 실행 분석

- TF-TC-026: 유효 Bearer 토큰이 실제 Security 필터와 `AuthService`의 인증·작성자 판정을 거쳐 HTTP 200·본문 코드 `200-4`와 댓글 수정 Service 호출에 도달해 `Pass`했다.
- TF-TC-028: 유효하지 않은 JWT가 실제 JWT 필터에서 HTTP 401로 차단되고 사용자 조회·댓글 수정 Service가 호출되지 않아 `Pass`했다.
- TF-TC-027 기대·관찰 기준: 유효 `accessToken` 쿠키의 Security 인증·Controller 전달 결과를 관찰하며, 요청 처리 예외나 5xx·정상 사용자 차단은 위험 근거로 기록한다.
- TF-TC-027 실제 결과: JWT 필터는 쿠키 토큰으로 사용자 인증까지 완료했지만 `CommentController.updateComment`는 존재하지 않는 Authorization 헤더 값을 실제 `AuthService.getLoggedInMember`에 전달했다. `AuthService.java:161`의 `token.replace()`에서 NPE가 발생해 정상 응답과 댓글 수정 Service 호출에 도달하지 못했다.
- 판정: TF-TC-027 `Fail`, TF-RISK-008 `Reproduced`. TF-EXEC-032 사용자 실행에서도 동일 실패를 재현해 `TF-BUG-005 — 쿠키 전용 인증 요청이 Controller에서 null 토큰 NPE로 실패함`, 상태 `Open`, 심각도 `Major`, 재현 실행 2/2로 등록했다.
- 통제: assertion을 약화하지 않았고 제품 코드·설정·기존 테스트를 수정하지 않았다. 테스트 구성 오류가 아니므로 승인된 두 번째 실행을 사용하지 않았다.

### TF-TC-044·045·049 Repository/H2 실행 분석

- 기대 결과: 제목 `부산` 검색과 부산 장소 필터는 해당 리뷰만 `createdAt` 최신순으로 반환하고, 미존재 제목 검색은 빈 목록을 반환한다.
- 구현: `@DataJpaTest`와 내장 H2, 트랜잭션 롤백 fixture로 네 리뷰와 세 장소를 저장하고 ID 순서·필터 조건·빈 목록을 assertion하도록 작성했다.
- 실제 결과: TF-EXEC-019의 필수 secret import와 TF-EXEC-020의 QueryDSL Bean 차단을 테스트 전용 설정과 기존 `QueryDslConfig` import로 순차 해소했다. TF-EXEC-021에서 세 query·assertion이 모두 기대와 일치했다.
- 판정: TF-TC-044·045·049 모두 `Pass`. 이전 환경·설정 Blocked는 해소됐으며 신규 결함은 없다.
- 통제: 비밀 파일·제품 설정을 생성하거나 수정하지 않았고, 실제 외부 DB 대신 내장 H2와 테스트 트랜잭션 롤백을 사용했다.

### TF-TC-043 댓글 reviewId 누락·null 실행 분석

- 기대·관찰 기준: 정확한 HTTP 400 코드·본문 형식은 미명시이므로 임의로 고정하지 않는다. 다만 누락·null 입력이 5xx나 요청 처리 예외로 끝나지 않고 4xx 범위에서 거부되며 댓글이 저장되지 않아야 한다.
- 테스트 구성: `@DataJpaTest`, 내장 H2, 실제 `CommentService`·Repository·기존 `QueryDslConfig`, 독립형 MockMvc와 실제 `GlobalExceptionHandler`; `AuthService`만 Mock했다.
- 실제 결과: 필드 누락과 명시적 `null`은 모두 DTO의 `reviewId=null`로 변환돼 Controller·Service에 진입했다. `CommentService.java:30`의 `reviewRepository.findById(null)`에서 `InvalidDataAccessApiUsageException`과 원인 `IllegalArgumentException: The given id must not be null`이 발생해 응답을 생성하지 못했다.
- 저장 경계: 각 변형의 요청 전 댓글 count 조회는 실행됐지만 예외 때문에 요청 후 count assertion에는 도달하지 않았다. XML SQL 로그에 댓글 INSERT는 없고 예외가 저장 로직 전 리뷰 조회에서 발생한 점은 미저장의 보조 근거이며, 요청 후 count 직접 검증으로 과장하지 않는다.
- 판정: TF-TC-043 `Fail`, TF-RISK-009 `Reproduced`. 한 테스트케이스의 두 데이터 변형이 동일 원인으로 실패했으므로 누적 Fail은 1건만 추가한다. TF-EXEC-035 사용자 실행에서 동일 실패를 재현해 `TF-BUG-006 — 댓글 생성 시 reviewId 누락·null이 요청 처리 예외로 종료됨`, 상태 `Open`, 심각도 `Minor`, 재현 실행 2/2로 등록했다.
- 통제: 제품 불일치에 도달했으므로 assertion을 약화하지 않았고 두 번째 제품 실행을 사용하지 않았다. 제품·설정·기존 테스트·Baseline은 변경하지 않았다.

### TF-TC-001~003 공개 조회 실행 분석

- 테스트 구성: `@DataJpaTest`, 내장 H2, 실제 `ReviewController`·`CommentController`·`ReviewService`·`CommentService`·Repository·기존 `QueryDslConfig`, 독립형 MockMvc와 실제 `GlobalExceptionHandler`; `AuthService`만 Mock했다.
- TF-TC-001: Authorization 없는 `GET /api/reviews`가 HTTP 200·`200-5`를 반환하고 SORT-BASE 네 리뷰를 최신순 04→03→02→01로 제공했다. 댓글 수 3·2·4·1과 조회수 40·10·20·30이 실제 H2 집계와 일치했다.
- TF-TC-002: Authorization 없는 `GET /api/reviews/{id}`가 HTTP 200·`200-1`과 R-SORT-01의 ID·제목·평점 4.0·댓글 수 1을 반환했다.
- TF-TC-003: Authorization 없는 `GET /api/comments/review/{reviewId}`가 HTTP 200·`200-2`와 R-SORT-02 댓글 네 개를 고정 생성일 오름차순 ID·내용으로 반환했다.
- 세 요청 모두 `AuthService`가 호출되지 않았다. 실제 Spring Security 필터 체인을 포함하지 않았으므로 `permitAll` 동작 자체는 후속 실제 API 범위이며, Controller 이후 공개 조회 경로와 H2 데이터 결과를 검증한 것으로 한정한다.
- 판정: TF-TC-001~003 모두 `Pass`. 신규 결함 후보는 없으며 누적 P0는 49/49 제품 검증 도달, 38 Pass·11 Fail·Not Run 0·product-test Blocked 0이다.

## 6. 증거

- 테스트 코드: `backend/src/test/java/com/tripfriend/domain/review/service/ReviewServiceTest.java`
- 댓글 테스트 코드: `backend/src/test/java/com/tripfriend/domain/review/service/CommentServiceTest.java`
- Controller 테스트 코드: `backend/src/test/java/com/tripfriend/domain/review/controller/ReviewControllerTest.java`
- 댓글 Controller 테스트 코드: `backend/src/test/java/com/tripfriend/domain/review/controller/CommentControllerTest.java`
- Repository 테스트 코드: `backend/src/test/java/com/tripfriend/domain/review/repository/ReviewRepositoryTest.java` — TF-EXEC-021 기준 SHA-256 `2106E9EF4DD7F94CE6C61EF5461DD9113E8D5BA9FF495AB222AAE0B377458FCC`.
- 정렬 통합 테스트 코드: `backend/src/test/java/com/tripfriend/domain/review/service/ReviewSortingIntegrationTest.java` — TF-EXEC-023 기준 SHA-256 `1E6E7FBCEBACF7B3A48F685554A60B058AEE295AAEE735CDD00703E662FC9B01`.
- JUnit XML: `backend/build/test-results/test/TEST-com.tripfriend.domain.review.service.ReviewServiceTest.xml`
- TF-EXEC-009 JUnit XML: `backend/build/test-results/test/TEST-com.tripfriend.domain.review.service.CommentServiceTest.xml` — 6건, 실패·오류·건너뜀 0건.
- Gradle HTML: `backend/build/reports/tests/test/index.html`
- TF-EXEC-006 XML: TF-TC-011~013 3건, 실패·오류·건너뜀 0건.
- 실패 지점: `ReviewServiceTest.java`의 TF-TC-008 장소 관계 assertion.
- 사용자 증거: IntelliJ Test Results 첨부 화면 — TF-TC-007·009·010 성공, TF-TC-008 실패, 전체 1초 243ms.
- 사용자 로그: 서로 다른 `Place` 객체가 같은 객체여야 한다는 assertion 불일치와 `ReviewServiceTest.java:103` 호출 위치.
- TF-EXEC-007 사용자 증거: [ReviewService 사용자 실행 화면](../evidence/TF-EXEC-007-review-service-user-run.png) — 최신 10건 중 9 Pass·1 Fail, 전체 1초 219ms; 114,600바이트, 보존본 SHA-256 `3031E3CD1E33B62BC3AB639E1518829CA67E34818EFF8F1328DBBF933044545B`.
- TF-EXEC-007 실패 지점: `ReviewServiceTest.updateReview_changesPlaceForAuthor(ReviewServiceTest.java:189)`.
- 결함 보고서: `projects/tripfriend/defects/TF-BUG-001-review-place-not-updated.md`.
- TF-EXEC-010 사용자 실행: IntelliJ `CommentServiceTest` 6/6 Pass, 전체 1초 168ms, `BUILD SUCCESSFUL in 9s`. 첨부 화면은 검토했지만 중복 Pass 증거이므로 별도 파일로 보존하지 않았다.
- TF-EXEC-011 JUnit XML: `backend/build/test-results/test/TEST-com.tripfriend.domain.review.controller.ReviewControllerTest.xml` — 4건, 2 Fail, 오류·건너뜀 0건; TF-TC-030·031 모두 기대 201 대비 실제 200.
- TF-EXEC-012 사용자 증거: [ReviewController 사용자 실행 화면](../evidence/TF-EXEC-012-review-controller-user-run.png) — 4건 중 TF-TC-029·032 Pass, TF-TC-030·031 Fail, 전체 2초 401ms; 92,520바이트, 보존본 SHA-256 `EA3385729F4FA45C325822126A4FA5664DDD8B112FC9085540152063F10EF699`.
- TF-EXEC-012 사용자 로그: 두 실패 모두 `Status expected:<201> but was:<200>`, `BUILD FAILED in 4s`.
- 결함 보고서: `projects/tripfriend/defects/TF-BUG-002-review-create-http-status-200.md`.
- TF-EXEC-013 JUnit XML: `backend/build/test-results/test/TEST-com.tripfriend.domain.review.controller.ReviewControllerTest.xml` — 필터 실행 8개 변형, 2 Pass·6 Fail, 오류·건너뜀 0건.
- TF-EXEC-014 사용자 증거: [최신 ReviewController 사용자 실행 화면](../evidence/TF-EXEC-014-review-controller-user-run.png) — 최신 전체 12개 중 4 Pass·8 Fail, 전체 2초 631ms; 158,556바이트, 보존본 SHA-256 `91A8E3E15E9FA6C0F936557209D20C019DFEFA0342885140C69ED2A31A50AA25`.
- TF-EXEC-014 핵심 로그: TF-TC-038 평점 0·6 모두 JSON `$.code` 기대 `400-2`·실제 `400-1`; 전체 `BUILD FAILED in 4s`.
- TF-EXEC-016 JUnit XML: `backend/build/test-results/test/TEST-com.tripfriend.domain.review.controller.CommentControllerTest.xml` — 4건, 2 Pass·2 Fail, 오류·건너뜀 0건; TF-TC-040·041 모두 기대 201 대비 실제 200.
- TF-EXEC-017 사용자 증거: [CommentController 사용자 실행 화면](../evidence/TF-EXEC-017-comment-controller-user-run.png) — 4건 중 TF-TC-039·042 Pass, TF-TC-040·041 Fail, 전체 2초 366ms; 보존본 55,715바이트, SHA-256 `FD6B2A41088E71624F00EDA628E397917F4DA2B42AE804C06C7D7F0CC9F6688E`.
- TF-EXEC-017 사용자 로그: TF-TC-040·041 모두 `Status expected:<201> but was:<200>`, `CommentControllerTest.java:78`, `:94`.
- 갱신 결함 보고서: `projects/tripfriend/defects/TF-BUG-002-review-create-http-status-200.md` — TF-TC-030·031·034·035·037, Open·Minor·4/4.
- 신규 결함 보고서: `projects/tripfriend/defects/TF-BUG-003-review-rating-error-code-mismatch.md` — TF-TC-038, Open·Minor·2/2.
- 신규 결함 보고서: `projects/tripfriend/defects/TF-BUG-004-comment-create-http-status-200.md` — TF-TC-040·041, Open·Minor·2/2.
- TF-EXEC-019 JUnit XML: `backend/build/test-results/test/TEST-com.tripfriend.domain.review.repository.ReviewRepositoryTest.xml` — 3건 모두 동일 `application-secret.yml` 누락에 따른 컨텍스트 로딩 차단.
- TF-EXEC-020 JUnit XML: 같은 경로의 최신 XML — 3건 모두 동일 `JPAQueryFactory` Bean 누락에 따른 컨텍스트 로딩 차단.
- TF-EXEC-021 JUnit XML: 같은 경로의 최신 XML — 3건, 실패·오류·건너뜀 0건.
- TF-EXEC-022 사용자 증거: [ReviewRepository 사용자 실행 화면](../evidence/TF-EXEC-022-review-repository-user-run.png) — 3건 모두 Pass, 전체 873ms; 보존본 45,182바이트, SHA-256 `FDF5316E92FFCBF41F7386529B92D7BD5157B946EFFEB92CF58EC881D0C90A8C`.
- TF-EXEC-023 JUnit XML: `backend/build/test-results/test/TEST-com.tripfriend.domain.review.service.ReviewSortingIntegrationTest.xml` — 정렬 4종, 실패·오류·건너뜀 0건.
- TF-EXEC-024 사용자 증거: [정렬 통합 사용자 실행 화면](../evidence/TF-EXEC-024-review-sorting-user-run.png) — 정렬 4종 모두 Pass, 전체 1초 20ms; 보존본 57,417바이트, SHA-256 `10A2835E5BC2BA1F3257C6516FE0D8773F04CCE099185239E24F46ACFD460DD6`.
- TF-EXEC-025 JUnit XML: `backend/build/test-results/test/TEST-com.tripfriend.domain.review.controller.ReviewCommentNotFoundIntegrationTest.xml` — 미존재 상세 조회 2건, 실패·오류·건너뜀 0건.
- TF-EXEC-026 사용자 증거: [미존재 상세 조회 사용자 실행 화면](../evidence/TF-EXEC-026-review-comment-not-found-user-run.png) — TF-TC-047·048 모두 Pass, 전체 1초 576ms; 보존본 43,055바이트, SHA-256 `1A0E4461EECFBA0FC78645BBC0293639D9591B6AA6029093B7B8B94B350CEE04`.
- Security 테스트 코드: `backend/src/test/java/com/tripfriend/domain/review/controller/ReviewCommentUnauthenticatedSecurityTest.java` — TF-TC-020~025 구현, JPA auditing 차단 최소 수정 후 6,430바이트, SHA-256 `23E99B7DA3C9DB6AE07FE56BD1AA3C67D737A2E36C3D321B460FA1F615C58A18`, TF-EXEC-029 실행 대상 상태.
- TF-EXEC-028 JUnit XML: `backend/build/test-results/test/TEST-com.tripfriend.domain.review.controller.ReviewCommentUnauthenticatedSecurityTest.xml` — 6건 모두 동일한 빈 JPA metamodel ApplicationContext 차단, 제품 요청·401 assertion 미도달.
- TF-EXEC-029 JUnit XML: 같은 경로의 최신 XML — 6건, 실패·오류·건너뜀 0건; 여섯 요청 모두 HTTP 401·Controller 의존성 미호출 assertion 통과.
- TF-EXEC-030 사용자 증거: [비로그인 Security 사용자 실행 화면](../evidence/TF-EXEC-030-review-comment-unauthenticated-security-user-run.png) — TF-TC-020~025 모두 Pass, 전체 158ms; 78,502바이트, 보존본 SHA-256 `3314F28CF1885E5377E92D0AE6E93E62A8E0AABAC2763EEF099281D2C909D371`, 첨부 원본과 일치.
- 인증 Security 테스트 코드: `backend/src/test/java/com/tripfriend/domain/review/controller/ReviewCommentAuthenticatedSecurityTest.java` — TF-TC-026~028 구현, 8,606바이트, SHA-256 `3FBF1CFF979396466EBD01EF53F379D3B4924E3685BB3BE58B9FFE3799D6A976`.
- TF-EXEC-031 JUnit XML: `backend/build/test-results/test/TEST-com.tripfriend.domain.review.controller.ReviewCommentAuthenticatedSecurityTest.xml` — 3건 중 TF-TC-026·028 Pass, TF-TC-027 Fail; `AuthService.java:161` null 토큰 NPE와 `CommentController.java:80` 호출 경로 기록.
- TF-EXEC-032 사용자 증거: [인증 Security 사용자 실행 화면](../evidence/TF-EXEC-032-review-comment-authenticated-security-user-run.png) — TF-TC-026·028 Pass, TF-TC-027 Fail, 전체 512ms; 54,498바이트, 보존본 SHA-256 `ECB01C7531EA2CAF190A551DAC681A55F656D4DEB7EB9B48142A99828F052F3A`, 첨부 원본과 일치.
- 결함 보고서: `projects/tripfriend/defects/TF-BUG-005-cookie-only-authentication-npe.md` — Open·Major·2/2.
- `reviewId` 검증 통합 테스트 코드: `backend/src/test/java/com/tripfriend/domain/review/controller/CommentReviewIdValidationIntegrationTest.java` — TF-TC-043 두 변형 구현, 4,256바이트, SHA-256 `56F4D1209631A3B8DBC869DA76394CDAE3386CBCF6E5C12C89AEDBE0AD1B35FA`.
- TF-EXEC-034 JUnit XML: `backend/build/test-results/test/TEST-com.tripfriend.domain.review.controller.CommentReviewIdValidationIntegrationTest.xml` — 2건 모두 요청 처리 예외로 Fail; `CommentService.java:30`의 `findById(null)`과 `The given id must not be null` 기록.
- TF-EXEC-035 사용자 증거: [댓글 reviewId 검증 사용자 실행 화면](../evidence/TF-EXEC-035-comment-review-id-validation-user-run.png) — TF-TC-043-A·B 모두 Fail, 전체 1초 428ms; 45,792바이트, 보존본 SHA-256 `E2B329AE6E17DF546A67E89899C9FC5F46D2E2F2983607365498C494609E58EE`, 첨부 원본과 일치.
- 결함 보고서: `projects/tripfriend/defects/TF-BUG-006-comment-review-id-null-request-processing-exception.md` — Open·Minor·2/2; 9,386바이트, SHA-256 `1BAD8E66B3656E4C061EA1EA460C42AD3E3857B5C77CA9B405D1DF0F042A1AF0`.
- 공개 조회 통합 테스트 코드: `backend/src/test/java/com/tripfriend/domain/review/controller/PublicReviewCommentQueryIntegrationTest.java` — TF-TC-001~003 구현, 11,187바이트, SHA-256 `FAA214C280ADB7C48ED6D1F710AD5C6B95B0E268CA53351DC8DE99CFB547023D`.
- TF-EXEC-036 JUnit XML: `backend/build/test-results/test/TEST-com.tripfriend.domain.review.controller.PublicReviewCommentQueryIntegrationTest.xml` — 3건, 실패·오류·건너뜀 0건.
- TF-EXEC-037 사용자 증거: [공개 조회 통합 사용자 실행 화면](../evidence/TF-EXEC-037-public-review-comment-query-user-run.png) — TF-TC-001~003 모두 Pass, 전체 1초 908ms; 51,020바이트, 보존본 SHA-256 `84B4EABF65F8C460FCC27294FF780519F6CFB48EF239A5E2663EE35F86269CED`, 첨부 원본과 일치.
- 사용자 전체 원본 로그는 반복 프레임과 로컬 절대경로를 포함해 별도 증거 파일로 복사하지 않고 핵심 구간만 결함 보고서에 정리했다.

생성된 build 결과는 이번 실행의 로컬 증거이며 그대로 공개 저장소에 업로드하지 않는다. 공개 증거는 로컬 사용자명·절대경로·호스트명 등 환경 식별 정보를 제거한 뒤 별도 선별한다.

## 7. 확인 한계와 남은 작업

- Service 결과는 Mock 단위 테스트이고 TF-TC-029~038은 독립형 MockMvc로 HTTP 상태·`RsData.code`·DTO Bean Validation을 검증했다. TF-TC-020~025는 실제 Security 필터 체인의 비로그인 요청 차단과 Controller 의존성 미호출을 검증했지만 실제 서버·Redis·DB 관계 갱신은 검증하지 않았다.
- P0 TF-TC-001~049 전체가 제품 검증 지점에 도달했으며 38 Pass·11 Fail·Not Run 0·product-test Blocked 0이다.
- Repository/H2 fixture와 테스트 코드는 준비·실행해 TF-TC-044~049가 Pass했다. TF-TC-047·048은 Controller·실제 Service·H2 Repository·공통 예외 처리기를 포함하지만 실제 서버·MySQL·Security 동작은 검증하지 않았다.
- TF-TC-004~006 리뷰 생성 서비스 단위는 모두 Pass했다.
- TF-BUG-001은 TF-EXEC-004에서도 동일하게 재현됐다. 제품 코드 수정·수정 후 재검증은 `Not Run`이다.
- 사용자가 최신 `ReviewServiceTest`를 직접 실행해 신규 TF-TC-004~006과 전체 6 Pass·1 Fail을 재현했다.
- TF-TC-011~013 리뷰 삭제 서비스 단위는 TF-EXEC-006에서 3 Pass이며 TF-TC-008은 필터에서 제외했다.
- 사용자가 TF-EXEC-007에서 전체 10건을 직접 실행해 TF-TC-011~013 Pass와 TF-TC-008 동일 Fail을 재현했다.
- TF-TC-014~019 댓글 CRUD 서비스 단위는 TF-EXEC-009에서 6 Pass다.
- 사용자가 TF-EXEC-010에서 `CommentServiceTest` 6건 전체를 직접 실행해 동일 6 Pass를 재현했다. 화면에 실행 날짜와 프로젝트 절대경로가 없어 임의로 기록하지 않는다.
- 사용자는 Mock이 실제 DB 저장소를 대신해 서비스 규칙을 격리한다는 점, assertion이 상태·반환값·예외를 검증하고 verify가 Mock 메서드 호출·미호출을 검증한다는 점을 설명했다.
- 사용자는 `403-2`가 댓글 작성자와 다른 사용자의 수정·삭제 차단, `404-3`이 존재하지 않는 리뷰에 대한 댓글 생성 차단임을 구분해 설명했다.
- TF-RISK-009는 TF-TC-043의 `reviewId` 누락·null 두 변형이 모두 `findById(null)` 요청 처리 예외로 실패해 `Reproduced`로 전환했다.
- 기존 Service 대표 사용자 재현·설명 체크포인트는 충족했다.
- TF-TC-029~032는 TF-EXEC-011과 TF-EXEC-012에서 동일하게 2 Pass·2 Fail이다. TF-TC-030·031의 기대 201·실제 200은 사용자 직접 재현까지 완료했다.
- TF-EXEC-012 시점에는 화면을 보존하고 `TF-BUG-002 Open · Minor · 2/2`로 최초 등록했다. 이후 TF-EXEC-013·014와 영향 케이스를 추가해 현재 4/4이며 실제 클라이언트 영향과 수정 후 재검증은 `Not Run`이다.
- 사용자 설명 체크포인트 완료: HTTP 201 지정 코드가 누락돼 Spring MVC 기본 성공 상태 200이 반환되고 Baseline 기대 결과와 달라졌다고 설명했다. 응답 본문의 `201-1`은 애플리케이션 전용 코드이므로 HTTP 상태를 자동 변경하지 않는다는 구분을 확인했다.
- TF-TC-033~038은 TF-EXEC-013에서 테스트케이스 기준 2 Pass·4 Fail이며 누적 P0는 19 Pass·7 Fail·23 Not Run이다.
- 사용자가 TF-EXEC-014에서 최신 `ReviewControllerTest` 전체 12개를 실행해 4 Pass·8 Fail을 재현했다. TF-BUG-002는 영향 케이스 확장·4/4로 갱신했고 TF-TC-038은 `TF-BUG-003 Open · Minor · 2/2`로 등록했다.
- 사용자 학습 체크포인트 완료: HTTP 400이 일치해도 본문 애플리케이션 코드 `400-2`·`400-1`이 다르면 전체 테스트는 Fail이라는 핵심을 사용자 표현으로 확인했다. TF-BUG-002는 정상 생성의 HTTP 상태 문제, TF-BUG-003은 범위 밖 평점의 오류 본문 코드 문제라 발생 조건·원인·수정 지점이 달라 분리한다는 기준은 설명 후 이해 완료를 확인했다.
- TF-TC-039~042는 TF-EXEC-016·017에서 동일 2 Pass·2 Fail이며, TF-TC-040·041의 기대 HTTP 201·실제 200을 `TF-BUG-004 Open · Minor · 2/2`로 등록했다.
- 사용자 증거 보존과 TF-BUG-004 등록은 완료했지만 실제 서버·DB·프론트 영향과 수정 후 재검증은 `Not Run`이다.
- 사용자 설명 체크포인트 완료: 응답 본문 `201-1`은 HTTP 상태를 설정하지 않으므로 명시적 201 처리가 없으면 Spring MVC 기본 성공 상태 200이 반환된다는 점을 사용자 표현으로 확인했다.
- 사용자 설명 체크포인트 완료: TF-BUG-002·004는 기술 패턴은 같지만 별도 Controller·엔드포인트 케이스이므로 분리한다는 핵심을 확인했다.
- TF-EXEC-021에서 기존 `QueryDslConfig`를 테스트 클래스에만 import해 TF-TC-044·045·049가 3 Pass했고 이전 3 Blocked를 해소했다.
- TF-EXEC-022 사용자 IntelliJ 실행에서도 동일 3/3 Pass·873ms를 재현했고 대표 Repository/H2 증거를 보존했다. 누적 테스트케이스 결과는 중복 합산하지 않는다.
- Repository/H2 설명 체크포인트는 실제 JPA·H2와 Mock의 차이를 사용자 표현으로 확인하고 설정·QueryDSL 용어는 안내 후 이해 확인으로 완료했다.
- TF-EXEC-023에서 TF-TC-046 정렬 4종이 모두 Pass했다. 실행 항목은 4개지만 P0 테스트케이스 기준 1 Pass로 합산한다.
- TF-EXEC-024 사용자 IntelliJ 실행에서도 동일 4/4 Pass·1초 20ms를 재현했고 대표 정렬 통합 증거를 보존했다. 누적 테스트케이스 결과는 중복 합산하지 않는다.
- TF-EXEC-025에서 TF-TC-047·048 미존재 상세 조회가 실제 Service·Repository/H2와 MockMvc·공통 예외 처리기를 거쳐 2 Pass했다.
- TF-EXEC-026 사용자 IntelliJ 실행에서도 동일 2/2 Pass·1초 576ms를 재현했고 대표 미존재 상세 조회 증거를 보존했다. 누적 테스트케이스 결과는 중복 합산하지 않는다.
- TF-EXEC-027은 샌드박스 Gradle 사용자 홈의 잠금 경로에서 테스트 전에 차단됐고, TF-EXEC-028은 테스트 컴파일 후 `@EnableJpaAuditing`의 빈 JPA metamodel 오류로 여섯 Security 케이스 모두 제품 검증 전에 중단됐다.
- TF-EXEC-028 시점에는 테스트 파일에 `JpaMetamodelMappingContext` Mock을 추가해 확인된 직접 원인에 대한 최소 수정을 준비했지만, 당시 승인된 최대 2회 실행을 모두 사용했으므로 수정 직후 상태는 `Not Run`이고 TF-TC-020~025는 `Blocked`를 유지했다.
- TF-EXEC-029에서 JPA auditing 차단을 해소하고 TF-TC-020~025 여섯 요청이 HTTP 401·Controller 의존성 미호출로 모두 Pass해 이전 6 Blocked를 해소했다.
- TF-EXEC-030 사용자 IntelliJ 실행에서도 동일 6/6 Pass·158ms를 재현했고 대표 Security 증거를 보존했다. 같은 TF-TC-020~025 재현이므로 누적 테스트케이스 결과는 중복 합산하지 않는다.
- 사용자가 HTTP 401과 Controller 의존성 미호출이 증명하는 범위, Web MVC slice·Mock 때문에 실제 서버·JWT·Redis·DB를 증명하지 못하는 범위를 본인 표현으로 설명해 Security 학습 체크포인트를 완료했다.
- TF-EXEC-031에서 TF-TC-026·028은 Pass, TF-TC-027은 쿠키 토큰의 Security 인증 후 Controller→AuthService null 헤더 전달 NPE로 Fail했다. 누적은 35 Pass·10 Fail·4 Not Run·product-test Blocked 0이다.
- TF-EXEC-032 사용자 IntelliJ 실행에서도 동일 2 Pass·1 Fail·512ms와 TF-TC-027 null 토큰 NPE를 재현했다. 증거를 보존하고 `TF-BUG-005 Open·Major·2/2`로 등록했으며 같은 테스트케이스 재현이므로 누적 수치는 중복 합산하지 않는다.
- TF-BUG-005 학습 체크포인트 완료: 사용자는 Security 필터와 Controller의 토큰 전달 방식 차이 때문에 쿠키 전용 정상 사용자의 변경 API가 실패하며, 실제 API·프론트에서 헤더·쿠키 전달을 비교하기 전에는 영향 범위를 확대할 수 없다고 설명했다.
- TF-EXEC-033은 샌드박스 Gradle 잠금 경로에서 제품 검증 전에 중단됐고 동일 명령의 TF-EXEC-034 외부 실행으로 해소됐다.
- TF-EXEC-034에서 TF-TC-043 누락·null 두 변형이 동일 `findById(null)` 요청 처리 예외로 Fail했다. TF-RISK-009를 `Reproduced`로 전환하고 누적을 35 Pass·11 Fail·3 Not Run·product-test Blocked 0으로 갱신했다.
- TF-EXEC-035 사용자 IntelliJ 실행에서도 동일 2 Fail·1초 428ms와 누락·null 두 변형의 `The given id must not be null`을 재현했다. 증거를 보존하고 `TF-BUG-006 Open·Minor·2/2`로 등록했으며 같은 테스트케이스 재현이므로 누적 수치는 중복 합산하지 않는다.
- 사용자 핵심 로그는 두 테스트명·요청 처리 예외·동일 null ID 원인과 호출 위치를 포함해 재현 판정에 충분하다. 반복 Spring·JUnit 프레임을 생략한 것은 증거 부족이 아니다.
- TF-BUG-006 학습 체크포인트 완료: 사용자는 JSON 필드 누락과 명시적 null이 모두 Java DTO의 null이 되는 이유, `@NotNull` 부재로 `@Valid`를 통과하는 흐름, MockMvc 응답 전 예외와 요청 후 count assertion 미도달 때문에 실제 서버 HTTP 500·DB 미저장을 직접 확정할 수 없는 한계를 설명했다.
- TF-EXEC-036에서 TF-TC-001~003 공개 조회가 3 Pass·`BUILD SUCCESSFUL in 22s`로 완료돼 누적 P0 49/49 실행, 38 Pass·11 Fail·Not Run 0·product-test Blocked 0이 됐다.
- TF-EXEC-037 사용자 IntelliJ 실행에서도 TF-TC-001~003이 동일 3 Pass·1초 908ms로 재현됐다. P0 49/49 실행 완료 대표 증거를 보존했고 같은 테스트케이스 재현이므로 누적 수치는 중복 합산하지 않는다.
- Kotlin 제한적 감사는 별도 `kotlin-limited-audit.md`에 기록했다. `main`·`ee116b0a40ed1dfe38db3f83363ac1e5a118915d`의 기존 ReviewService MockK 테스트 28개와 리뷰·댓글 핵심 계약을 정적으로 대조했고 TF-KAUD-001~005를 정적 회귀 위험·계약 관찰로 분리했다.
- Kotlin 빌드·테스트·서버·DB는 실행하지 않아 Kotlin 제품 테스트는 `Not Run`이며 Java P0 수치와 결함 재현 횟수에 합산하지 않는다.
- Kotlin MockK 설명 체크포인트는 사용자가 `any()`·미리 정한 Mock getter의 한계와 정적 관찰을 결함으로 확정하지 않는 이유를 본인 표현으로 설명해 완료했다.
- Stage 6 공개 후보는 실행 보고서·Kotlin 감사·결함 6건·증거 11개·Java 테스트 11개와 자동화 README로 구성하고 로컬 경로·민감정보·상대 링크·수치 정합성을 검토했다.
- 장시간 중단 지점: 게시용 작업본에 Stage 6 공개 후보를 준비하고 검증한 직후다.
- 다음 확인 지점: Stage 6 공개 후보의 Git 반영 전 정확한 커밋 메시지·포함·제외 파일·검증 결과·대상 저장소와 브랜치를 사용자에게 제시하고 별도 승인받는다. 7단계는 별도 착수 승인 전까지 미시작이다.

## 8. Stage 8 테스트 구성 오탐 교정

### 8.1 정정 근거와 판정 원칙

- Stage 7 실제 서버 검증에서 `ReviewController.createReview`는 HTTP 201을 반환했다. 이 결과로 TF-BUG-002는 제품 결함이 아니라 독립형 MockMvc가 런타임 `ResponseAspect`를 포함하지 않은 테스트 구성 오탐임을 확인했다.
- TF-EXEC-011~014의 원본 실행 이력은 삭제하지 않는다. 다만 TF-TC-030·031·034·035·037의 당시 Fail은 제품 검증에 유효한 근거가 아니므로 교정 실행 전에는 `Blocked`, 교정 실행 후에는 실제 결과에 따라 다시 판정한다.
- `ReviewControllerTest`는 실제 `ReviewController`·`GlobalExceptionHandler`·`ResponseAspect`가 참여하는 Spring Web MVC slice로 교정했다. `ReviewService`·`AuthService`·JPA metamodel은 Mock하고 Security 필터는 비활성화했다.
- 제품 코드와 기존 기대 assertion은 변경하지 않았다.

### 8.2 Stage 8 실행 이력

| 실행 ID | 명령·환경 | 검증 지점 도달 | 결과 | 처리 |
|---|---|---|---|---|
| TF-S8-PREP-001 | 지정 Gradle 명령의 샌드박스 실행 | 아니요 | `C:\\.gradle` 잠금 파일 부모 경로 생성 실패 | 제품 결과가 아닌 실행 환경 차단. 승인된 외부 실행으로 전환 |
| TF-S8-EXEC-001 | `ReviewControllerTest` 지정 오프라인 외부 실행 | 아니요 | 12개 모두 ApplicationContext 로딩 실패; `DeletedMemberFilter`가 요구하는 `JwtUtil` Bean 누락 | 제품 Fail로 계산하지 않음. Web MVC slice에서 목적 밖 필터를 제외하도록 테스트 구성 보완 |
| TF-S8-EXEC-002 | 동일 지정 오프라인 외부 실행 | 예 | 12 invocations, 10 Pass·2 Fail, errors 0·skipped 0, `BUILD FAILED in 16s` | TF-TC-030·031·034·035·037 Pass. TF-TC-038의 0·6 두 변형만 기대 `400-2` 대비 실제 `400-1`로 Fail |
| TF-S8-EXEC-003 | IntelliJ에서 교정된 `ReviewControllerTest` 전체 클래스 실행 | 예 | 사용자 화면 기준 12 tests, 10 Pass·2 Fail, 전체 522ms | TF-S8-EXEC-002와 동일 재현. TF-TC-038 평점 0·6만 Fail; 누적 케이스 수 중복 합산 안 함 |

TF-S8-EXEC-002의 JUnit XML은 `tests="12"`, `failures="2"`, `errors="0"`, `skipped="0"`, 타임스탬프 `2026-09-02T03:29:51`, suite time `0.647`을 기록했다. Gradle의 `BUILD FAILED`는 TF-TC-038 두 변형의 assertion 불일치 때문에 발생한 클래스 종합 결과이며 나머지 10개 변형의 Pass를 무효화하지 않는다.

### 8.3 Stage 8 교정 당시 테스트케이스 판정

| 케이스 | Stage 6 기록 | 오탐 확인 후 임시 판정 | TF-S8-EXEC-002 실제 결과 | 현재 판정 |
|---|---|---|---|---|
| TF-TC-030 | Fail | Blocked | HTTP 201·본문 `201-1` 일치 | Pass |
| TF-TC-031 | Fail | Blocked | HTTP 201·본문 `201-1` 일치 | Pass |
| TF-TC-034 | Fail | Blocked | HTTP 201·본문 `201-1` 일치 | Pass |
| TF-TC-035 | Fail | Blocked | HTTP 201·본문 `201-1` 일치 | Pass |
| TF-TC-037 | Fail | Blocked | HTTP 201·본문 `201-1` 일치 | Pass |
| TF-TC-038 | Fail | Fail | 평점 0·6 모두 HTTP 400, 기대 `400-2` 대비 실제 `400-1` | Fail · TF-BUG-003 |

- 현재 P0 판정: **43 Pass·6 Fail·0 Blocked·0 Not Run**.
- 현재 Pass: TF-TC-001~007·009~026·028~037·039·042·044~049, 총 43개.
- 현재 Fail: TF-TC-008·027·038·040·041·043, 총 6개.
- TF-BUG-002는 삭제하지 않고 `Closed — Not a Product Defect / Test Setup False Positive`로 보존한다.
- 현재 Open 제품 결함은 TF-BUG-001·003~008, 총 7건이다.
- TF-S8-EXEC-003 사용자 증거: [교정 ReviewController 사용자 실행 화면](../evidence/TF-S8-EXEC-003-review-controller-user-run.png) — 12개 중 10 Pass·2 Fail, 전체 522ms, TF-TC-038 평점 0·6 두 변형만 Fail; 109,409바이트, SHA-256 `F31B4A6B9BA2B05C4C1DBBF7B42F8E6C7F161F7B10C9BF5D2294842B14B969B4`.
- 사용자 IntelliJ 교정 재현은 TF-S8-EXEC-002와 동일하며 현재 P0 집계에 중복 합산하지 않는다. 화면에 실행 시각·프로젝트 절대경로가 없어 임의로 기록하지 않는다.

</details>
