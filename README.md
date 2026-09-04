# QA Engineer Portfolio

팀 개발에 참여했던 TripFriend 웹 서비스를 독립적인 QA 대상으로 다시 분석하고, **요구사항 → 위험 → 테스트 → 실행 → 결함 → 재검증**을 증거로 연결한 포트폴리오입니다.

[TripFriend QA 사례 연구](projects/tripfriend/README.md) · [최종 결과와 판단 기준](projects/tripfriend/reports/tripfriend-stage-8-results-summary.md) · [자동화 코드](projects/tripfriend/automation/) · [결함 보고서](projects/tripfriend/defects/)

## QA 접근 방식

- **실패 분류:** 제품 결함, 테스트 구성 문제, 실행 환경 문제를 같은 실패로 계산하지 않습니다.
- **추적성:** 요구사항과 관찰 항목을 Risk, Test Condition, Test Case, 실행 결과, Defect까지 연결합니다.
- **자동화 선정:** 비즈니스 중요도, 반복 비용, 회귀 위험, 명확한 합격·실패 기준을 바탕으로 자동화 대상을 고릅니다.
- **판단 경계:** 제품 동작까지 확인하지 못했거나 정책이 불명확한 현상은 결함으로 단정하지 않습니다.

## 핵심 결과와 의미

| 결과 | 의미 |
|---|---|
| **핵심 우선순위 테스트(P0) 49건 — 43 Pass·6 Fail** | 사용자 영향과 위험도가 높은 49건 모두 제품 판정 단계까지 실행했습니다. 6건의 불일치는 숨기지 않고 결함과 연결했습니다. |
| **핵심 API 추가 검증 7건 — 5 Pass·2 Fail** | 댓글 CRUD·입력·인증의 대표 흐름을 실제 HTTP 수준에서 확인하고 기존 결함 2건을 재현했습니다. |
| **Playwright 정상 회귀 5/5 Pass** | 로그인, 검색, 리뷰 작성·수정·삭제, 댓글 등록의 핵심 사용자 경로가 별도 QA 환경에서 정상 작동함을 확인했습니다. |
| **등록된 결함 3/3 재현** | 자동화 오류가 아니라 이미 알려진 TF-BUG-010·014를 테스트가 예상대로 검출한 결과입니다. 정상 회귀 성공률과 분리했습니다. |
| **결함 기록 15건 — 14 Open·1 Closed** | 제품 결함 14건과 테스트 구성 오탐 1건을 구분하고 재현·영향·한계·재검증 조건을 남겼습니다. |

## 결과를 해석한 기준

테스트가 실패했다는 사실만으로 제품 결함을 선언하지 않았습니다. 실제 제품 동작까지 확인했는지, 테스트 구성이 운영 응답 구조를 반영했는지, 환경 문제로 검증이 중단된 것은 아닌지를 먼저 구분했습니다. 반대로 테스트가 초록색이어도 잘못된 응답을 허용하는지 확인해 판정 기준 자체를 다시 검증했습니다.

## 대표 판단 사례

- [리뷰 수정 폼 초기값 결함(TF-BUG-015)](projects/tripfriend/defects/TF-BUG-015-review-edit-form-initial-values-empty.md): 자동화 문제 가능성을 확인한 뒤 제품 Fail로 재분류하고, 첫 원인 가설을 반증한 후 별도 QA 환경의 재검증과 전체 회귀까지 연결했습니다.
- [제품 결함이 아니었던 테스트 구성 오탐(TF-BUG-002)](projects/tripfriend/defects/TF-BUG-002-review-create-http-status-200.md): 실제 서버 결과로 기존 MockMvc 구성을 반증하고 연결 테스트 5건을 교정했습니다.
- [초록색 API 테스트의 거짓 통과(TF-BUG-006)](projects/tripfriend/defects/TF-BUG-006-comment-review-id-null-request-processing-exception.md): HTTP 401·빈 본문을 허용하던 넓은 assertion을 발견하고 정확한 상태·본문 기준으로 다시 검증했습니다.

## 추천 열람 순서

1. [TripFriend QA 사례 연구](projects/tripfriend/README.md) — 범위, 전략, 결과, 대표 판단
2. [최종 결과와 판단 기준](projects/tripfriend/reports/tripfriend-stage-8-results-summary.md) — 전체 수치와 실행 근거
3. [리뷰 수정 폼 초기값 결함](projects/tripfriend/defects/TF-BUG-015-review-edit-form-initial-values-empty.md) — 가설 검증·반증·재검증 사례
4. [Playwright 자동화](projects/tripfriend/automation/playwright/) — 정상 회귀와 등록 결함 재현 분리

과거 개발 기여와 현재 QA 활동은 구분해 기록했습니다. Codex는 코드 탐색과 초안 구조화를 보조했으며, 테스트 범위·합격 기준·결함 판정은 작성자가 실제 증거를 검토하고 대표 실행을 직접 재현해 확정했습니다.
