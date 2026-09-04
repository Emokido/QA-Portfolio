# TF-BUG-005 — 쿠키 전용 인증 요청이 Controller에서 null 토큰 NPE로 실패함

- 결함 상태: `Open`
- 발견일: 2026-08-31
- 등록일: 2026-09-01
- 기준 프로젝트: 스프린트 1 Java/Spring 완성본
- 기준 커밋: `e2431223ce2a18c8f944d544cba76b951ce0356d`
- 영향 기능: 쿠키 전용 JWT 인증 후 댓글 수정 요청
- 테스트 수준: Spring Security Web MVC slice test
- 심각도: `Major`
- 수정 우선순위: 제품 담당자 결정 필요
- 재현성: 검증 지점에 도달한 실행 2/2에서 동일 실패
- 보고 주체: `Codex with user authorization`, 사용자 직접 재현 확인

> 이 결함은 실제 `SecurityConfig`·JWT 필터·탈퇴회원 필터·`AuthService`를 사용하는 TF-TC-027에서 유효한 `accessToken` 쿠키가 Security 인증을 통과한 뒤 Controller→AuthService 전달 불일치로 요청 처리 예외가 발생한 결과를 근거로 등록한다. 쿠키가 필수 인증 채널인지에 대한 제품 정책은 미명시이므로 임의로 확정하지 않지만, 유효 쿠키를 인증한 뒤 NPE로 종료되는 동작은 정상적인 인증 성공·거부 계약으로 볼 수 없다.

## 1. 요약

Authorization 헤더 없이 유효한 `accessToken` 쿠키만 포함한 댓글 수정 요청을 보내면 `JwtAuthenticationFilter`는 쿠키에서 토큰을 추출해 인증을 완료한다. 그러나 `CommentController.updateComment`는 존재하지 않는 Authorization 헤더 값을 `AuthService.getLoggedInMember()`에 전달하고, `AuthService`가 null 값에 `replace()`를 호출해 `NullPointerException`이 발생한다. 요청은 정상 응답과 댓글 수정 Service 호출에 도달하지 못한다.

## 2. 추적 연결

`TF-REQ-008·009 → TF-COND-005·006 → TF-RISK-008 → TF-TC-027 → TF-EXEC-031·032 → TF-BUG-005`

| 식별자 | 연결 내용 |
|---|---|
| TF-REQ-008 | 인증 사용자의 댓글 수정 계약 |
| TF-REQ-009 | JWT 인증·인가와 변경 API 인증 요구 |
| TF-COND-005 | 인증 사용자의 댓글 CRUD와 작성자 수정 권한 |
| TF-COND-006 | 인증 상태·권한 결과 구분 |
| TF-RISK-008 | 쿠키 인증과 Controller 헤더 전달 불일치로 정상 사용자 차단 가능 |
| TF-TC-027 | 쿠키 전용 인증 전달의 현재 동작 관찰 |
| TF-EXEC-031 | Codex 승인 실행 — TF-TC-026·028 Pass, TF-TC-027 null 토큰 NPE Fail |
| TF-EXEC-032 | 사용자 IntelliJ 직접 실행 — 동일 2 Pass·1 Fail 재현 |

## 3. 테스트 환경

- Java: 빌드 선언 기준 17
- Gradle Wrapper: 8.12.1
- Spring Boot Gradle plugin: 3.2.4
- 테스트 도구: JUnit 5, Mockito, Spring MockMvc, Spring Security Test Context
- 실행 대상: `ReviewCommentAuthenticatedSecurityTest`
- 실제 구성: `SecurityConfig`, `JwtAuthenticationFilter`, `DeletedMemberFilter`, `AuthService`, `CommentController`
- Mock 협력 객체: `JwtUtil`, `RedisTemplate`, `MemberRepository`, `CommentService`, OAuth 관련 Bean
- 실제 서버·실제 JWT 생성·외부 Redis·DB·Docker·실제 계정·비밀 설정: 사용하지 않음

## 4. 사전조건과 데이터

- Authorization 헤더는 전송하지 않는다.
- 이름이 `accessToken`인 쿠키에 안전한 테스트 토큰을 설정한다.
- JWT claims는 검증된 일반 사용자 `security-author`, 권한 `USER`를 반환하도록 격리한다.
- Mock Redis에는 같은 사용자의 액세스 토큰이 저장된 것으로 구성한다.
- 댓글 수정 요청은 `commentId=20`, `reviewId=10`, 유효 내용 `수정된 댓글`을 사용한다.

## 5. 재현 절차

1. IntelliJ에서 `ReviewCommentAuthenticatedSecurityTest` 클래스를 실행한다.
2. TF-TC-026 Bearer 인증과 TF-TC-028 invalid JWT 차단이 Pass하는지 확인한다.
3. TF-TC-027 `accessToken` 쿠키 전용 댓글 수정 결과를 확인한다.
4. 실패 로그에서 `token is null`, `AuthService.java:161`, `CommentController.java:80` 호출 경로를 확인한다.

## 6. 기대 결과

- 유효 쿠키를 지원하는 현재 Security 필터 계약을 유지한다면 요청은 인증 회원으로 Controller·댓글 수정 Service에 전달되고 HTTP 200·본문 코드 `200-4`를 반환해야 한다.
- 제품 정책상 쿠키 전용 인증을 지원하지 않는다면 Security 계층에서 통제된 401/403으로 거부해야 한다.
- 어느 정책이든 null 토큰 NPE나 요청 처리 예외·5xx로 종료되면 안 된다.
- TF-TC-026 Bearer 인증 성공과 TF-TC-028 유효하지 않은 JWT HTTP 401은 계속 유지돼야 한다.

