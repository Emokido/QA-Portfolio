# TF-BUG-004 — 댓글 생성 성공 응답이 HTTP 201 대신 200을 반환함

- 결함 상태: `Open`
- 발견일: 2026-08-31
- 등록일: 2026-08-31
- 기준 프로젝트: 스프린트 1 Java/Spring 완성본
- 기준 커밋: `e2431223ce2a18c8f944d544cba76b951ce0356d`
- 영향 기능: 댓글 생성 응답 계약
- 테스트 수준: 독립형 MockMvc Controller 단위 테스트
- 심각도: `Minor`
- 수정 우선순위: 제품 담당자 결정 필요
- 재현성: 검증 지점에 도달한 실행 2/2에서 동일 실패
- 보고 주체: `Codex with user authorization`, 사용자 직접 재현 확인

> 이 결함은 Stage 5 Baseline의 HTTP 201 오라클과 Codex·사용자 실행의 동일한 기대 201·실제 200 결과를 근거로 등록한다. 댓글 생성 자체의 실패나 실제 DB 저장 실패는 이번 테스트에서 확인하지 않았다.

## 1. 요약

유효한 최소 2자 또는 최대 100자 댓글 생성 요청을 보내면 Controller 처리는 성공 경로에 진입하지만 HTTP 상태가 기대한 `201 Created`가 아니라 `200 OK`로 반환된다. Controller는 응답 본문용 코드 `201-1`을 구성하지만 HTTP 상태를 201로 지정하지 않는다.

## 2. 추적 연결

`TF-REQ-008·TF-REQ-012 → TF-COND-005·TF-COND-008 → TF-TC-040·041 → TF-EXEC-016·017 → TF-BUG-004`

| 식별자 | 연결 내용 |
|---|---|
| TF-REQ-008 | 인증 사용자의 댓글 생성 계약 |
| TF-REQ-012 | 댓글 내용 2~100자 입력 경계 검증 계약 |
| TF-COND-005 | 인증 사용자의 댓글 생성 |
| TF-COND-008 | 댓글 내용 2~100자 경계 처리 |
| TF-TC-040 | 댓글 2자 최소 유효 경계에서 HTTP 201 기대 |
| TF-TC-041 | 댓글 100자 최대 유효 경계에서 HTTP 201 기대 |
| TF-EXEC-016 | Codex 승인 실행 — 두 케이스 모두 기대 201·실제 200 |
| TF-EXEC-017 | 사용자 IntelliJ 직접 실행 — 동일 2 Pass·2 Fail 재현 |

## 3. 테스트 환경

- Java: 빌드 선언 기준 17
- Gradle Wrapper: 8.12.1
- Spring Boot Gradle plugin: 3.2.4
- 테스트 도구: JUnit 5, Mockito, Spring MockMvc, Hibernate Validator
- 실행 대상: `CommentControllerTest`
- Controller 구성: 독립형 MockMvc와 실제 `GlobalExceptionHandler`
- 의존성: `CommentService`, `AuthService`는 Mock
- Security 필터·외부 서버·실제 DB·Docker·실제 계정·비밀 설정: 사용하지 않음

## 4. 사전조건과 데이터

- 인증 헤더 값과 Mock 인증 회원을 준비한다.
- TF-TC-040은 댓글 내용 2자, TF-TC-041은 댓글 내용 100자를 사용한다.
- 두 요청 모두 존재 리뷰를 나타내는 논리 `reviewId=10`을 사용한다.
- Mock `AuthService`는 인증 회원을 반환한다.

## 5. 재현 절차

1. IntelliJ에서 `CommentControllerTest` 클래스를 실행한다.
2. TF-TC-040 댓글 2자와 TF-TC-041 댓글 100자 결과를 확인한다.
3. 각 성공 요청의 기대 HTTP 상태와 실제 HTTP 상태를 비교한다.
4. TF-TC-039 댓글 1자와 TF-TC-042 댓글 101자 결과도 회귀 기준으로 확인한다.

## 6. 기대 결과

- TF-TC-040·041은 HTTP `201 Created`를 반환한다.
- 응답 본문 코드는 `201-1`이다.
- TF-TC-039·042는 HTTP 400과 본문 코드 `400-1`을 반환하고 인증·댓글 서비스가 호출되지 않는다.

## 7. 실제 결과

- TF-TC-040·041 모두 HTTP `200 OK`를 반환해 상태 assertion이 실패했다.
- TF-TC-039·042는 기대 결과와 일치해 Pass했다.
- 사용자 제공 실패 로그의 핵심은 다음과 같다.

