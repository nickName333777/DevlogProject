package com.devlog.project.chatbotTemplate.websocket;


import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;
import org.springframework.ai.openai.*;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.*;

@Component
public class ChatbotWebSocketHandler extends TextWebSocketHandler {

    private final OpenAiChatModel chatModel;

    public ChatbotWebSocketHandler(OpenAiChatModel chatModel){
        this.chatModel = chatModel;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        ChatResponse response = chatModel.call(
                new Prompt(new UserMessage(message.getPayload()))
        );

        //String reply = response.getResult().getOutput().getContent();// Spring AI 0.1.x 이전
        String reply = response.getResult().getOutput().getText(); // Spring AI 0.1.x 이후
        System.out.println("[Chat GPT] Answer: " + reply);
        //int tokens = response.getUsage().getTotalTokens();
        int tokens = 777; // dummy value

        String json = """
            {
              "reply": "%s",
              "usage": { "total_tokens": %d }
            }
        """.formatted(reply.replace("\"","\\\""), tokens);

        session.sendMessage(new TextMessage(json));
    }
}
