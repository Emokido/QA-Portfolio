# TripFriend 리뷰·댓글 P0 테스트케이스

- 문서 상태: `Stage 5 Baseline — 사용자 검토·명시적 완료 승인 완료`
- 작업 단계: `5단계 — TripFriend 테스트케이스와 데이터 설계`
- 기준 프로젝트: 스프린트 1 Java/Spring 완성본
- 기준 커밋: `e2431223ce2a18c8f944d544cba76b951ce0356d`
- 우선순위 범위: `P0`
- 테스트케이스 범위: `TF-TC-001~049`
- 실행 상태: 모두 `Not Run`
- 작성일: 2026-08-28
- 작성 주체: `Codex with user-authorized file creation`

> 이 문서는 4단계의 P0 테스트 조건 `TF-COND-001~011`을 실행 가능한 테스트케이스(Test Case)로 구체화한 설계 산출물이다. 테스트·빌드·애플리케이션을 실행한 결과가 아니며, 기대 결과가 정책 또는 현재 계약 관찰에 한정된 항목은 제품 `Fail` 오라클로 임의 확정하지 않는다.

## 1. 용어와 상태

| 용어 | 영어 | 이 문서에서의 의미 |
|---|---|---|
| 테스트케이스 | Test Case | 사전조건·입력·절차·기대 결과를 가진 개별 검증 단위 |
| 테스트 오라클 | Test Oracle | 실제 결과의 옳고 그름을 판단하는 요구사항·사용자 확인·코드 계약 근거 |
| 추적성 | Traceability | 요구사항·위험·조건·케이스·실행 결과를 식별자로 연결하는 관계 |
| 데이터 변형 | Data Variant | 같은 절차에서 입력만 바꾸어 각각 독립 실행하는 하위 데이터 행 |
| Not Run | Not Run | 테스트 실행을 시도하지 않은 상태 |

`TF-TC-*`는 테스트케이스 ID다. 한 케이스에 A·B 데이터 변형이 있으면 실행 단계에서 각 변형을 별도 실행으로 기록한다. 현재는 설계만 했으므로 49개 케이스와 모든 데이터 변형의 상태가 `Not Run`이다.

## 2. 5W1H

| 항목 | 내용 |
|---|---|
| Why | 리뷰·댓글 P0 위험을 실행 가능한 절차와 기대 결과로 바꾸고 6단계 자동화·실행의 추측을 줄이기 위해 작성한다. |
| What | 공개 조회, 리뷰·댓글 CRUD, 인증·인가, 입력 경계, 검색·필터·정렬, 대표 400·403·404 계약을 설계한다. |
| Who | Codex는 Baseline을 근거로 Draft를 작성하고, 사용자는 기대 결과·정책 미명시 처리·설명 가능성과 5단계 완료 여부를 검토한다. |
| When | 4단계 계획 완료 후, 6단계 Java/Spring 테스트 작성·실행 전에 수행한다. |
| Where | 포트폴리오 문서에서 설계하고 실제 구현·실행은 승인된 `QA-Lab/execution/TripFriend` 작업본에서만 수행한다. |
| How | `TF-REQ → TF-RISK → TF-COND → TF-TC → 데이터 → 기대 결과 → 실행 증거` 순으로 연결한다. |

## 3. 공통 실행 전제

- 실제 실행 위치와 명령은 6단계 별도 승인 후 확정한다.
- 테스트 데이터는 `p0-review-comment-test-data.md`의 논리 ID를 사용한다.
- 각 케이스는 다른 케이스의 실행 순서에 의존하지 않고 독립 fixture 또는 초기화된 데이터로 시작한다.
- 실제 비밀값·운영 계정·개인정보를 사용하지 않는다.
- API 응답은 HTTP 상태와 `RsData.code`, 저장 상태를 함께 확인한다.
- 작성자·비작성자 판정은 화면 버튼이 아니라 백엔드 응답과 데이터 변경 여부를 오라클로 사용한다.
- 정책 미명시 항목은 `정책 확인 필요` 또는 `현재 동작 관찰`로 표시한다.

