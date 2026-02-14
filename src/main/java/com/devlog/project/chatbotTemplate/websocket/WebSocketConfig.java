package com.devlog.project.chatbotTemplate.websocket;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatbotWebSocketHandler handler;

    public WebSocketConfig(ChatbotWebSocketHandler handler){
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry){
        registry.addHandler(handler, "/ws/chat").setAllowedOrigins("*");
    }
}
