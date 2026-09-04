# TripFriend 리뷰·댓글 테스트 베이시스와 기능 흐름 분석

- 문서 상태: `Stage 2 Baseline`
- 작업 단계: `2단계 — TripFriend 테스트 대상 이해와 기능 흐름 분석`
- 테스트 실행 상태: `Not Run`
- 기준 저장소: `https://github.com/Emokido/tripfriend-spring`
- 기준 브랜치: `main`
- 기준 커밋: `e2431223ce2a18c8f944d544cba76b951ce0356d`
- 요구사항 근거: 개인 Notion 백업본의 2차 Java/Spring 기획서·요구사항 명세서
- 주 테스트 대상: 스프린트 1 Java/Spring 완성본의 리뷰·댓글 영역
- 보조 대상: 스프린트 2 Kotlin 완성본의 선택적 마이그레이션 회귀 비교
- 작성 주체: `Codex with user-authorized file creation`

> 이 문서는 코드와 README를 읽어 테스트 기준과 후보를 정리한 분석 산출물이다. 테스트·빌드·애플리케이션을 실행한 결과가 아니며, 코드 기반 위험은 재현 전까지 제품 결함으로 확정하지 않는다.

## 1. 5W1H

| 구분 | 내용 |
|---|---|
| Why | 리뷰·댓글 기능을 실제 구현 근거로 이해하고, 이후 환경 구성·테스트 설계·실행이 추측에 의존하지 않도록 한다. |
| What | 리뷰·댓글의 화면, HTTP API, Controller, Service, Repository, Entity, 인증·권한, 입력 검증, 예외 처리와 기존 테스트를 분석한다. |
| Who | 사용자는 과거 개발 기여와 원래 기능 의도를 확인하고, Codex는 승인된 범위에서 코드 근거 분석과 문서 작성을 보조한다. |
| When | 3단계 테스트 환경 감사와 4~5단계 테스트 계획·케이스 설계 전에 수행한다. |
| Where | 기존 팀 저장소를 수정하지 않고 별도의 읽기 전용 감사본을 기준으로 분석한다. 공개 후보 산출물은 개인 QA 포트폴리오에 작성한다. |
| How | `README·요구사항 기록 → 화면 호출 → API 계약 → 서비스 규칙 → 저장 구조 → 테스트 공백·위험 가설` 순으로 추적한다. |

## 2. 범위와 근거 수준

### 포함 범위

- 리뷰 목록·상세·생성·수정·삭제
- 장소별·회원별·내 리뷰 조회
- 검색·정렬·인기 리뷰·조회수
- 댓글 목록·상세·생성·수정·삭제·내 댓글 조회
- 비로그인 사용자, 로그인 사용자, 작성자, 비작성자의 접근 흐름
- 프론트엔드 커뮤니티 화면과 백엔드 API 연결
- 기존 자동화 테스트 존재 여부와 테스트 공백

### 제외 또는 후속 범위

- 애플리케이션·DB·Redis·OAuth·메일의 실제 실행 가능성: 3단계
- 상세 테스트 계획과 우선순위 확정: 4단계
- 테스트케이스와 데이터 최종 설계: 5단계
- Java 테스트 작성·실행: 6단계
- API·UI 실제 검증: 7단계
- Kotlin 전체 코드 감사: 초기 제출 범위에서 제외

### 근거 구분

| 구분 | 의미 |
|---|---|
| 확인된 사실 | 기준 커밋의 README·코드·Git 상태에서 직접 확인한 내용 |
| 코드 기반 위험 가설 | 정적 분석으로 예상한 문제이며 실제 실행 재현 전인 내용 |
| 확인 필요 | 코드만으로 원래 요구사항이나 기대 결과를 확정할 수 없는 내용 |
| 계획 | 후속 단계에서 설계하거나 실행할 내용 |

### Notion 요구사항 근거

개인 Notion 백업본에서 다음 경로를 읽기 전용으로 확인했다.

