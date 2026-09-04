# TripFriend 리뷰·댓글 P0 테스트 데이터 설계

- 문서 상태: `Stage 5 Baseline — 사용자 검토·명시적 완료 승인 완료`
- 작업 단계: `5단계 — TripFriend 테스트케이스와 데이터 설계`
- 기준 프로젝트: 스프린트 1 Java/Spring 완성본
- 기준 커밋: `e2431223ce2a18c8f944d544cba76b951ce0356d`
- 연결 테스트케이스: `TF-TC-001~049`
- 데이터 실행 상태: `Not Prepared / Not Run`
- 작성일: 2026-08-28
- 작성 주체: `Codex with user-authorized file creation`

> 이 문서는 테스트 실행에 사용할 논리 데이터와 예상 관계를 정의한 설계 산출물이다. 실제 DB·계정·토큰을 만들거나 애플리케이션을 실행하지 않았다. 모든 ID와 토큰은 설명용 별칭이며 실제 비밀값·운영 데이터가 아니다.

## 1. 데이터 설계 원칙

- 각 테스트는 독립 fixture 또는 초기화된 상태를 사용한다.
- 논리 ID는 테스트 코드에서 생성되는 실제 ID와 매핑하되 숫자 고정을 요구하지 않는다.
- 정렬 검증 데이터는 평점·댓글 수·조회수·생성 시각이 서로 달라 순서를 계산할 수 있게 한다.
- 경계 문자열은 생성 규칙과 기대 길이를 함께 기록한다.
- 실패 케이스는 실행 전후 저장 건수를 비교해 부분 저장이 없는지 확인한다.
- 실제 이름·이메일·토큰·쿠키·비밀번호를 사용하지 않는다.
- 테스트 종료 후 데이터 정리가 실패해 다음 케이스에 영향을 주면 실행을 중단하고 `Blocked` 여부를 판단한다.

## 2. 회원·인증 데이터

| 논리 ID | 역할 | 안전한 속성 | 사용 목적 |
|---|---|---|---|
| MEMBER-A | 작성자 A | 테스트 전용 회원, 실제 개인정보 없음 | 본인 리뷰·댓글 생성·수정·삭제 |
| MEMBER-B | 비작성자 B | MEMBER-A와 다른 테스트 전용 회원 | 타인 자원 수정·삭제 차단 |
| ANONYMOUS | 비로그인 | 토큰·쿠키 없음 | 공개 조회와 변경 API 인증 경계 |
| TOKEN-A | 작성자 A 테스트 토큰 | 실행 시 테스트 도구가 생성한 비운영 JWT | Bearer 헤더 인증 대표 성공 |
| TOKEN-B | 비작성자 B 테스트 토큰 | 실행 시 테스트 도구가 생성한 비운영 JWT | 타인 자원 권한 차단 |
| COOKIE-A | 작성자 A 테스트 쿠키 | 이름 `accessToken`, 값은 테스트용 토큰 | 쿠키 전용 인증 현재 동작 관찰 |
| TOKEN-INVALID | 만료·변조 토큰 | 실제 비밀이 아닌 테스트 문자열 | 인증 실패와 데이터 불변 확인 |

관리자 계정은 P0 데이터에 포함하지 않는다. 타인 리뷰·댓글에 대한 관리자 우회 권한이 요구사항에 없기 때문이다.

## 3. 장소 데이터

| 논리 ID | 이름 | 존재 여부 | 사용 목적 |
|---|---|---|---|
| P-BUSAN | 부산 테스트 장소 | 존재 | 리뷰 생성, 장소 필터, 여행지 변경 전 |
| P-SEOUL | 서울 테스트 장소 | 존재 | 리뷰 생성과 정렬 기준 |
| P-JEJU | 제주 테스트 장소 | 존재 | 여행지 변경 후, 정렬 기준 |
| PLACE-NOT-FOUND | 미존재 장소 | 없음 | 리뷰 생성 `404-2` |

실제 장소 ID는 fixture 생성 결과를 사용한다. `PLACE-NOT-FOUND`는 생성된 최대 ID와 충돌하지 않는 값으로 실행 시 결정한다.

## 4. 기본 CRUD 데이터

| 논리 ID | 작성자 | 장소 | 댓글 | 초기 상태·목적 |
|---|---|---|---|---|
| R-UPDATE-A | MEMBER-A | P-SEOUL | 없음 | 제목·내용·평점 기본 수정과 비작성자 수정 차단 |
| R-PLACE-CHANGE | MEMBER-A | P-BUSAN | 없음 | 수정 요청 후 장소가 P-JEJU로 실제 변경되는지 확인 |
| R-DELETE-EMPTY | MEMBER-A | P-SEOUL | 없음 | 작성자 삭제·비작성자 삭제 차단 |
| R-COMMENT-TARGET | MEMBER-A | P-BUSAN | C-AUTHOR-A 존재 | 댓글 생성·수정·삭제 대상 리뷰 |
| C-AUTHOR-A | MEMBER-A | R-COMMENT-TARGET | 해당 없음 | MEMBER-A의 본인 댓글 |
| REVIEW-NOT-FOUND | 없음 | 없음 | 없음 | 미존재 리뷰 예외 |
| COMMENT-NOT-FOUND | 없음 | 없음 | 해당 없음 | 미존재 댓글 예외 |