## 4. P0 테스트케이스

### 4.1 공개 조회와 대표 조회 예외

| ID | 테스트 목적·수준 | 사전조건·데이터 | 절차 | 기대 결과·오라클 | 추적 | 상태 |
|---|---|---|---|---|---|---|
| TF-TC-001 | 비로그인 리뷰 목록 조회 · Controller/API | 비로그인, SORT-BASE | `GET /api/reviews` | HTTP 200, code `200-5`; 전체 리뷰와 계산 가능한 댓글 수·조회수가 반환됨 · API 명세·코드 계약 | TF-REQ-007, TF-COND-001 | Not Run |
| TF-TC-002 | 비로그인 리뷰 상세 조회 · Controller/API | 비로그인, R-SORT-01 존재 | `GET /api/reviews/{id}` | HTTP 200, code `200-1`; 대상 리뷰 ID·제목·평점과 댓글 수가 일치함 · API 명세·코드 계약 | TF-REQ-007, TF-COND-001 | Not Run |
| TF-TC-003 | 비로그인 리뷰별 댓글 조회 · Controller/API | 비로그인, R-SORT-02와 댓글 4개 | `GET /api/comments/review/{reviewId}` | HTTP 200, code `200-2`; 댓글 4개가 생성일 오름차순으로 반환됨 · API 명세·코드 계약 | TF-REQ-008, TF-COND-001 | Not Run |
| TF-TC-047 | 미존재 리뷰 상세 조회 · Controller/API | 비로그인, REVIEW-NOT-FOUND | `GET /api/reviews/{id}` | HTTP 404, code `404-1`; 리뷰 데이터 변경 없음 · 코드 예외 계약 | TF-REQ-007, TF-COND-011 | Not Run |
| TF-TC-048 | 미존재 댓글 상세 조회 · Controller/API | 비로그인, COMMENT-NOT-FOUND | `GET /api/comments/{id}` | HTTP 404, code `404-4`; 데이터 변경 없음 · 코드 예외 계약 | TF-REQ-008, TF-COND-011 | Not Run |

### 4.2 리뷰 생성·수정·삭제

