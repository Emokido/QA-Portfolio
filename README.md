# QA Engineer Portfolio


    테스트 결과를 실제 동작과 비교하고, 실패의 원인을 끝까지 확인하는 QA Engineer 박현모입니다.
    Contact: gusah0p@naver.com


팀 개발에 참여했던 **TripFriend**를 QA 대상으로 다시 분석해  
**요구사항 → 위험 → 테스트 → 실행 → 결함 → 재검증**을 하나의 흐름으로 연결했습니다.

리뷰·댓글 영역을 중심으로 테스트를 설계하고, Java/Spring 계층별 테스트·Postman API 검증·수동 탐색·Playwright E2E를 수행했습니다. 테스트 실패를 곧바로 제품 결함으로 판단하지 않고 실제 응답과 실행 환경을 비교해 제품 문제, 테스트 구성 문제, 환경 문제를 구분했습니다.
<br>
<br>
<br>
[TripFriend QA 사례 연구](projects/tripfriend/README.md) · [최신 결과와 근거](projects/tripfriend/reports/tripfriend-stage-8-results-summary.md) · [자동화 코드](projects/tripfriend/automation/) · [결함·관찰 기록](projects/tripfriend/defects/)

<br>

## 한눈에 보기

| 구분 | 내용 |
|---|---|
| **프로젝트** | 여행지 정보와 사용자 리뷰·댓글 기능을 제공하는 팀 프로젝트 |
| **주요 QA 범위** | 리뷰·댓글 CRUD, 인증·권한, 입력 경계, 검색·정렬, API·UI 계약 |
| **테스트 방식** | 위험 기반 테스트 설계, JUnit·MockMvc·H2, Postman, 수동 탐색, Playwright |
| **핵심 테스트** | 핵심 우선순위 테스트(P0) 49건 판정 · 45 Pass · 4 Fail |
| **추가 API 검증** |  7건 · 5 Pass · 2 Fail |
| **Playwright 자동화** | 정상 회귀 5/5 Pass · 기존 결함 재현 3/3 |
| **결함·관찰 기록** | 총 15건 · 13 Open · 2 Closed(테스트 구성 오탐) |
<br>

## 이번 포트폴리오에서 한 일

- 요구사항과 구현 동작을 비교해 테스트 조건·케이스와 요구사항 추적표를 작성했습니다.
- 사용자 영향과 발생 가능성을 기준으로 위험을 분석하고 핵심 테스트 49건을 설계해 실행 우선순위를 정했습니다.
- Java/Spring 계층별 테스트와 Postman API 시나리오를 구현·실행하고, 실제 API·브라우저 흐름과 프런트 화면을 탐색했습니다.
- 반복 비용과 회귀 위험이 높은 사용자 흐름을 Playwright로 자동화하고 테스트 데이터를 분리·정리했습니다.
- 실행 결과, 증거, 결함, 재검증 조건을 ID로 연결해 결과의 추적성을 관리했습니다.
<br>

## 핵심 결과

| 결과 | 확인한 범위와 의미 |
|---|---|
| **핵심 우선순위 테스트(P0) 49건 -  누적 45 Pass·4 Fail** | 리뷰·댓글의 CRUD, 권한, 입력 경계, 조회·정렬 등을 우선 검증했습니다. 테스트 구성 오탐 2건은 실제 서버 응답과 교정 테스트를 근거로 Pass로 재분류했습니다. 45 Pass·4 Fail은 케이스별 최신 유효 판정을 집계한 값이며, 49건 전체를 한 번에 재실행한 결과는 아닙니다. |
| **추가 API 검증 7건 <br>5 Pass · 2 Fail** | 댓글 CRUD·인증·입력 검증을 실제 HTTP 수준에서 확인했습니다. 정상 흐름뿐 아니라 기존 입력 검증 문제 2건도 다시 재현해 테스트 코드 결과와 실제 서버 동작을 비교했습니다. |
| **Playwright 정상 회귀 <br> 5/5 Pass** | 로그인·검색·리뷰·댓글의 핵심 사용자 흐름을 자동화했습니다. CORS·접근성 속성·수정 폼 문제를 별도 QA 검증용 사본에서 교정한 뒤 회귀 결과를 확인했으며, 원본 제품의 수정 완료를 의미하지 않습니다. |
| **결함·관찰 기록 15건 <br> 13 Open · 2 Closed** | 제품 문제, 테스트 구성 문제, 정책 확인 필요 항목, 환경 제한 관찰을 구분해 관리했습니다. 이 중 2건은 실제 제품 결함이 아니라 테스트 구성 오탐임을 확인해 Closed로 재분류했습니다. |
| **기존 결함 재현 자동화 3/3** | TF-BUG-010·014의 3개 시나리오가 기대 동작과 불일치하는 것을 자동화로 재현했습니다. 실패가 예상되는 결함 재현 테스트는 정상 회귀 성공률과 분리해 관리했습니다. |


[댓글 Controller 테스트 구성 교정](projects/tripfriend/reports/portfolio-review-correction-report.md) · [전체 결과와 실행 이력](projects/tripfriend/reports/tripfriend-stage-8-results-summary.md)
<br>
<br>
## 대표 QA 판단

- **사용자가 변경한 값이 실제 데이터에 반영되는가**  
  [리뷰 여행지 변경 미반영](projects/tripfriend/defects/TF-BUG-001-review-place-not-updated.md)에서 수정 요청의 성공 여부와 실제 연결 데이터 갱신을 분리해 확인했습니다.

- **테스트 실패가 정말 제품 결함인가**  
  [리뷰 생성 테스트](projects/tripfriend/defects/TF-BUG-002-review-create-http-status-200.md)의 실패를 실제 서버 응답과 비교해 테스트 구성 오탐으로 재분류했고, 같은 패턴이 [댓글 생성 테스트](projects/tripfriend/defects/TF-BUG-004-comment-create-http-status-200.md)에도 존재하는지 추가 점검해 판정을 정정했습니다.

- **통과한 테스트 결과도 신뢰할 수 있는가**  
  [댓글 입력 검증](projects/tripfriend/defects/TF-BUG-006-comment-review-id-null-request-processing-exception.md)에서 실제 HTTP 401·빈 본문을 통과시키던 너무 넓은 검증 조건(assertion)을 발견하고 판정 기준을 강화했습니다.

<br>


## 더 자세히 보기

1. [TripFriend QA 사례 연구](projects/tripfriend/README.md)  
   QA 전략, 사용자 영향, 대표 결함과 판단 과정

2. [최신 결과 요약](projects/tripfriend/reports/tripfriend-stage-8-results-summary.md)  
   최신 Pass·Fail 판정과 실행 근거

3. [테스트케이스 49건](projects/tripfriend/test-cases/p0-review-comment-test-cases.md) ·
   [요구사항 추적표](projects/tripfriend/test-plan/requirements-code-traceability.md)  
   테스트 설계와 요구사항·결함 간 추적 관계

4. [자동화와 재현 조건](projects/tripfriend/automation/)  
   Java/Spring 테스트, Playwright, Postman 구현과 검증 한계

<br>
<br>


과거 개발 기여와 이번 QA 활동은 구분합니다. Codex는 코드 탐색, 테스트·문서 초안 작성과 오류 분석을 보조했고, 작성자는 테스트 범위와 판정 기준을 검토하며 대표 실행과 결과를 직접 확인했습니다.
