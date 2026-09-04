# TripFriend 테스트 환경 감사

- 문서 상태: `Stage 3 Baseline`
- 기준 프로젝트: 스프린트 1 Java/Spring
- 저장소: `https://github.com/Emokido/tripfriend-spring`
- 브랜치·커밋: `main` / `e2431223ce2a18c8f944d544cba76b951ce0356d`
- 제품 테스트 상태: `Not Run`
- 감사일: 2026-08-26
- 최근 환경 확인: 2026-08-26

## 1. 5W1H

| 항목 | 내용 |
|---|---|
| Why | 리뷰·댓글 테스트를 실행하기 전에 재현 가능한 최소 환경과 보안·의존성·기준본 위험을 확인한다. |
| What | Git 기준선, Java·Gradle, Node·npm, Spring 프로필, DB·Redis 등 외부 의존성, 비밀 설정과 기존 생성물을 감사한다. |
| Who | Codex는 승인된 감사·작업본 생성·기준 컴파일과 문서화를 수행했고, 사용자는 권한과 완료 여부를 확인하고 IntelliJ에서 환경 설정과 기준 컴파일을 직접 재현했다. |
| When | 2단계 완료 후, 테스트케이스 작성과 테스트 실행 전에 수행한다. |
| Where | 원본 팀 원격 저장소가 아니라 기존 로컬 감사본과 별도 실행용 작업본을 사용한다. |
| How | 비밀값을 열거나 출력하지 않고 파일명·키 이름·빌드 선언·Git 상태만 확인한다. 실행 명령은 추가 권한 후 별도 작업본에서 수행한다. |

## 2. 읽기 전용 감사 결과

| 영역 | 확인 사실 | 해석·조치 |
|---|---|---|
| Git 기준선 | `main`, 기준 커밋 일치 | 테스트 결과에는 이 SHA를 기록한다. |
| 작업 트리 | 기존 `qa/` 폴더가 미추적 상태 | 과거 산출물로 보이며 사용자 소유 파일로 보존한다. 기준본에서 삭제·수정하지 않는다. |
| 적용 지침 | 감사본과 상위 경로에서 `AGENTS.md` 미발견 | 고정 인수인계와 현재 채팅 권한을 따른다. |
| Backend Java | Gradle toolchain Java 17, PATH 기본 Java 21.0.3 | `C:\Program Files\Java` 아래 JDK 17·21 디렉터리가 존재한다. Java 17 toolchain 선언 상태에서 기준 컴파일이 성공했지만 실제 선택된 컴파일러 경로는 별도 명령으로 출력하지 않았다. |
| Gradle | Wrapper 8.12.1 | Wrapper 8.12.1을 승인된 네트워크 범위에서 내려받아 기준 컴파일 1회를 완료했다. |
| Spring Boot | 빌드 플러그인 3.2.4 | README의 3.4.2 표기와 불일치한다. 실제 기준은 빌드 파일로 본다. |
| Backend 데이터 | MySQL·H2·Redis 의존성 존재 | 리뷰·댓글 초기 자동화는 H2 기반 테스트 프로필 우선 검토가 적절하다. 전체 앱 실행은 외부 의존성 감사 후 결정한다. |
| Frontend | Next.js 15.2.4, React 19, npm lockfile | 로컬 Node 22.14.0, npm 10.9.2를 기록했다. `package.json`에 Node 엔진 범위가 없어 실제 프론트 빌드 호환성은 아직 `Not Run`이다. |
| 기존 생성물 | backend `build`, frontend `node_modules`·`.next` 존재 | 과거 생성물이므로 현재 빌드 성공의 증거로 사용하지 않는다. |
| 비밀 설정 | `application-secret.yml`이 Git 추적 대상 | 내용은 열지 않았다. 공개 포트폴리오에 복제하지 않고 실행용 작업본에서도 제외·대체해야 한다. 실제 비밀값이 들어 있다면 사용자가 별도로 폐기·교체 여부를 확인해야 한다. |
| 과거 QA 문서 | 미추적 `qa/`에 계획·실행·결함 문서 존재 | 2026-08-19 과거 기록으로 보존하되 현재 실행 결과로 재사용하지 않는다. 별도 검증 전에는 현재 `Pass/Fail` 근거가 아니다. |

## 3. 확인된 환경 위험

