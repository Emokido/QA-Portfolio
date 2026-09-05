# TripFriend 리뷰·댓글 요구사항–코드 추적표

> 이 문서는 작성 당시의 설계·감사·계획 기준선입니다. 아래 상태와 미실행 표현은 해당 기록 시점에 한정됩니다. [최신 결과 요약](../reports/tripfriend-stage-8-results-summary.md)과 [2026-09-05 교정 결과](../reports/portfolio-review-correction-report.md)를 함께 확인하십시오.

- 문서 상태: `Stage 2 Baseline`
- 기준 프로젝트: 스프린트 1 Java/Spring
- 기준 커밋: `e2431223ce2a18c8f944d544cba76b951ce0356d`
- 테스트 실행 상태: `Not Run`
- 작성 기준일: 2026-08-26
- 식별자 용어 정밀화: 2026-08-27 — 항목 의미·우선순위·테스트 상태 변경 없음

## 용어 및 식별자 안내

| 표기 | 한글 의미 | 영어 원문 | 이 문서에서의 사용 |
|---|---|---|---|
| RTM | 요구사항 추적 매트릭스 | Requirements Traceability Matrix | 요구사항·관찰 항목·코드·위험·후속 테스트를 연결한 이 문서의 표 전체를 뜻한다. 개별 항목 ID로 사용하지 않는다. |
| `TF-REQ-*` | TripFriend 요구사항 ID | TripFriend Requirement ID | 문서 요구사항, 사용자 확인 오라클 또는 검증 가능한 코드 계약을 식별한다. |
| `TF-OBS-*` | TripFriend 관찰 항목 ID | TripFriend Observation ID | 제품 요구사항으로 확정되지 않은 코드·UI 동작을 탐색적으로 관찰할 항목이다. |
| `TF-NFR-*` | TripFriend 비기능 요구사항 ID | TripFriend Non-Functional Requirement ID | 성능·보안처럼 기능 동작 외의 품질 목표를 식별한다. |
| `TF-RISK-*` | TripFriend 위험 가설 ID | TripFriend Risk ID | 실행 전 정적 분석으로 발견한 위험 가능성을 식별하며 재현 전에는 결함이 아니다. |

`REQ`, `OBS`, `NFR`, `RISK`는 실무에서 흔히 볼 수 있는 영어 약어지만 조직마다 명명 규칙은 다를 수 있다. `TF-` 접두사와 이 문서의 조합은 TripFriend 포트폴리오용 식별자 규칙이다.

2026-08-27에 기존 `TF-RTM-001~015` 표기를 의미에 맞게 정밀화했다. `TF-RTM-001~012`는 `TF-REQ-001~012`, `TF-RTM-013`은 `TF-OBS-001`, `TF-RTM-014`는 `TF-OBS-002`, `TF-RTM-015`는 `TF-NFR-001`로 변경했다. 과거 인수인계의 옛 표기는 당시 기록으로 보존한다.

## 1. 5W1H

| 항목 | 내용 |
|---|---|
| Why | 요구사항, 사용자 확인, 코드 구현, 위험 가설과 후속 테스트케이스를 한 줄로 추적하기 위해 작성한다. |
| What | TripFriend의 리뷰·댓글 핵심 기능과 인증·조회수·CORS·성능 목표를 추적한다. |
| Who | 과거 개발 기여는 사용자와 팀의 역할을 구분하고, 현재 QA 분석은 사용자가 검토하며 Codex가 문서화를 보조한다. |
| When | 2단계 기능 흐름 분석 종료 시점에 기준선을 만들고, 4~7단계에서 테스트케이스와 실행 결과를 연결한다. |
| Where | 2차 Notion 기획서·요구사항, Java/Spring 기준 커밋의 화면·Controller·Service·Repository·Entity를 근거로 한다. |
| How | 각 항목에 근거 유형, 코드 추적, 위험 ID, 실행 상태를 연결하고 미논의 정책은 임의로 확정하지 않는다. |

## 2. 근거 우선순위

