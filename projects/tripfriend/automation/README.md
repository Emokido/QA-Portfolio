# TripFriend 핵심 백엔드·브라우저 자동화

이 폴더는 TripFriend Java/Spring 리뷰·댓글의 핵심 우선순위 테스트(P0)와 Playwright 프런트 E2E를 제품 코드와 분리해 보관한다.

- 제품 기준 저장소: `https://github.com/Emokido/tripfriend-spring`
- 기준 브랜치·커밋: `main` / `e2431223ce2a18c8f944d544cba76b951ce0356d`
- Java·Spring Boot: Java 17 / 빌드 파일 기준 Spring Boot 3.2.4
- 도구: JUnit 5, Mockito, AssertJ, Spring MockMvc, Spring Security Test, Spring Data JPA `@DataJpaTest`, H2
- 실행 기간: 2026-08-28~2026-09-02
- 최신 누적 판정: P0 49개 중 45 Pass·4 Fail·0 Blocked·0 Not Run (2026-09-05 댓글 Controller 교정 반영)

> 이 폴더만으로 독립 실행되는 프로젝트가 아니다. 위 기준 제품 저장소의 같은 패키지 구조에 적용한 테스트 소스이며, 제품 코드와 Gradle Wrapper·의존성·테스트 프로필이 필요하다.

## 자동화 구성

- `backend/`: JUnit 5·Mockito·MockMvc·H2 기반 P0 백엔드 자동화 사본
- `playwright/`: 정상 회귀 5개 실행 단위와 등록 결함 재현(Known Defect) 3개 실행 변형을 분리한 프런트 E2E 독립 프로젝트

Playwright 정상 회귀는 CORS·접근성 속성·수정 폼 초기화를 교정한 QA 사본의 기존 전체 실행에서 `5 Pass·0 Fail·0 Blocked`다. Known Defect 3개 실행 변형은 `0 Pass·3 Fail·0 Blocked`로 TF-BUG-010과 TF-BUG-014의 1자·101자 경계를 재현했다. 101자의 dialog 처리 지연을 교정한 단일 재실행에서도 같은 제품 Fail과 fixture cleanup을 확인했다. 설치된 Chrome channel을 사용하고 실제 계정 값은 환경변수로만 받는다. 실행·데이터 격리·한계는 [Playwright README](playwright/README.md)를 사용한다.

후속 작업에서는 정상 회귀 5개를 GitHub Actions CI에 연결하고, 등록 결함 재현 실행은 필수 회귀 CI 작업과 분리할 예정이다. 워크플로와 CI 실행 결과는 아직 생성·실행하지 않았다.

## 테스트 클래스

| 계층 | 클래스 | 주요 범위 | 기록 결과 |
|---|---|---|---|
| Service | `ReviewServiceTest` | 리뷰 생성·수정·삭제, 권한·미존재·여행지 변경 | 최신 10개 실행 중 9 Pass·1 Fail |
| Service | `CommentServiceTest` | 댓글 CRUD, 작성자 권한·미존재 리뷰·댓글 | 6 Pass |
| Controller | `ReviewControllerTest` | 제목·내용·평점 경계와 HTTP·본문 코드 | 테스트 구성 교정 후 12개 변형 중 10 Pass·2 Fail |
| Controller | `CommentControllerTest` | 댓글 내용 경계와 HTTP·본문 코드 | 테스트 구성 교정 후 4 Pass |
| Repository/H2 | `ReviewRepositoryTest` | 제목 검색·장소 필터·결과 없음 | 3 Pass |
| Service·Repository/H2 | `ReviewSortingIntegrationTest` | 최신순·평점순·댓글순·조회수순 | 정렬 4개 변형 모두 Pass (TF-TC-046) |
| Controller·Service·Repository/H2 | `ReviewCommentNotFoundIntegrationTest` | 미존재 리뷰·댓글 상세 404와 데이터 불변 | 2 Pass |
| Security/Controller | `ReviewCommentUnauthenticatedSecurityTest` | 비로그인 변경 요청 6건의 HTTP 401·Controller 미호출 | 6 Pass |
| Security/Controller | `ReviewCommentAuthenticatedSecurityTest` | Bearer·쿠키·유효하지 않은 JWT 전달 | 2 Pass·1 Fail |
| Controller·Service·Repository/H2 | `CommentReviewIdValidationIntegrationTest` | 댓글 `reviewId` 누락·null 두 변형 | 2 Fail |
| Controller·Service·Repository/H2 | `PublicReviewCommentQueryIntegrationTest` | 비로그인 목록·상세·댓글 조회 | 3 Pass |

세부 실행 ID, 기대·실제 결과, 환경 차단 이력과 증거는 [P0 백엔드 테스트 실행 보고서](../reports/p0-backend-test-execution-report.md)를 사용한다.

## 구조

```text
backend/src/test/java/com/tripfriend/domain/review/
├─ controller/
├─ repository/
└─ service/
```

제품 소스의 패키지 이름을 유지해 어떤 계층과 협력 객체를 검증하는지 확인할 수 있도록 했다.

## 실행 예시

제품 저장소의 `backend`에서 개별 클래스를 다음 형식으로 실행한다.

```powershell
.\gradlew.bat test --tests "com.tripfriend.domain.review.service.ReviewServiceTest" --no-daemon --offline
```

실행 명령은 예시이며 이 포트폴리오 저장소에서 실행한 결과가 아니다. 실제 기록은 기준 제품 실행용 작업본에서 생성했으며, 기록된 IntelliJ 재현 결과와 연결했습니다.