- 주 근거: `백업본 → 2차 프로젝트 (1) → Team 10 | 텐텐 → TripFirend(동행친구) → 서비스 소개 → 기획서·요구사항 명세서`
- 비교 근거: `백업본 → Team 10 | 텐텐 (1) → TripFirend(동행친구) → 서비스 소개 → 기획서·요구사항 명세서`
- `(1)`이 없는 경로는 2차 Java/Spring, `(1)` 경로는 3차 Kotlin 프로젝트다.
- 내부 Notion 링크와 팀 문서 원문은 공개 후보 문서에 복제하지 않는다.

2차 요구사항에서 직접 확인된 여행지 후기 테스트 기준은 다음과 같다.

| 요구사항 기준 | 문서에 명시된 내용 | 사용 방법 |
|---|---|---|
| 리뷰 검색 | 사용자가 원하는 여행지 후기를 검색할 수 있음 | 검색 기능의 기본 오라클 |
| 리뷰 작성 | 여행지 방문 후 후기를 작성할 수 있음 | 장소와 연결된 리뷰 생성 오라클 |
| 리뷰 수정·삭제 | 사용자가 작성한 리뷰를 수정·삭제할 수 있음 | 작성자 권한의 기본 오라클 |
| 정렬 조회 | 조회수·평점·댓글 기준으로 정렬 가능 | 세 정렬 기능의 기본 오라클 |
| 평점 시스템 | 평점을 부여하고 높은 평점 게시글을 상위 노출 | 평점 저장·정렬의 기본 오라클 |
| 인증 | JWT 기반 로그인·인증 | 변경 API 인증 테스트의 기본 오라클 |
| 데이터 모델 | `Review`, `Comment`, `Review_View_Count` | 저장·삭제·조회수 관계 테스트 근거 |

2026-08-26 사용자 확인으로 `리뷰 수정 시 여행지 변경 가능`을 추가 오라클로 확정했다. 이는 Notion 문서 직접 근거가 아니라 과거 프로젝트의 실제 의도에 대한 사용자 확인 근거로 구분한다.

문서의 API 500ms와 초당 1,000명 요청은 과거 프로젝트의 비기능 목표다. 이번 단계에서 달성 여부를 검증하지 않았으므로 결과나 성과로 주장하지 않는다.

## 3. 본인 기여와 팀원 기여 경계

README 역할 분담과 고정 인수인계의 Git·사용자 확인 기록을 기준으로 다음 경계를 사용한다.

| 구분 | 범위 |
|---|---|
| 본인 개발 기여 | 여행 후기 게시판, 댓글 관리, 평점·조회수, 리뷰 화면과 API 연동 및 관련 오류 수정 |
| 팀원 주 기여 | 회원·JWT·Security·OAuth·배포, 여행지·여행 일정, 동행, 관리자·공지·이벤트 등 다른 도메인 |
| 현재 QA 신규 활동 | 리뷰·댓글 테스트 베이시스 분석, 테스트 계획·케이스·데이터, 승인된 테스트 구현·실행, 결과·증거·결함 정리 |
| 공동·연동 영역 | 인증된 사용자 정보, 회원 프로필, 여행지 연결 등은 팀원 구현과 본인 연동 수정이 함께 포함될 수 있으므로 단독 구현으로 주장하지 않는다. |

## 4. 확인된 기능 범위와 API 계약

공통 성공 응답은 `RsData<T>`의 `code`, `msg`, 선택적 `data` 구조다. `ResponseAspect`가 `code`의 앞 세 자리를 실제 HTTP 상태 코드로 사용한다.

### 리뷰 API

