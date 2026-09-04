# TF-BUG-002 — 리뷰 생성 HTTP 상태 불일치 의심 · 테스트 구성 오탐으로 재분류

- 결함 상태: `Closed — Not a Product Defect`
- 재분류일: 2026-09-01
- 해결 분류: `Test Setup False Positive`
- 발견일: 2026-08-29
- 등록일: 2026-08-31
- 기준 프로젝트: 스프린트 1 Java/Spring 완성본
- 기준 커밋: `e2431223ce2a18c8f944d544cba76b951ce0356d`
- 영향 기능: 리뷰 생성 응답 계약
- 테스트 수준: 독립형 MockMvc Controller 단위 테스트
- 심각도: `Minor`
- 수정 우선순위: 제품 담당자 결정 필요
- 재현성: 독립형 MockMvc 4/4 실패, 실제 서버 Postman 1/1 정상 HTTP 201
- 보고 주체: `Codex with user authorization`, 사용자 직접 재현 확인

> 최초 등록은 독립형 MockMvc의 기대 201·실제 200 결과를 근거로 했다. Stage 7 실제 서버 검증에서 HTTP 201·code `201-1`·저장 확인이 모두 통과했고, 원인은 테스트 구성에서 `ResponseAspect`가 제외된 것이므로 제품 결함이 아닌 테스트 구성 오탐으로 재분류한다.

## 1. 요약

독립형 MockMvc는 유효한 리뷰 생성에서 HTTP 200을 반환했지만, 실제 애플리케이션에는 `ResponseAspect`가 있어 `RsData`의 상태 코드 201을 `HttpServletResponse`에 설정한다. Stage 7 실제 서버의 인증 리뷰 생성은 HTTP `201 Created`·code `201-1`로 성공했고 생성 ID·제목도 후속 내 리뷰 조회에서 확인됐다. 제품 동작은 계약과 일치한다.

## 2. 추적 연결

`TF-REQ-006·TF-REQ-012 → TF-COND-002·TF-COND-007 → TF-TC-030·031·034·035·037 → TF-EXEC-011~014 → TF-BUG-002`

| 식별자 | 연결 내용 |
|---|---|
| TF-REQ-006·012 | 리뷰 제목·내용·평점의 유효 입력 경계를 검증하는 계약 |
| TF-COND-002 | 인증 사용자의 유효한 리뷰 생성 |
| TF-COND-007 | 리뷰 제목 2~30자, 내용 10~2000자, 평점 1~5 경계 처리 |
| TF-TC-030 | 제목 2자 최소 유효 경계에서 HTTP 201 기대 |
| TF-TC-031 | 제목 30자 최대 유효 경계에서 HTTP 201 기대 |
| TF-TC-034 | 내용 10자 최소 유효 경계에서 HTTP 201 기대 |
| TF-TC-035 | 내용 2000자 최대 유효 경계에서 HTTP 201 기대 |
| TF-TC-037 | 평점 1·5 유효 경계에서 각각 HTTP 201 기대 |
| TF-EXEC-011 | Codex 승인 실행 — 두 케이스 모두 기대 201·실제 200 |
| TF-EXEC-012 | 사용자 IntelliJ 직접 실행 — 동일 2 Pass·2 Fail 재현 |
| TF-EXEC-013 | Codex 승인 필터 실행 — 신규 유효 경계 4개 변형 모두 기대 201·실제 200 |
| TF-EXEC-014 | 사용자 IntelliJ 전체 클래스 실행 — 제목·내용·평점 유효 경계 6개 변형 모두 동일 실패 |

## 3. 테스트 환경

- Java: 빌드 선언 기준 17
- Gradle Wrapper: 8.12.1
- Spring Boot Gradle plugin: 3.2.4
- 테스트 도구: JUnit 5, Mockito, Spring MockMvc, Hibernate Validator
- 실행 대상: `ReviewControllerTest`
- Controller 구성: 독립형 MockMvc와 실제 `GlobalExceptionHandler`
- 의존성: `ReviewService`, `AuthService`는 Mock
- Security 필터·외부 서버·실제 DB·Docker·실제 계정·비밀 설정: 사용하지 않음