| ID | 테스트 목적·수준 | 사전조건·데이터 | 절차 | 기대 결과·오라클 | 추적 | 상태 |
|---|---|---|---|---|---|---|
| TF-TC-004 | 유효한 리뷰 생성 · Service/Controller | 작성자 A 인증, P-SEOUL, REVIEW-VALID | `POST /api/reviews` | HTTP 201, code `201-1`; 작성자 A·장소·제목·내용·평점 저장, 조회수 0 생성 · 요구사항·코드 계약 | TF-REQ-002, TF-REQ-009, TF-COND-002, TF-RISK-008 | Not Run |
| TF-TC-005 | 장소 ID null 리뷰 생성 · DTO/Controller | 작성자 A 인증, `placeId=null` | 생성 요청 | HTTP 400, code `400-1`; 리뷰·조회수 미생성 · 코드 입력 계약 | TF-REQ-002, TF-REQ-012, TF-COND-002, TF-COND-007, TF-COND-011 | Not Run |
| TF-TC-006 | 미존재 장소 리뷰 생성 · Service/Controller | 작성자 A 인증, PLACE-NOT-FOUND | 생성 요청 | HTTP 404, code `404-2`; 리뷰·조회수 미생성 · 코드 예외 계약 | TF-REQ-002, TF-COND-002, TF-COND-011 | Not Run |
| TF-TC-007 | 작성자 리뷰 기본 필드 수정 · Service/Controller | 작성자 A 인증, R-UPDATE-A | 제목·내용·평점 변경 후 `PUT` | HTTP 200, code `200-3`; 세 필드가 저장되고 작성자·ID 유지 · 요구사항·코드 계약 | TF-REQ-003, TF-COND-003 | Not Run |
| TF-TC-008 | 작성자 리뷰 여행지 변경 · Service/Repository/API | 작성자 A 인증, R-PLACE-CHANGE의 P-BUSAN → P-JEJU | 변경 장소 ID로 `PUT`, 재조회 | HTTP 200 후 실제 저장 장소가 P-JEJU로 변경됨 · 2026-08-26 사용자 확인 오라클 | TF-REQ-004, TF-COND-003, TF-RISK-001 | Not Run |
| TF-TC-009 | 비작성자 리뷰 수정 차단 · Service/Controller | 비작성자 B 인증, 작성자 A의 R-UPDATE-A | `PUT` | HTTP 403, code `403-1`; 모든 리뷰 필드 불변 · 요구사항·코드 권한 계약 | TF-REQ-003, TF-REQ-009, TF-COND-003, TF-COND-006 | Not Run |
| TF-TC-010 | 미존재 리뷰 수정 · Service/Controller | 작성자 A 인증, REVIEW-NOT-FOUND | `PUT` | HTTP 404, code `404-1`; 신규 리뷰 생성 없음 · 코드 예외 계약 | TF-REQ-003, TF-COND-003, TF-COND-011 | Not Run |
| TF-TC-011 | 댓글 없는 본인 리뷰 삭제 · Service/Repository | 작성자 A 인증, R-DELETE-EMPTY | `DELETE`, 리뷰·조회수 재조회 | HTTP 200, code `200-4`; 리뷰와 연결 조회수 제거 · 요구사항·코드 계약 | TF-REQ-003, TF-COND-004 | Not Run |
| TF-TC-012 | 비작성자 리뷰 삭제 차단 · Service/Controller | 비작성자 B 인증, 작성자 A의 R-DELETE-EMPTY | `DELETE` | HTTP 403, code `403-1`; 리뷰·조회수 유지 · 요구사항·코드 권한 계약 | TF-REQ-003, TF-REQ-009, TF-COND-004, TF-COND-006 | Not Run |
| TF-TC-013 | 미존재 리뷰 삭제 · Service/Controller | 작성자 A 인증, REVIEW-NOT-FOUND | `DELETE` | HTTP 404, code `404-1`; 다른 리뷰에 영향 없음 · 코드 예외 계약 | TF-REQ-003, TF-COND-004, TF-COND-011 | Not Run |

### 4.3 댓글 생성·수정·삭제

| ID | 테스트 목적·수준 | 사전조건·데이터 | 절차 | 기대 결과·오라클 | 추적 | 상태 |
|---|---|---|---|---|---|---|
| TF-TC-014 | 유효한 댓글 생성 · Service/Controller | 작성자 A 인증, R-COMMENT-TARGET, COMMENT-VALID | `POST /api/comments` | HTTP 201, code `201-1`; 대상 리뷰와 작성자 A에 연결된 댓글 저장 · API 명세·코드 계약 | TF-REQ-008, TF-REQ-009, TF-COND-005 | Not Run |
| TF-TC-015 | 작성자 댓글 수정 · Service/Controller | 작성자 A 인증, C-AUTHOR-A | 내용 변경 후 `PUT` | HTTP 200, code `200-4`; 내용 변경, 작성자·리뷰 연결 유지 · API 명세·코드 계약 | TF-REQ-008, TF-COND-005 | Not Run |
| TF-TC-016 | 작성자 댓글 삭제 · Service/Controller | 작성자 A 인증, C-AUTHOR-A | `DELETE` | HTTP 200, code `200-5`; 대상 댓글 제거, 리뷰 유지 · API 명세·코드 계약 | TF-REQ-008, TF-COND-005 | Not Run |
| TF-TC-017 | 비작성자 댓글 수정 차단 · Service/Controller | 비작성자 B 인증, 작성자 A의 C-AUTHOR-A | `PUT` | HTTP 403, code `403-2`; 댓글 내용 불변 · 코드 권한 계약 | TF-REQ-008, TF-REQ-009, TF-COND-005, TF-COND-006 | Not Run |
| TF-TC-018 | 비작성자 댓글 삭제 차단 · Service/Controller | 비작성자 B 인증, 작성자 A의 C-AUTHOR-A | `DELETE` | HTTP 403, code `403-2`; 댓글 유지 · 코드 권한 계약 | TF-REQ-008, TF-REQ-009, TF-COND-005, TF-COND-006 | Not Run |
| TF-TC-019 | 미존재 리뷰에 댓글 생성 · Service/Controller | 작성자 A 인증, REVIEW-NOT-FOUND | `POST /api/comments` | HTTP 404, code `404-3`; 댓글 미생성 · 코드 예외 계약 | TF-REQ-008, TF-COND-005, TF-COND-008, TF-COND-011, TF-RISK-009 | Not Run |

