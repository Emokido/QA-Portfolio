# QA Engineer Portfolio

팀 개발에 참여했던 TripFriend를 QA 대상으로 다시 분석하고 **요구사항 → 위험 → 테스트 → 실행 → 결함 → 재검증**을 증거로 연결했습니다. 리뷰·댓글 영역의 입력 경계, 권한, API·UI 계약을 검증하고 테스트 구성 오탐과 제품 문제를 구분했습니다.

[TripFriend QA 사례 연구](projects/tripfriend/README.md) · [최신 결과와 근거](projects/tripfriend/reports/tripfriend-stage-8-results-summary.md) · [자동화 코드](projects/tripfriend/automation/) · [결함·관찰 기록](projects/tripfriend/defects/)

## 핵심 결과

| 결과 | 확인한 범위 |
|---|---|
| **핵심 우선순위 테스트(P0) 49건 — 누적 45 Pass·4 Fail** | 케이스별 최신 실행 근거를 집계했습니다. 댓글 Controller 교정으로 기존 오탐 2건을 Pass로 정정했으며, 49건 전체를 이번에 재실행한 결과는 아닙니다. |
| **추가 API 검증 7건 — 5 Pass·2 Fail** | 댓글 CRUD·입력·인증을 실제 HTTP 수준에서 확인하고 기존 문제 2건을 재현했습니다. |
| **별도 QA 검증용 사본 교정 후 Playwright 정상 회귀 5/5 Pass** | 로그인·검색·리뷰·댓글의 선별 경로를 확인했습니다. CORS·접근성 속성·수정 폼 문제를 원인 검증용 사본에서 교정한 결과이며, 원본 제품의 수정 완료를 의미하지 않습니다. |
| **기존 결함 재현 자동화 3/3** | TF-BUG-010·014의 3개 변형이 예상 동작과 불일치했습니다. 정상 회귀 성공률과 분리했습니다. |

결함·관찰 기록은 **15건 — 13 Open·2 Closed(테스트 구성 오탐)**입니다. Open 기록에는 정책 확인이 필요한 TF-BUG-012와 환경 제한 관찰인 TF-BUG-013이 포함돼 있으며, 전체를 확정 제품 결함 수로 표현하지 않습니다.

[2026-09-05 댓글 Controller 판정 교정](projects/tripfriend/reports/portfolio-review-correction-report.md) · [전체 결과·실행 이력](projects/tripfriend/reports/tripfriend-stage-8-results-summary.md)

## 대표 QA 판단

- **사용자 변경이 실제 데이터에 반영되는가:** [리뷰 여행지 변경 미반영](projects/tripfriend/defects/TF-BUG-001-review-place-not-updated.md)에서 요청 성공 여부와 연결 여행지 갱신을 구분했습니다.
- **테스트 실패가 제품 결함인가:** [리뷰 생성 오탐](projects/tripfriend/defects/TF-BUG-002-review-create-http-status-200.md)을 실제 서버 응답으로 반증했고, 같은 오탐 패턴이 [댓글 생성](projects/tripfriend/defects/TF-BUG-004-comment-create-http-status-200.md) 테스트에도 존재하는지 추가 점검해 판정을 정정했습니다.
- **첫 원인 가설이 맞는가:** [수정 폼 초기값 문제](projects/tripfriend/defects/TF-BUG-015-review-edit-form-initial-values-empty.md)의 Next.js 동적 경로 처리 가설을 반증하고 별도 QA 검증용 사본 교정 후 단일 재검증·전체 회귀를 확인했습니다.
- **초록색 테스트도 잘못될 수 있는가:** [댓글 입력 검증](projects/tripfriend/defects/TF-BUG-006-comment-review-id-null-request-processing-exception.md)에서 HTTP 401·빈 본문을 통과시키던 너무 넓게 설정된 검증 조건(assertion)을 교정했습니다.

## 수행 범위와 열람 안내

요구사항·코드 계약 분석, 위험 기반 계획, 테스트케이스 49건, JUnit·MockMvc·H2 테스트, Postman HTTP 검증, 수동 탐색과 Playwright 선별 자동화를 수행했습니다. 기대 결과의 출처와 실행 증거를 ID로 연결하고, 제품·구성·환경 문제 및 정책 미확정 관찰을 구분했습니다.

1. [TripFriend 사례 연구](projects/tripfriend/README.md) — 사용자 영향·전략·대표 판단
2. [최신 결과 요약](projects/tripfriend/reports/tripfriend-stage-8-results-summary.md) — 판정과 근거
3. [테스트케이스](projects/tripfriend/test-cases/p0-review-comment-test-cases.md) · [추적표](projects/tripfriend/test-plan/requirements-code-traceability.md) — 설계 기준선
4. [자동화와 재현 조건](projects/tripfriend/automation/) — 구현·실행 조건·확인 한계

과거 개발 기여와 이번 QA 활동은 구분합니다. Codex는 코드 탐색, 테스트·문서 초안 작성과 오류 분석을 보조했고, 작성자는 테스트 범위와 판정 기준을 검토하며 대표 실행과 결과를 직접 확인했습니다.
