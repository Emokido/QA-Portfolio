# TF-BUG-003 — 범위 밖 리뷰 평점이 전용 코드 400-2 대신 공통 코드 400-1을 반환함

- 결함 상태: `Open`
- 발견일: 2026-08-31
- 등록일: 2026-08-31
- 기준 프로젝트: 스프린트 1 Java/Spring 완성본
- 기준 커밋: `e2431223ce2a18c8f944d544cba76b951ce0356d`
- 영향 기능: 리뷰 생성 평점 검증 응답 계약
- 테스트 수준: MockMvc Controller 단위 기록과 실제 `ResponseAspect`를 포함한 Spring Web MVC slice
- 심각도: `Minor`
- 수정 우선순위: 제품 담당자 결정 필요
- 재현성: 검증 지점에 도달한 실행 4/4에서 동일 실패
- 보고 주체: `Codex with user authorization`, 사용자 직접 재현 확인

> 이 결함은 Stage 5 Baseline의 평점 전용 코드 `400-2` 오라클과 Codex·사용자 실행의 동일한 기대 `400-2`·실제 `400-1` 결과를 근거로 등록한다. HTTP 400 자체는 일치하며 실제 DB 저장·클라이언트 분기 영향은 이번 테스트에서 확인하지 않았다.

## 1. 요약

리뷰 생성 요청의 평점을 허용 범위 1~5 밖인 0 또는 6으로 보내면 HTTP 400으로 거부되지만, 응답 본문 코드가 기대한 평점 전용 `400-2`가 아니라 공통 Bean Validation 코드 `400-1`로 반환된다.

## 2. 추적 연결

`TF-REQ-006·TF-REQ-012 → TF-COND-007·TF-COND-011 → TF-TC-038 → TF-EXEC-013·014·TF-S8-EXEC-002·003 → TF-BUG-003`

| 식별자 | 연결 내용 |
|---|---|
| TF-REQ-006 | 리뷰 평점 범위 1~5 계약 |
| TF-REQ-012 | 리뷰 입력 경계 검증 계약 |
| TF-COND-007 | 평점 1~5 경계 처리 |
| TF-COND-011 | 검증 실패를 HTTP 400으로 구분 |
| TF-TC-038 | 평점 0·6에서 HTTP 400과 본문 코드 `400-2` 기대 |
| TF-EXEC-013 | Codex 승인 필터 실행 — 두 변형 모두 실제 `400-1` |
| TF-EXEC-014 | 사용자 IntelliJ 전체 클래스 실행 — 동일 결과 재현 |
| TF-S8-EXEC-002 | 실제 `ResponseAspect` 포함 Web MVC slice Codex 승인 실행 — 0·6 두 변형 동일 실패 |
| TF-S8-EXEC-003 | 사용자 IntelliJ 교정 클래스 실행 — 12개 중 10 Pass·2 Fail, TF-TC-038만 실패 |

## 3. 테스트 환경

- Java: 빌드 선언 기준 17
- Gradle Wrapper: 8.12.1
- Spring Boot Gradle plugin: 3.2.4
- 테스트 도구: JUnit 5, Mockito, Spring MockMvc, Hibernate Validator
- 실행 대상: `ReviewControllerTest.rating_rejectsOutOfRange`
- Controller 구성: Stage 6은 독립형 MockMvc, Stage 8은 실제 `ReviewController`·`GlobalExceptionHandler`·`ResponseAspect`를 포함한 Web MVC slice
- 의존성: `ReviewService`, `AuthService`는 Mock
- Security 필터·외부 서버·실제 DB·Docker·실제 계정·비밀 설정: 사용하지 않음

## 4. 사전조건과 데이터

- 제목·내용·장소 ID는 유효하게 유지한다.
- A 변형은 평점 0, B 변형은 평점 6을 사용한다.
- `ReviewRequestDto.rating`에는 `@Min(1)`과 `@Max(5)`가 선언돼 있다.
- Baseline은 두 변형 모두 HTTP 400과 본문 코드 `400-2`를 기대한다.

## 5. 재현 절차

1. IntelliJ에서 최신 `ReviewControllerTest` 클래스를 실행한다.
2. `TF-TC-038 평점 0.0` 결과를 연다.
3. `TF-TC-038 평점 6.0` 결과를 연다.
4. 각 응답의 HTTP 상태와 JSON `$.code` 기대·실제 값을 비교한다.

## 6. 기대 결과

- 평점 0과 6은 각각 HTTP `400 Bad Request`로 거부된다.
- 두 응답의 본문 코드는 평점 범위 오류를 나타내는 `400-2`다.
- 리뷰 생성 서비스와 저장 작업은 실행되지 않는다.

## 7. 실제 결과

- 두 변형 모두 HTTP 400으로 거부됐다.
- 두 변형 모두 JSON `$.code`가 `400-1`이어서 assertion이 실패했다.
- 사용자 제공 전체 로그에서 필요한 핵심 구간만 발췌하면 다음과 같다.

