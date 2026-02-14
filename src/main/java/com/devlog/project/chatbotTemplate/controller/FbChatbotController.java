package com.devlog.project.chatbotTemplate.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.devlog.project.chatbotTemplate.dto.CbSession;
import com.devlog.project.chatbotTemplate.service.CbSessionService;
import com.devlog.project.chatbotTemplate.service.CbtTokenUsageService;
import com.devlog.project.chatbotTemplate.service.ChatbotService;
import com.devlog.project.member.model.dto.MemberLoginResponseDTO;
import com.devlog.project.member.model.entity.Member;
import com.devlog.project.member.model.repository.MemberRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/api/chatbot/freeboard")
public class FbChatbotController {

    private final ChatbotService chatService;
    private final CbSessionService cbSessionService;
    private final MemberRepository memberRepository;
    
    public FbChatbotController(
    		ChatbotService chatService
    		, CbSessionService cbSessionService
    		, MemberRepository memberRepository
    		){ 
		    	this.chatService = chatService; 
		    	this.cbSessionService = cbSessionService;
		    	this.memberRepository = memberRepository;
    	}


    @GetMapping("page") // chatbotTemplate pop-up window
    public String chatbotTemplatePage() {
    	return "chatbotTemplate/chatbotTemplate";
    }    
    
    
    // 챗봇 팝업차 화면 보여주기 
    @GetMapping("popupBasicChatbot") // chatbot pop-up window (무료버전)
    public String fbChatbotPopupBasicCB(Model model) {
    	// BasicChatbot id 넘겨주기
        model.addAttribute("chatbotId", "BASIC"); // 기본형 챗봇
        model.addAttribute("chatbotType", "free");
        model.addAttribute("cbtProfileImg", "/images/board/freeboard/chatbot1.png");
        
    	return "board/freeboard/fbChatbotRevBasic"; // 커피콩 챗봇
    }
    
    @GetMapping("popupKongChatbot") // chatbot pop-up window (유료버전)
    public String fbChatbotPopupKongCB(Model model) {
    	// KongChatbot id 넘겨주기
        model.addAttribute("chatbotId", "KONG");
        model.addAttribute("chatbotType", "beanCharge");
        model.addAttribute("cbtProfileImg", "/images/board/freeboard/chatbot3.png");    	
    	return "board/freeboard/fbChatbotRevKong";
    }    
        
    
//    // [ 과금 작업 draft ] : 토큰 프론트에서 대강계산(4ch/token) + DB에 대이터삽입X
//	@PostMapping("/{sessionId}")
//	@ResponseBody
//	public ResponseEntity<Map<String, Object>>  chat(@PathVariable  String sessionId, @RequestBody String message){
//	    return ResponseEntity.ok(chatService.sendMessage(sessionId, message));
//	}    

    
    // [ 과금 작업 using meta-data ] : 토큰 백엔트에서 제대로(openAI meta data 이용) + DB에 대이터삽입O
    /**
     * 챗봇 메시지 처리 (토큰 사용량 기록 포함)
     * @param sessionId 세션ID (DB의 CB_SESSION_ID)
     * @param userMessage 사용자 메시지
     * @param loginMember 로그인 회원
     * @param session HTTP 세션
     * @return 챗봇 응답
     */
    //@PostMapping("/{sessionId}")
    @PostMapping("/{sessionId:\\d+}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> chat(
    		@PathVariable Long sessionId,
    		@RequestBody String userMessage,
    		@SessionAttribute(name = "loginMember", required = false) MemberLoginResponseDTO loginMember // 로그인시 Session에 저장된 loginMember 가져오기
    		){
    	
        log.info("챗봇 요청 - 세션ID: {}, 회원번호: {}, 메시지: {}", 
                sessionId, loginMember != null ? loginMember.getMemberNo() : "비회원", userMessage);
    	
        Map<String, Object> result = new HashMap<>(); // 챗봇의 응답을 담는 객체
        
        try {
        	
            // 세션 검증
            CbSession session = cbSessionService.getCbSession(sessionId);
            if(session == null) {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "유효하지 않은 세션입니다."
                ));
            }        	
            
            log.info("현 챗봇 세션 정보: cbSession 객체 = {}", session);
            