| 기능 | HTTP API | 접근 조건 | 주요 입력 | 성공 코드 | 주요 서비스·저장 흐름 |
|---|---|---|---|---|---|
| 리뷰 생성 | `POST /api/reviews` | 인증 필요 | 제목, 내용, 평점, 장소 ID | `201-1` | 회원 확인 → 장소 조회 → 리뷰 저장 → 조회수 0 생성 |
| 리뷰 상세 | `GET /api/reviews/{reviewId}` | 공개 조회 | 리뷰 ID, 세션 | `200-1` | 리뷰 조회 → 세션 최초 조회 판단 → 조회수 처리 → 댓글 수 계산 |
| 장소별 리뷰 | `GET /api/reviews/place/{placeId}` | 공개 조회 | 장소 ID | `200-2` | 장소 ID 기준 최신순 조회 → 댓글 수·조회수 결합 |
| 리뷰 수정 | `PUT /api/reviews/{reviewId}` | 인증 및 작성자 | 리뷰 ID, 제목, 내용, 평점, 장소 ID가 요청 DTO에 포함 | `200-3` | 요구사항상 여행지 변경 가능. 현재 정적 분석에서는 리뷰 조회 → 작성자 비교 → 제목·내용·평점 수정만 확인됨 |
| 리뷰 삭제 | `DELETE /api/reviews/{reviewId}` | 인증 및 작성자 | 리뷰 ID | `200-4` | 리뷰 조회 → 작성자 비교 → 조회수 데이터 삭제 → 리뷰 삭제 |
| 전체 목록 | `GET /api/reviews` | 공개 조회 | `sort`, `keyword`, `placeId` | `200-5` | 조건별 Repository 조회 → 댓글 수·조회수 결합 |
| 인기 리뷰 | `GET /api/reviews/popular` | 공개 조회 | `limit`, 기본 10 | `200-6` | 전체 리뷰 → 조회수·평점·댓글 수 가중 점수 → 내림차순 |
| 내 리뷰 | `GET /api/reviews/my` | 인증 필요 | 토큰 | `200-7` | 로그인 회원 ID → 작성 리뷰 최신순 조회 |
| 회원별 리뷰 | `GET /api/reviews/member/{memberId}` | 공개 조회 | 회원 ID | `200-8` | 회원 ID → 작성 리뷰 최신순 조회 |

### 댓글 API

| 기능 | HTTP API | 접근 조건 | 주요 입력 | 성공 코드 | 주요 서비스·저장 흐름 |
|---|---|---|---|---|---|
| 댓글 생성 | `POST /api/comments` | 인증 필요 | 내용, 리뷰 ID | `201-1` | 회원 확인 → 리뷰 조회 → 댓글 저장 |
| 댓글 상세 | `GET /api/comments/{commentId}` | 공개 조회 | 댓글 ID | `200-1` | 댓글 조회 → 작성자 표시 정보 결합 |
| 리뷰별 댓글 | `GET /api/comments/review/{reviewId}` | 공개 조회 | 리뷰 ID | `200-2` | 리뷰 존재 확인 → 생성일 오름차순 조회 |
| 내 댓글 | `GET /api/comments/my` | 인증 필요 | 토큰 | `200-3` | 로그인 회원 ID → 작성 댓글 최신순 조회 |
| 댓글 수정 | `PUT /api/comments/{commentId}` | 인증 및 작성자 | 댓글 ID, 내용 | `200-4` | 댓글 조회 → 작성자 비교 → 내용 수정 |
| 댓글 삭제 | `DELETE /api/comments/{commentId}` | 인증 및 작성자 | 댓글 ID | `200-5` | 댓글 조회 → 작성자 비교 → 댓글 삭제 |

## 5. 화면에서 DB까지의 주요 흐름