| ID | 우선순위 | 위험 | 현재 상태 | 다음 확인 |
|---|---|---|---|---|
| TF-ENV-001 | High | 기준 감사본에 미추적 `qa/`가 있어 깨끗한 기준선과 과거 산출물이 섞일 수 있음 | 사실 확인 | 별도 실행용 작업본 생성, 감사본 보존 |
| TF-ENV-002 | Critical | 이름과 설정 키상 비밀 가능성이 있는 `application-secret.yml`이 Git 추적 대상 | 파일 추적 사실만 확인, 값 미열람 | 사용자 수동 비밀 폐기·교체 확인, 공개·실행 복사 제외 |
| TF-ENV-003 | Medium | README Spring 3.4.2와 빌드 플러그인 3.2.4가 불일치 | 정적 확인 | 문서에는 기준 커밋의 빌드 파일 버전 사용 |
| TF-ENV-004 | Medium | Node 엔진 버전이 선언되지 않아 재현 환경이 불명확 | 로컬 Node 22.14.0·npm 10.9.2 기록 | 프론트 빌드 단계에서 실제 호환성 확인 |
| TF-ENV-005 | Medium | MySQL·Redis·OAuth·메일 등 전체 앱 외부 의존성이 많음 | 설정 키·의존성 확인 | P0 백엔드 테스트는 H2와 Mock 중심의 최소 환경부터 검증 |
| TF-ENV-006 | Medium | 기존 build·node_modules·.next가 현재 성공 결과처럼 오인될 수 있음 | 존재 확인 | 새 실행 ID로 수행한 결과만 증거로 채택 |
| TF-ENV-007 | Medium | 과거 미추적 QA 문서에 실행·결함 주장이 있으나 현재 포트폴리오 기준과 분리되지 않음 | 존재·문서 상태 확인 | 새 실행 전에는 과거 참고 자료로만 취급 |
| TF-ENV-008 | Medium | PATH 기본 Java 21.0.3과 빌드 선언 Java 17이 다름 | JDK 17·21 디렉터리 존재 및 기준 컴파일 성공 확인 | 후속 테스트 실행 시 Gradle toolchain과 실행 JVM을 구분해 기록 |

`TF-ENV-002`는 실제 유효한 비밀값 노출을 확인했다는 뜻이 아니다. 파일명, 키 이름과 Git 추적 상태만으로 예방 조치가 필요한 위험으로 분류했다.

## 4. 권장 최소 환경

### P0 백엔드 리뷰·댓글 테스트

- Java 17
- 저장소 제공 Gradle Wrapper 8.12.1
- Spring `test` 프로필과 H2
- JUnit 5·Spring Security Test
- 회원·인증·장소 같은 공동 영역은 테스트 목적에 따라 fixture 또는 Mock 사용
- MySQL·Redis·OAuth·메일·운영 인증서는 초기 Service·Controller·Repository 테스트의 필수 조건으로 두지 않음

### 프론트 정적·빌드 확인

- `package-lock.json`을 기준으로 npm 사용
- 로컬 Node·npm 버전을 실행 기록에 남김
- 기존 `node_modules`와 `.next`를 현재 성공 증거로 사용하지 않음

### 전체 애플리케이션·E2E

- 초기 P0가 아니다.
- MySQL·Redis, 안전한 로컬 비밀 설정, 프론트 API URL, CORS, 테스트 계정과 격리 데이터를 준비한 뒤 별도 승인으로 수행한다.

## 5. 실행용 작업본 원칙

- 기존 감사본은 읽기 전용으로 보존한다.
- 실행용 작업본은 `QA-Lab/execution/TripFriend`처럼 분리된 위치에 만든다.
- `.git`, `qa/`, `build`, `.gradle`, `node_modules`, `.next`, `application-secret.yml`을 그대로 복제하지 않는다.
- 필요한 설정은 실제 비밀값이 없는 로컬 전용 템플릿으로 새로 만든다.
- 실행용 작업본 생성과 빌드·테스트 명령은 사용자 추가 권한 후 수행한다.

### 실행용 작업본 생성 결과

- 생성 위치: 로컬의 별도 `QA-Lab\execution\TripFriend` 작업본
- 원본 감사본은 수정하지 않고 `robocopy`를 1회 사용했다.
- 제외 확인: `.git`, `qa`, `build`, `.gradle`, `node_modules`, `.next`, `application-secret.yml`, `application-prod.yml`
- 복사 직후 민감 가능 파일명 검색 결과: 없음
- 원본에 있던 `.DS_Store`와 `backend/src/main/generated`의 16개 Java 파일은 그대로 복사됐다. 새 빌드 결과로 생성된 파일이 아니며 삭제하지 않았다.

## 6. 승인된 실행 범위와 결과

