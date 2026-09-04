# TF-BUG-006 — 댓글 생성 시 reviewId 누락·null이 요청 처리 예외로 종료됨

- 결함 상태: `Open`
- 발견일: 2026-09-01
- 등록일: 2026-09-01
- 기준 프로젝트: 스프린트 1 Java/Spring 완성본
- 기준 커밋: `e2431223ce2a18c8f944d544cba76b951ce0356d`
- 영향 기능: 댓글 생성 요청의 대상 리뷰 식별자 검증·예외 응답
- 테스트 수준: Controller·Service·Repository/H2 integration test
- 심각도: `Minor`
- 수정 우선순위: 제품 담당자 결정 필요
- 재현성: 검증 지점에 도달한 실행 2/2에서 두 데이터 변형 모두 동일 실패
- 보고 주체: `Codex with user authorization`, 사용자 직접 재현 확인

> 이 결함은 `reviewId` 필드를 누락하거나 명시적으로 null로 보낸 댓글 생성 요청이 DTO 검증에서 거부되지 않고 실제 `CommentService`와 Repository까지 전달된 뒤 요청 처리 예외로 종료된 결과를 근거로 등록한다. 정확한 HTTP 400 코드·본문 계약은 미명시이므로 임의로 확정하지 않지만, 잘못된 입력이 통제된 클라이언트 오류 응답 없이 Repository의 null ID 예외로 종료되는 동작은 일관된 입력 검증·예외 처리로 볼 수 없다.

## 1. 요약

댓글 생성 요청에서 `reviewId`를 누락하거나 null로 전송하면 두 입력 모두 `CommentRequestDto.reviewId=null`이 된다. 해당 필드에는 null 검증 annotation이 없어 Controller의 `@Valid`를 통과하고, `CommentService.createComment()`가 `reviewRepository.findById(null)`을 호출한다. Spring Data JPA가 null ID를 거부하면서 `InvalidDataAccessApiUsageException`과 원인 `IllegalArgumentException: The given id must not be null`이 발생하고 정상 응답을 만들지 못한다.

## 2. 추적 연결

`TF-REQ-008·012 → TF-COND-008·011 → TF-RISK-009 → TF-TC-043 → TF-EXEC-034·035 → TF-BUG-006`

| 식별자 | 연결 내용 |
|---|---|
| TF-REQ-008 | 인증 사용자의 댓글 생성 계약 |
| TF-REQ-012 | 댓글 입력 경계와 코드 입력 계약 |
| TF-COND-008 | 댓글 `reviewId` 누락·null·미존재 처리 구분 |
| TF-COND-011 | 검증 실패와 대표 404 응답의 구분 |
| TF-RISK-009 | 누락 `reviewId`가 일관된 400 응답 대신 서버 요청 처리 예외로 이어질 가능성 |
| TF-TC-043 | `reviewId` 필드 누락과 명시적 null의 현재 동작 관찰 |
| TF-EXEC-034 | Codex 승인 실행 — 2개 데이터 변형 모두 동일 요청 처리 예외 Fail |
| TF-EXEC-035 | 사용자 IntelliJ 직접 실행 — 동일 2/2 Fail 재현 |

## 3. 테스트 환경

- Java: 빌드 선언 기준 17
- Gradle Wrapper: 8.12.1
- Spring Boot Gradle plugin: 3.2.4
- 테스트 도구: JUnit 5, AssertJ, Mockito, Spring MockMvc, Spring Data JPA `@DataJpaTest`, 내장 H2
- 실행 대상: `CommentReviewIdValidationIntegrationTest`
- 실제 구성: `CommentController`, `CommentService`, `CommentRepository`, `ReviewRepository`, `QueryDslConfig`, `GlobalExceptionHandler`
- Mock 협력 객체: `AuthService`
- 실제 서버·외부 DB·Docker·실제 계정·비밀 설정: 사용하지 않음

## 4. 사전조건과 데이터

- 안전한 테스트 Authorization 헤더 `Bearer test-token`을 사용한다.
- `AuthService`는 테스트 작성자 객체를 반환하도록 격리한다.
- 댓글 내용은 Bean Validation을 통과하는 `정상 댓글`을 사용한다.
- 변형 A — `reviewId` 필드 누락:

```json
{"content":"정상 댓글"}
```

- 변형 B — `reviewId` 명시적 null:

```json
{"content":"정상 댓글","reviewId":null}
```

## 5. 재현 절차

1. IntelliJ에서 `CommentReviewIdValidationIntegrationTest` 클래스를 실행한다.
2. TF-TC-043-A `reviewId` 필드 누락 결과를 확인한다.
3. TF-TC-043-B `reviewId=null` 결과를 확인한다.
4. 두 실패 로그에서 `The given id must not be null`과 `performCreate()` 호출 경로를 확인한다.

## 6. 기대 결과

- 정확한 HTTP 400 코드와 본문 형식은 제품 정책이 미명시이므로 임의로 고정하지 않는다.
- 누락·null 입력은 4xx 범위의 통제된 클라이언트 오류로 거부되어야 한다.
- Repository null ID 예외, 요청 처리 예외 또는 5xx로 종료되면 안 된다.
- 댓글이 부분 저장되면 안 된다.
- 미존재하지만 null이 아닌 리뷰 ID는 기존 TF-TC-019 계약의 `404-3` 처리를 유지해야 한다.

## 7. 실제 결과

