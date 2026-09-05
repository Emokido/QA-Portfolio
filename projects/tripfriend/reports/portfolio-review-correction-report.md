# 제출본 검토에 따른 댓글 Controller 판정 교정

- 검토일: 2026-09-05
- 실행 ID: `TF-REVIEW-EXEC-001`
- 실행자: `Codex with user authorization`
- 제품 기준 커밋: `e2431223ce2a18c8f944d544cba76b951ce0356d`
- 최신 누적 P0: **45 Pass·4 Fail·0 Blocked·0 Not Run**
- 결함·관찰 기록: **15건 — 13 Open·2 Closed(테스트 구성 오탐)**

## 결론

TF-BUG-004는 제품 결함이 아니라 독립형 MockMvc가 실제 응답 변환 `ResponseAspect`를 포함하지 않아 발생한 테스트 구성 오탐이다. 교정한 Web MVC slice에서 댓글 길이 1·2·100·101자 네 테스트가 모두 통과했다. TF-TC-040·041을 Pass로 정정하고 TF-BUG-004를 `Closed — Not a Product Defect / Test Setup False Positive`로 분류했다. 제품 코드는 수정하지 않았다.

45 Pass·4 Fail은 **케이스별 최신 근거를 합친 누적 판정**이다. 이번 실행에서 P0 49개 전체를 다시 실행한 것이 아니다. Stage 6의 38 Pass·11 Fail, Stage 8 완료 당시의 43 Pass·6 Fail은 역사적 결과로 보존한다.

## 가설과 교정

기존 `CommentControllerTest`는 `standaloneSetup`과 예외 처리기만 사용했다. 실제 앱은 `ResponseAspect`가 `RsData`의 상태 코드를 HTTP 응답에 적용하며, 기존 Postman 정상 댓글 생성 기록도 HTTP 201을 기대하는 요청을 Pass로 기록했다. 따라서 기존 200 관찰을 실제 서버 결함으로 일반화할 수 없었다.

기존 리뷰 Controller 교정과 같은 `@WebMvcTest` 구성에 실제 `CommentController`, `GlobalExceptionHandler`, `ResponseAspect`와 AOP를 포함했다. `CommentService`, `AuthService`, JPA metamodel은 Mock이며 Security 필터는 비활성화했다. 목적 밖 `DeletedMemberFilter`는 제외했다. 기존 입력·HTTP·본문·서비스 호출 assertion은 유지했다.

## 실행 결과

| 케이스 | 입력 | 기대·실제 결과 | 판정 변화 |
|---|---|---|---|
| TF-TC-039 | 댓글 1자 | HTTP 400·본문 `400-1`, 인증·댓글 서비스 미호출 | Pass 유지 |
| TF-TC-040 | 댓글 2자 | HTTP 201·본문 `201-1`, 인증·댓글 서비스 호출 확인 | 과거 Fail → 교정 후 Pass |
| TF-TC-041 | 댓글 100자 | HTTP 201·본문 `201-1`, 인증·댓글 서비스 호출 확인 | 과거 Fail → 교정 후 Pass |
| TF-TC-042 | 댓글 101자 | HTTP 400·본문 `400-1`, 인증·댓글 서비스 미호출 | Pass 유지 |

- Java 17, 기존 Gradle 캐시, `--offline`, 지정 클래스 1회 실행; 승인된 최대 2회 중 1회 사용.
- Gradle: `BUILD SUCCESSFUL in 20s`.
- JUnit XML: tests 4, failures 0, errors 0, skipped 0, suite time 0.405초.
- XML 원문 timestamp: `2026-09-05T03:08:12` (시간대 표기가 없으므로 변환하지 않음).
- XML SHA-256: `f69243c328d04ed997051dfd5e6e94cc850606ffe934560da7714cbe8ec0db42`.
- 원본 XML·로그는 로컬 검증 근거다. 공개 자료에는 판정에 필요한 요약만 제공하며 인증 자료·호스트명·로컬 절대경로를 포함하지 않는다.

제품 실행용 작업본의 `backend`에서 실행한 명령:

```powershell
.\gradlew.bat test --tests "com.tripfriend.domain.review.controller.CommentControllerTest" --no-daemon --offline
```

## 현재 집계와 범위

- Pass 45개: TF-TC-001~007·009~026·028~037·039~042·044~049.
- Fail 4개: TF-TC-008·027·038·043. 각각 TF-BUG-001·005·003·006과 연결된다.
- Closed 오탐: TF-BUG-002·004. 제품 수정 완료로 Closed한 것이 아니다.
- Open 기록 13개: TF-BUG-001·003·005~015. TF-BUG-012는 정책 확인 필요, TF-BUG-013은 환경 제한 관찰로 확정 수준을 구분한다.
- Stage 7 API·브라우저 7 Pass·1 Fail, 추가 Postman 5 Pass·2 Fail, QA 사본 교정 후 Playwright 정상 회귀 5 Pass와 Known Defect 3 Fail은 기존 실행 결과를 보존한다. 이번에는 재실행하지 않았다.
- Security 실제 체인, 실제 DB 저장, 외부 서버·Redis·OAuth와 제품 배포 수정은 이번 교정으로 검증되지 않는다.
- 이번 교정의 사용자 직접 재현은 아직 수행하지 않았다. 기존 사용자 실행 이력을 이번 실행으로 대체하지 않는다.

## 삭제 검증 검토 — 미실행 보강안

Playwright TF-E2E-003-B는 수정 후 제목 표시, 삭제 UI와 목록 복귀를 확인하지만 삭제 응답·후속 자원 미존재를 직접 assertion하지 않는다. Postman TF-S8-POSTMAN-004도 삭제 응답 계약까지 확인한다. 따라서 현재 성과는 각 검증 지점에 한정한다. 향후 삭제 후 GET 404 또는 목록 미존재와 수정 내용 재조회를 추가할 수 있으나 이번에는 Playwright·Postman 코드나 실행 범위를 확대하지 않았다.

## 연결 자료

- [교정 테스트 소스](../automation/backend/src/test/java/com/tripfriend/domain/review/controller/CommentControllerTest.java)
- [TF-BUG-004 정정 및 원본 이력](../defects/TF-BUG-004-comment-create-http-status-200.md)
- [전체 결과 요약](tripfriend-stage-8-results-summary.md)
- [백엔드 실행 역사](p0-backend-test-execution-report.md)