## 4. 사전조건과 데이터

- 인증 헤더 값과 Mock 인증 회원을 준비한다.
- 제목 외 필드는 유효하게 유지한다: 내용 10자 이상, 평점 4.0, 장소 ID 10.
- TF-TC-030·031은 제목 2·30자, TF-TC-034·035는 내용 10·2000자, TF-TC-037은 평점 1·5를 사용한다.
- Mock `AuthService`는 인증 회원을 반환한다.

## 5. 재현 절차

1. IntelliJ에서 `ReviewControllerTest` 클래스를 실행한다.
2. TF-TC-030·031 제목 2·30자 유효 경계 결과를 확인한다.
3. TF-TC-034·035 내용 10·2000자와 TF-TC-037 평점 1·5 유효 경계 결과를 확인한다.
4. 각 성공 요청의 기대 HTTP 상태와 실제 HTTP 상태를 비교한다.
5. TF-TC-029 제목 1자와 TF-TC-032 제목 31자 결과도 회귀 기준으로 확인한다.

## 6. 기대 결과

- TF-TC-030·031·034·035·037의 모든 유효 입력 변형은 HTTP `201 Created`를 반환한다.
- 응답 본문 코드는 `201-1`이다.
- TF-TC-029와 TF-TC-032는 HTTP 400과 본문 코드 `400-1`을 반환하고 서비스가 호출되지 않는다.

## 7. 실제 결과

- TF-TC-030·031·034·035·037의 여섯 입력 변형은 모두 HTTP `200 OK`를 반환해 상태 assertion이 실패했다.
- TF-TC-029와 TF-TC-032는 기대 결과와 일치해 Pass했다.
- 사용자 실행의 핵심 로그는 다음과 같다.

```text
Status
Expected :201
Actual   :200

java.lang.AssertionError: Status expected:<201> but was:<200>
at ReviewControllerTest.createReview_acceptsTitleAtMaximum(ReviewControllerTest.java:96)
at ReviewControllerTest.createReview_acceptsTitleAtMinimum(ReviewControllerTest.java:79)

4 tests completed, 2 failed
BUILD FAILED in 4s
```

## 8. 재현 결과

| 실행 | 실행자 | 결과 | 증거 |
|---|---|---|---|
| TF-EXEC-011 | Codex with user authorization | TF-TC-029·032 Pass, TF-TC-030·031 Fail | JUnit XML, Gradle HTML·콘솔 결과 |
| TF-EXEC-012 | User | 동일 2 Pass·2 Fail, IntelliJ 2초 401ms, Gradle 4초 | [사용자 실행 화면](../evidence/TF-EXEC-012-review-controller-user-run.png), 사용자 제공 실패 로그 |
| TF-EXEC-013 | Codex with user authorization | 신규 8개 변형 중 유효 내용·평점 4개 변형에서 동일 201·200 불일치 | JUnit XML, Gradle HTML·콘솔 결과 |
| TF-EXEC-014 | User | 전체 12개 중 4 Pass·8 Fail, 유효 제목·내용·평점 6개 변형에서 동일 201·200 불일치 | [최신 사용자 실행 화면](../evidence/TF-EXEC-014-review-controller-user-run.png), 사용자 제공 핵심 실패 로그 |
| TF-S7-EXEC-005 | User · Postman Desktop App | 실제 서버 HTTP 201·code `201-1`·생성 ID/제목, 4/4 Passed | [실제 서버 인증 리뷰 생성](../evidence/stage-07/TF-S7-EXEC-005-postman-authenticated-review-create-pass.png) |
| TF-S7-EXEC-006 | User · Postman Desktop App | 실제 서버 HTTP 200·code `200-7`, 생성 ID·제목 저장 확인, 4/4 Passed | 사용자 결과 보고, 별도 증거 미보존 |