- 필드 누락과 명시적 null은 모두 `CommentRequestDto.reviewId=null`로 변환된다.
- `reviewId`에는 null 검증 annotation이 없어 Controller의 `@Valid`에서 거부되지 않는다.
- `CommentService.createComment()`가 `reviewRepository.findById(null)`을 호출한다.
- 두 변형 모두 다음 요청 처리 예외로 정상 응답에 도달하지 못한다.

```text
Request processing failed:
org.springframework.dao.InvalidDataAccessApiUsageException:
The given id must not be null

at com.tripfriend.domain.review.controller.CommentReviewIdValidationIntegrationTest.performCreate(
    CommentReviewIdValidationIntegrationTest.java:89)
Caused by: org.springframework.dao.InvalidDataAccessApiUsageException:
The given id must not be null
```

- `GlobalExceptionHandler`는 `MethodArgumentNotValidException`과 `ServiceException`을 처리하지만 이번 `InvalidDataAccessApiUsageException`은 처리하지 않는다.

## 8. 재현 결과

| 실행 | 실행자 | 결과 | 증거 |
|---|---|---|---|
| TF-EXEC-034 | Codex with user authorization | 2 tests, 2 Fail; 두 변형 모두 동일 null ID 요청 처리 예외 | JUnit XML, Gradle HTML·콘솔 결과 |
| TF-EXEC-035 | User | 동일 2 tests, 2 Fail, IntelliJ 1초 428ms | [사용자 실행 화면](../evidence/TF-EXEC-035-comment-review-id-validation-user-run.png), 사용자 제공 핵심 실패 로그 |

- 검증 지점 도달 실행: 2회.
- 동일 실패: 2회.
- 각 실행의 누락·null 변형: 2/2 동일 실패.
- 환경·테스트 코드 오류와 분리: 완료.
- 제품 코드 수정 후 재검증: `Not Run`.

## 9. 영향과 심각도 근거

- 잘못된 댓글 생성 요청이 입력 경계에서 통제되지 않고 Repository까지 전달돼 요청 처리 예외로 종료된다.
- 클라이언트가 오류 상태·형식을 일관되게 처리하지 못하고 서버 로그에 불필요한 예외가 누적될 수 있다.
- 정상적인 유효 `reviewId` 댓글 생성 경로의 실패, 정상 사용자 기능 차단, 권한 우회, 데이터 손상·삭제는 이번 테스트에서 확인하지 않았다.
- 유효하지 않은 입력의 오류 처리 문제이고 정상 CRUD 경로 차단이나 데이터 손상 증거가 없으므로 심각도를 `Minor`로 제안한다.
- 실제 서버에서 정확히 어떤 HTTP 상태·본문을 반환하는지는 후속 API 실행에서 확인해야 한다.

## 10. 코드 상관관계와 추정 원인

- `CommentRequestDto.content`에는 `@NotBlank`·`@Size`가 있지만 `reviewId`에는 `@NotNull` 또는 동등한 null 검증이 없다.
- `CommentController.createComment()`는 DTO에 `@Valid`를 적용하지만 null `reviewId`는 검증 실패가 아니다.
- `CommentService.createComment()`는 null 여부를 검사하거나 통제된 `ServiceException`으로 변환하지 않고 바로 `reviewRepository.findById()`를 호출한다.
- `GlobalExceptionHandler`에는 이번 DataAccess 예외를 클라이언트 오류로 변환하는 처리기가 없다.
- 입력 DTO 검증과 Service·공통 예외 처리 사이의 null 계약 누락이 직접 원인으로 판단된다.

## 11. 수정 후 재검증 조건

- TF-TC-043-A·B가 Repository null ID 예외나 요청 처리 예외 없이 통제된 4xx 응답으로 완료되는지 확인한다.
- 제품에서 확정한 상태·본문 코드·메시지가 두 변형에 일관되게 적용되는지 확인한다.
- 댓글 Repository 개수가 요청 전후 동일한지 직접 assertion한다.
- `reviewId`가 존재하지만 대상 리뷰가 없는 TF-TC-019가 계속 `404-3`이고 댓글을 저장하지 않는지 확인한다.
- 유효 `reviewId`의 TF-TC-014 댓글 생성 성공 경로가 회귀하지 않는지 확인한다.
- 실제 API 단계에서 누락·null 요청의 HTTP 상태·본문과 서버 로그를 확인한다.

## 12. 현재 한계와 금지 주장

- 독립형 MockMvc와 실제 Service·Repository·내장 H2를 사용한 결과이며 실제 서버·외부 DB 실행이 아니다.
- MockMvc에서 응답 생성 전 `ServletException`이 전파됐으므로 실제 서버가 정확히 HTTP 500을 반환한다고 단정하지 않는다.
- 각 변형에서 요청 전 댓글 count 조회 후 예외가 발생해 요청 후 count assertion에는 도달하지 않았다. XML SQL 로그에 댓글 INSERT가 없고 예외가 저장 호출 전 발생한 점은 미저장 보조 근거지만 직접 count 검증으로 과장하지 않는다.
- 사용자가 제공한 로그는 핵심 예외와 테스트 호출 경로를 선별한 내용이다. 반복 프레임을 생략했어도 동일 원인 재현 판정에는 충분하다.
- 모든 null ID API에서 같은 문제가 발생한다고 주장하지 않는다. 직접 재현 범위는 댓글 생성의 `reviewId` 누락·null이다.
- 수정 코드와 수정 후 `Pass`는 아직 없다.
- Java 결과로 Kotlin 구현의 동일 결함을 주장하지 않는다.
