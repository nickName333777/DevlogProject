package com.devlog.project.notification.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.devlog.project.member.model.dto.MemberLoginResponseDTO;
import com.devlog.project.notification.dto.NotifiactionDTO.ResponseDTO;
import com.devlog.project.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SseService {
	
	private final NotificationRepository notificationRepository;
	
	
	// 멀티스레드 환경에서 데이터 동기화 할 수 있는 Map 각 유저 끼리 같은 map을 공유
	private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
	
	public SseEmitter connect(Long memberNo) {
		
		String clientId = memberNo + "";
		
		// SseEmitter 객체 생성 -> 연결 대기 시간 10분(ms 단위)
		SseEmitter emitter = new SseEmitter(10 * 60 * 1000L);
		
		emitters.put(clientId, emitter);
		
		// 클라이언트 연결 종료 시 제거
		emitter.onCompletion(() -> emitters.remove(clientId));
		
		// 클라이언트 타임 아웃 시 제거
		emitter.onTimeout(() -> emitters.remove(clientId));
		
		System.out.println("emitter 확인 : " + emitter);
		
		return emitter;
	
	}
	
	


	// 알림 전송 
	public void send(Long memberNo) {
		
		
		Long unreadCount = notificationRepository.countUnreadCount(memberNo);
		
		String clientId = memberNo.toString();
		
		SseEmitter emitter = emitters.get(clientId);
		
		Map<String, Object> map = new HashMap<>();
		map.put("memberNo", memberNo);
		map.put("unreadCount", unreadCount);
		
		if(emitter != null) {
			try {
				
				emitter.send(map);
				
			} catch (Exception e) {
				emitters.remove(clientId);
			}
		}
	}
	
	
}


	
