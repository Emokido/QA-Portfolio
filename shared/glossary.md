# QA 용어 및 식별자 안내

이 문서는 포트폴리오에서 사용하는 QA 용어와 TripFriend 전용 식별자의 의미를 설명한다. 영어 약어는 코드·이슈·테스트 도구에서 짧게 추적하기 위해 사용하지만, 본문 설명은 한글을 우선한다.

## 상태와 우선순위

| 용어 | 영어 원문 | 의미 |
|---|---|---|
| P0·P1·P2 | Priority 0·1·2 | 테스트 실행 우선순위. P0가 가장 먼저 검증할 범위이며 결함 심각도나 실행 결과가 아니다. |
| Not Run | Not Run | 테스트 실행을 시도하지 않은 상태 |
| Blocked | Blocked | 환경·도구·데이터·불명확한 오라클 등으로 제품 검증 지점에 도달하지 못한 상태 |
| Pass | Pass | 검증 지점까지 실행한 실제 결과가 근거 있는 기대 결과와 일치한 상태 |
| Fail | Fail | 검증 지점까지 실행한 실제 결과가 근거 있는 기대 결과와 불일치한 상태 |
| 오라클 | Test Oracle | 실제 결과가 맞는지 판단할 요구사항·사용자 확인·명확한 계약 등의 기준 |

## 분석과 테스트 설계

| 용어 | 영어 원문 | 의미 |
|---|---|---|
| 테스트 베이시스 | Test Basis | 테스트 분석과 설계의 근거가 되는 요구사항·코드·설계·사용자 확인 자료 |
| 요구사항 추적 매트릭스 | Requirements Traceability Matrix, RTM | 요구사항과 코드·위험·테스트·실행 결과를 연결한 표 또는 문서 전체 |
| 테스트 조건 | Test Condition | 상세 절차를 작성하기 전에 정의한 검증 대상과 관점 |
| 테스트케이스 | Test Case | 사전조건·데이터·절차·기대 결과를 구체화한 개별 테스트 |
| 경계값 분석 | Boundary Value Analysis | 허용 범위의 최소·최대와 바로 바깥값을 검증하는 설계 기법 |
| 동등 분할 | Equivalence Partitioning | 동일하게 처리될 것으로 예상되는 입력을 그룹으로 나눠 대표값을 선택하는 기법 |
| Mock | Mock Object | 실제 의존 객체 대신 호출과 결과를 통제하고 상호작용을 검증하는 테스트 대역 |
| Fixture | Test Fixture | 테스트에 필요한 객체·데이터·환경의 준비 상태 |
| Assertion | Assertion | 실제 결과가 기대 결과와 일치하는지 확인하는 검증문 |

## TripFriend 식별자

| 식별자 | 한글 의미 | 영어 원문 | 사용 |
|---|---|---|---|
| `TF-REQ-*` | TripFriend 요구사항 ID | TripFriend Requirement ID | 문서 요구사항, 사용자 확인 오라클 또는 검증 가능한 코드 계약 |
| `TF-OBS-*` | TripFriend 관찰 항목 ID | TripFriend Observation ID | 요구사항으로 확정되지 않은 코드·UI 동작의 탐색적 관찰 항목 |
| `TF-NFR-*` | TripFriend 비기능 요구사항 ID | TripFriend Non-Functional Requirement ID | 성능·보안처럼 기능 동작 외의 품질 목표 |
| `TF-RISK-*` | TripFriend 위험 가설 ID | TripFriend Risk ID | 정적 분석으로 발견한 실행 전 위험 가능성. 재현 전에는 결함이 아님 |
| `TF-COND-*` | TripFriend 테스트 조건 ID | TripFriend Test Condition ID | 상세 테스트케이스 전의 검증 조건 |
| `TF-TC-*` | TripFriend 테스트케이스 ID | TripFriend Test Case ID | 개별 상세 테스트케이스 |
| `TF-EXEC-*` | TripFriend 실행 ID | TripFriend Execution ID | 명령·실행자·환경·실제 결과·증거를 구분하는 실행 단위 |
| `TF-ENV-*` | TripFriend 환경 위험 ID | TripFriend Environment Risk ID | 도구·설정·의존성·비밀 관리 등 환경 위험 |
| `TF-BUG-*` | TripFriend 결함 ID | TripFriend Bug ID | 실행과 오라클로 재현·검토한 제품 결함 |
| `TF-KAUD-*` | TripFriend Kotlin 감사 ID | TripFriend Kotlin Audit ID | Kotlin 제한적 정적 감사에서 확인한 회귀 위험·계약 관찰·커버리지 차이. 제품 테스트 결과나 결함 ID가 아님 |

`TF-` 접두사와 ID 조합은 TripFriend 포트폴리오용 규칙이며 모든 조직에 동일하게 적용되는 국제 표준은 아니다.
