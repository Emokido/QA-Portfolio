# TF-BUG-007 — localhost 프런트 요청이 CORS preflight 403으로 차단됨

- 결함 상태: `Open — QA execution patch retest passed; product fix pending`
- 발견일: 2026-09-01
- 등록일: 2026-09-01
- QA 실행 사본 재검증일: 2026-09-01
- 기준 프로젝트: 스프린트 1 Java/Spring 완성본과 Next.js 프런트
- 기준 커밋: `e2431223ce2a18c8f944d544cba76b951ce0356d`
- 영향 기능: 로컬 프런트의 Place·Review API 조회와 커뮤니티 목록 표시
- 테스트 수준: 실제 localhost 서버·브라우저 API/UI flow
- 심각도: `Major`
- 수정 우선순위: 제품 담당자 결정 필요
- 재현성: 검증 지점에 도달한 브라우저 실행 1/1에서 재현
- 보고 주체: `Codex with user authorization`, 사용자 Chrome 직접 재현

> 이 결함은 동일한 로컬 백엔드 API가 curl과 Postman Desktop App에서 HTTP 200으로 성공하지만, Origin이 `http://localhost:3000`인 Chrome 요청에서는 OPTIONS preflight 403과 CORS error로 차단되고 커뮤니티 화면이 실제 리뷰 18개 대신 빈 결과를 표시한 결과를 근거로 등록한다.

## 1. 요약

로컬 Next.js 프런트 `http://localhost:3000/community`가 Spring Boot `http://localhost:8080`의 공개 Place·Review API를 호출하면 브라우저의 CORS preflight가 HTTP 403으로 거부된다. 실제 GET은 CORS error로 완료되지 않고, 프런트가 목록 API 오류를 빈 배열로 변환해 사용자에게 `검색 결과가 없습니다.`를 표시한다.

## 2. 추적 연결

`TF-REQ-007 → TF-COND-001 → TF-TC-001·049 → TF-S7-EXEC-003 → TF-BUG-007`

| 식별자 | 연결 내용 |
|---|---|
| TF-REQ-007 | 공개 리뷰 API 조회 계약 |
| TF-COND-001 | 비로그인 공개 조회 |
| TF-TC-001 | 공개 리뷰 목록 HTTP 200·code `200-5` |
| TF-TC-049 | 결과 없음은 HTTP 200·빈 목록 |
| TF-S7-EXEC-003 | 실제 `/community` 브라우저·Network·UI 흐름 |

## 3. 테스트 환경

- 백엔드: Spring Boot 3.2.4, Java 17, `stage7` 인메모리 H2, localhost 8080
- 프런트: Next.js 15.2.4 dev server, localhost 3000
- 브라우저: 사용자 Chrome DevTools Network
- 초기 데이터: BaseInitData 리뷰 18개·댓글 28개
- 비교 경로: curl·Postman Desktop App 공개 GET 성공
- 외부 MySQL·Redis·OAuth·메일·실계정·비밀값: 사용하지 않음

## 4. 재현 절차

1. 승인된 `stage7` 프로필로 백엔드를 8080에서 기동한다.
2. Next.js 프런트를 3000에서 기동한다.
3. Chrome DevTools Network를 열고 `http://localhost:3000/community`에 접속한다.
4. `/place`와 `/api/reviews?sort=newest&page=1` 요청을 확인한다.
5. preflight와 fetch 상태, 최종 화면을 비교한다.

## 5. 기대 결과

- Origin `http://localhost:3000`의 OPTIONS preflight가 허용된다.
- 공개 Place·Review GET이 HTTP 200으로 완료된다.
- 초기 리뷰 18개가 커뮤니티 목록에 표시된다.
- 실제 API 오류를 정상적인 빈 검색 결과로 오인하게 만들지 않는다.

## 6. 실제 결과

- `/place`: HTTP 403.
- `/api/reviews?sort=newest&page=1`: fetch `CORS error`.
- 동일 Review 요청의 preflight: HTTP 403.
- 화면은 API 실패에도 `검색 결과가 없습니다.`를 표시한다.
- 동일 백엔드에 대한 준비용 curl은 HTTP 200·code `200-5`·리뷰 18개, Postman Desktop App은 HTTP 200·4/4 Passed였다.

## 7. 증거