사용자는 2026-08-26 다음 범위를 승인했다.

1. 별도 실행용 작업본 생성 1회
2. `java -version`, `node --version`, `npm --version` 각 1회
3. Gradle Wrapper 8.12.1과 컴파일에 필요한 의존성 다운로드
4. 실행용 작업본 Backend에서 `gradlew.bat compileJava compileTestJava --no-daemon` 1회
5. 이 문서에 실제 환경·컴파일 결과 반영

승인 범위에는 테스트 메서드, 애플리케이션·서버·Docker·DB 실행, 제품 코드 수정, Git commit·push가 포함되지 않았다. 해당 작업은 수행하지 않았다.

## 7. 현재 실행 상태

- 제품 테스트·애플리케이션·서버·Docker·DB: 실행하지 않음
- Java: PATH 기준 21.0.3, JDK 17·21 설치 디렉터리 존재 확인
- Node: 22.14.0
- npm: 10.9.2
- Gradle Wrapper: 8.12.1 다운로드 완료
- 기준 명령: `.\gradlew.bat compileJava compileTestJava --no-daemon` 1회
- 결과: `compileJava`, `processResources`, `compileTestJava` 성공, `BUILD SUCCESSFUL in 1m 1s`
- 컴파일 메시지: Lombok `@Builder` 초기값 관련 경고 11건, deprecated API와 unchecked operation 참고 메시지
- 테스트 결과 디렉터리: 없음. 테스트 메서드가 실행되지 않았음을 재확인했다.
- 비밀값: 읽기·출력·사용하지 않음
- 원본 팀 저장소·감사본 소스: 수정하지 않음
- 신규 `Pass`, `Fail`, `Blocked`: 없음
- 컴파일 검증: 성공. 제품 기능 테스트 상태와 분리한다.
- 사용자 IntelliJ 설정: Project SDK JDK 17, Language level 17, Gradle JVM `Project SDK 17`, Gradle Wrapper 사용을 스크린샷으로 확인했다.
- 사용자 직접 재현: IntelliJ에서 기준 컴파일을 실행해 `BUILD SUCCESSFUL`을 확인했다고 보고했다. 실행 주체는 `User`, 근거 유형은 `사용자 보고`이며 Codex 실행 결과와 구분한다.
- 3단계 상태: `완료 — 실행용 작업본·버전·기준 컴파일·사용자 재현·완료 승인 확인`

## 8. 사용자 재현 순서

1. 감사본이 아니라 별도 `QA-Lab\execution\TripFriend` 작업본을 사용한다.
2. `.git`, 과거 `qa`, 생성물, `application-secret.yml`, `application-prod.yml`이 실행용 작업본에 없는지 확인한다.
3. `java -version`, `node --version`, `npm --version`으로 현재 환경을 기록한다.
4. Backend에서 `.\gradlew.bat compileJava compileTestJava --no-daemon`을 실행한다.
5. `compileJava`와 `compileTestJava` 결과를 구분하고 테스트 결과 파일이 생성되지 않았는지 확인한다.
6. 컴파일 성공을 제품 테스트 `Pass`로 기록하지 않는다.
7. 테스트 프로필·H2·fixture 또는 Mock 구성은 후속 테스트 작성 전에 목적별로 추가하고, 서버·DB가 필요하면 별도 승인을 받는다.

### 이번 단계의 학습·실습 체크포인트 적용

- Codex가 작업 목적과 컴파일·테스트의 차이를 설명하고 승인된 범위에서 작업본과 Gradle 환경을 준비했다.
- 사용자는 IntelliJ에서 JDK 17과 Gradle JVM을 설정하고 기준 컴파일을 직접 재현했다.
- Codex 실행 결과와 사용자 실행 결과를 분리했으며 어느 컴파일 성공도 제품 테스트 `Pass`로 기록하지 않았다.
- 후속 단계에서도 `Codex 설명 → 승인된 준비 → 사용자 직접 실습 → 결과 공유 → Codex 검토 → 실행 주체와 상태 기록` 순서를 사용한다.

## 9. 3단계 완료 기준 확인

- 감사본과 실행용 작업본 경계 확인: 완료
- 비밀 설정을 제외한 안전한 작업본 준비: 완료
- 로컬 Java·Node·npm 버전 기록: 완료
- Backend `compileJava`·`compileTestJava` 기준 결과 기록: 완료
- P0용 H2·Mock 중심 최소 환경과 후속 외부 의존성 구분: 완료
- 사용자 재현 순서 문서화: 완료
- 사용자 3단계 완료 확인: 완료