```text
JSON path "$.code"
Expected :400-2
Actual   :400-1

java.lang.AssertionError: JSON path "$.code" expected:<400-2> but was:<400-1>
at ReviewControllerTest.rating_rejectsOutOfRange(ReviewControllerTest.java:200)

12 tests completed, 8 failed
BUILD FAILED in 4s
```

전체 원본 로그는 반복 프레임과 로컬 절대경로를 포함하므로 별도 공개 증거 파일로 복사하지 않았다.

## 8. 재현 결과

| 실행 | 실행자 | 결과 | 증거 |
|---|---|---|---|
| TF-EXEC-013 | Codex with user authorization | 평점 0·6 모두 기대 `400-2`·실제 `400-1` | JUnit XML, Gradle HTML·콘솔 결과 |
| TF-EXEC-014 | User | 전체 12개 중 4 Pass·8 Fail, TF-TC-038 두 변형 동일 실패 | [사용자 실행 화면](../evidence/TF-EXEC-014-review-controller-user-run.png), 사용자 제공 핵심 실패 로그 |
| TF-S8-EXEC-002 | Codex with user authorization | 교정 전체 12개 중 10 Pass·2 Fail, TF-TC-038 평점 0·6만 동일 실패 | JUnit XML, Gradle HTML·콘솔 결과 |
| TF-S8-EXEC-003 | User | 교정 전체 12개 중 10 Pass·2 Fail, 522ms, TF-TC-038 평점 0·6만 동일 실패 | [Stage 8 사용자 실행 화면](../evidence/TF-S8-EXEC-003-review-controller-user-run.png) |

- 검증 지점 도달 실행: 4회.
- 동일 실패: 4회.
- HTTP 상태 400 일치: 4회.
- 제품 코드 수정 후 재검증: `Not Run`.

## 9. 영향과 심각도 근거

- 클라이언트가 본문 코드로 일반 입력 오류와 평점 범위 오류를 구분하면 기대한 분기·메시지·분석 집계가 달라질 수 있다.
- HTTP 상태는 올바른 400이므로 요청 거부 자체는 작동한다.
- 저장 실패·데이터 손상·권한 우회·서비스 중단은 이번 테스트에서 확인하지 않았다.
- 현재 확인된 영향은 오류 응답의 세부 계약 불일치이므로 심각도를 `Minor`로 제안한다. 실제 클라이언트 기능 중단이 확인되면 재평가한다.

## 10. 코드 상관관계와 추정 원인

`ReviewRequestDto.rating`의 `@Min/@Max` 검증은 Controller 메서드 실행 전에 적용된다. 검증 실패 시 `GlobalExceptionHandler.handleMethodArgumentNotValidException`은 필드 종류와 관계없이 본문 코드 `400-1`을 반환한다.

한편 `ReviewService.createReview`에는 범위 밖 평점을 `ServiceException("400-2", ...)`로 처리하는 분기가 있다. 그러나 Controller 요청에서는 DTO 검증이 먼저 실패하므로 해당 Service 분기까지 도달하지 않는다. 이 계층 간 오류 코드 계약 충돌이 실행 결과와 일치하는 추정 원인이다.

## 11. 수정 후 재검증 조건

- TF-TC-038 A: 평점 0에서 HTTP 400과 본문 코드 `400-2`를 확인한다.
- TF-TC-038 B: 평점 6에서 HTTP 400과 본문 코드 `400-2`를 확인한다.
- TF-TC-037: 평점 1·5가 검증 오류로 차단되지 않는지 확인한다. 성공 HTTP 201 문제는 TF-BUG-002와 분리해 추적한다.
- 후속 실제 API 검증에서 HTTP 상태·본문 코드·저장 미발생을 함께 확인한다.
- 실제 QA 클라이언트가 오류 코드로 메시지나 분기를 달리하는지 확인해 사용자 영향과 심각도를 재평가한다.

## 12. 현재 한계와 금지 주장

- Stage 8 교정은 실제 `ResponseAspect`를 포함한 Web MVC slice 결과이며 Security 필터와 실제 서버 요청을 검증하지 않았다.
- `ReviewService`가 Mock이고 코드 assertion 실패 후 `verifyNoInteractions` 줄까지 완료되지 않았으므로 서비스 미호출 assertion 완료를 주장하지 않는다.
- 실제 DB 미저장과 프론트 오류 표시를 아직 증명하지 않았다.
- Stage 6 전체 원본 사용자 로그는 별도 공개 증거로 보존하지 않았고, Stage 8 대표 IntelliJ 화면은 비밀값 없이 보존했다.
- 수정 코드와 수정 후 `Pass`는 아직 없다.
- Java 결과로 Kotlin 구현의 동일 결함을 주장하지 않는다.
- 제품 담당자가 심각도·수정 우선순위·배포 결정을 확정했다고 주장하지 않는다.
