# TripFriend API·프런트 흐름 테스트 계획

- 문서 상태: `Stage 7 Complete`
- 기준일: 2026-09-01
- 주 테스트 대상: Sprint 1 Java/Spring 완성본과 연결된 Next.js 프런트
- 실행 작업본: 별도 로컬 QA 실행 사본 `QA-Lab/execution/TripFriend`
- 완료 범위: 공개 조회와 인증 리뷰 생성의 실제 서버·Postman·프런트 흐름

## 1. 5W1H

| 항목 | 내용 |
|---|---|
| Why | Stage 6의 Service·Controller slice·Repository/H2 결과만으로 증명하지 못한 실제 서버·HTTP·CORS·프런트 표시 경계를 검증한다. |
| What | Postman API 요청과 브라우저 `/community` 흐름을 연결해 공개 리뷰 목록과 빈 검색 결과를 확인한다. |
| Who | Codex는 Collection·격리 환경·판정·문서화를 준비하고, 사용자는 자신의 비공개 Postman Workspace에 가져와 직접 `Send`를 실행한다. |
| When | Stage 6 완료 후, Stage 8 결과 통합 전에 수행한다. |
| Where | 승인된 QA-Lab 실행본, localhost 8080·3000, 사용자 Postman 비공개 Workspace에서 수행한다. |
| How | 정적 계약 확인 → 격리 서버 기동 → curl 준비 확인 → Postman 사용자 실행 → 브라우저 UI 확인 → 결과·증거 기록 순서로 진행한다. |

## 2. 첫 검증 단위

| 실행 ID | 연결 케이스 | 요청·화면 | 핵심 오라클 | 초기 상태 |
|---|---|---|---|---|
| TF-S7-EXEC-001 | TF-TC-001 | `GET /api/reviews?sort=newest&page=0` | HTTP 200, JSON, code `200-5`, 1개 이상 `data` 배열 | Not Run |
| TF-S7-EXEC-002 | TF-TC-049 | `GET /api/reviews?sort=newest&keyword=stage7_no_result_20260901` | HTTP 200, JSON, code `200-5`, 빈 `data` 배열 | Not Run |
| TF-S7-EXEC-003 | TF-TC-001·049 | `http://localhost:3000/community`와 검색 | 요청 성공, 목록·빈 결과 UI, 콘솔·CORS 영향 | Not Run |
| TF-S7-RETEST-001 | TF-BUG-007 | QA execution CORS patch 후 `/community` 재실행 | preflight 허용, Place·Review 200, 목록 표시, CORS 오류 없음 | Not Run |

## 3. 환경과 데이터

- 백엔드는 별도 `stage7` 프로필의 인메모리 H2와 `create-drop`을 사용한다.
- 애플리케이션의 `BaseInitData`가 로컬 예시 회원·장소·리뷰·댓글을 만든다.
- 인증 단위는 설치된 로컬 Redis를 디스크 저장 없이 임시 사용한다. 최초 6379 계획은 기존 자동 시작 Redis 서비스의 포트 점유·비격리·디스크 저장 위험 때문에 실행 전 중단했으며, QA 실행 전용 포트 6380 변경 권한을 기다린다.
- 외부 MySQL·OAuth·메일을 호출하지 않는다.
- 공개 GET에 토큰과 쿠키를 의도적으로 넣지 않는다.
- `page`는 프런트가 전달하지만 현재 Controller가 소비하지 않는 정적 관찰 항목이다.

## 4. 두 번째 검증 단위

| 실행 ID | 연결 케이스 | 요청·화면 | 핵심 오라클 | 초기 상태 |
|---|---|---|---|---|
| TF-S7-EXEC-004 | TF-TC-020 | 토큰·쿠키 없는 `POST /api/reviews` | HTTP 401, 성공 코드 없음 | Not Run |
| TF-S7-PREP-007 | 인증 준비 | `POST /member/login` | HTTP 200, code `200-1`, 로컬 테스트 토큰 발급·Redis 저장 | Not Run |
| TF-S7-EXEC-005 | TF-TC-004·TF-BUG-002 | Bearer `POST /api/reviews` | HTTP 201, code `201-1`, 작성자·장소·필드 저장 | Not Run |
| TF-S7-EXEC-006 | TF-TC-004 | Bearer `GET /api/reviews/my` | HTTP 200, code `200-7`, 생성 ID·제목 존재 | Not Run |
| TF-S7-EXEC-007 | TF-TC-004·TF-BUG-002 | 브라우저 로그인 → `/community/write` → 목록 | Bearer 요청, 저장·카드 표시, 생성 HTTP 상태의 프런트 영향 | Not Run |

