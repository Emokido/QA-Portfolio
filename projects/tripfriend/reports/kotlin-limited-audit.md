# TripFriend Kotlin 리뷰 영역 제한적 감사

- 문서 상태: `Stage 6 Limited Static Audit — 완료`
- 감사 대상: 스프린트 2 Kotlin 마이그레이션 완성본의 리뷰 영역과 기존 `ReviewServiceTest.kt`
- 저장소: `https://github.com/Emokido/tripfriend-kotlin`
- 기준 브랜치·커밋: `main` / `ee116b0a40ed1dfe38db3f83363ac1e5a118915d`
- 기준 커밋 일시·제목: `2025-04-13T19:13:47+09:00` / `소셜 로그인 로직 수정`
- 감사일: 2026-09-01
- 감사 주체: `Codex with user authorization`
- 제품 테스트 상태: `Not Run` — 빌드·테스트·애플리케이션·서버·DB를 실행하지 않았다.

> 이 문서는 Kotlin 전체 회귀나 현재 정상 동작을 증명하지 않는다. 기존 테스트의 설명 가능성과 Java P0에서 드러난 핵심 계약의 정적 동등성만 제한적으로 확인한다.

## 1. 목적과 범위

### 목적

- AI 도움으로 작성된 기존 MockK 테스트가 무엇을 검증하고 무엇을 검증하지 못하는지 분리한다.
- Java/Spring P0 실행에서 중요했던 리뷰 수정·입력·인증·HTTP 계약이 Kotlin 마이그레이션 코드에서 어떻게 표현되는지 정적으로 비교한다.
- 과거 개발 당시 통과했다는 사용자 진술을 현재 QA 실행 결과로 바꾸지 않는다.

### 포함

- `backend/build.gradle.kts`
- `backend/src/test/java/com/tripfriend/domain/review/service/ReviewServiceTest.kt`
- 리뷰·댓글의 Service, Controller, 요청 DTO와 `Review` Entity
- Java P0 실행 결과와의 선택적 계약 비교

### 제외

- Kotlin 테스트·빌드·서버·DB 실행
- Kotlin 전체 도메인·MockK 전체 감사
- Java–Kotlin 전체 회귀
- 제품 코드·기존 테스트·설정 수정
- 정적 관찰만으로 Kotlin 결함 확정 또는 Java 결함 재현 횟수에 합산

## 2. 기존 테스트 구조

`ReviewServiceTest.kt`는 JUnit 5와 MockK를 사용한 Service 단위 테스트 28개다.

| 그룹 | 테스트 수 | 대표 검증 |
|---|---:|---|
| 리뷰 생성 | 4 | 성공, 장소 ID null, 평점 6.0, 미존재 장소 |
| 리뷰 상세 조회 | 4 | 조회수 증가·미증가, 조회수 엔티티 생성, 미존재 리뷰 |
| 리뷰 수정 | 4 | 성공, 미존재, 타인, 평점 6.0 |
| 리뷰 삭제 | 3 | 성공, 미존재, 타인 |
| 장소별 조회 | 3 | 정상, 장소 ID null, 빈 목록 |
| 목록·검색 | 5 | 최신순, 키워드, 장소, 회원, 잘못된 정렬값 |
| 인기 리뷰 | 2 | 정상, 빈 목록 |
| 회원별 조회 | 3 | 정상, 회원 ID null, 빈 목록 |

### 확인한 강점

- `@Nested`와 한글 `@DisplayName`으로 기능별 의도가 구분된다.
- Given–When–Then 구조, 정상·예외 분기, 오류 코드·메시지 assertion이 있다.
- 일부 예외 테스트는 `verify(exactly = 0)`으로 저장소가 호출되지 않았음을 확인한다.
- 조회수 증가 여부, 미존재 데이터, 작성자 권한 등 Service 규칙을 빠르게 격리한다.

### 확인한 한계

- Repository·Entity·조회수 객체를 모두 Mock하므로 실제 JPA 저장·조회·정렬과 Controller HTTP 계약은 검증하지 않는다.
- 생성 성공 결과는 미리 정한 `testReview` Mock 반환값을 사용하고, 저장 인자의 필드 매핑을 캡처해 확인하지 않는다.
- 수정 성공은 `testReview.update(any(), any(), any())`와 미리 정한 getter 결과를 사용하므로 전달 인자의 정확성과 여행지 변경을 검증하지 않는다.
- 정렬·인기 조회 정상 테스트는 리뷰 한 건으로 결과를 확인해 순서 규칙이나 동률 우선순위를 증명하지 못한다.
- CommentService·Controller·Security 테스트가 없어 댓글 `reviewId`, HTTP status와 인증 전달 계약을 검출하지 못한다.
- `TestPlace`와 `TestReview` 보조 클래스는 선언돼 있지만 실제 fixture에 사용되지 않는다.

## 3. Java 핵심 계약과 정적 비교