### 4.4 인증·인가 전달 경계

| ID | 테스트 목적·수준 | 사전조건·데이터 | 절차 | 기대 결과·오라클 | 추적 | 상태 |
|---|---|---|---|---|---|---|
| TF-TC-020 | 비로그인 리뷰 생성 차단 · Security/Controller | 토큰·쿠키 없음 | 유효 데이터로 리뷰 생성 | 요청 거부, 리뷰·조회수 미생성; 정확한 401/403 형식은 현재 계약 관찰 · JWT 인증 요구사항 | TF-REQ-009, TF-COND-006, TF-RISK-008 | Not Run |
| TF-TC-021 | 비로그인 리뷰 수정 차단 · Security/Controller | 토큰·쿠키 없음, R-UPDATE-A | `PUT` | 요청 거부, 리뷰 불변; 정확한 401/403 형식은 현재 계약 관찰 | TF-REQ-003, TF-REQ-009, TF-COND-006, TF-RISK-008 | Not Run |
| TF-TC-022 | 비로그인 리뷰 삭제 차단 · Security/Controller | 토큰·쿠키 없음, R-DELETE-EMPTY | `DELETE` | 요청 거부, 리뷰·조회수 유지; 정확한 401/403 형식은 현재 계약 관찰 | TF-REQ-003, TF-REQ-009, TF-COND-006, TF-RISK-008 | Not Run |
| TF-TC-023 | 비로그인 댓글 생성 차단 · Security/Controller | 토큰·쿠키 없음, R-COMMENT-TARGET | 댓글 생성 | 요청 거부, 댓글 미생성; 정확한 401/403 형식은 현재 계약 관찰 | TF-REQ-008, TF-REQ-009, TF-COND-006, TF-RISK-008 | Not Run |
| TF-TC-024 | 비로그인 댓글 수정 차단 · Security/Controller | 토큰·쿠키 없음, C-AUTHOR-A | `PUT` | 요청 거부, 댓글 불변; 정확한 401/403 형식은 현재 계약 관찰 | TF-REQ-008, TF-REQ-009, TF-COND-006, TF-RISK-008 | Not Run |
| TF-TC-025 | 비로그인 댓글 삭제 차단 · Security/Controller | 토큰·쿠키 없음, C-AUTHOR-A | `DELETE` | 요청 거부, 댓글 유지; 정확한 401/403 형식은 현재 계약 관찰 | TF-REQ-008, TF-REQ-009, TF-COND-006, TF-RISK-008 | Not Run |
| TF-TC-026 | Bearer 헤더 인증 대표 성공 · Security/Controller | 작성자 A의 안전한 테스트 토큰, 쿠키 없음 | 본인 댓글 수정 | 인증·작성자 판정 후 HTTP 200, code `200-4`; 댓글 변경 · JWT 요구사항·코드 계약 | TF-REQ-008, TF-REQ-009, TF-COND-006, TF-RISK-008 | Not Run |
| TF-TC-027 | 쿠키 전용 인증 전달 · Security 통합/API | 헤더 없음, 테스트용 `accessToken` 쿠키만 존재 | 본인 댓글 수정 | 인증·Controller 전달 결과와 데이터 변경 여부 기록; 전달 채널 정책 미명시이므로 `현재 동작 관찰`, 5xx·정상 사용자 차단 영향은 위험 근거로 기록 | TF-REQ-009, TF-COND-006, TF-RISK-008 | Not Run |
| TF-TC-028 | 유효하지 않은 JWT 차단 · Security/Controller | 만료·변조된 안전한 테스트 토큰 | 변경 API 대표 요청 | 요청 거부, 데이터 불변; 정확한 오류 형식은 현재 계약 관찰 · 인증 요구사항 | TF-REQ-009, TF-COND-006, TF-RISK-008 | Not Run |

