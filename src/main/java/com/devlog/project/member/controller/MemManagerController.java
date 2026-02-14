package com.devlog.project.member.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.devlog.project.member.model.dto.MemberInfoResponseDTO;
import com.devlog.project.member.model.service.MemManagerRetrieveService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Controller 
@RequiredArgsConstructor 
@RequestMapping("/api/manager")  // GET and POST 다 처리
public class MemManagerController {

	private final MemManagerRetrieveService service;
	
	// 관리자 회원 정보 조회 화면 이동
	@GetMapping({"", "/"})
	public String login(HttpServletRequest request, Model model) {  
		    	    
	    return "manager/manager-customer";
	}	
	
	
	// 이메일로 회원 정보조회
	@PostMapping(value = "/selectMemberbyMemberEmail", produces = "application/json; charset=UTF-8") // 응답 데이터에 한글 있으므로 produces = "application/json; charset=UTF-8" (단, text아니라 json으로 응답이 돌아옴)
	@ResponseBody // java 데이터 -> JSON, TEXT로 변환 + 비동기 요청한 곳으로 응답
	public MemberInfoResponseDTO selectMemberByMemberEmail(
			@RequestBody Map<String, Object> paraMap
			) { 
		
		String email = (String)paraMap.get("email");
		
		MemberInfoResponseDTO mem = service.selectMemberByMemberEmail(email);
		System.out.println("member-info by email : " + mem);
		return mem;
	}
	
	
	// 닉네임으로 회원 정보조회
	@PostMapping(value = "/selectMemberByMemberNickname", produces = "application/json; charset=UTF-8") 
	@ResponseBody 
	public MemberInfoResponseDTO selectMemberBymemberNickname(
			@RequestBody Map<String, Object> paraMap
			) { 
		
		String nickname = (String)paraMap.get("nickname"); 
		
		MemberInfoResponseDTO mem = service.selectMemberByMemberNickname(nickname);
		System.out.println("member-info by nickname: " + mem);
		return mem;
	}	
	

	// 회원번호로 회원 정보조회
	@PostMapping(value = "/selectMemberByMemberNo") 
	@ResponseBody 
	public MemberInfoResponseDTO selectMemberBymemberNo(
			@RequestBody Map<String, Object> paraMap
			) { 
		
		Long memberNo = ((Number)paraMap.get("memberNo")).longValue();
		
		MemberInfoResponseDTO mem = service.selectMemberByMemberNo(memberNo);
		System.out.println("member-info by memberNo : " + mem);
		return mem;
	}	
	
}