| 사용자 흐름 | 프론트 화면·서비스 | Controller | Service | Repository·Entity |
|---|---|---|---|---|
| 리뷰 목록 열기 | `/community` → `review-list.tsx` → `getReviews()` | `ReviewController.getReviews()` | 정렬·검색·장소 조건 분기 후 DTO 생성 | `ReviewRepository`, `CommentRepository`, `ReviewViewCountRepository` |
| 리뷰 상세 열기 | `/community/{id}` → `getReviewById()`와 `getCommentsByReviewId()` | `getReview()`, `getCommentsByReview()` | 리뷰·조회수·댓글 수와 댓글 목록 조회 | `Review`, `ReviewViewCount`, `Comment` |
| 리뷰 작성 | `/community/write` → `ReviewForm` → `createReview()` | `createReview()` | 토큰 회원 확인, 장소 존재 확인, 리뷰·조회수 생성 | `Member`, `Place`, `Review`, `ReviewViewCount` |
| 리뷰 수정 | `/community/edit/{id}` → 작성자 화면 검사 → `updateReview()` | `updateReview()` | 토큰 회원 확인, 작성자 ID 비교, 제목·내용·평점 수정 | `Review` 영속 상태 변경 |
| 리뷰 삭제 | 상세 화면 → 삭제 확인 → `deleteReview()` | `deleteReview()` | 토큰 회원 확인, 작성자 ID 비교, 조회수·리뷰 삭제 | `ReviewViewCountRepository`, `ReviewRepository` |
| 댓글 작성 | 상세 화면 → `createComment()` | `createComment()` | 토큰 회원 확인, 대상 리뷰 조회, 댓글 저장 | `ReviewRepository`, `CommentRepository` |
| 댓글 수정·삭제 | 상세 화면의 작성자 버튼 → `updateComment()`·`deleteComment()` | 수정·삭제 API | 토큰 회원 확인, 작성자 ID 비교, 변경 또는 삭제 | `CommentRepository`, `Comment` |

## 6. 인증과 권한 흐름

| 사용자 유형 | 공개 조회 | 리뷰·댓글 생성 | 본인 글 수정·삭제 | 타인 글 수정·삭제 | 관리자 예외 |
|---|---|---|---|---|---|
| 비로그인 | 허용된 GET 경로 가능 | Spring Security에서 인증 필요 | 해당 없음 | 해당 없음 | 해당 없음 |
| 로그인 일반 사용자 | 가능 | 가능 | 서비스의 작성자 ID가 일치하면 가능 | `403-1` 또는 `403-2` 서비스 예외 | 없음 |
| 관리자 | 조회·생성은 인증 상태에 따름 | 가능 | 작성자이면 가능 | 코드상 관리자 우회 권한 없음 | 원래 요구사항 확인 필요 |

확인된 인증 구현:

- 프론트는 `localStorage.accessToken`을 `Authorization: Bearer ...` 헤더로 전송하면서 쿠키도 포함한다.
- 백엔드 필터는 헤더 토큰을 우선하고, 없으면 `accessToken` 쿠키를 사용할 수 있다.
- 리뷰·댓글 Controller의 변경 API는 선택적 `Authorization` 헤더 문자열을 다시 `AuthService.getLoggedInMember()`에 전달한다.
- 작성자 권한은 프론트 버튼 표시와 백엔드 서비스의 회원 ID 비교 양쪽에서 확인한다. 실제 보안 오라클은 백엔드 판정이다.

## 7. 입력 제한과 예외 처리

### 요청 값

| 항목 | 확인된 코드 규칙 | 경계 후보 |
|---|---|---|
| 리뷰 제목 | 필수, 공백만 입력 불가, 2~30자 | 0, 1, 2, 30, 31자와 앞뒤 공백 |
| 리뷰 내용 | 필수, 공백만 입력 불가, 10~2000자 | 0, 9, 10, 2000, 2001자와 줄바꿈 |
| 평점 | `double`, 1 이상 5 이하 | 누락, 0, 1, 5, 6, 소수점 값 |
| 장소 ID | 생성 시 null 불가, 실제 장소가 존재해야 함 | 누락, 미존재, 음수·0, 삭제된 장소 |
| 댓글 내용 | 필수, 공백만 입력 불가, 2~100자 | 0, 1, 2, 100, 101자와 공백 |
| 댓글의 리뷰 ID | 생성 시 서비스가 ID로 리뷰를 조회 | 누락, 미존재, 음수·0 |
| 리뷰 정렬 | `newest`, `oldest`, `highest_rating`, `lowest_rating`, `comments`, `most_viewed` | 미지원 값, 빈 문자열, 대소문자 |
| 검색어 | 제목 부분 일치, 최신순 | 빈 값, 공백, 한글·영문, 특수문자 |
| 인기 리뷰 제한 | 기본 10, 코드상 명시적 범위 검증 없음 | 음수, 0, 1, 전체 건수 초과 |

