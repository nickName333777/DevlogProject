package com.devlog.project.chatting.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.devlog.project.chatting.dto.ChattingDTO;
import com.devlog.project.chatting.dto.MentionDTO;
import com.devlog.project.chatting.dto.ChattingDTO.ChattingListDTO;
import com.devlog.project.chatting.service.ChattingService;
import com.devlog.project.common.utility.Util;
import com.devlog.project.member.model.dto.MbMember;
import com.devlog.project.member.model.dto.MemberLoginResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatRestController {
	
	private final ChattingService chattingService;
	
	// 채팅방 목록 조회
	@GetMapping("/devtalk/chatList")
	public String selectChatList(
			Model model,
			@SessionAttribute("loginMember") MemberLoginResponseDTO loginMember,
			@RequestParam String query){
		
		// System.out.println("검색어 확인 : " + query);
		
		
		List<ChattingDTO.ChattingListDTO> chatList = chattingService.selectChatList(loginMember.getMemberNo(), query);
		
		for (ChattingListDTO dto : chatList) {
			dto.setFormatTime(Util.formatChatTime(dto.getLastMessageAt()));
			
		}
		
		// log.info("chatList = {}", chatList);
		
		model.addAttribute("chatList", chatList);
		
		return "chatting/chatting ::#roomList";
	}
	
	
	// 회원 초대할 팔로우 목록 조회
	@GetMapping("/devtalk/followSelect")
	public String selectFollowList(
			@RequestParam(value = "roomNo", required = false) Long roomNo,
			// 세션 로그인 멤버
			@SessionAttribute("loginMember") MemberLoginResponseDTO loginMember,
			Model model
			) {
		
		// System.out.println("팔로우 목록 조회 roomNo 확인 : " + roomNo);
		
		List<ChattingDTO.FollowListDTO> followList = chattingService.selectFollowList(loginMember.getMemberNo(), roomNo);
		
		// log.info("팔로우 리스트 조회 결과 : {} ", followList);
		
		model.addAttribute("followList", followList);
		
		if(roomNo == null) {
			
			return "chatting/chatting :: #chatFollowList";
		} else {
			return "chatting/chatting :: #inviteUserList";
		}
	}
	
	
	// 개인 채팅방 생성
	@PostMapping("/devtalk/create/private")
	@ResponseBody
	public Long privateCreate(
			@RequestBody Long targetMemberNo,
			@SessionAttribute("loginMember") MemberLoginResponseDTO loginMember
			) {
		Long myMemberNo = loginMember.getMemberNo();
		
		// log.info("myMemberNo={}, targetMemberNo={}", myMemberNo, targetMemberNo);
		
		return chattingService.privateCreate(myMemberNo, targetMemberNo);
	}
	
	
	// 그룹 채팅방 생성
	@PostMapping("/devtalk/create/group")
	@ResponseBody
	public Long gropuCreate(
			@ModelAttribute ChattingDTO.GroupCreateDTO group,
			@SessionAttribute("loginMember") MemberLoginResponseDTO loginMember
			) throws IOException {
		
		log.info("파라미터 확인 group : {}", group);
		
		Long loginMemberNo = loginMember.getMemberNo();
		
		group.getMemberNo().add(0, loginMemberNo);
		
		
		
		return chattingService.groupCreate(group, loginMemberNo);
	}
	
	
	// 해당 채팅방 정보 조회
	@GetMapping("/devtalk/roomInfoLoad")
	public String roomInfoLoad(
			@RequestParam("roomNo") Long roomNo,
			@SessionAttribute("loginMember") MemberLoginResponseDTO loginMember,
			Model model) {
		
		Long memberNo = loginMember.getMemberNo();
		
		ChattingDTO.RoomInfoDTO roomInfo = chattingService.roomInfoLoad(roomNo, memberNo);
		
		model.addAttribute("roomInfo", roomInfo);
		
		boolean isOwner = chattingService.isOwner(roomNo, memberNo);
		
		model.addAttribute("isOwner", isOwner);
		
		return "chatting/chatting ::#chatting-space";
		
	}
	
	
	// 채팅방 나가기
	@GetMapping("/devtalk/roomExit")
	public ResponseEntity<Void> roomExit(
			@RequestParam("roomNo") Long roomNo,
			@SessionAttribute("loginMember") MemberLoginResponseDTO loginMember
			){
		
		// System.out.println("채팅방 나가기 방 버호 파라미터 확인 : " + roomNo);
		chattingService.roomExit(roomNo, loginMember.getMemberNo());
		
		
		
		return ResponseEntity.ok().build();
	}
	
	
	// 채팅방 초대
	@PostMapping("/devtalk/inviteChat")
	public ResponseEntity<Void> userInvite(
			@RequestBody Map<String, Object> paramMap
			) {
			
		System.out.println("초대 파라미터 확인 : " + paramMap);
		
		chattingService.userInvite(paramMap);
		
		
		return ResponseEntity.ok().build();
		
	}
	
	// 멘션 후보 조회
	@GetMapping("/devtalk/mention")
	public ResponseEntity<List<MentionDTO>> mentionUsers(
			@RequestParam("roomNo") Long roomNo,
			@RequestParam("keyword") String keyword,
			@SessionAttribute("loginMember") MemberLoginResponseDTO loginMember
			) {
		
		Long memberNo = loginMember.getMemberNo();
		
		List<MentionDTO> resp = chattingService.mentionUsersSelect(roomNo, keyword, memberNo);
		
		System.out.println("멘션 후보 확인 : "+ resp);
		
		return ResponseEntity.ok(resp);
	}
	
	
	// 그룹 채팅방 이름 변경
	@PostMapping("/devtalk/roomName")
	public ResponseEntity<Void> roomNameChange(
			@RequestBody Map<String, Object> paramMap
			) {
		
		chattingService.roomNameChange(paramMap);
		
		
		return ResponseEntity.ok().build();
	}
	
	@PostMapping("/devtalk/pinUpdate")
	public ResponseEntity<Void> pinUpdate(
			@RequestBody Map<String, Object> paramMap
			){
		
		chattingService.pinUpdate(paramMap);
		
		return ResponseEntity.ok().build();
	}
	
	
}	