- 인증 리뷰 생성의 HTTP 201 기대는 기존 Baseline대로 유지했다. 실제 서버 TF-S7-EXEC-005는 HTTP 201·code `201-1`·4/4 Passed로 계약을 충족했다.
- 기존 TF-BUG-002의 HTTP 200은 `ResponseAspect`가 빠진 독립형 MockMvc 테스트 구성 오탐으로 확인돼 제품 결함이 아닌 테스트 결함으로 재분류한다.
- 로그인은 준비 단계이며 제품 Pass 합계에 중복 산입하지 않는다.

## 5. 정적 사전 관찰

| ID | 관찰 | 실행 시 확인 |
|---|---|---|
| TF-S7-OBS-001 | `ReviewController`는 목록 응답을 `RsData<List<ReviewResponseDto>>`, code `200-5`로 반환한다. | Postman 응답 계약 |
| TF-S7-OBS-002 | 프런트는 `page`를 보내지만 백엔드 목록 Controller에는 `page` 파라미터가 없다. | 응답 수·페이지 UI 영향 |
| TF-S7-OBS-003 | CORS 설정에서 localhost 허용 목록을 설정한 뒤 운영 URL 설정이 다시 호출되어 앞 값을 덮어쓸 가능성이 있다. | `/community` 브라우저 요청의 CORS 성공·실패 |
| TF-S7-OBS-004 | 프런트 `getReviews`는 오류를 빈 목록 형태로 바꾸므로 CORS/API 실패가 `검색 결과가 없습니다`로 보일 수 있다. | Network·Console과 화면을 함께 판정 |
| TF-S7-OBS-005 | 로그인과 보호 요청은 Redis의 access token 저장·대조에 의존한다. | 임시 Redis PING·로그인·Bearer 요청 |
| TF-S7-OBS-006 | 로그인 응답은 HttpOnly·Secure Cookie와 본문 access token을 함께 제공하지만 프런트 리뷰 API는 localStorage Bearer 헤더를 사용한다. | Network에서 Bearer 성공 경로 확인 |
| TF-S7-OBS-007 | 리뷰 생성 Controller는 code `201-1`을 반환하고 전체 애플리케이션의 `ResponseAspect`가 이를 HTTP 201로 설정한다. 독립형 MockMvc는 Aspect 미포함 시 기본 200을 관찰한다. | 실제 서버 HTTP 상태와 테스트 격리 수준 차이 |
| TF-S7-OBS-008 | 백엔드 리뷰 평점은 `double`이고 API는 4.5를 허용하지만 작성 UI는 정수 별 버튼만 제공하며 목록·상세의 `i < rating` 조건은 4.5를 5개 별로 표시한다. | 평점 입력·저장·표시 정책의 계층 간 일관성 |

정적 관찰은 제품 결함 확정이 아니다. 실제 서버·브라우저 재현 후 사용자 영향과 기대 계약을 비교해 판정한다.

## 6. 통과·실패·중단 기준

- Pass: 요청과 화면의 모든 오라클이 충족되고 숨은 네트워크 오류가 없다.
- Fail: 실제 HTTP·응답·화면이 확정 기대 결과와 다르다.
- Blocked: 승인된 격리 범위에서 실행 환경을 만들 수 없다.
- 비밀값, 실계정, 외부 서비스, 제품 코드 수정이 필요하면 즉시 중단하고 추가 승인을 요청한다.

## 7. 현재 제외

- 댓글 쓰기, 실제 계정·운영 토큰
- TF-BUG-001~008 제품 수정과 수정 후 재검증
- 외부 MySQL·운영 Redis·OAuth·메일
- Kotlin 회귀, 성능·동시성, Python 자동화
- Git·GitHub 반영

## 8. 최종 실행 연결

- Stage 7 계획 실행 8건을 모두 판정했다.
- 최종 결과: `7 Pass · 1 Fail · 0 Blocked · 0 Not Run`.
- 원본 브라우저 CORS 실행 TF-S7-EXEC-003은 Fail이며, QA 실행 사본 수정 후 TF-S7-RETEST-001은 Pass다.
- 인증 API와 브라우저 리뷰 작성 TF-S7-EXEC-004~007은 모두 Pass다.
- TF-BUG-002는 실제 서버 반증으로 테스트 구성 오탐 Closed, TF-BUG-007·008은 Open이다.
- 상세 실행·증거·한계는 `reports/api-frontend-flow-execution-report.md`를 사용한다.