### 4.5 리뷰 입력 경계

| ID | 테스트 목적·수준 | 입력 데이터 | 절차 | 기대 결과·오라클 | 추적 | 상태 |
|---|---|---|---|---|---|---|
| TF-TC-029 | 제목 1자 최소 미만 · DTO/Controller | TITLE-LEN-01 | 리뷰 생성 | HTTP 400, code `400-1`; 미저장 · 코드 입력 계약 | TF-REQ-012, TF-COND-007, TF-COND-011 | Not Run |
| TF-TC-030 | 제목 2자 최소 경계 · DTO/Controller | TITLE-LEN-02 | 리뷰 생성 | HTTP 201, code `201-1`; 제목 길이 2로 저장 · 코드 입력 계약 | TF-REQ-012, TF-COND-007 | Not Run |
| TF-TC-031 | 제목 30자 최대 경계 · DTO/Controller | TITLE-LEN-30 | 리뷰 생성 | HTTP 201, code `201-1`; 제목 길이 30으로 저장 · 코드 입력 계약 | TF-REQ-012, TF-COND-007 | Not Run |
| TF-TC-032 | 제목 31자 최대 초과 · DTO/Controller | TITLE-LEN-31 | 리뷰 생성 | HTTP 400, code `400-1`; 미저장 · 코드 입력 계약 | TF-REQ-012, TF-COND-007, TF-COND-011 | Not Run |
| TF-TC-033 | 내용 9자 최소 미만 · DTO/Controller | CONTENT-LEN-09 | 리뷰 생성 | HTTP 400, code `400-1`; 미저장 · 코드 입력 계약 | TF-REQ-012, TF-COND-007, TF-COND-011 | Not Run |
| TF-TC-034 | 내용 10자 최소 경계 · DTO/Controller | CONTENT-LEN-10 | 리뷰 생성 | HTTP 201, code `201-1`; 내용 길이 10으로 저장 · 코드 입력 계약 | TF-REQ-012, TF-COND-007 | Not Run |
| TF-TC-035 | 내용 2000자 최대 경계 · DTO/Controller | CONTENT-LEN-2000 | 리뷰 생성 | HTTP 201, code `201-1`; 내용 길이 2000으로 저장 · 코드 입력 계약 | TF-REQ-012, TF-COND-007 | Not Run |
| TF-TC-036 | 내용 2001자 최대 초과 · DTO/Controller | CONTENT-LEN-2001 | 리뷰 생성 | HTTP 400, code `400-1`; 미저장 · 코드 입력 계약 | TF-REQ-012, TF-COND-007, TF-COND-011 | Not Run |
| TF-TC-037 | 평점 1·5 유효 경계 · DTO/Controller | A: RATING-1, B: RATING-5 | 각 데이터 변형으로 리뷰 생성 | 각 변형 HTTP 201, code `201-1`; 입력 평점 저장 · 코드 입력 계약 | TF-REQ-006, TF-REQ-012, TF-COND-007 | Not Run |
| TF-TC-038 | 평점 0·6 범위 밖 · DTO/Controller | A: RATING-0, B: RATING-6 | 각 데이터 변형으로 리뷰 생성 | 각 변형 HTTP 400, code `400-2`; 미저장 · 코드 입력 계약 | TF-REQ-006, TF-REQ-012, TF-COND-007, TF-COND-011 | Not Run |

### 4.6 댓글 입력 경계