```text
Status
Expected :201
Actual   :200

java.lang.AssertionError: Status expected:<201> but was:<200>
at CommentControllerTest.createComment_acceptsContentAtMaximum(CommentControllerTest.java:94)
at CommentControllerTest.createComment_acceptsContentAtMinimum(CommentControllerTest.java:78)
```

## 8. 재현 결과

| 실행 | 실행자 | 결과 | 증거 |
|---|---|---|---|
| TF-EXEC-016 | Codex with user authorization | TF-TC-039·042 Pass, TF-TC-040·041 Fail | JUnit XML, Gradle HTML·콘솔 결과 |
| TF-EXEC-017 | User | 동일 2 Pass·2 Fail, IntelliJ 2초 366ms | [사용자 실행 화면](../evidence/TF-EXEC-017-comment-controller-user-run.png), 사용자 제공 실패 로그 |

- 검증 지점 도달 실행: 2회.
- 동일 실패: 2회.
- 환경·테스트 코드 오류와 분리: 완료.
- 제품 코드 수정 후 재검증: `Not Run`.

## 9. 영향과 심각도 근거

- HTTP 201을 기준으로 댓글 생성 성공을 판정하는 클라이언트·자동화·모니터링은 정상 생성 응답을 예상과 다르게 처리할 수 있다.
- 응답 본문 코드와 HTTP 상태가 서로 다른 의미를 전달해 API 계약의 일관성과 관찰 가능성을 낮춘다.
- 이번 Controller 단위 테스트에서는 댓글 서비스 호출 경로가 막히거나 데이터가 손상되는 결과를 확인하지 않았다.
- 현재 확인된 영향은 응답 계약 불일치이므로 심각도를 `Minor`로 제안한다. 실제 클라이언트 흐름이 중단되는 증거가 확인되면 `Major` 재평가가 필요하다.
- 권한 우회·데이터 삭제·전체 서비스 중단은 이번 테스트에서 확인하지 않았다.

## 10. 코드 상관관계와 추정 원인

기준 코드의 `CommentController.createComment`는 `new RsData<>("201-1", ...)`를 직접 반환한다. 해당 메서드에는 HTTP 상태를 201로 지정하는 `ResponseEntity.status(HttpStatus.CREATED)`, `@ResponseStatus(HttpStatus.CREATED)` 또는 동등한 처리가 확인되지 않는다.

응답 본문의 애플리케이션 코드 `201-1`은 HTTP 상태를 자동으로 201로 설정하지 않으므로 Spring MVC의 기본 성공 상태 200이 반환되는 것으로 판단한다. 이는 TF-BUG-002와 유사한 기술 패턴이지만 별도 Controller·엔드포인트·수정 지점·회귀 케이스이므로 별도 결함으로 추적한다.

## 11. 수정 후 재검증 조건

- TF-TC-040: 댓글 2자에서 HTTP 201과 본문 코드 `201-1`을 확인한다.
- TF-TC-041: 댓글 100자에서 HTTP 201과 본문 코드 `201-1`을 확인한다.
- TF-TC-039·042: 범위 밖 댓글이 계속 HTTP 400·`400-1`로 차단되고 인증·댓글 서비스가 호출되지 않는지 확인한다.
- TF-TC-014: 유효 댓글 생성 서비스 규칙에 회귀가 없는지 확인한다.
- 후속 실제 API 검증에서 서버 응답의 HTTP 상태·본문·저장 결과를 함께 확인한다.
- 실제 QA 클라이언트가 HTTP 201에 의존하는지 확인해 사용자 영향과 심각도를 재평가한다.

## 12. 현재 한계와 금지 주장

- 독립형 MockMvc Controller 단위 결과이며 Security 필터와 실제 서버 요청을 검증하지 않았다.
- `CommentService`가 Mock이므로 실제 DB 저장 성공·실패를 증명하지 않는다.
- 성공 케이스는 상태 assertion에서 먼저 실패했으므로 본문 `201-1`과 서비스 호출 assertion 완료를 주장하지 않는다. 본문 코드 구성은 Controller 소스에서 확인했다.
- 사용자 증거 화면에는 정확한 프로젝트 절대경로와 실행 날짜가 표시되지 않았다.
- 수정 코드와 수정 후 `Pass`는 아직 없다.
- Java 결과로 Kotlin 구현의 동일 결함을 주장하지 않는다.
- 제품 담당자가 심각도·수정 우선순위·배포 결정을 확정했다고 주장하지 않는다.
