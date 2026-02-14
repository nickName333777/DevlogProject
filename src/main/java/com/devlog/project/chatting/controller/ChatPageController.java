package com.devlog.project.chatting.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatPageController {
	
	
	// devtalk 화면 전환
	@GetMapping("/devtalk")
	public String chatPage() {
		
		
		return "/chatting/chatting";
	}

}