| 실행 | 실행자 | 결과 | 증거 |
|---|---|---|---|
| TF-S7-EXEC-003 | User | Review CORS error·preflight 403·Place 403·빈 결과 UI | 내부 원본 보존·공개 제외(로컬 경로 노출) |
| TF-S7-RETEST-001 | User | QA execution patch 후 Place·Review 200·리뷰 카드 표시·CORS 오류 없음 | 내부 원본 보존·공개 제외(로컬 경로 노출) |

- 수정 전 파일: 194,350바이트, SHA-256 `B4D55CDB2FF314A88B014635CA17E110F8262F6C1CCBBBE24347B0BB9C838D20`.
- 수정 후 파일: 193,158바이트, SHA-256 `50B0A56347BA73F8490EBEA285C626B8CB6396638D9CDDAF25E98D67BD47B0AE`.
- 미존재 키워드 검색 후 화면은 채팅에서 요청 HTTP 200과 정상 빈 결과 UI 판정에 사용했지만, 승인된 추가 증거 한도에 따라 별도 증거 파일로 복제하지 않았다.

## 8. 영향과 심각도 근거

- 로컬 프런트에서 Place·Review 공개 조회가 차단돼 커뮤니티 주요 화면을 정상 사용할 수 없다.
- API 오류가 빈 결과 UI로 표현되어 사용자가 실제 장애를 데이터 없음으로 오인한다.
- 브라우저 기반 API·UI 통합 테스트와 로컬 개발 확인을 차단한다.
- 배포 Origin `https://tripfriend.vercel.app`의 실제 동작과 운영 사용자 영향은 이번 실행에서 확인하지 않았다.
- 로컬 통합 기능 전체 차단과 오인 UI를 근거로 `Major`를 제안하되 최종 우선순위는 제품 담당자가 결정한다.

## 9. 코드 상관관계와 추정 원인

`SecurityConfig.corsConfigurationSource()`가 다음 두 호출을 순서대로 수행한다.

```java
configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
configuration.setAllowedOrigins(Arrays.asList("https://tripfriend.vercel.app"));
```

두 번째 setter가 첫 번째 목록을 덮어써 최종 허용 Origin에 localhost가 남지 않는 것이 직접 원인으로 판단된다. 별도로 프런트 `getReviews()`는 fetch 예외를 빈 배열 결과로 변환해 장애를 빈 검색 결과처럼 표시한다.

## 10. 승인된 QA 실행 수정

원본·감사 저장소가 아닌 `QA-Lab/execution/TripFriend` 작업본에서만 두 Origin을 하나의 목록으로 합쳤다.

```java
configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:3000",
        "https://tripfriend.vercel.app"
));
```

이 수정은 후속 API·UI 흐름을 열기 위한 QA execution patch다. 수정 후 성공해도 원본 기준 최초 결과를 Pass로 바꾸지 않는다.

## 11. 수정 후 재검증 조건

- localhost Origin의 OPTIONS preflight가 403이 아닌지 확인한다.
- Review·Place GET이 HTTP 200인지 확인한다.
- Console에서 해당 CORS 오류가 사라지는지 확인한다.
- 커뮤니티에 초기 리뷰 카드가 표시되는지 확인한다.
- 미존재 키워드 검색에서만 정상 빈 결과 UI가 표시되는지 확인한다.
- 운영 Origin 허용이 목록에 계속 유지되는지 정적으로 확인한다.

### 재검증 결과

- `/place`: HTTP 200.
- `/api/reviews?sort=newest&page=1`: HTTP 200, 리뷰 카드 표시.
- 미존재 키워드 검색: HTTP 200, `검색 결과가 없습니다.` 정상 표시.
- 사용자 Console 확인: CORS 오류 없음.
- 실패하는 preflight는 재발하지 않았고 브라우저 fetch 응답이 정상 완료됐다.
- QA 실행 사본 판정: `Pass`.

## 12. 현재 한계와 금지 주장

- 현재 증거는 localhost Chrome 한 환경의 실행이다.
- 배포 환경과 실제 운영 Origin을 실행하지 않았다.
- 프런트 오류 은폐 동작은 별도 결함 후보이며 이번 patch에서 수정하지 않았다.
- QA 실행 작업본 patch를 원본 제품의 수정 완료로 주장하지 않는다.
- TF-S7-RETEST-001은 QA 실행 사본에서 `Pass`다. 다만 원본·감사 저장소에는 수정하지 않았으므로 제품 결함 상태는 `Open`이며 제품 수정 완료를 주장하지 않는다.