### 정상 리뷰 요청 REVIEW-VALID

| 필드 | 값 | 설계 이유 |
|---|---|---|
| title | `서울 여행 후기` | 2~30자 정상 동등 분할 |
| content | `서울에서 즐거운 여행을 보냈습니다.` | 10~2000자 정상 동등 분할 |
| rating | `4` | 1~5 정상 동등 분할 |
| placeId | P-SEOUL의 실제 ID | 존재 장소 연결 |

### 정상 댓글 요청 COMMENT-VALID

| 필드 | 값 | 설계 이유 |
|---|---|---|
| content | `좋은 후기 감사합니다.` | 2~100자 정상 동등 분할 |
| reviewId | R-COMMENT-TARGET의 실제 ID | 존재 리뷰 연결 |

## 5. 입력 경계 데이터

### 문자열 생성 표기

`repeat("가", N)`은 한글 문자 `가`를 N번 반복한 문자열이다. 실행 전 실제 문자열 길이를 assertion으로 확인하고 요청한다.

| 데이터 ID | 생성 값 | 기대 길이 | 기대 분류 |
|---|---|---:|---|
| TITLE-LEN-01 | `repeat("가", 1)` | 1 | 최소 미만·거부 |
| TITLE-LEN-02 | `repeat("가", 2)` | 2 | 최소 경계·허용 |
| TITLE-LEN-30 | `repeat("가", 30)` | 30 | 최대 경계·허용 |
| TITLE-LEN-31 | `repeat("가", 31)` | 31 | 최대 초과·거부 |
| CONTENT-LEN-09 | `repeat("나", 9)` | 9 | 최소 미만·거부 |
| CONTENT-LEN-10 | `repeat("나", 10)` | 10 | 최소 경계·허용 |
| CONTENT-LEN-2000 | `repeat("나", 2000)` | 2000 | 최대 경계·허용 |
| CONTENT-LEN-2001 | `repeat("나", 2001)` | 2001 | 최대 초과·거부 |
| COMMENT-LEN-01 | `repeat("다", 1)` | 1 | 최소 미만·거부 |
| COMMENT-LEN-02 | `repeat("다", 2)` | 2 | 최소 경계·허용 |
| COMMENT-LEN-100 | `repeat("다", 100)` | 100 | 최대 경계·허용 |
| COMMENT-LEN-101 | `repeat("다", 101)` | 101 | 최대 초과·거부 |

### 숫자·필수 관계 경계

| 데이터 ID | 값 | 기대 처리 |
|---|---:|---|
| RATING-0 | 0 | HTTP 400, code `400-2` |
| RATING-1 | 1 | 허용·저장 |
| RATING-5 | 5 | 허용·저장 |
| RATING-6 | 6 | HTTP 400, code `400-2` |
| PLACE-NULL | null | HTTP 400, code `400-1` |
| REVIEW-ID-MISSING | 필드 자체 누락 | 현재 동작 관찰, 댓글 미저장 확인 |
| REVIEW-ID-NULL | null | 현재 동작 관찰, 댓글 미저장 확인 |

평점 `1.5` 같은 소수 입력은 제품 정책이 미명시되어 P0 경계 데이터에서 제외한다.

## 6. 검색·필터·정렬 기준 데이터 SORT-BASE

모든 정렬 값이 서로 달라 별도 동점 규칙 없이 결과를 계산할 수 있도록 한다.

| 논리 ID | 제목 | 장소 | 평점 | 댓글 수 | 조회수 | 생성 시각 |
|---|---|---|---:|---:|---:|---|
| R-SORT-01 | 부산 바다 여행 | P-BUSAN | 4 | 1 | 30 | 2026-01-01 10:00:00 |
| R-SORT-02 | 서울 야경 추천 | P-SEOUL | 1 | 4 | 20 | 2026-01-02 10:00:00 |
| R-SORT-03 | 부산 맛집 후기 | P-BUSAN | 5 | 2 | 10 | 2026-01-03 10:00:00 |
| R-SORT-04 | 제주 산책 기록 | P-JEJU | 2 | 3 | 40 | 2026-01-04 10:00:00 |

### 연결 댓글