## 테스트 수준별 한계

- Mockito Service 단위 테스트는 도메인 규칙과 협력 객체 호출을 격리하지만 실제 DB·HTTP·Security 필터를 검증하지 않는다.
- `ReviewControllerTest` Web MVC slice는 실제 `ReviewController`·`GlobalExceptionHandler`·`ResponseAspect`를 포함해 입력 검증·HTTP·본문 계약을 확인한다. Service·인증 협력 객체는 Mock하고 Security 필터는 비활성화하므로 실제 서버와 전체 Spring Security 체인을 검증하지 않는다.
- `@DataJpaTest`·H2 테스트는 JPA 저장·조회·정렬을 확인하지만 MySQL·Redis·운영 설정과 동일함을 보장하지 않는다.
- Security Web MVC slice는 실제 `SecurityConfig`와 필터를 포함하지만 JWT·Redis 등 외부 협력 객체는 테스트 목적에 따라 Mock했다.
- 공개 조회 통합 테스트는 실제 Controller·Service·Repository·H2를 사용하지만 Spring Security 필터 체인은 포함하지 않았다.
- Playwright 정상 회귀는 localhost 프런트·백엔드를 대상으로 수정 후 전체 실행해 5 Pass·0 Fail·0 Blocked다. Known Defect 실행 변형은 0 Pass·3 Fail·0 Blocked이며 정상 회귀 집계와 분리한다. 외부 MySQL·OAuth·메일·배포 환경은 검증 범위가 아니다.

## 결과·결함 경계

- `BUILD FAILED` 자체를 모든 테스트의 Fail로 처리하지 않고 개별 검증 지점 도달 여부로 판정했다.
- 환경·테스트 구성 문제는 제품 Fail과 분리하고 해소 후 같은 케이스를 다시 판정했다.
- 현재 Open 결함 기록은 [결함 보고서 폴더](../defects/)의 TF-BUG-001·003·005~015 총 13건입니다.
- TF-BUG-012는 정책 확인 필요 항목이고, TF-BUG-013은 환경 제한 관찰로 대표 결함 성과에서 제외합니다.
- TF-BUG-015는 별도 QA 검증용 사본에서 재검증만 Pass했으며 원본 제품에는 반영하지 않았습니다.
- TF-BUG-002·004는 실제 응답 구성 반증과 각각의 교정 테스트에 따라 `Closed — 제품 결함 아님 / 테스트 구성 오탐`로 보존한다. 제품 코드는 변경하지 않았다.
- 교정 결과와 현재 집계는 [최종 QA 결과 요약](../reports/tripfriend-stage-8-results-summary.md)을 사용한다.

## AI 활용

Codex는 테스트 코드 초안 작성, 코드 탐색과 오류 분석을 보조했습니다. 작성자는 테스트 목적, Mock 범위, assertion과 검증 한계를 검토하고 IntelliJ에서 대표 테스트를 직접 재현했습니다.


## 재현 조건과 공개 범위

| 대상 | 필요한 조건 | 공개 자료와 한계 |
|---|---|---|
| 백엔드 테스트 | Java 17, 기준 제품 커밋, 제품 Gradle 의존성, 같은 패키지의 테스트 소스, 안전한 `application-test` 설정 | 이 저장소에는 테스트 사본이 있다. 제품 Wrapper·설정 파일 전체는 제공하지 않아 이 저장소 단독 실행은 지원하지 않는다. |
| Controller 교정 | 실제 응답 Aspect와 예외 처리기, Mock 서비스·인증·JPA metamodel, 비활성 Security 필터 | [CommentControllerTest](backend/src/test/java/com/tripfriend/domain/review/controller/CommentControllerTest.java)에 구성이 명시돼 있다. 실제 DB 저장을 증명하지 않는다. |
| Postman | 로컬 H2 서버, 비저장 Redis, 테스트 회원·여행지, 로그인 후 생성 ID | Collection·빈 환경 템플릿 제공. 계정·서버 설정은 각자의 격리 환경에서 준비해야 한다. |
| Playwright | 로컬 프런트·백엔드·Redis, 테스트 회원과 일치하는 여행지 ID/옵션명, Chrome 및 Playwright 실행 의존성 | [실행 안내](playwright/README.md)와 [fixture helper](playwright/helpers/api-fixtures.ts)를 제공한다. 준비 서버 없이 단독 E2E 실행은 불가능하다. |

Playwright 5/5는 기준 제품 그대로의 결과가 아닙니다. QA 실행 사본에는 [CORS 교정](../defects/TF-BUG-007-localhost-cors-preflight-blocked.md), 별점 버튼의 접근성 속성, [수정 폼 초기화 교정](../defects/TF-BUG-015-review-edit-form-initial-values-empty.md)이 포함돼 있다. QA 검증용 교정 내용, 테스트 실행 프로필, fixture를 자동 구성하는 재현 스크립트는 현재 제공하지 않습니다. 따라서 새 환경에서 동일 5/5를 보장하지 않습니다.

`--offline`은 기존 의존성 캐시가 준비된 실행 환경에서 사용한 옵션이다. 최초 설치 절차를 대신하지 않는다. CI와 재현 환경 자동 구성은 후속 범위이며 현재 완료 성과로 표시하지 않는다.

[2026-09-05 댓글 Controller 실행 결과와 판정 교정](../reports/portfolio-review-correction-report.md)
