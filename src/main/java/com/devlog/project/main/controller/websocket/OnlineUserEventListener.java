package com.devlog.project.main.controller.websocket;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.core.index.AliasAction.Add;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.devlog.project.member.model.dto.FollowDTO;
import com.devlog.project.member.model.entity.Member;
import com.devlog.project.member.model.repository.MemberRepository;

import co.elastic.clients.elasticsearch._types.Result;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class OnlineUserEventListener {

	private final OnlineService onlineService;
	private final MemberRepository memberRepository;
	private final SimpMessageSendingOperations messagingTemplate;
	private static final Map<String, Set<Long>> onlineUsers = new ConcurrentHashMap<>();

	@EventListener
	public void handleWebSocketConnectListener(SessionConnectEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		Map<String, Object> attributes = headerAccessor.getSessionAttributes();
		Principal principal = headerAccessor.getUser();
		String memberEmaiil = principal.getName();
		Member member = memberRepository.findMemberNoByMemberEmail(memberEmaiil);

		Long memberNo = member.getMemberNo();
		System.out.println(memberNo);

		onlineUsers.computeIfAbsent("online", k -> ConcurrentHashMap.newKeySet()).add(memberNo);
		System.out.println("현재 온라인 유저들: " + onlineUsers.get("online"));
		
		List<FollowDTO> myFollowers = onlineService.selectFollows(memberNo); 
	    Set<Long> onlineSet = onlineUsers.get("online");

	    if (myFollowers != null && onlineSet != null) {
	        for (FollowDTO follower : myFollowers) {
	            Long followerNo = follower.getMemberNo();
	            // 현재 접속 중인 친구라면, 그 친구의 친구 목록을 다시 쏴서 나를 포함시킴
	            if (onlineSet.contains(followerNo)) {
	                sendFollowListByMemberNo(followerNo);
	            }
	        }
	    }

	}

	@MessageMapping("/requestOnline")
	public void sendFollowList(Map<String, Object> paramMap) {
		Long memberNo = ((Number) paramMap.get("memberNo")).longValue();
		List<FollowDTO> online = onlineService.selectFollow(memberNo);
		System.out.println("팔로우리스트" + online);

		List<FollowDTO> resp = new ArrayList<>();
		Iterator<?> iter = onlineUsers.get("online").iterator();
		while (iter.hasNext()) {
			Long onlineMemberNo = (Long) iter.next();

			for (FollowDTO follow : online) {
				if (follow.getMemberNo().equals(onlineMemberNo)) {
					resp.add(follow);
				}
			}
		}
		System.out.println("온라인 유저 목록" + resp);
		messagingTemplate.convertAndSend("/topic/online/" + memberNo, resp);

	}
	
	@EventListener
	public void onDisconnect(SessionDisconnectEvent event) {
	    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
	    Principal principal = accessor.getUser();
	    
	    if (principal == null) return;

	    // 나가는 유저 정보 파악
	    String memberEmail = principal.getName();
	    Member member = memberRepository.findMemberNoByMemberEmail(memberEmail);
	    if (member == null) return;
	    Long disconnectedMemberNo = member.getMemberNo();

	    // 전체 온라인 명단에서 나를 제거
	    Set<Long> onlineSet = onlineUsers.get("online");
	    if (onlineSet != null) {
	        onlineSet.remove(disconnectedMemberNo);
	    }

	    System.out.println("[OFFLINE] 유저 번호: " + disconnectedMemberNo);

	    // 나를 팔로우하는 사람들에게 "나 나갔어"라고 리스트 갱신 전송
	    List<FollowDTO> myFollowers = onlineService.selectFollows(disconnectedMemberNo);

	    if (myFollowers != null && onlineSet != null) {
	        for (FollowDTO follower : myFollowers) {
	            Long followerNo = follower.getMemberNo();
	            
	            // 그 친구가 지금 접속 중일 때만 업데이트 메시지 전송
	            if (onlineSet.contains(followerNo)) {
	                sendFollowListByMemberNo(followerNo);
	            }
	        }
	    }
	}

		// 특정 유저에게 그 사람의 온라인 친구 목록을 전송하는 메서드
		private void sendFollowListByMemberNo(Long targetMemberNo) {
		    List<FollowDTO> allFollows = onlineService.selectFollows(targetMemberNo);
		    Set<Long> onlineSet = onlineUsers.get("online");
	
		    List<FollowDTO> resp = allFollows.stream()
		            .filter(f -> onlineSet != null && onlineSet.contains(f.getMemberNo()))
		            .collect(Collectors.toList());
	
		    messagingTemplate.convertAndSend("/topic/online/" + targetMemberNo, resp);
		}
	}