| 리뷰 | 댓글 논리 ID | 작성자 | 생성 순서 |
|---|---|---|---:|
| R-SORT-01 | C-SORT-01 | MEMBER-A | 1 |
| R-SORT-02 | C-SORT-02 | MEMBER-A | 1 |
| R-SORT-02 | C-SORT-03 | MEMBER-B | 2 |
| R-SORT-02 | C-SORT-04 | MEMBER-A | 3 |
| R-SORT-02 | C-SORT-05 | MEMBER-B | 4 |
| R-SORT-03 | C-SORT-06 | MEMBER-A | 1 |
| R-SORT-03 | C-SORT-07 | MEMBER-B | 2 |
| R-SORT-04 | C-SORT-08 | MEMBER-A | 1 |
| R-SORT-04 | C-SORT-09 | MEMBER-B | 2 |
| R-SORT-04 | C-SORT-10 | MEMBER-A | 3 |

### 예상 ID 순서

| 조회 조건 | 예상 결과 |
|---|---|
| `sort=newest` | R-SORT-04 → R-SORT-03 → R-SORT-02 → R-SORT-01 |
| `sort=highest_rating` | R-SORT-03 → R-SORT-01 → R-SORT-04 → R-SORT-02 |
| `sort=comments` | R-SORT-02 → R-SORT-04 → R-SORT-03 → R-SORT-01 |
| `sort=most_viewed` | R-SORT-04 → R-SORT-01 → R-SORT-02 → R-SORT-03 |
| `keyword=부산` | R-SORT-03 → R-SORT-01 |
| `placeId=P-BUSAN` | R-SORT-03 → R-SORT-01 |
| `keyword=존재하지않는제목` | 빈 목록 |

조회수 fixture를 직접 설정할 수 없는 테스트 수준에서는 Repository fixture 또는 조회수 저장소를 사용한다. UI 클릭 반복으로 수치를 만들지 않는다.

## 7. 실행 전후 확인 항목

| 작업 | 실행 전 | 실행 후 |
|---|---|---|
| 리뷰 생성 | 리뷰·조회수 건수 | 리뷰 1건과 조회수 0 레코드 증가 또는 실패 시 모두 불변 |
| 리뷰 수정 | 제목·내용·평점·장소 ID | 승인된 필드만 변경, ID·작성자 유지 |
| 리뷰 삭제 | 리뷰·조회수 존재 | 성공 시 둘 다 제거, 차단·실패 시 둘 다 유지 |
| 댓글 생성 | 대상 리뷰 존재, 댓글 건수 | 성공 시 해당 리뷰 댓글 1건 증가, 실패 시 불변 |
| 댓글 수정 | 내용·작성자·리뷰 ID | 내용만 변경, 작성자·리뷰 연결 유지 |
| 댓글 삭제 | 댓글·대상 리뷰 존재 | 댓글만 제거, 리뷰 유지 |
| 권한 차단 | 대상 자원 전체 스냅샷 | HTTP 오류뿐 아니라 저장 상태도 완전 불변 |

## 8. 격리와 정리 전략

- Service 단위 테스트는 Repository·Auth·Place 의존성을 목적에 맞게 Mock하고 저장 호출·인자·예외를 검증한다.
- Controller 테스트는 요청 DTO·인증 컨텍스트·HTTP와 `RsData.code`를 검증한다.
- Repository 테스트는 H2와 독립 transaction rollback을 우선 사용한다.
- 동일 테스트가 생성한 데이터만 정리하고 다른 케이스 fixture를 재사용해 수정하지 않는다.
- `@BeforeEach`, factory 또는 fixture builder의 선택은 6단계 구현 시 기존 테스트 구조를 확인한 뒤 정한다.
- MySQL·Redis·OAuth·메일·운영 인증서와 실제 `application-secret.yml`은 P0 데이터 준비에 사용하지 않는다.

## 9. 데이터–케이스 연결

| 데이터 묶음 | 주요 테스트케이스 |
|---|---|
| 회원·인증 | TF-TC-004, 007~028 |
| 장소 | TF-TC-004~008, 044~046 |
| 기본 CRUD | TF-TC-001~019, 047~048 |
| 제목·내용·평점 경계 | TF-TC-029~038 |
| 댓글 경계·reviewId | TF-TC-039~043 |
| SORT-BASE | TF-TC-001~003, 044~046, 049 |

## 10. 준비·실행 상태

- 실제 회원·장소·리뷰·댓글 fixture 생성: `Not Prepared`
- 테스트 프로필·H2 데이터 적용: `Not Prepared`
- 테스트 코드 작성: `Not Started`
- 테스트 실행: `Not Run`
- 실제 데이터 정리 검증: `Not Run`

2026-08-28 사용자 검토와 명시적 5단계 완료 승인을 반영해 이 데이터 설계를 `Stage 5 Baseline`으로 확정했다. 실제 데이터는 계속 `Not Prepared / Not Run`이며, 6단계 별도 승인 전에는 fixture·DB·토큰을 생성하거나 테스트를 실행하지 않는다.
