package com.devlog.project.notification.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.devlog.project.member.model.dto.MemberLoginResponseDTO;
import com.devlog.project.notification.service.SseService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SSEController {
	
	
	private final SseService sseService;
	
	// 멀티스레드 환경에서 데이터 동기화 할 수 있는 Map 각 유저 끼리 같은 map을 공유
	private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
	
	// 클라이언트 연결 요청 처리
	@GetMapping("/sse/connect")
	public SseEmitter sseConnect(@SessionAttribute("loginMember") MemberLoginResponseDTO loginMember) {
		
		
		return sseService.connect(loginMember.getMemberNo());
	}
}