            String cbSessionType = session.getCbSessionType();
            
            // 커피콩 챗봇인 경우 로그인 체크
            // CbSessionService.startCbSession()에서  cbSessionType 챗봇 유형 (BASIC, KONG), cbBoardType 게시글 유형 (INSERT, UPDATE)
            //  에 대한 정보를 CB_SESSION 테이블에 삽입
            if("KONG".equals(cbSessionType) && loginMember == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "커피콩 챗봇은 로그인이 필요합니다."
                ));
            }            
        	
        	result = chatService.sendMessageTokenInfo(sessionId, cbSessionType, userMessage, loginMember);
        	
        } catch (Exception e) {
            log.error("챗봇 처리 중 오류 발생", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("reply", "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    	
    	return ResponseEntity.ok(result);
    }

    
    /**
     * 회원의 토큰 사용량 조회
     * @param loginMember
     * @return 토큰 사용량 정보
     */
    @GetMapping("/usage")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUsage(
            @SessionAttribute(name = "loginMember", required = false) MemberLoginResponseDTO loginMember) {
        
        if(loginMember == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        
        
        Map<String, Object> result = new HashMap<>(); 
        result = chatService.getUsagebyMember(loginMember);
        
        return ResponseEntity.ok(result);
        
    }
    
    
    /**
     * 챗봇 사용 후 회원의 커피콩 잔액 업데이트
     * @param request {loginMemberNo, updatedBeansAmount}
     * @return 업데이트 결과
     */
    @PostMapping("/updateBeansAmount")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateBeansAmount(
            @RequestBody Map<String, Object> request,
            @SessionAttribute(name = "loginMember", required = false) MemberLoginResponseDTO loginMember) {
        
        log.info("=== updateBeansAmount 호출됨 ===");
        log.info("요청 데이터: {}", request);
        log.info("로그인 회원: {}", loginMember);
        
        // 로그인 체크 완화 - request에 memberNo가 있으면 진행
        // beforeunload에서는 세션이 이미 만료될 수 있음       
//        if(loginMember == null) {
//            return ResponseEntity.status(401).body(Map.of(
//                "success", false,
//                "message", "로그인이 필요합니다."
//            ));
//        }
        
        try {
            Long memberNo = Long.valueOf(request.get("loginMemberNo").toString());
            Integer updatedBeansAmount = Integer.valueOf(request.get("updatedBeansAmount").toString());
            
//            // 로그인한 회원과 요청한 회원이 일치하는지 확인
//            if(!loginMember.getMemberNo().equals(memberNo)) {
//                return ResponseEntity.status(403).body(Map.of(
//                    "success", false,
//                    "message", "권한이 없습니다."
//                ));
//            }
            
            // 로그인한 회원과 요청한 회원이 일치하는지 확인 (세션이 있을 때만)
            if(loginMember != null && !loginMember.getMemberNo().equals(memberNo)) {
                log.warn("권한 불일치 - 로그인: {}, 요청: {}", loginMember.getMemberNo(), memberNo);
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "권한이 없습니다."
                ));
            }
            
            // 음수 체크
            if(updatedBeansAmount < 0) {
                log.warn("음수 잔액 시도: {}", updatedBeansAmount);
                return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", "잔액이 음수일 수 없습니다."
                ));
            }
            
            // JPA로 Member 조회 및 업데이트
            Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
            
            log.info("업데이트 전 - 회원 {} 커피콩: {}", memberNo, member.getBeansAmount());
            
            // Member Entity에 업데이트 메서드 호출
            member.updateBeansAmount(updatedBeansAmount);
            Member savedMember = memberRepository.save(member);
            
            log.info("✅ 업데이트 후 - 회원 {} 커피콩: {} → {}", 
                    memberNo, member.getBeansAmount(), savedMember.getBeansAmount());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "커피콩 잔액이 업데이트되었습니다.");
            result.put("updatedBeansAmount", updatedBeansAmount);
            result.put("beforeAmount", member.getBeansAmount());
            result.put("afterAmount", savedMember.getBeansAmount());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("커피콩 잔액 업데이트 실패", e);
            log.error("Stack trace:", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "커피콩 잔액 업데이트 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }    
    
}
