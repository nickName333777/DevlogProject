package com.devlog.project.chatbotTemplate.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.devlog.project.chatbotTemplate.service.ChatbotService;

import java.util.Map;

@Controller
@RequestMapping("/api/ai/freeboard")
public class FbAiController {

    private final ChatbotService chatService;
    
    public FbAiController(ChatbotService chatService){ this.chatService = chatService; }

    @GetMapping("page") // chatbotTemplate pop-up window
    public String chatbotTemplatePage() {
    	return "chatbotTemplate/chatbotTemplate";
    }
    
    @PostMapping("/{sessionId}")
    @ResponseBody
    //public Map<String,Object> chat(@PathVariable  String sessionId, @RequestBody String message){
    public Map<String,Object> chat(@PathVariable  Long sessionId, @RequestBody String message){
        return chatService.sendMessage(sessionId, message);
    }

    @PostMapping("/lastAnswer")
    @ResponseBody
    public Map<String,Object> lastAnswer(@RequestBody Map<String,String> payload){
        String sessionId = payload.get("sessionId");
        String question = payload.get("lastQuestion");
        String answer = payload.get("lastAiAnswer");
        return chatService.sendLastAnswer(sessionId, question, answer);
    }
}