- 검증 지점 도달 실행: 4회.
- 동일 실패: 4회.
- 환경·테스트 코드 오류와 분리: 완료.
- 제품 코드 수정: 없음.
- 실제 서버 반증: HTTP 201·4/4 Passed, 후속 저장 확인 Pass.

## 9. 영향과 심각도 근거

- HTTP 201을 기준으로 생성 성공을 판정하는 클라이언트·자동화·모니터링은 정상 생성 응답을 예상과 다르게 처리할 수 있다.
- 응답 본문 코드와 HTTP 상태가 서로 다른 의미를 전달해 API 계약의 일관성과 관찰 가능성을 낮춘다.
- 이번 Controller 단위 테스트에서는 생성 서비스 호출 경로가 막히거나 데이터가 손상되는 결과를 확인하지 않았다.
- 현재 확인된 영향은 응답 계약 불일치이므로 심각도를 `Minor`로 제안한다. 실제 클라이언트 흐름이 중단되는 증거가 확인되면 `Major` 재평가가 필요하다.
- 권한 우회·데이터 삭제·전체 서비스 중단은 이번 테스트에서 확인하지 않았다.

## 10. 코드 상관관계와 추정 원인

`ReviewController.createReview`는 `new RsData<>("201-1", ...)`를 반환한다. 전체 애플리케이션에서는 `ResponseAspect.responseAspect()`가 이 `RsData`의 `getStatusCode()`를 읽어 `HttpServletResponse.setStatus(201)`을 호출한다.

기존 `ReviewControllerTest`는 `MockMvcBuilders.standaloneSetup(reviewController)`와 예외 처리기만 등록해 Spring AOP 프록시와 `ResponseAspect`를 포함하지 않았다. 따라서 실제 런타임 계약이 아니라 Aspect가 빠진 Controller 객체의 기본 HTTP 200을 관찰했다. 제품 코드 원인이 아니라 테스트 격리 수준과 assertion 대상의 불일치다.

## 11. 테스트 교정·회귀 조건

- TF-TC-030: 제목 2자에서 HTTP 201과 본문 코드 `201-1`을 확인한다.
- TF-TC-031: 제목 30자에서 HTTP 201과 본문 코드 `201-1`을 확인한다.
- TF-TC-034·035: 내용 10·2000자에서 HTTP 201과 본문 코드 `201-1`을 확인한다.
- TF-TC-037: 평점 1·5에서 각각 HTTP 201과 본문 코드 `201-1`을 확인한다.
- TF-TC-029·032: 유효 범위 밖 제목이 계속 HTTP 400·`400-1`로 차단되는지 확인한다.
- 독립형 MockMvc에서 HTTP 상태를 검증하려면 `ResponseAspect`를 명시적으로 포함하거나, AOP가 적용되는 Spring Context 기반 MockMvc 테스트로 전환한다.
- HTTP 201 assertion은 약화하지 않는다. 실제 서버는 이 계약을 충족했다.
- 경계값별 교정 테스트는 Stage 8 결과 정리에서 별도 계획하고, 현재 기존 테스트 코드는 수정하지 않는다.

## 12. 현재 한계와 금지 주장

- 독립형 MockMvc Controller 단위 결과이며 Security 필터와 실제 서버 요청을 검증하지 않았다.
- `ReviewService`가 Mock이므로 실제 DB 저장 성공·실패를 증명하지 않는다.
- 성공 케이스는 상태 assertion에서 먼저 실패했으므로 본문 `201-1`의 실행 assertion 완료를 주장하지 않는다. 본문 코드 구성은 Controller 소스에서 확인했다.
- 제품 수정 코드는 없으며 실제 서버 대표 유효 입력은 Pass했다.
- TF-S7-EXEC-007 브라우저 흐름도 POST HTTP 201·목록 반영으로 Pass했다. 이는 대표 정상 생성 흐름의 반증이며 모든 경계값 자동화가 교정됐다는 뜻은 아니다.
- Java 결과로 Kotlin 구현의 동일 결함을 주장하지 않는다.
- 제품 담당자가 심각도·수정 우선순위·배포 결정을 확정했다고 주장하지 않는다.
