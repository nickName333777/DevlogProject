package com.devlog.project.member.controller;


import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.devlog.project.member.model.service.EmailServiceJpa;

import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/sendEmail")
@SessionAttributes("authKey") // 인증번호를 DB가 아니라 session에 올려서 쓸때 필요.
@RequiredArgsConstructor 
public class EmailController {

	private final EmailServiceJpa emailServiceJpa; // Jpa

	@GetMapping("/signUp")
	@ResponseBody
	public int signUp(@RequestParam("email") String email) {
		return emailServiceJpa.signUp(email, "회원 가입") ? 1 :0;
	}


	@GetMapping("/checkAuthKey")
	@ResponseBody
	public int checkAuthKey(@RequestParam Map<String, Object> paramMap){
		return emailServiceJpa.checkAuthKey(paramMap); // DB 조회 (select)
	}

}