### 예외 계약

| 상황 | 코드상 응답 |
|---|---|
| DTO 검증 실패 | HTTP 400, 코드 `400-1`, 필드별 검증 메시지 결합 |
| 장소 ID 누락 | `400-1` |
| 평점 범위 위반 | `400-2` |
| 장소 ID 누락 조회 | `400-3` |
| 정렬 옵션 오류 | `400-4` |
| 회원 ID 누락 | `400-5` |
| 리뷰 없음 | `404-1` |
| 장소 없음 | `404-2` |
| 댓글 생성 대상 리뷰 없음 | `404-3` |
| 댓글 없음 | `404-4` |
| 리뷰 비작성자의 수정·삭제 | `403-1` |
| 댓글 비작성자의 수정·삭제 | `403-2` |

인증 필터나 `RuntimeException`의 모든 오류 형식이 위 `RsData` 계약으로 통일되는지는 코드만으로 확정하지 않는다.

## 8. 기존 테스트 목록과 공백

| 테스트 파일 | 확인된 범위 | 리뷰·댓글 직접 보장 여부 | 현재 실행 상태 |
|---|---|---|---|
| `TravelPlanApplicationTests.java` | `@SpringBootTest` 컨텍스트 로딩 1건 | 없음 | `Not Run` |
| `MemberServiceTest.java` | 회원 가입·수정·삭제 테스트 | 없음 | `Not Run` |

기준 커밋의 `backend/src/test/`에는 리뷰·댓글 전용 테스트가 확인되지 않았다. 따라서 다음 범위는 자동화 보장이 확인되지 않은 테스트 공백이다.

- ReviewService·CommentService 단위 테스트
- ReviewController·CommentController 요청 검증과 상태 코드 테스트
- Repository 정렬·검색 쿼리 테스트
- 작성자·비작성자 권한 테스트
- 리뷰·댓글·조회수 관계와 삭제 테스트
- 화면과 API 계약의 통합 테스트

## 9. 코드 기반 위험 가설과 테스트 후보

아래 항목은 모두 실행 재현 전이다. `Fail` 또는 제품 결함으로 기록하지 않는다.