1. 2차 기획서·요구사항에 명시된 제품 요구사항
2. 과거 프로젝트 의도에 관한 사용자의 명시적 확인
3. 기준 커밋 코드와 UI가 보여주는 현재 계약
4. 실행 전 위험 가설과 미논의 정책

코드 동작은 요구사항이 없는 항목의 원래 의도를 자동으로 증명하지 않는다. 실행하지 않은 항목은 모두 `Not Run`이다.

## 3. 추적 매트릭스

| 요구사항·기준 ID | 기능·품질 항목 | 오라클 또는 확인 기준 | 근거 유형 | 주요 코드 추적 | 연결 위험 | 작성 당시 상태 |
|---|---|---|---|---|---|---|
| TF-REQ-001 | 리뷰 검색 | 사용자가 원하는 여행지 후기를 검색할 수 있어야 함 | 2차 요구사항 | `GET /api/reviews?keyword=`, Review Controller·Repository | TF-RISK-004 | 코드 확인 / Not Run |
| TF-REQ-002 | 리뷰 작성 | 인증 사용자가 존재하는 여행지에 제목·내용·평점을 포함한 리뷰를 작성할 수 있어야 함 | 2차 요구사항 + 코드 입력 계약 | `POST /api/reviews`, ReviewRequestDto·ReviewService | 없음 | 코드 확인 / Not Run |
| TF-REQ-003 | 본인 리뷰 수정·삭제 | 작성자는 자신의 리뷰를 수정·삭제할 수 있고 비작성자는 차단되어야 함 | 2차 요구사항 + 코드 권한 계약 | `PUT·DELETE /api/reviews/{reviewId}`, 작성자 ID 비교 | TF-RISK-006 | 코드 확인 / Not Run |
| TF-REQ-004 | 수정 시 여행지 변경 | 리뷰 수정 요청에서 선택한 여행지로 실제 연결 정보가 변경되어야 함 | 2026-08-26 사용자 확인 | 수정 DTO의 `placeId`, ReviewService·Review Entity 수정 경로 | TF-RISK-001 | 코드 불일치 가능성 / Not Run |
| TF-REQ-005 | 리뷰 정렬 | 조회수·평점·댓글 기준의 단일 정렬 결과가 기준에 맞아야 함 | 2차 요구사항 | `GET /api/reviews?sort=`, Review Controller·Repository | TF-RISK-005 | 코드 확인 / Not Run |
| TF-REQ-006 | 평점 | 1~5 평점을 저장할 수 있고 높은 평점 정렬을 제공해야 함 | 2차 요구사항 + 코드 입력 계약 | ReviewRequestDto·Review Entity·평점 정렬 | TF-RISK-014 | 코드 확인 / Not Run |
| TF-REQ-007 | 리뷰 조회 API | 전체·상세·장소별·내 리뷰·회원별 리뷰 API가 명세 경로와 메서드에 맞아야 함 | 2차 API 명세 | ReviewController | TF-RISK-003, TF-RISK-007 | 정적 일치 / Not Run |
| TF-REQ-008 | 댓글 CRUD | 인증 사용자는 댓글을 작성하고 작성자는 자신의 댓글을 수정·삭제할 수 있어야 함 | 2차 API 명세 + 코드 권한 계약 | CommentController·CommentService·CommentRepository | TF-RISK-009 | 코드 확인 / Not Run |
| TF-REQ-009 | JWT 인증·인가 | 변경 API는 인증을 요구하고 타인 자원 변경을 차단해야 함 | 2차 요구사항 + 코드 보안 계약 | SecurityConfig·JwtAuthenticationFilter·AuthService | TF-RISK-008 | 코드 확인 / Not Run |
| TF-REQ-010 | 리뷰 조회수 | 리뷰별 조회수를 관리해야 함 | 2차 데이터 모델 + 코드 | ReviewViewCount Entity·Repository | TF-RISK-011 | 중복 정책 미명시 / Not Run |
| TF-REQ-011 | CORS | 허용된 프론트 출처에서 API 호출이 가능해야 함 | 2차 보안 요구 | SecurityConfig | TF-RISK-013 | 설정 위험 / Not Run |
| TF-REQ-012 | 입력 경계 | 제목 2~30자, 내용 10~2000자, 댓글 2~100자, 평점 1~5를 검증함 | 코드 계약 | ReviewRequestDto·CommentRequestDto·예외 처리 | TF-RISK-009, TF-RISK-014 | 코드 확인 / Not Run |
| TF-OBS-001 | 페이지네이션 | UI 페이지 이동 시 기대한 데이터 묶음이 표시되는지 관찰함 | 코드·UI, 요구사항 미명시 | 프론트 `page` 요청과 백엔드 전체 List 응답 | TF-RISK-003 | 정책 미명시 / Not Run |
| TF-OBS-002 | 리뷰 이미지 | 이미지 UI와 백엔드 API의 현재 연결 상태를 관찰함 | 코드·UI, 요구사항 미명시 | 프론트 이미지 요청, 대응 Controller 미확인 | TF-RISK-002 | 초기 범위 제외 / Not Run |
| TF-NFR-001 | 성능 목표 | API 500ms와 초당 1,000 요청은 과거 목표로만 기록함 | 2차 비기능 목표 | 성능 환경 미감사 | 없음 | 초기 범위 제외 / Not Run |

