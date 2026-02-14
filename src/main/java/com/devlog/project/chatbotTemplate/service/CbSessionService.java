package com.devlog.project.chatbotTemplate.service;


import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devlog.project.chatbotTemplate.dto.CbSession;
import com.devlog.project.chatbotTemplate.mapper.CbSessionMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CbSessionService {

	
    private final CbSessionMapper cbSessionMapper;
    private final CbtTokenUsageService cbtTokenUsageService;  // 추가!
    
    /**
     * 챗봇 세션 시작 (세션 생성)
     * @param cbSessionType 챗봇 유형 (BASIC, KONG)
     * @param cbBoardType 게시글 유형 (INSERT, UPDATE)
     * @param memberNo 회원번호
     * @param boardNo 게시글번호 (없으면 null)
     * @return 생성된 세션 ID
     */
    public Long startCbSession(String cbSessionType, String cbBoardType, Long memberNo, Long boardNo) {
        
        CbSession cbSession = CbSession.builder()
                .cbSessionType(cbSessionType)
                .cbBoardType(cbBoardType)
                .memberNo(memberNo)
                .boardNo(boardNo)
                .build();
        
        int result = cbSessionMapper.insertCbSession(cbSession);
        
        if(result > 0) {
            log.info("챗봇 세션 생성 완료 - 세션ID: {}, 회원번호: {}, 유형: {}", 
                    cbSession.getCbSessionId(), memberNo, cbSessionType);
            return cbSession.getCbSessionId();
        } else {
            log.error("챗봇 세션 생성 실패");
            throw new RuntimeException("챗봇 세션 생성 실패");
        }
    }
    
    /**
     * 챗봇 세션 종료
     * @param cbSessionId 세션 ID
     * @return 종료 성공 여부
     */
    public boolean endCbSession(Long cbSessionId) {
        
        int result = cbSessionMapper.updateCbSessionEndTime(cbSessionId);
        
        if(result > 0) {
            log.info("챗봇 세션 종료 완료 - 세션ID: {}", cbSessionId);
            
            // 세션 종료 시 메모리에서 누적 토큰 정리 꼭 하자.
            cbtTokenUsageService.clearSession(cbSessionId);
            log.info("세션 {} 메모리 정리 완료", cbSessionId);
            
            return true;
        } else {
            log.warn("챗봇 세션 종료 실패 - 세션ID: {}", cbSessionId);
            return false;
        }
    }
    
    /**
     * 세션 정보 조회
     * @param cbSessionId 세션 ID
     * @return CbSession 객체
     */
    public CbSession getCbSession(Long cbSessionId) {
        return cbSessionMapper.selectCbSessionById(cbSessionId);
    }
    
    /**
     * 회원의 활성 세션 조회
     * @param memberNo 회원번호
     * @return 활성 세션 리스트
     */
    public List<CbSession> getActiveSessions(Long memberNo) {
        return cbSessionMapper.selectActiveSessions(memberNo);
    }
    
    /**
     * 회원의 모든 세션 조회
     * @param memberNo 회원번호
     * @return 세션 리스트
     */
    public List<CbSession> getAllSessions(Long memberNo) {
        return cbSessionMapper.selectSessionsByMemberNo(memberNo);
    }
    
    /**
     * 종료되지 않은 세션들 자동 종료 (정리용)
     * @param memberNo 회원번호
     */
    public void closeAllActiveSessions(Long memberNo) {
        List<CbSession> activeSessions = getActiveSessions(memberNo);
        for(CbSession session : activeSessions) {
            endCbSession(session.getCbSessionId());
        }
        log.info("회원 {}의 활성 세션 {} 개 종료 완료", memberNo, activeSessions.size());
        
    }
}