| 감사 ID | Kotlin 정적 근거 | Java P0와의 관계 | 판정 |
|---|---|---|---|
| TF-KAUD-001 | `ReviewRequestDto.placeId`가 존재하지만 `ReviewService.updateReview`는 `review.update(title, content, rating)`만 호출하고, `Review.update`도 여행지를 받지 않는다. 기존 수정 성공 테스트 역시 placeId를 준비·검증하지 않는다. | Java TF-TC-008·TF-BUG-001에서 확인한 여행지 관계 미갱신과 같은 구조다. | `정적 회귀 위험` — Kotlin 실행 전 결함 확정 금지 |
| TF-KAUD-002 | `CommentRequestDto.reviewId`는 nullable이고 `@NotNull`이 없으며, `CommentService.createComment`가 `requestDto.reviewId!!`를 사용한다. | Java TF-TC-043·TF-BUG-006의 누락·null 입력 위험과 대응한다. Kotlin에서는 Repository 호출 전 강제 역참조 예외 가능성이 있다. | `정적 회귀 위험` — HTTP·미저장 결과 미검증 |
| TF-KAUD-003 | 변경 Controller의 Authorization 헤더는 `required = false`인데 `token!!`로 AuthService에 전달한다. | Java TF-TC-027·TF-BUG-005에서 확인한 헤더·쿠키 전달 경계와 대응한다. Kotlin Security 필터·실제 요청은 실행하지 않았다. | `정적 회귀 위험` — 쿠키 경로 영향 미확정 |
| TF-KAUD-004 | 리뷰·댓글 생성 Controller가 본문 코드 `201-1`을 반환하지만 별도 HTTP 201 설정은 보이지 않는다. | Java TF-BUG-002·004의 HTTP 201·200 불일치와 같은 Controller 응답 형태다. | `정적 계약 관찰` — 실제 HTTP status 미실행 |
| TF-KAUD-005 | 기존 테스트는 ReviewService Mock 단위 28개로 한정되고 Controller·Comment·Repository/H2·Security 계층은 없다. | Java Stage 6은 계층별 테스트로 P0 49개를 실행했다. Kotlin 기존 테스트만으로 동일 범위를 주장할 수 없다. | `커버리지 차이` |

정적 형태가 같다는 사실은 Kotlin 제품 결과가 Java와 같다는 뜻이 아니다. Kotlin에서 별도로 실행하지 않았으므로 TF-BUG-001·002·004·005·006의 재현 횟수와 P0 38 Pass·11 Fail 수치에는 반영하지 않는다.

## 4. 테스트·의존성 설명 가능성

- `@ExtendWith(MockKExtension::class)`: JUnit 5가 MockK 필드를 초기화하도록 연결한다.
- `@MockK`: Repository와 도메인 협력 객체를 가짜 객체로 만든다.
- `@InjectMockKs`: 생성자 의존성을 Mock으로 채워 실제 `ReviewService`를 만든다.
- `every { ... } returns ...`: 호출 시 돌려줄 값을 준비하는 stub이다.
- `just runs`: 반환값 없는 함수의 호출을 허용한다.
- `verify { ... }`: 예상한 협력 객체 호출이 있었는지 확인한다.
- `verify(exactly = 0)`: 예외 경로에서 저장·후속 조회가 발생하지 않았는지 확인한다.
- `assertFailsWith<ServiceException>`: 예상 예외 타입을 검증하고 오류 코드·메시지를 추가로 비교한다.

MockK Service 테스트의 가치는 규칙을 빠르게 격리하는 데 있다. 실제 HTTP status, Security 필터, JPA 쿼리, 트랜잭션 저장 결과와 정렬 순서는 MockK 결과만으로 증명할 수 없다.

## 5. 공개 포트폴리오 표현 경계

### 사용 가능한 표현

- Kotlin 마이그레이션 프로젝트의 기존 MockK 리뷰 Service 테스트 28개를 정적으로 감사했다.
- 테스트 의도·Mock·stub·verify·assertion과 계층 한계를 Java P0 실행 경험을 기준으로 재검토했다.
- Java에서 재현된 핵심 위험이 Kotlin 코드에도 같은 형태로 남아 있을 가능성을 정적으로 식별했다.

### 사용하면 안 되는 표현

- Kotlin 테스트 28개가 현재 모두 Pass했다.
- Java 결함이 Kotlin에서도 실행 재현됐다.
- Kotlin 전체 회귀·전체 마이그레이션 검증을 완료했다.
- 기존 `ReviewServiceTest.kt`를 AI 도움 없이 독립적으로 설계·작성했다.
- Kotlin·MockK를 숙련되게 사용한다.

## 6. 결론

- Stage 6의 `Kotlin 제한적 감사`는 현재 기준 커밋의 정적 검토 범위로 완료했다.
- Kotlin 전체 회귀는 P2 후속 범위로 유지한다.
- Kotlin 제품 테스트 상태는 계속 `Not Run`이다.
- 기존 테스트는 Service 규칙 학습 자료로는 유효하지만 현재 포트폴리오의 핵심 실행 증거는 Java P0 49개 결과를 사용한다.
- TF-KAUD-001~005는 정적 감사 결과이며 신규 제품 결함이 아니다.
