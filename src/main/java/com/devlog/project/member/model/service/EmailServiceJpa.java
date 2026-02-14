package com.devlog.project.member.model.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devlog.project.member.model.entity.Auth;
import com.devlog.project.member.model.repository.EmailRepository;

import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service // 비즈니스 로직 처리 + bean 등록
@RequiredArgsConstructor
public class EmailServiceJpa {

    private final EmailRepository emailRepository;
	private final JavaMailSender mailSender;

	private String fromEmail = "yypark.rok@gmail.com"; // 발송자
	private String fromUsername = "수업용프로젝트";

	public String createAuthKey() { // 6자리 난수 생성 함수
		String key = "";
		for(int i=0 ; i< 6 ; i++) { // 6바퀴 돈다

			int sel1 = (int)(Math.random() * 3); // 0:숫자 / 1,2:영어
			              // 0.0 <= Math.random() < 1

			if(sel1 == 0) { // 0:숫자

				int num = (int)(Math.random() * 10); // 0~9
				key += num;

			}else {

				char ch = (char)(Math.random() * 26 + 65); // A~Z

				int sel2 = (int)(Math.random() * 2); // 0:소문자 / 1:대문자

				if(sel2 == 0) { // 0:소문자
					ch = (char)(ch + ('a' - 'A')); // 소문자로 변경
				}

				key += ch;
			}

		}
		return key;
	}


	@Transactional // 이메일 인증은 하나의 트랜잭션(DB 삽입)
	public boolean signUp(String email, String title) { // JPA: SaveOrUpdateAuthKey

		//6자리 난수 인증번호 생성
		String authKey = createAuthKey();
		
		// 1) DB에 저장
		try {
			
			// 이메일 중복 체크 
			Optional<Auth> optionalAuth = emailRepository.findByEmail(email);
			if (emailRepository.existsByEmail(email)) { // 이미 존재하는 이메일이면 code (=authKey) 업데이트
				Auth authEmail = optionalAuth.get();
				authEmail.setCode(authKey);
				emailRepository.save(authEmail);            //  
				log.info("기존 인증번호 업데이트 - email: {}", email);
			} else {
				// 새 Auth Entity 생성 
				Auth authEmail= new Auth(authKey, email); // 객체가 완성된상태로 생성되어야 할 때(인증용 엔티티, 상태값 반드시 있어야 하는 경우)
				emailRepository.save(authEmail);
				log.info("새 인증번호 생성 - email: {}", email);
			}		
			
		
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("### signUp Exception 발생[ DB저장/수정시]");
			return false;
		}		
		
		
		// 2) 이메일 발송
		try {
			//인증메일 보내기
			MimeMessage mail = mailSender.createMimeMessage(); // javax.mail.internet.MimeMessage;
			// 제목
			String subject = "[Board Project]"+title+" 인증코드"; //  yypark.rok@gmail.com에서 받는 이메일 제목을 바꿀수 있다
			// 문자 인코딩
			String charset = "UTF-8";
			// 메일 내용
			String mailContent 
			= "<p>Board Project "+title+" 인증코드입니다.</p>"
					+ "<h3 style='color:blue'>" + authKey + "</h3>";
			// 송신자(보내는 사람) 지정
			mail.setFrom(new InternetAddress(fromEmail, fromUsername));
			// 수신자(받는사람) 지정
			mail.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
			// 이메일 제목 세팅
			mail.setSubject(subject, charset);
			// 내용 세팅
			mail.setText(mailContent, charset, "html"); //"html" 추가 시 HTML 태그가 해석됨
			mailSender.send(mail);
		} catch (Exception e) {
			e.printStackTrace();
			return false; // JPA
		}			
		
		// 여기 까지 도달했으면, DB저장과 이메일발송 모두 성공
		return true; // JPA
	}


	@Transactional(readOnly = true)
	public int checkAuthKey(Map<String, Object> paramMap) {
		return emailRepository.countByCodeAndEmail((String)paramMap.get("inputKey"), (String)paramMap.get("email"));
	}          
    
    
}
