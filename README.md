# DevlogProject

KDT  빅데이터 분석 & Java 웹개발 과정 Final 프로젝트 (Team-ZeroBoost github link: https://github.com/Team-ZeroBoost/Devlog)

- 동기: 기존 개발자 블로그 플랫폼들이 정보전달과 기술 기록 기능에 주로 치중하고 있는 단점을 보완하여 전문적인 개발내용의 블로깅 기능 뿐아니라 커뮤니티 기능과 수익창출 모델 기능 부분을 보강하여 개발자 커뮤니티로 한단계 발전하고자 했다.<br>

- 프로젝트명:  Devlog  프로젝트<br>

- 수행 기간: 25.11.27 ~ 26.01.12 (1.5개월)<br>

- 수행 인원: 총 5명이 블로그 / 자유게시판, 소셜 기능 (팔로우, 구독), 댓글, 공감 , 회원가입 / 로그인 / 소셜 로그인, 마이페이지 / 내 활동 관리, 회원 레벨 시스템, 회원 신고, 실시간 채팅, 알림 시스템, 온라인 활동중 표시, 외부 결제 연동을 통한 결제 기능들을 각각 나눠 맡아, 맡은 부분의 frontend&backend 구현. <br>

- 기술스택(개발환경, 언어 등): Spring Boot 3.5.7, VS Code, Gradle, Docker, ELK Stack (Elasticsearch, Logstash, Kibana), Java 17, Python, HTML / CSS / JavaScript, Oracle DB, JPA, HikariCP, MyBatis, STOMP(WebSocket), Toast UI, Lombok, Selenium, PortOne 결제 API, OpenAI API, Kakao Developers API <br>

- **담당 부분: 로그인, 회원가입, 소셜 로그인, 자유게시판, 챗봇 - 로그인/회원가입, 카카오 소셜 로그인, 자유게시판과 댓글 목록/상세조회/새글작성/수정/삭제 CRUD 구현, 자유게시판 글쓰기/수정 챗봇 도우미구현** <br>
- **담당 부분 데모 영상**

https://github.com/user-attachments/assets/0255a6f4-1a4e-4262-8b37-7d327bf8c04b

- 프로젝트 다운로드/실행시 유의사항:
1. 첨부된 config_properties_example.txt 처럼 유효한 키 값들이 설정되어야 합니다.
2. Project의 Java Compiler 설정에서 methed parameter 저장(via reflection)이 아래 처럼 활성화 되어 있어야 합니다.
- <img width="735" height="730" alt="JavaCompilerMethodParaOptionCheck" src="https://github.com/user-attachments/assets/3c463ee1-8def-458e-9865-a5c85603fca9" />