| ID | 테스트 목적·수준 | 입력 데이터 | 절차 | 기대 결과·오라클 | 추적 | 상태 |
|---|---|---|---|---|---|---|
| TF-TC-039 | 댓글 1자 최소 미만 · DTO/Controller | COMMENT-LEN-01 | 댓글 생성 | HTTP 400, code `400-1`; 미저장 · 코드 입력 계약 | TF-REQ-012, TF-COND-008, TF-COND-011 | Not Run |
| TF-TC-040 | 댓글 2자 최소 경계 · DTO/Controller | COMMENT-LEN-02 | 댓글 생성 | HTTP 201, code `201-1`; 길이 2로 저장 · 코드 입력 계약 | TF-REQ-008, TF-REQ-012, TF-COND-008 | Not Run |
| TF-TC-041 | 댓글 100자 최대 경계 · DTO/Controller | COMMENT-LEN-100 | 댓글 생성 | HTTP 201, code `201-1`; 길이 100으로 저장 · 코드 입력 계약 | TF-REQ-008, TF-REQ-012, TF-COND-008 | Not Run |
| TF-TC-042 | 댓글 101자 최대 초과 · DTO/Controller | COMMENT-LEN-101 | 댓글 생성 | HTTP 400, code `400-1`; 미저장 · 코드 입력 계약 | TF-REQ-012, TF-COND-008, TF-COND-011 | Not Run |
| TF-TC-043 | 댓글 reviewId 누락·null · DTO/Controller | A: 필드 누락, B: `reviewId=null` | 각 데이터 변형으로 댓글 생성 | 응답 상태·형식과 미저장 여부 기록; 정확한 400 계약은 미명시이므로 `현재 동작 관찰`, 5xx 발생 시 TF-RISK-009 영향 근거로 기록 | TF-REQ-008, TF-COND-008, TF-COND-011, TF-RISK-009 | Not Run |

### 4.7 검색·장소 필터·단일 정렬

| ID | 테스트 목적·수준 | 사전조건·데이터 | 절차 | 기대 결과·오라클 | 추적 | 상태 |
|---|---|---|---|---|---|---|
| TF-TC-044 | 제목 키워드 단독 검색 · Repository/Controller | SORT-BASE, 키워드 `부산` | `GET /api/reviews?keyword=부산` | HTTP 200, code `200-5`; R-SORT-01·03만 반환, 최신순 03→01 · 명시 요구사항·코드 계약 | TF-REQ-001, TF-COND-009, TF-RISK-004 | Not Run |
| TF-TC-045 | 장소 단독 필터 · Repository/Controller | SORT-BASE, P-BUSAN | `GET /api/reviews?placeId={P-BUSAN}` | HTTP 200, code `200-5`; R-SORT-01·03만 반환, 최신순 03→01 · 명시 요구사항·코드 계약 | TF-REQ-007, TF-COND-009 | Not Run |
| TF-TC-046 | 단일 정렬 4종 · Repository/Controller | SORT-BASE | A: newest, B: highest_rating, C: comments, D: most_viewed로 각각 조회 | 각 변형 HTTP 200, code `200-5`; 데이터 문서의 예상 ID 순서와 일치 · 명시 요구사항·코드 계약 | TF-REQ-005, TF-COND-010, TF-RISK-005 | Not Run |
| TF-TC-049 | 검색 결과 없음 · Repository/Controller | SORT-BASE, 키워드 `존재하지않는제목` | 키워드 단독 조회 | HTTP 200, code `200-5`; 빈 목록이며 404·5xx가 아님 · 조회 계약 | TF-REQ-001, TF-REQ-007, TF-COND-009 | Not Run |

## 5. 인증·인가 결정 테이블

| 사용자 | 대상 | 생성 | 본인 수정·삭제 | 타인 수정·삭제 | 핵심 케이스 |
|---|---|---|---|---|---|
| 비로그인 | 리뷰 | 차단 | 해당 없음 | 차단 | TF-TC-020~022 |
| 비로그인 | 댓글 | 차단 | 해당 없음 | 차단 | TF-TC-023~025 |
| 인증 작성자 A | 리뷰 | 허용 | 허용 | 해당 없음 | TF-TC-004, 007~008, 011, 026 |
| 인증 작성자 A | 댓글 | 허용 | 허용 | 해당 없음 | TF-TC-014~016, 026 |
| 인증 비작성자 B | 리뷰 | 허용 | 해당 없음 | 차단·`403-1` | TF-TC-009, 012 |
| 인증 비작성자 B | 댓글 | 허용 | 해당 없음 | 차단·`403-2` | TF-TC-017~018 |