## 4. 미논의 정책 처리

| 미논의 항목 | 현재 처리 |
|---|---|
| 리뷰 삭제 시 댓글·조회수 처리 | 현재 동작과 데이터 영향을 관찰하되 삭제·유지 중 하나를 임의 정답으로 삼지 않음 |
| 평점 소수 허용 | 코드가 허용하는 입력을 관찰하되 정수·소수 정책 결함으로 단정하지 않음 |
| 검색·장소 필터·정렬 조합 | 단일 기능은 요구사항으로 검증하고 조합은 탐색적 관찰로 분리 |
| 페이지 크기와 방식 | UI·API 계약 차이를 관찰하고 요구사항 결함과 구현 불일치를 구분 |
| 리뷰 이미지 | 초기 필수 범위에서 제외하고 코드·UI 위험으로 유지 |
| 인기 리뷰 가중치 | 정확한 공식 대신 정렬의 기본 일관성만 관찰 가능 |
| 조회수 중복 방지 | 세션·사용자·시간 기준 중 어느 것이 정답인지 임의 결정하지 않음 |
| Bearer·쿠키 인증 방식 | 인증·인가 성공과 우회 방지를 우선 검증하고 전달 채널 정책은 별도 기록 |
| 관리자 타인 리뷰·댓글 권한 | 명시가 없으므로 작성자 전용을 기본으로 하며 관리자 우회 기능을 추가하지 않음 |

## 5. 5일 초기 제출 추적 범위

- P0: TF-REQ-001~009와 TF-REQ-012의 대표 정상·권한·경계·예외 흐름
- P1: TF-REQ-010~011, TF-OBS-001과 리뷰 삭제 연관 데이터의 탐색적 관찰
- P2: TF-OBS-002, TF-NFR-001, 인기 점수 공식, 동시 조회수, Kotlin 전체 회귀 비교

P0를 먼저 테스트케이스로 전환하고 실행 증거까지 완결한다. P1·P2를 위해 P0 산출물의 완성도를 낮추지 않는다.

## 6. 후속 연결 규칙

- 4단계 테스트 조건·케이스에는 최소 한 개의 `TF-REQ-*`, `TF-OBS-*` 또는 `TF-NFR-*`를 근거로 연결한다.
- 실행 전에는 상태를 `Not Run`으로 유지한다.
- 미논의 정책을 검증하는 케이스는 기대 결과에 `정책 확인 필요` 또는 `현재 동작 관찰`을 표시한다.
- 실제 결과가 요구사항 또는 사용자 확인 오라클과 다를 때만 `Fail` 후보가 될 수 있다.
- 환경이나 데이터 때문에 검증 지점에 도달하지 못하면 `Blocked`로 기록한다.
