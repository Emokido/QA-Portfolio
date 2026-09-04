# TripFriend Playwright E2E 테스트 계획

- 상태: `Stage 8 Complete — regression 5 Pass · 0 Fail · 0 Blocked, Known Defect variants 0 Pass · 3 Fail · 0 Blocked`
- 기준일: 2026-09-04
- 기준 제품 커밋: `e2431223ce2a18c8f944d544cba76b951ce0356d`
- 도구: Playwright Test, 설치된 Chrome channel

## 1. 선정 원칙

비즈니스 중요도, 여러 기능의 연결성, 반복 실행 비용, 과거 결함의 회귀 위험, 자동화 안정성을 함께 평가했다. 수동 Fail 전체를 자동화하지 않고 핵심 정상 회귀와 명확한 오라클이 있는 Known Defect만 선별했다.

## 2. 자동화 범위

| ID | 분류 | 시나리오 | 준비 | 핵심 오라클 |
|---|---|---|---|---|
| TF-E2E-001 | 정상 회귀 | 정상 로그인 | 환경변수의 로컬 회원 | `/` 이동과 accessToken 저장 |
| TF-E2E-002 | 정상 회귀 | 공개 리뷰의 동일 화면 상태 기반 검색 결과 없음 | 고유 검색어 | keyword 포함 API GET의 HTTP 200, `/community` 유지, 빈 결과 안내 |
| TF-E2E-003-A | 정상 회귀 | UI 리뷰 작성·결과 확인 | 고유 제목·환경변수 여행지 | HTTP 201, reviewId, 목록·상세의 제목과 내용 |
| TF-E2E-003-B | 정상 회귀 | 리뷰 조회·수정·삭제 · TF-BUG-015 발견/재검증 | API 고유 리뷰 | 기존 폼 값 보존, PUT 200, 수정 내용 표시와 삭제 후 목록 복귀 |
| TF-E2E-004 | 정상 회귀 | 댓글 등록·표시 | API 고유 리뷰 | 입력한 고유 댓글의 화면 표시 |
| TF-E2E-005 | Known Defect | TF-BUG-010 회원가입 CTA | 없음 | `/member/signup`과 회원가입 제목 |
| TF-E2E-006 | Known Defect | TF-BUG-014 댓글 1자·101자 경계 안내 | API 고유 리뷰 | 두 변형 모두 2~100자 범위를 포함한 오류 안내 |

정상 회귀와 Known Defect는 별도 디렉터리와 명령으로 실행한다. Known Defect 2개는 제품 수정 전 Fail이 예상되며 정상 회귀 집계와 성공률에 합치지 않는다. TF-E2E-003과 TF-E2E-006을 각각 두 변형으로 나눠 Playwright 실행 단위는 정상 회귀 5개·Known Defect 3개, 총 8개다.

## 3. 제외·보류

- UI 댓글 수정·삭제: 댓글별 semantic container가 없고 반복 버튼을 순번 없이 구분할 수 없다.
- TF-BUG-009 홈 검색: 최초 Known Defect 후보였지만 목적 화면·검색 대상·성공 결과 정책이 아직 명시되지 않아 정확한 오라클이 없다. 현재 무응답을 성공으로 assertion하거나 임의 목적지를 기대하지 않고, 정책 확정 전까지 TF-BUG-010을 명확한 오라클이 있는 대체 대상으로 유지한다.
- TF-BUG-011: 비현실적인 초장문 입력이고 최대 길이 정책이 없다.
- TF-BUG-012: 유효 모집 인원과 범위 관계 정책이 없다.
- TF-BUG-013: 환경 제한 관찰로 후속 재검증을 보류하며 자동화 분모와 대표 결함 성과에서 제외한다.
- 동행 신청: 다중 사용자와 상태 fixture가 필요해 초기 6개 범위를 크게 확장한다.

보류 항목을 억지 CSS selector나 임의 요구사항으로 구현하지 않는다. 향후 제품에 접근 가능한 이름·semantic container가 추가되거나 정책이 확정되면 재평가한다.

## 4. 데이터와 cleanup

- 실제 운영 계정과 운영 데이터를 사용하지 않는다.
- `TF_E2E_USERNAME`, `TF_E2E_PASSWORD`, `TF_E2E_PLACE_ID`, `TF_E2E_PLACE_NAME`은 실행 셸에서만 주입한다.
- 생성 데이터는 실행 시각과 worker 번호로 고유하게 만든다.
- TF-E2E-003-A는 UI에서 리뷰를 작성하고 생성 응답·목록·상세를 확인한다. 다른 API fixture는 사전조건 구성과 cleanup에만 사용하고 핵심 동작·assertion은 UI에서 확인한다.
- 각 CRUD 테스트는 `try/finally`로 정리하며, 404 이외 cleanup 실패는 별도 오류로 드러낸다.

## 5. 실행 및 판정

```powershell
cd projects\tripfriend\automation\playwright
npm run test:regression
npm run test:known-defects
```

- Pass: 핵심 UI assertion이 모두 기대와 일치하고 cleanup이 완료된다.
- Fail: 제품 검증 지점에 도달했지만 기대와 실제가 다르다.
- Blocked: 환경, 계정, fixture, locator 문제로 제품 검증 지점에 도달하지 못한다.
- Not Run: 실행하지 않았다.

현재 정상 회귀 5개는 수정 후 전체 실행에서 `5 Pass · 0 Fail · 0 Blocked`다. Known Defect 3개 실행 변형은 `0 Pass · 3 Fail · 0 Blocked`이며 정상 회귀 집계와 분리한다. TF-E2E-005와 TF-E2E-006 1자·101자가 기존 결함을 재현했다. 101자는 dialog event를 즉시 처리하도록 교정한 단일 재실행에서도 4.3초에 일반 오류 문구를 검출했고 fixture cleanup을 완료했다.

실행은 정상 회귀를 먼저 수행해 환경·fixture 안정성을 확인한 뒤 Known Defect를 별도 명령으로 수행한다. Stage 8에서는 이 순서로 실행을 완료했으며 실패 화면·video·trace는 판정 보조 자료로 사용했다.

GitHub Actions CI 연결은 Stage 8 결과를 변경하지 않는 후속 `Stage 8A`에서 설계한다. 기본 대상은 정상 회귀 5개이며, Known Defect는 예상 실패가 CI 성공률을 오염시키지 않도록 기본 필수 job에서 분리한다.
