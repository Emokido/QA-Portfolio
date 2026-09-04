# TF-BUG-001 — 리뷰 수정 시 선택한 여행지가 실제 관계에 반영되지 않음

- 결함 상태: `Open`
- 발견일: 2026-08-28
- 기준 프로젝트: 스프린트 1 Java/Spring 완성본
- 기준 커밋: `e2431223ce2a18c8f944d544cba76b951ce0356d`
- 영향 기능: 리뷰 수정
- 테스트 수준: 서비스 단위 테스트(Service unit test)
- 심각도: `Major`
- 수정 우선순위: 제품 담당자 결정 필요
- 재현성: 검증 지점에 도달한 실행 5/5에서 동일 실패
- 보고 주체: `Codex with user authorization`, 사용자 직접 재현 확인

> 이 결함은 TF-REQ-004 사용자 확인 오라클과 다섯 번의 검증 지점 도달 실행을 근거로 유지한다. 아직 제품 코드 수정이나 수정 후 재검증은 수행하지 않았다.

## 1. 요약

작성자가 리뷰의 제목·내용·평점과 여행지를 함께 수정하면 기본 필드는 변경되지만, 요청한 `placeId`가 실제 `Review-Place` 관계에 반영되지 않는다. 그 결과 리뷰가 수정 전 여행지에 계속 연결된다.

## 2. 추적 연결

`TF-REQ-004 → TF-RISK-001 → TF-COND-003 → TF-TC-008 → TF-EXEC-002~005·TF-EXEC-007 → TF-BUG-001`

| 식별자 | 연결 내용 |
|---|---|
| TF-REQ-004 | 리뷰 수정 요청에서 선택한 여행지로 실제 연결 정보가 변경돼야 함 |
| TF-RISK-001 | 여행지 변경 요청과 실제 엔티티 관계가 불일치할 위험 |
| TF-COND-003 | 작성자의 리뷰 기본 필드·여행지 수정 |
| TF-TC-008 | 작성자 리뷰 여행지 변경 검증 |
| TF-EXEC-002 | Codex 승인 실행 — 동일 assertion 실패 |
| TF-EXEC-003 | 사용자 IntelliJ 직접 실행 — 동일 assertion 실패 |
| TF-EXEC-004 | Codex 승인 실행 — 동일 assertion 실패 |
| TF-EXEC-005 | 사용자 IntelliJ 직접 실행 — 동일 assertion 실패 |
| TF-EXEC-007 | 사용자 IntelliJ 최신 10건 직접 실행 — 동일 assertion 실패 |

## 3. 테스트 환경

- Java: 빌드 선언 기준 17
- Gradle Wrapper: 8.12.1
- Spring Boot Gradle plugin: 3.2.4
- 테스트 도구: JUnit 5, Mockito, AssertJ
- 실행 대상: `ReviewServiceTest`
- 실행 명령 기준: `.\gradlew.bat test --tests "com.tripfriend.domain.review.service.ReviewServiceTest" --no-daemon`
- 외부 서버·DB·Docker·실제 계정·비밀 설정: 사용하지 않음

## 4. 사전조건과 데이터

- 작성자 A가 존재한다.
- 부산 장소와 제주 장소가 서로 다른 ID로 존재한다.
- 작성자 A의 기존 리뷰는 부산 장소에 연결돼 있다.
- 수정 요청에는 유효한 제목·내용·평점과 제주 장소 ID가 포함된다.
- 리뷰·댓글·조회수·장소 Repository는 서비스 단위 목적에 맞게 Mock한다.

## 5. 재현 절차

1. 부산 장소에 연결된 작성자 A의 기존 리뷰를 준비한다.
2. 수정 요청 DTO의 `placeId`를 제주 장소 ID로 설정한다.
3. `ReviewService.updateReview(reviewId, requestDto, author)`를 호출한다.
4. 수정된 리뷰의 제목·내용·평점과 `Review.getPlace()`를 확인한다.
5. 실제 장소 관계가 요청한 제주 장소와 같은지 비교한다.

## 6. 기대 결과

- 제목·내용·평점이 요청값으로 변경된다.
- 리뷰 ID와 작성자는 유지된다.
- `Review.getPlace()`는 요청의 제주 장소를 참조한다.

## 7. 실제 결과