| ID | 우선순위 | 코드 기반 위험 가설 | 확인 근거 | 후속 검증 |
|---|---|---|---|---|
| TF-RISK-001 | High | 수정 화면에서 장소를 바꿔 전송해도 서비스가 제목·내용·평점만 변경해 장소가 유지될 수 있다. | 수정 DTO에는 `placeId`가 있지만 `ReviewService.updateReview()`와 `Review.update()`는 장소를 변경하지 않음 | 요구사항 확인 후 API·DB 수정 결과 비교 |
| TF-RISK-002 | High | 이미지 선택 UI가 호출하는 업로드 API가 백엔드에 없어 이미지가 있는 리뷰 등록 후 업로드만 실패할 수 있다. | 프론트 `POST /api/reviews/{id}/images` 호출, 리뷰 Controller에 대응 매핑 없음 | 기능 범위 확인 후 네트워크·화면 메시지 검증 |
| TF-RISK-003 | High | 프론트는 `page`를 전송하지만 백엔드는 페이지 파라미터와 `Pageable`을 사용하지 않아 페이지 이동 시 동일한 전체 목록을 표시할 수 있다. | 프론트 페이지 계산·전송, 백엔드는 전체 `List` 반환 | 7개 이상 데이터로 페이지별 ID 비교 |
| TF-RISK-004 | High | 장소 필터와 검색어를 함께 보내면 장소 조건이 먼저 적용되어 검색어가 무시될 수 있다. | `getReviews()`의 `placeId` 분기가 `keyword` 분기보다 앞섬 | 조합 조건 결과의 교집합 여부 검증 |
| TF-RISK-005 | High | 장소 필터와 일부 정렬을 함께 사용하면 `oldest`, `comments`, `most_viewed`가 적용되지 않고 최신순이 될 수 있다. | 장소 분기는 평점 두 종류 외 모두 최신순 Repository 호출 | 필터별 정렬 순서 비교 |
| TF-RISK-006 | High | 댓글이 있는 리뷰 삭제 시 댓글 관계 정리 규칙이 보이지 않아 FK 제약으로 삭제가 실패하거나 기대하지 않은 데이터 처리가 발생할 수 있다. | `Review`·`Comment` 관계에 삭제 cascade가 없고 삭제 서비스는 조회수와 리뷰만 삭제 | 댓글 유무별 삭제와 DB 잔존 데이터 확인 |
| TF-RISK-007 | High | 목록 API 오류를 프론트 서비스가 빈 배열로 바꾸어 실제 서버 오류가 정상적인 검색 결과 없음처럼 보일 수 있다. | `getReviews()` 등에서 catch 후 빈 결과 반환 | 서버 4xx·5xx·네트워크 오류별 UI 구분 검증 |
| TF-RISK-008 | High | 쿠키로만 인증된 요청은 Security 필터에서는 인증돼도 Controller가 null 헤더를 `getLoggedInMember()`에 전달할 수 있다. | 필터는 쿠키 지원, Controller는 선택적 헤더 문자열을 직접 사용 | 헤더 전용·쿠키 전용·둘 다 있는 요청 비교 |
| TF-RISK-009 | Medium | 댓글 생성의 `reviewId`가 누락되면 명시적 DTO 검증 없이 Repository 호출에서 처리되어 일관된 400 응답이 아닐 수 있다. | `CommentRequestDto.reviewId`에 `@NotNull` 없음 | 누락·null·미존재 ID 응답 계약 확인 |
| TF-RISK-010 | Medium | 인기 리뷰의 음수 `limit`이 명시적으로 검증되지 않아 통일되지 않은 서버 오류가 날 수 있다. | `Stream.limit(limit)` 전에 범위 검증 없음 | 음수·0·과대 limit 응답 확인 |
| TF-RISK-011 | Medium | 동일 리뷰 조회수의 동시 증가가 읽기-증가-저장 방식이라 갱신 손실 가능성이 있다. | `ReviewViewCount.increment()` 후 일반 저장 | 동시 요청 전후 조회수 비교 |
| TF-RISK-012 | Medium | 목록과 상세에서 프로필 이미지 포함 방식이 달라 같은 작성자의 표시가 일관되지 않을 수 있다. | 일부 DTO 생성 경로만 `profileImage` 전달 | 목록·상세·댓글 프로필 표시 비교 |
| TF-RISK-013 | Medium | 로컬 프론트 출처가 CORS 허용 목록에서 덮어써질 수 있다. | `setAllowedOrigins()`가 localhost 뒤에 배포 URL로 다시 호출됨 | 3단계 설정 감사 후 7단계 브라우저 요청 검증 |
| TF-RISK-014 | Medium | 평점 타입이 `double`이라 API는 소수점 평점을 허용할 수 있지만 UI는 정수 별점만 제공한다. | DTO·Entity는 `double`, UI는 1~5 정수 버튼 | 제품 의도 확인 후 1.5 등 소수 입력 검증 |

### 위험 가설의 요구사항 연결 분류

| 분류 | 위험 ID | 해석 |
|---|---|---|
| 요구사항 직접 연결 | TF-RISK-004, 005, 013 | 검색·정렬·CORS는 2차 요구사항에 명시돼 있어 관련 기본 동작을 테스트 오라클로 사용할 수 있다. 다만 복합 필터의 정확한 결합 규칙은 미명시다. |
| 사용자 확인 요구사항 직접 연결 | TF-RISK-001 | 사용자가 리뷰 수정 시 여행지를 변경할 수 있어야 한다고 확인했다. 수정 요청 후 장소 ID가 실제로 변경되는지가 명확한 오라클이다. |
| 기능은 명시, 세부 규칙은 미명시 | TF-RISK-006, 014 | 리뷰 삭제·평점은 요구사항에 있으나 연관 댓글 처리와 평점 단위까지는 정해져 있지 않다. |
| 코드·UI 기반 | TF-RISK-002, 003, 007, 008, 009, 010, 011, 012 | 이미지, 페이지네이션, 오류 표시, 인증 전달 방식, 누락 입력, limit, 동시 조회수와 프로필 표시의 세부 계약은 Notion 요구사항에 없다. |

