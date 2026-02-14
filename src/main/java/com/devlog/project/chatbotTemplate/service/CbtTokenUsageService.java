package com.devlog.project.chatbotTemplate.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devlog.project.chatbotTemplate.dto.CbtTokenUsage;
import com.devlog.project.chatbotTemplate.mapper.CbtTokenUsageMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CbtTokenUsageService {

    private final CbtTokenUsageMapper cbtTokenUsageMapper;
    
    // 토큰당 커피콩 환산 비율 (500토큰 = 1커피콩)
    //private static final int TOKENS_PER_BEAN = 500;
    private static final int TOKENS_PER_BEAN = 5; // 테스트용
    
    
    /**
     * 세션별 누적 토큰 (메모리 캐시)
     */
    private final Map<Long, Long> accumulatedTokenMap = new ConcurrentHashMap<>();
        
    
    /**
     * 토큰 사용 기록 저장
     * @param promptText 사용자 질문
     * @param answerText 챗봇 답변
     * @param promptTokens 질문 토큰 수
     * @param answerTokens 답변 토큰 수
     * @param modelName 모델명
     * @param memberNo 회원번호
     * @param cbSessionId 세션ID
     * @return 저장된 TokenUsage 객체
     */
    public CbtTokenUsage saveTokenUsage(
            String promptText, 
            String answerText, 
            int promptTokens, 
            int answerTokens,
            String modelName,
            Long memberNo,
            Long cbSessionId) {
        
        // 총 토큰 계산
        int totalTokens = promptTokens + answerTokens;
        
        // 커피콩 포인트 계산 (500토큰당 1커피콩)
        int beanSwe = calculateBeans(totalTokens);
        
        CbtTokenUsage tokenUsage = CbtTokenUsage.builder() // 저장할 과금 DTO 생성
                .promptText(promptText)
                .answerText(answerText)
                .promptTokens(promptTokens)
                .answerTokens(answerTokens)
                .totalTokens(totalTokens)
                .beanSwe(beanSwe)
                .modelName(modelName)
                .memberNo(memberNo)
                .cbSessionId(cbSessionId)
                .build();
        
        // 과금 CbtTokenUsage DTO를 CB_TOKEN_USAGE 테이블에 넣어주자
        int result = cbtTokenUsageMapper.insertTokenUsage(tokenUsage); // CB_TOKEN_USAGE DB
        
        if(result > 0) {
            log.info("토큰 사용 기록 저장 완료 - 회원번호: {}, 총 토큰: {}, 차감할 커피콩: {}", 
                    memberNo, totalTokens, beanSwe);
            return tokenUsage;
        } else {
            log.error("토큰 사용 기록 저장 실패");
            throw new RuntimeException("토큰 사용 기록 저장 실패");
        }
    }
    
    /**
     * 토큰 수를 커피콩으로 환산
     * @param tokens 토큰 수
     * @return 커피콩 포인트
     */
    public int calculateBeans(int tokens) {
        return (int) Math.ceil((double) tokens / TOKENS_PER_BEAN);
    }
    
    /**
     * 회원의 총 토큰 사용량 조회
     * @param memberNo 회원번호
     * @return 총 토큰 수
     */
    public int getTotalTokensByMember(Long memberNo) {
        return cbtTokenUsageMapper.getTotalTokensByMemberNo(memberNo);
    }
    
    /**
     * 세션의 총 토큰 사용량 조회
     * @param cbSessionId 세션ID
     * @return 총 토큰 수
     */
    public int getTotalTokensBySession(Long cbSessionId) {
        return cbtTokenUsageMapper.getTotalTokensBySessionId(cbSessionId);
    }
    
    /**
     * 회원의 토큰 사용 내역 조회
     * @param memberNo 회원번호
     * @return 토큰 사용 내역 리스트
     */
    public List<CbtTokenUsage> getTokenUsageList(Long memberNo) {
        return cbtTokenUsageMapper.getTokenUsageListByMemberNo(memberNo);
    }
    
    /**
     * 회원의 총 사용 커피콩 조회
     * @param memberNo 회원번호
     * @return 총 커피콩 포인트
     */
    public int getTotalBeansByMember(Long memberNo) {
        return cbtTokenUsageMapper.getTotalBeansByMemberNo(memberNo);
    }
    
    /**
     * 회원의 잔여 커피콩 계산
     * @param memberNo 회원번호
     * @param currentBeans 현재 보유 커피콩
     * @return 잔여 커피콩
     */
    public int getRemainingBeans(Long memberNo, int currentBeans) {
        int usedBeans = getTotalBeansByMember(memberNo);
        return currentBeans - usedBeans;
    }
    
    
    
    
    ////////////////////////////////////////////////////////////////////
    
    /**
     * 세션별 누적 토큰 (메모리 캐시)
     */
    private final Map<String, Long> accumulatedTokenMap_draft =
            new ConcurrentHashMap<>();
    
    /**
     * 세션별 누적 토큰 증가
     */
    public long accumulate_draft(String sessionId, long totalTokens) {
        return accumulatedTokenMap_draft.merge(
                sessionId,
                totalTokens,
                Long::sum
        );
    }

    
    
    /**
     * 세션별 누적 토큰 증가
     */
    public long accumulate(Long sessionId, long totalTokens) {
        long accumulated = accumulatedTokenMap.merge(
                sessionId,
                totalTokens,
                Long::sum
        );
        
        log.debug("세션 {} 토큰 누적: +{} → 총 {}", sessionId, totalTokens, accumulated);
        return accumulated;
    }    
    
    /**
     * 현재 누적 토큰 조회 (선택)
     */
    public long getAccumulated(String sessionId) {
        return accumulatedTokenMap.getOrDefault(sessionId, 0L);
    }

    
    /**
     * 세션 종료 시 메모리 정리 (매우 중요!)
     */
    public void clearSession(Long sessionId) {
        Long removed = accumulatedTokenMap.remove(sessionId);
        if(removed != null) {
            log.info("세션 {} 메모리 정리 완료 - 누적 토큰: {}", sessionId, removed);
        } else {
            log.warn("세션 {} 메모리에 데이터 없음", sessionId);
        }
    }
    
    /**
     * 모든 세션 메모리 정리 (관리자 도구용)
     */
    public void clearAllSessions() {
        int size = accumulatedTokenMap.size();
        accumulatedTokenMap.clear();
        log.info("모든 세션 메모리 정리 완료 - {} 개 세션", size);
    }
    
    /**
     * 현재 메모리에 있는 세션 수 확인 (디버깅용)
     */
    public int getActiveSessionCount() {
        return accumulatedTokenMap.size();
    }    
	
}
