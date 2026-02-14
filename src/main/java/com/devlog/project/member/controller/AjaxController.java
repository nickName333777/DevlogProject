package com.devlog.project.member.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.devlog.project.member.model.service.AjaxServiceJpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Controller // 요청과 응답 제어 + bean 등록
@RequiredArgsConstructor 
public class AjaxController {
	
	private final AjaxServiceJpa ajaxServiceJpa; //
	
	
	// 이메일 중복 유효성 검사
	@GetMapping("/dupCheck/email") 
	@ResponseBody 	
	public int dupCheckEmail(String email){
		
		log.info("[ 이메일 중복 유효성 검사 ] email 출력 : {}", email);
		return ajaxServiceJpa.dupCheckEmail(email); // JPA
	}
		
	// 닉네임 중복 유효성 검사
	@GetMapping(value="/dupCheck/nickname")
	@ResponseBody
	public int dupCheckNickname(String nickname) {
		log.info("[ 닉네임 중복 유효성 검사 ] nickname 출력 : {}", nickname);
		return ajaxServiceJpa.dupCheckNickname(nickname);
	}	
	
	// 관리자 승인 코드 유효성 검사
	@GetMapping(value="/checkCode/adminCode")
	@ResponseBody
	public int checkAdminCode(String adminCode) {
		log.info("[ 관리자 승인코드 유효성 검사 ] adminCode 출력 : {}", adminCode);
		return adminCode.equals("devlog1234")? 1:0;
	}		

}