요구사항에 없는 항목의 실행 결과가 코드와 다르다는 이유만으로 제품 `Fail`을 판정하지 않는다. 먼저 UI가 약속하는 동작, 현재 코드 계약, 일반적인 사용자 기대 중 어떤 기준을 사용했는지 테스트케이스에 기록한다.

## 10. 5일 초기 제출 범위

### P0 — 반드시 포함

1. 공개 리뷰 목록·상세와 댓글 목록
2. 로그인 사용자의 리뷰·댓글 생성
3. 작성자의 리뷰 수정, 여행지 변경, 삭제
4. 작성자의 댓글 수정·삭제
5. 비작성자의 리뷰·댓글 수정·삭제 차단
6. 제목·내용·댓글·평점·장소의 대표 정상값과 최소·최대 경계값
7. 최신순·평점순·댓글순·조회수순의 단일 정렬, 제목 검색, 장소 필터
8. 리뷰 없음·댓글 없음·장소 없음의 대표 예외 응답
9. TF-RISK-001 여행지 변경과 TF-RISK-008 인증 전달의 대표 고위험 검증

### P1 — 시간이 허용되면 포함

- 장소 필터와 검색·정렬의 조합 동작 관찰
- 댓글이 있는 리뷰 삭제 시 현재 처리 관찰
- 페이지 이동과 목록 오류 표시
- 동일 세션 조회수 중복 증가 방지

P1의 미논의 정책은 기대 결과를 임의로 만들지 않는다. 실행 시에는 `현재 동작 관찰`로 기록하고, 데이터 손실·서버 오류·권한 우회처럼 독립적으로 명백한 문제가 있는 경우에만 별도 결함 후보 판단을 한다.

### P2 — 초기 제출에서 제외

- 이미지 업로드
- 인기 점수의 정확한 가중치와 비정상 limit
- 동시 조회수 증가
- 프로필 이미지 표시 일관성
- 성능 목표 검증
- Kotlin 전체 회귀 비교

범위를 좁힌 이유는 5일 안에 테스트 베이시스, 케이스, 실행 기록, 증거와 결함 보고를 서로 추적 가능하게 완성하기 위해서다.

## 11. Kotlin 선택적 비교 후보

Kotlin 저장소를 이번 단계에서 새로 clone하거나 전체 감사하지 않았다. 다음 항목만 비교 후보로 유지하며 현재 결과는 모두 `Not Run / Not Compared`다.

- `/api/reviews`, `/api/comments`의 HTTP 메서드·경로·성공·예외 코드
- 제목·내용·평점·댓글 경계값
- 작성자 권한과 인증 토큰 처리
- 장소 연결, 리뷰 수정 시 변경 가능 필드
- 검색·정렬·인기 리뷰 규칙
- 리뷰 삭제 시 댓글·조회수 관계
- Java의 대표 결함 또는 위험 가설이 Kotlin에서 유지·변경됐는지 여부

## 12. 요구사항으로 확인된 오라클과 미명시 항목

### 문서로 확인된 오라클

- 사용자는 자신이 작성한 리뷰를 수정·삭제할 수 있어야 한다.
- 사용자는 원하는 여행지 후기를 검색할 수 있어야 한다.
- 리뷰는 조회수·평점·댓글 기준으로 정렬할 수 있어야 한다.
- 리뷰에 평점을 부여할 수 있고 높은 평점 게시글은 상위 노출될 수 있어야 한다.
- 변경 기능의 인증 기준은 JWT다.
- 리뷰·댓글·조회수는 별도 데이터 모델로 관리한다.

### 사용자 확인으로 추가된 오라클

