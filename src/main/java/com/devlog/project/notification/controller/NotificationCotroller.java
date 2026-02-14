package com.devlog.project.notification.controller;


import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.devlog.project.member.model.dto.MemberLoginResponseDTO;
import com.devlog.project.notification.dto.NotifiactionDTO;
import com.devlog.project.notification.dto.NotifiactionDTO.ResponseDTO;
import com.devlog.project.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class NotificationCotroller {
	
	private final NotificationService service;
	
	@GetMapping("/notiCount")
	@ResponseBody
	public Long notiCount(
			@SessionAttribute("loginMember") MemberLoginResponseDTO loginMember
			) {
				
		return service.notiCount(loginMember.getMemberNo());
				
	}
	
	@GetMapping("/notification/selectList")
	public String selectList(
			@RequestParam("type") String type,
			@SessionAttribute("loginMember") MemberLoginResponseDTO loginMember,
			Model model
			) {		
			
		List<NotifiactionDTO.ResponseDTO> list = service.selectList(type, loginMember.getMemberNo());
		
		
		model.addAttribute("notiList", list);
		
		System.out.println(list);
		return "common/header ::#alarm-panel";
	}
	
	
	@PostMapping("/notification/click/{notiNo}")
	@ResponseBody
	public String notificationClick(
			@PathVariable("notiNo") Long notiNo
			
			) {
			
		
		String url = service.readAndGet(notiNo);
		
		return url;
	}
	
	@DeleteMapping("/notification/{notiNo}")
	@ResponseBody
	public void deleteNotification(
			@PathVariable("notiNo") Long notiNo
			) {
		
		service.deleteNoti(notiNo);
	}
	
	
	// 알림 전체 삭제
	@DeleteMapping("/notification/allDelete")
	@ResponseBody
	public void deleteAllNotification(
			@SessionAttribute("loginMember") MemberLoginResponseDTO loginMember
			) {
		
		service.deleteAllNotification(loginMember.getMemberNo());
		
		
	}
	
	@PostMapping("/notification/allRead")
	@ResponseBody
	public void readAllNotification(
			@SessionAttribute("loginMember") MemberLoginResponseDTO loginMember
			) {
		
		service.readAllNotification(loginMember.getMemberNo());
		
		
	}
}