## 7. 실제 결과

- 유효 쿠키는 `JwtAuthenticationFilter`에서 사용자 인증까지 완료한다.
- `CommentController.updateComment`는 선택적 Authorization 헤더 값 null을 `AuthService.getLoggedInMember()`에 전달한다.
- `AuthService.getLoggedInMember()`가 `token.replace("Bearer ", "")`를 실행하면서 NPE가 발생한다.
- 정상 HTTP 응답과 `CommentService.updateComment` 호출에는 도달하지 못한다.
- 핵심 로그:

```text
Request processing failed: java.lang.NullPointerException:
Cannot invoke "String.replace(java.lang.CharSequence, java.lang.CharSequence)"
because "token" is null

at com.tripfriend.domain.member.member.service.AuthService.getLoggedInMember(AuthService.java:161)
at com.tripfriend.domain.review.controller.CommentController.updateComment(CommentController.java:80)
```

## 8. 재현 결과

| 실행 | 실행자 | 결과 | 증거 |
|---|---|---|---|
| TF-EXEC-031 | Codex with user authorization | TF-TC-026·028 Pass, TF-TC-027 Fail; 3 tests, 1 failure | JUnit XML, Gradle HTML·콘솔 결과 |
| TF-EXEC-032 | User | 동일 2 Pass·1 Fail, IntelliJ 512ms | [사용자 실행 화면](../evidence/TF-EXEC-032-review-comment-authenticated-security-user-run.png), 사용자 제공 실패 로그 |

- 검증 지점 도달 실행: 2회.
- 동일 실패: 2회.
- 환경·테스트 코드 오류와 분리: 완료.
- 제품 코드 수정 후 재검증: `Not Run`.

## 9. 영향과 심각도 근거

- Security 필터가 인증한 정상 사용자의 댓글 수정 요청이 Controller 이후 요청 처리 예외로 중단된다.
- 인증 경계와 Controller의 토큰 전달 방식이 달라 쿠키 전용 세션·복구 경로에서 정상 사용자가 변경 API를 사용할 수 없다.
- TF-RISK-008은 인증·인가 우회 또는 정상 사용자 차단이 변경 API 전체에 영향을 줄 수 있는 P0 위험이다. 이번 실행에서 직접 재현한 범위는 댓글 수정 대표 요청이다.
- 핵심 변경 기능 차단과 서버 요청 처리 예외를 근거로 심각도를 `Major`로 제안한다.
- 권한 우회, 데이터 손상·삭제와 실제 운영 장애는 이번 테스트에서 확인하지 않았다.
- 현재 프론트는 헤더와 쿠키를 함께 전송하므로 쿠키 전용 경로의 실제 사용자 빈도는 후속 API·UI 검증이 필요하다.

## 10. 코드 상관관계와 추정 원인

- `JwtAuthenticationFilter`는 Authorization 헤더 토큰을 우선하고, 헤더가 없으면 `accessToken` 쿠키를 사용해 SecurityContext 인증을 설정한다.
- `CommentController.updateComment`는 SecurityContext의 인증 주체를 사용하지 않고 선택적 Authorization 헤더 문자열을 `AuthService.getLoggedInMember()`에 다시 전달한다.
- `AuthService.getLoggedInMember()`는 token null 여부를 확인하지 않고 `replace("Bearer ", "")`를 호출한다.
- 필터의 쿠키 지원과 Controller·AuthService의 헤더 전용 처리 방식이 일치하지 않는 것이 직접 원인으로 판단된다.

## 11. 수정 후 재검증 조건

- TF-TC-027에서 null 토큰 NPE와 요청 처리 예외가 발생하지 않는지 확인한다.
- 쿠키 인증을 지원한다면 HTTP 200·본문 코드 `200-4`와 작성자 댓글 수정 Service 호출을 확인한다.
- 쿠키 인증을 지원하지 않는 정책으로 확정한다면 Security 계층에서 일관된 401/403과 변경 Service 미호출을 확인한다.
- TF-TC-026 Bearer 인증 성공이 계속 HTTP 200·`200-4`인지 확인한다.
- TF-TC-028 invalid JWT가 계속 HTTP 401이고 사용자 조회·변경 Service를 호출하지 않는지 확인한다.
- 실제 API·프론트 단계에서 헤더+쿠키, 헤더 전용, 쿠키 전용 전달을 비교해 실제 사용자 영향을 재평가한다.

## 12. 현재 한계와 금지 주장

- Web MVC slice와 Mock JWT·Redis·회원 Repository·댓글 Service를 사용한 결과다.
- 실제 서버 HTTP 상태, 실제 JWT 생성·만료, 외부 Redis, DB 댓글 변경을 검증하지 않았다.
- TF-TC-027은 요청 처리 예외로 정상 응답이 완료되지 않았으며 실제 서버가 정확히 어떤 5xx 본문을 반환하는지는 확인하지 않았다.
- 모든 변경 API에서 같은 장애가 실행 재현됐다고 주장하지 않는다. 직접 재현 범위는 댓글 수정 대표 요청이다.
- 쿠키가 제품의 필수 인증 채널이라고 임의 확정하지 않는다.
- 수정 코드와 수정 후 `Pass`는 아직 없다.
- Java 결과로 Kotlin 구현의 동일 결함을 주장하지 않는다.
