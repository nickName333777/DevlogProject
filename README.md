# DevlogProject

KDT  빅데이터 분석 & Java 웹개발 과정 Final 프로젝트 (Team-ZeroBoost github link: https://github.com/Team-ZeroBoost/Devlog)

- 동기: 기존 개발자 블로그 플랫폼들이 정보전달과 기술 기록 기능에 주로 치중하고 있는 단점을 보완하여 전문적인 개발내용의 블로깅 기능 뿐아니라 커뮤니티 기능과 수익창출 모델 기능 부분을 보강한 개발자 커뮤니티 사이트를 개발하고자 한다.<br>

- 프로젝트명:  Devlog  프로젝트<br>

- 수행 기간: 25.11.27 ~ 26.01.12 (1.5개월)<br>

- 수행 인원: 총 5명이 블로그 / 자유게시판, 소셜 기능 (팔로우, 구독), 댓글, 공감 , 회원가입 / 로그인 / 소셜 로그인, 마이페이지 / 내 활동 관리, 회원 레벨 시스템, 회원 신고, 실시간 채팅, 알림 시스템, 온라인 활동중 표시, 외부 결제 연동을 통한 결제 기능들을 각각 나눠 맡아, 맡은 부분의 frontend&backend 구현. <br>

- 기술스택(개발환경, 언어 등): Spring Boot 3.5.7, VS Code, Gradle, Docker, ELK Stack (Elasticsearch, Logstash, Kibana), Java 17, Python, HTML / CSS / JavaScript, Oracle DB, JPA, HikariCP, MyBatis, STOMP(WebSocket), Toast UI, Lombok, Selenium, PortOne 결제 API, OpenAI API, Kakao Developers API <br>

- **담당 부분: 로그인/회원가입(smtp 인증), 카카오 소셜 로그인, 자유게시판과 댓글 목록/상세조회/새글작성/수정/삭제 CRUD 구현, 자유게시판 글쓰기/수정 챗봇 도우미구현** <br>
- **담당 부분 데모 영상**

https://github.com/user-attachments/assets/0255a6f4-1a4e-4262-8b37-7d327bf8c04b

- 프로젝트 다운로드/실행시 유의사항:
1. 첨부된 config_properties_example.txt 에서 요구되는 유효한 키 값들이 설정된 파일이 다음 경로: Devlog/src/main/resources/config.properties 로 저장되어야 함.

2. Project의 Java Compiler 설정에서 methed parameter 저장(via reflection)이 아래 처럼 활성화 되어야 함.
<img width="735" height="730" alt="JavaCompilerMethodParaOptionCheck" src="https://github.com/user-attachments/assets/3c463ee1-8def-458e-9865-a5c85603fca9" />

3. 카카오 소셜로그인과 챗봇구현을 위한 키 설정은 아래와 같이 Run configurations의 환경변수 탭에 환경변수로 설정한다.
<img width="1336" height="868" alt="image" src="https://github.com/user-attachments/assets/e930b6e6-a46d-4bf7-9279-5492951d588f" />
4. 오라클 데이터 베이스는 관리자 계정에서 아래 처럼 사용자 계정을 생성한 후, 사용자 계정으로 로그인 하여 "Devlog 테스트용 sql문.sql" SQL 스크립트를 실행하여 각 테이블을 생성하고, 테스트용 dummy data를 삽입하여야 한다.<br>

   - [관리자 계정 생성 구문] <br>
   ALTER SESSION SET "_ORACLE_SCRIPT" = TRUE; <br>
   - 계정 생성 <br>
   CREATE USER your_user_name IDENTIFIED BY your_user_pwd;
   - 권한 부여 <br>
    GRANT CONNECT, RESOURCE, CREATE VIEW TO your_user_name;
   - 객체 생성 공간 할당 <br>
    ALTER USER your_user_name DEFAULT TABLESPACE SYSTEM QUOTA UNLIMITED ON SYSTEM; 