- 제목·내용·평점 수정 검증은 통과했다.
- `Review.getPlace()`는 기존 부산 장소를 계속 참조했다.
- 기대한 제주 장소와 실제 장소가 서로 다른 객체여서 assertion이 실패했다. 초기 등록 실행의 호출 위치는 `ReviewServiceTest.java:103`, 최신 10건 사용자 실행은 `ReviewServiceTest.java:189`다.

사용자 제공 실패 로그의 핵심은 다음과 같다.

```text
[TF-REQ-004에 따라 수정 요청의 placeId가 실제 Review-Place 관계에 반영되어야 한다]
Expecting actual and expected Place instances to refer to the same object
at ReviewServiceTest.updateReview_changesPlaceForAuthor(ReviewServiceTest.java:103)
```

객체 식별용 해시 문자열은 환경마다 달라질 수 있어 공개 결함 보고서에는 포함하지 않는다.

## 8. 재현 결과

| 실행 | 실행자 | 결과 | 증거 |
|---|---|---|---|
| TF-EXEC-002 | Codex with user authorization | TF-TC-007·009·010 Pass, TF-TC-008 Fail | JUnit XML, Gradle HTML·콘솔 결과 |
| TF-EXEC-003 | User | 동일 3 Pass·1 Fail | IntelliJ Test Results 화면, 사용자 제공 실패 로그 |
| TF-EXEC-004 | Codex with user authorization | 신규 TF-TC-004~006 Pass, TF-TC-008 동일 Fail | 최신 JUnit XML, Gradle HTML·콘솔 결과 |
| TF-EXEC-005 | User | 최신 7개 중 6 Pass, TF-TC-008 동일 Fail | IntelliJ Test Results 화면, 사용자 제공 실패 로그 |
| TF-EXEC-007 | User | 최신 10개 중 9 Pass, TF-TC-008 동일 Fail | [사용자 실행 화면](../evidence/TF-EXEC-007-review-service-user-run.png) |

- 검증 지점 도달 실행: 5회.
- 동일 실패: 5회.
- 환경·테스트 코드 오류와 분리: 완료.
- 제품 코드 수정 후 재검증: `Not Run`.

## 9. 영향

- 사용자가 리뷰 수정 화면에서 다른 여행지를 선택해도 실제 저장 관계가 이전 여행지로 남을 수 있다.
- 수정 응답의 기본 필드만 보면 성공처럼 보이지만 장소별 조회·필터·상세 표시에서는 이전 장소로 노출될 가능성이 있다.
- 핵심 수정 기능의 관계 데이터 정확성을 훼손하므로 심각도를 `Major`로 제안한다.
- 데이터 삭제·권한 우회·전체 서비스 중단은 이번 테스트에서 확인하지 않았다.

## 10. 코드 상관관계와 추정 원인

기준 코드의 `ReviewService.updateReview`는 리뷰와 작성자를 확인한 뒤 `review.update(title, content, rating)`을 호출한다. 해당 경로에서 요청의 `placeId`로 장소를 조회하거나 리뷰의 장소 관계를 갱신하는 처리는 확인되지 않았다.

이는 실행 결과와 일치하는 코드 상관관계이며, 수정 구현 자체는 제품 코드 변경 승인과 설계 검토 후 별도로 수행해야 한다.

## 11. 수정 후 재검증 조건

- TF-TC-008: 여행지 관계가 요청 장소로 변경되는지 재실행한다.
- TF-TC-007: 제목·내용·평점 수정 회귀가 없는지 확인한다.
- TF-TC-009: 비작성자가 장소를 포함한 어떤 필드도 변경하지 못하는지 확인한다.
- TF-TC-010: 미존재 리뷰가 새로 생성되거나 다른 데이터에 영향을 주지 않는지 확인한다.
- 후속 Repository 또는 API 검증에서 실제 저장 장소 ID와 응답 장소 정보를 확인한다.

## 12. 현재 한계와 금지 주장

- 서비스 단위 Mock 테스트 결과이며 실제 H2·MySQL 저장 관계는 아직 검증하지 않았다.
- HTTP 응답·Security 필터·프론트 화면은 검증하지 않았다.
- 수정 코드와 수정 후 `Pass`는 아직 없다.
- Java 결과로 Kotlin 구현의 동일 결함을 주장하지 않는다.
- 제품 담당자가 수정 우선순위와 배포 결정을 확정했다고 주장하지 않는다.