관리자의 타인 리뷰·댓글 수정·삭제 권한은 요구사항에 명시되지 않았다. P0 기대 결과에 관리자 우회 권한을 추가하지 않는다.

## 6. 테스트 조건 추적 요약

| 테스트 조건 | 연결 테스트케이스 | 설계 상태 |
|---|---|---|
| TF-COND-001 공개 조회 | TF-TC-001~003 | Baseline / Not Run |
| TF-COND-002 리뷰 생성 | TF-TC-004~006 | Baseline / Not Run |
| TF-COND-003 리뷰 수정·여행지 변경 | TF-TC-007~010 | Baseline / Not Run |
| TF-COND-004 리뷰 삭제 | TF-TC-011~013 | Baseline / Not Run |
| TF-COND-005 댓글 CRUD | TF-TC-014~019 | Baseline / Not Run |
| TF-COND-006 인증·인가 | TF-TC-009, 012, 017~018, 020~028 | Baseline / Not Run |
| TF-COND-007 리뷰 입력 경계 | TF-TC-005, 029~038 | Baseline / Not Run |
| TF-COND-008 댓글 입력 경계 | TF-TC-019, 039~043 | Baseline / Not Run |
| TF-COND-009 검색·장소 필터 | TF-TC-044~045, 049 | Baseline / Not Run |
| TF-COND-010 단일 정렬 | TF-TC-046 | Baseline / Not Run |
| TF-COND-011 대표 400·403·404 | TF-TC-005~006, 009~010, 012~013, 017~019, 029, 032~033, 036, 038~039, 042~043, 047~048 | Baseline / Not Run |

모든 P0 테스트 조건은 하나 이상의 테스트케이스에 연결됐다. 연결 완료는 설계 추적성의 충족이며 테스트 실행 결과의 `Pass`가 아니다.

## 7. 정책 확인·현재 동작 관찰 항목

- 비로그인 변경 요청의 정확한 401·403 응답 형식: 인증 차단과 데이터 불변은 오라클, 세부 형식은 현재 계약 관찰.
- 쿠키 전용 인증: 전달 채널 정책 미명시. 성공·실패와 내부 오류를 관찰하며 코드와 다르다는 이유만으로 `Fail` 처리하지 않는다.
- 댓글 `reviewId` 누락·null: 정확한 400 계약 미명시. 응답과 데이터 영향을 관찰하고 5xx이면 TF-RISK-009의 사용자 영향을 평가한다.
- 리뷰 삭제 시 댓글 처리: P0는 댓글 없는 리뷰 삭제만 확정한다. 댓글이 있는 리뷰 삭제는 P1 현재 동작 관찰이다.
- 평점 소수 단위, 복합 필터·정렬, 페이지네이션, 이미지, 성능·동시성은 P0 케이스에서 제외한다.

## 8. 5단계 완료 검토 기준

- P0 TF-COND-001~011이 테스트케이스에 누락 없이 연결된다.
- 각 케이스에 목적·수준·사전조건·데이터·절차·기대 결과·오라클·추적 ID·상태가 있다.
- 권한 결정 테이블, 경계값, 상태 변경과 대표 예외가 포함된다.
- 실행용 데이터와 예상 정렬 순서가 별도 데이터 문서에 정의된다.
- 정책 미명시 항목이 임의 `Fail` 오라클로 바뀌지 않는다.
- 사용자가 케이스의 설명 가능성·범위·중복·누락을 검토하고 5단계 완료를 명시적으로 승인한다.

2026-08-28 사용자 검토와 명시적 5단계 완료 승인을 반영해 이 문서를 `Stage 5 Baseline`으로 확정했다. 이는 테스트 설계 완료를 뜻하며, 49개 테스트케이스의 실행 상태는 모두 `Not Run`이다.