- 리뷰 수정 시 여행지를 변경할 수 있어야 한다.
- 근거 유형은 `2026-08-26 사용자 확인`이며 Notion 문서에 직접 적힌 요구사항으로 표현하지 않는다.
- 수정 전후 장소 ID 또는 장소 표시가 실제로 바뀌어야 한다. 요청 DTO에 장소 ID가 존재하는 것만으로 충족했다고 판정하지 않는다.

관리자 기능의 게시글 관리 대상에는 여행지 정보, 공지사항, Q&A, 이벤트가 명시돼 있지만 타인의 리뷰·댓글 수정·삭제는 명시돼 있지 않다. 따라서 현재 리뷰·댓글 범위에서는 코드와 문서가 공통으로 보여주는 `작성자만 수정·삭제`를 기본 오라클로 사용하고 관리자 우회 권한을 임의로 추가하지 않는다.

### 요구사항 문서에도 명시되지 않은 항목

다음은 사용자에게 즉시 답변이나 일괄 승인을 요구하지 않는다. 테스트가 필요한 시점에 `요구사항 미명시`로 표시하고 UI·코드 기반 가설 또는 사용자 경험과 구분한다.

1. 리뷰 삭제 시 댓글·조회수의 삭제 또는 차단 규칙
2. 평점의 정수·소수 허용 단위
3. 검색어와 장소 필터의 결합 규칙
4. 장소 필터 상태에서 정렬 옵션을 결합하는 규칙
5. 페이지네이션 방식과 페이지 크기
6. 리뷰 이미지 업로드 범위
7. 인기 리뷰의 정확한 가중치 계산식
8. 동일 리뷰 조회수의 중복 방지 기준
9. Bearer 헤더와 쿠키 인증 중 필수 계약

위 세부 정책은 과거 프로젝트에서 논의되지 않았다는 사용자 설명을 기록한다. 임의로 정하지 않으며, 테스트가 필요하면 기대 결과를 `정책 확인 필요`로 두고 현재 동작과 사용자 영향을 관찰한다.

리뷰 이미지 업로드는 2차 기획서·요구사항의 리뷰 범위에 명시되지 않았으므로 5일 초기 제출의 필수 기능에서 제외한다. 이미지 UI와 미구현 가능성은 코드 기반 위험 가설로만 유지한다.

## 13. 이번 문서에서 실행하지 않은 작업

- 테스트·빌드·애플리케이션·서버·Docker·DB 실행: 없음
- clone·fetch·pull: 없음
- 원본 팀 저장소 파일 변경: 없음
- Pass·Fail·Blocked 신규 판정: 없음
- 제품 결함 신규 확정: 없음

## 14. 주요 코드 근거

- `README.md`
- `backend/src/main/java/com/tripfriend/domain/review/controller/ReviewController.java`
- `backend/src/main/java/com/tripfriend/domain/review/controller/CommentController.java`
- `backend/src/main/java/com/tripfriend/domain/review/service/ReviewService.java`
- `backend/src/main/java/com/tripfriend/domain/review/service/CommentService.java`
- `backend/src/main/java/com/tripfriend/domain/review/dto/ReviewRequestDto.java`
- `backend/src/main/java/com/tripfriend/domain/review/dto/CommentRequestDto.java`
- `backend/src/main/java/com/tripfriend/domain/review/repository/ReviewRepository.java`
- `backend/src/main/java/com/tripfriend/domain/review/repository/CommentRepository.java`
- `backend/src/main/java/com/tripfriend/domain/review/entity/Review.java`
- `backend/src/main/java/com/tripfriend/domain/review/entity/Comment.java`
- `backend/src/main/java/com/tripfriend/domain/review/entity/ReviewViewCount.java`
- `backend/src/main/java/com/tripfriend/domain/member/member/service/AuthService.java`
- `backend/src/main/java/com/tripfriend/global/security/SecurityConfig.java`
- `backend/src/main/java/com/tripfriend/global/filter/JwtAuthenticationFilter.java`
- `backend/src/main/java/com/tripfriend/global/exception/GlobalExceptionHandler.java`
- `backend/src/main/java/com/tripfriend/global/dto/RsData.java`
- `frontend/src/app/community/`
- `backend/src/test/`
