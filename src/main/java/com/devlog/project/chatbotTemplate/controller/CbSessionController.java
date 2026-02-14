package com.devlog.project.chatbotTemplate.controller;



import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.devlog.project.chatbotTemplate.dto.CbSession;
import com.devlog.project.chatbotTemplate.service.CbSessionService;
import com.devlog.project.member.model.dto.MemberLoginResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/chatbot/session")
@RequiredArgsConstructor
public class CbSessionController {

    private final CbSessionService cbSessionService;
    
    /**
     * 챗봇 세션 시작 (팝업 열 때)
     * @param requestData {cbSessionType, cbBoardType, boardNo}
     * @param loginMember 로그인한 회원
     * @return 생성된 세션 ID
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startSession(
            @RequestBody Map<String, Object> requestData, // requestData {cbSessionType, cbBoardType, boardNo}
            @SessionAttribute(name = "loginMember", required = false) MemberLoginResponseDTO loginMember) {
        
        if(loginMember == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        
        try {
            String cbSessionType = (String) requestData.get("cbSessionType"); // BASIC, KONG
            String cbBoardType = (String) requestData.get("cbBoardType");     // INSERT, UPDATE
            Long boardNo = requestData.get("boardNo") != null 
                    ? Long.valueOf(requestData.get("boardNo").toString()) 
                    : null;
            
            log.info("챗봇 세션 시작 요청 - 회원: {}, 유형: {}, 게시글유형: {}, 게시글번호: {}", 
                    loginMember.getMemberNo(), cbSessionType, cbBoardType, boardNo);
            
            Long sessionId = cbSessionService.startCbSession( // DB에 삽입성공하면 생성된 sessionId (Long)을 반환
                    cbSessionType, 
                    cbBoardType, 
                    loginMember.getMemberNo(), 
                    boardNo
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("sessionId", sessionId); // DB 삽입 성공후 반환받은 sessionId를 프론트로 전달해야함 
            result.put("message", "챗봇 세션이 시작되었습니다.");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("챗봇 세션 시작 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "세션 시작 중 오류가 발생했습니다."
            ));
        }
    }
    
    /**
     * 챗봇 세션 종료 (팝업 닫을 때)
     * @param sessionId 세션 ID
     * @return 종료 결과
     */
    @PostMapping("/end/{sessionId}")
    public ResponseEntity<Map<String, Object>> endSession(
            @PathVariable Long sessionId,
            @SessionAttribute(name = "loginMember", required = false) MemberLoginResponseDTO loginMember) {
        
        if(loginMember == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        
        try {
            log.info("챗봇 세션 종료 요청 - 세션ID: {}, 회원: {}", 
                    sessionId, loginMember.getMemberNo());
            
            boolean success = cbSessionService.endCbSession(sessionId); // .updateCbSessionEndTime(cbSessionId) 성공 여부를 반환받음
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("message", success ? "챗봇 세션이 종료되었습니다." : "세션 종료에 실패했습니다.");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("챗봇 세션 종료 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "세션 종료 중 오류가 발생했습니다."
            ));
        }
    }
    
    /**
     * 세션 정보 조회
     * @param sessionId 세션 ID
     * @return 세션 정보
     */
    @PostMapping("/info/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSessionInfo(
            @PathVariable Long sessionId,
            @SessionAttribute(name = "loginMember", required = false) MemberLoginResponseDTO loginMember) {
        
        if(loginMember == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        
        try {
            CbSession session = cbSessionService.getCbSession(sessionId); // .selectCbSessionById(cbSessionId) 성공 결과 반환
            
            if(session == null) {
                return ResponseEntity.status(404).body(Map.of("error", "세션을 찾을 수 없습니다."));
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("session", session);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("세션 정보 조회 실패", e);
            return ResponseEntity.status(500).body(Map.of("error", "세션 정보 조회 중 오류가 발생했습니다."));
        }
    }
    
}
