package com.devlog.project.chatbotTemplate.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.devlog.project.chatbotTemplate.dto.CbSession;



@Mapper
public interface CbSessionMapper {

    
    /**
     * 챗봇 세션 생성
     * @param cbSession
     * @return 삽입된 행 수
     */
    int insertCbSession(CbSession cbSession);
    
    /**
     * 챗봇 세션 종료 시간 업데이트
     * @param cbSessionId
     * @return 업데이트된 행 수
     */
    int updateCbSessionEndTime(Long cbSessionId);
    
    /**
     * 챗봇 세션 조회
     * @param cbSessionId
     * @return CbSession 객체
     */
    CbSession selectCbSessionById(Long cbSessionId);
    
    /**
     * 회원의 활성 세션 조회 (종료되지 않은 세션)
     * @param memberNo
     * @return 활성 세션 리스트
     */
    List<CbSession> selectActiveSessions(Long memberNo);
    
    /**
     * 회원의 모든 세션 조회
     * @param memberNo
     * @return 세션 리스트
     */
    List<CbSession> selectSessionsByMemberNo(Long memberNo);
    
    /**
     * 세션 삭제
     * @param cbSessionId
     * @return 삭제된 행 수
     */
    int deleteCbSession(Long cbSessionId);
    
}
