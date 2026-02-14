package com.devlog.project.chatting.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.devlog.project.common.utility.Util;
import com.devlog.project.chatting.dto.MessageDTO;
import com.devlog.project.chatting.dto.MessageDTO.ChatMessageResponse;
import com.devlog.project.chatting.dto.ParticipantDTO.ChatListUpdateDTO;
import com.devlog.project.chatting.dto.QueryMessageResponseDTO;
import com.devlog.project.chatting.service.ChattingService;
import com.devlog.project.chatting.service.MessageService;
import com.devlog.project.member.model.dto.MemberLoginResponseDTO;
import com.devlog.project.member.model.entity.Member;
import com.devlog.project.member.model.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MessageController {
	
	private final SimpMessagingTemplate templete;
	
	private final MessageService service;
	
	private final ChattingService chatService;
	
	private final MemberRepository memberRepository;
	
	
	// 현재 채팅방에 참여중인 회원 목록을 담을 Map
	// key = roomNo, value = 해당 채팅방에 접속한 memberNo 집합
	// ConcurrentHashMap + newKeySet() → 멀티스레드 환경에서도 안전하게 관리
	private final Map<Long, Set<Long>> roomViewers = new ConcurrentHashMap<>();
	
	// 유저 채팅방 구독 시 Map에 추가
	@MessageMapping("/chat.enter")
	public void enter(@Payload MessageDTO.messageReadRequest req
	                  ) {
		

	    roomViewers
	        .computeIfAbsent(req.getRoomNo(), k -> ConcurrentHashMap.newKeySet())
	        .add(req.getMemberNo());
	    // computeIfAbsent(K key, Function) : key 가 없을 경우 값을 생성
	    
	    
	    Long lastReadMessageNo = chatService.selectLastReadNo(req.getRoomNo(), req.getMemberNo());
	    Integer roomLastMessageNo = service.selectLastMessageNo(req.getRoomNo());
		
	    chatService.updateLastRead(req.getRoomNo(), req.getMemberNo());
	    
		// List<Long> messageNos = service.searchMessageList(null)
		
		System.out.println("업데이트 전 마지막 읽은 메세지 : " + lastReadMessageNo);
		System.out.println("현재 채팅방 마지막 메세지 : " + roomLastMessageNo);
		
		Map<String, Object> result = new HashMap<>();
		
		
		
		 result.put("type", "READ"); 
		 result.put("LastReadNo", lastReadMessageNo);
		 result.put("roomLastReadNo", roomLastMessageNo);
		 result.put("memberNo", req.getMemberNo());
		 
		 
		 templete.convertAndSend( "/topic/room/" + req.getRoomNo(), result );
		 
	}
	
	
	@MessageMapping("/chat.leave")
	public void leave(@Payload MessageDTO.messageReadRequest req
	                  ) {

	    Set<Long> viewers = roomViewers.get(req.getRoomNo());
	    if (viewers != null) {
	        viewers.remove(req.getMemberNo());
	    }
	    
	    Long count = chatService.countParticipant(req.getRoomNo());
	    
	    Map<String, Object> respMap = new HashMap<>();
	    
	    respMap.put("type", "LEAVE");
	    
	    respMap.put("count", count);
	    
	    templete.convertAndSend(
	    		"/topic/room/" + req.getRoomNo(), respMap
	    		);
	    
	    
	}
	
	// 
	@MessageMapping("/chat.send")
	public void send(@Payload MessageDTO.ChatMessage msg) {
		// @Payload : STOMP 메시지의 body 부분을 이 파라미터에 직접 매핑
		
		log.info("보낸 메세지 확인 : {}", msg);
		
		
		ChatMessageResponse res = service.insertMsg(msg);
		
		
		if(res.getContent().contains("@")) {
			
			chatService.processMention(res);
		}
		
		int totalViewers = msg.getTotalCount();
		int onlineViewers = roomViewers.getOrDefault(msg.getChatRoomNo(), Set.of()).size();
		
		res.setUnreadCount(totalViewers-onlineViewers);
		templete.convertAndSend(
				"/topic/room/" + res.getRoomNo(),
				res
				);
		
		
		
		List<Long> memberNos = chatService.selectUsers(res.getRoomNo());
		log.info("회원 번호 조회 결과 : {}", memberNos);
		
		
		for (Long memberNo : memberNos) {
			
			ChatListUpdateDTO updateDto = new ChatListUpdateDTO(); 
			
			updateDto.setLastMessage(res.getContent());
			updateDto.setSendtime(res.getSendtime());
			updateDto.setRoomNo(res.getRoomNo());
			updateDto.setUnreadCount(service.countUnreadMsg(memberNo, res.getRoomNo()));
			
			log.info("채팅방 업데이트용 DTO 확인 : {}", updateDto);
			templete.convertAndSend( "/topic/chat-list/" + memberNo,
					updateDto);
		}
		
		 
		log.info("msg 응답 확인 : {}", res);
		
	}
	
	
	@MessageMapping("/chat.read")
	public void messageRead(@Payload MessageDTO.messageReadRequest req) {
		
		// System.out.println("req = " + req);
		
		chatService.updateLastRead(req.getRoomNo(), req.getMemberNo());
		
		
		
		
		
	
		
	}
	
	
	
	@PostMapping("/devtalk/message/edit")
	@ResponseBody
	public void messageUpdate(
			@RequestBody MessageDTO.MessageEdit edit
			){
		
		
		
		log.info("수정 파라미터 확인 : {}", edit);
		
		service.editMessage(edit);
		
	}
	
	
	
	// 이미지 전송
	@PostMapping("/devtalk/send-img")
	public ResponseEntity<Void> sendImage(
			@ModelAttribute MessageDTO.ImageRequest dto
			) throws IllegalStateException, IOException {
		
		

		System.out.println("방 번호, 회원 번호 확인 : " + dto.getMemberNo() + dto.getRoomNo());
		
		ChatMessageResponse res = service.sendImg(dto);
		
		
		// unread 계산
		int totalViewers = dto.getTotalCount();
		int onlineViewers = roomViewers.getOrDefault(dto.getRoomNo(), Set.of()).size();
		res.setUnreadCount(totalViewers - onlineViewers);
		
		templete.convertAndSend(
		        "/topic/room/" + res.getRoomNo(),
		        res
		);
		
		
		List<Long> memberNos = chatService.selectUsers(res.getRoomNo());
		log.info("회원 번호 조회 결과 : {}", memberNos);
		
		
		for (Long memberNo : memberNos) {

		    ChatListUpdateDTO updateDto = new ChatListUpdateDTO();

		    updateDto.setLastMessage(res.getContent());
		    updateDto.setSendtime(res.getSendtime());
		    updateDto.setRoomNo(res.getRoomNo());
		    updateDto.setUnreadCount(service.countUnreadMsg(memberNo, res.getRoomNo()));

		    log.info("채팅방 업데이트용 DTO 확인 : {}", updateDto);
		    
		    templete.convertAndSend(
		            "/topic/chat-list/" + memberNo,
		            updateDto
		    );
		}
		
		
		return ResponseEntity.ok().build();
		
	}
	
	
	@GetMapping("/devtalk/delete-msg")
	public ResponseEntity<Void> deleteMessage(
			@RequestParam("messageNo") Long messageNo) {
		
		
		service.deleteMessage(messageNo);
		
		
		return ResponseEntity.ok().build();
		
	}
	
	
	@PostMapping("/devtalk/sendEmoji")
	public ResponseEntity<Void> sendEmoji(
			@RequestBody Map<String, Object> paramMap,
			@SessionAttribute("loginMember") MemberLoginResponseDTO loginMember
			) {
		
		System.out.println("이모지 파라미터 퐉인 : " + paramMap);
		
		paramMap.put("memberNo", loginMember.getMemberNo());
		
		service.sendEmoji(paramMap);
		
		
		return ResponseEntity.ok().build();
	}
	
	
	// 검색어 일치하는 메세지 목록 조회
	
	@PostMapping("/devtalk/searchMessageList")
	public String searchMessageList(
			@RequestBody Map<String, Object> paramMap,
			Model model
			) {
		
		List<QueryMessageResponseDTO> resp = service.searchMessageList(paramMap);
		
		
		for (QueryMessageResponseDTO dto : resp) {
			
			dto.setFormatTime(Util.formatChatTime(dto.getSendTime()));
			
		}
		System.out.println("메시지 목록 확인 : " + resp);
		
		model.addAttribute("msgList", resp);
		
		return "chatting/chatting :: #searchMsgArea";
	}
	
	@MessageMapping("/chat.typing")
	public void typing(
			@RequestBody Map<String, Object> paramMap
			) {
			
		Long memberNo = ((Number)paramMap.get("memberNo")).longValue();
		paramMap.put("type", "Typing");
		Member member = memberRepository.findById(memberNo).orElseThrow();
		
		String memberNickname = member.getMemberNickname();
		
		paramMap.put("memberNickname", memberNickname);
		
		System.out.println(paramMap);
		
		templete.convertAndSend(
				"/topic/room/" + paramMap.get("roomNo"),
				
				paramMap
				);
		
	}
	
	
	
	@EventListener
	public void onDisconnect(SessionDisconnectEvent event) {

	    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

	    Principal principal = accessor.getUser();

	    if (principal == null) {
	        System.out.println("[DISCONNECT] principal is null");
	        return;
	    }
	    String memberEmail = principal.getName();
	    Member member = memberRepository.findMemberNoByMemberEmail(memberEmail);
	    
	    Long memberNo = member.getMemberNo();

	    for (Map.Entry<Long, Set<Long>> entry : roomViewers.entrySet()) {

	        Long roomNo = entry.getKey();
	        Set<Long> users = entry.getValue();


	        if (users.remove(memberNo)) {

	            if (users.isEmpty()) {
	                roomViewers.remove(roomNo);
	            }

	            break;
	        }
	    }
	    

	}

	

}
