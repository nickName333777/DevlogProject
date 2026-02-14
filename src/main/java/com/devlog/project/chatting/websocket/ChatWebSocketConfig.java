package com.devlog.project.chatting.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // STOMP 기반 메세지 브로커 활성화
public class ChatWebSocketConfig implements WebSocketMessageBrokerConfigurer{
	
	
	/*
	 *  클라이언트가 처음 연결할 WebSocket 주소 설정
	 *  -> 브라우저에서 new SockJS("/ws-chat")할 ㄸ ㅐㅇ ㅣ 주소로 접속
	 *   
	 */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry reg) {
		
		reg.addEndpoint("/ws-chat") //접속 URL
			.setAllowedOriginPatterns("*") // CORS 허용
			.withSockJS(); // WebSocket 지원 안 될 경우 fallback 제공
							// websocke 안 될 시 sockjs 사용
		
		reg.addEndpoint("/ws")
        .setAllowedOriginPatterns("*")
        .withSockJS();
	}
	
	
	/*
	 * 메세지 이동 경로 규칙
	 * (STOMP 프로토콜 핵심)
	 */
	@Override
	public void configureMessageBroker(MessageBrokerRegistry reg) {
		
		// 클라이언트 -> 서버로 보내는 메세지의 접두사
		// ex ) stompClient.send("/app/chat..send", .. )
		// 	    -> @MessageMapping("/chat.send")로 매핑
		reg.setApplicationDestinationPrefixes("/devtalk", "/online");
		
		// 서버 -> 클라이언트로 보내는 브로드캐스트 경로
		// 			구독 중인 모든 클라이언트가 수신
		reg.enableSimpleBroker("/topic", "/queue"); //topic -> 구독한 모든 클라이언트
													// queue 특정 클라이언트 
	}
}
