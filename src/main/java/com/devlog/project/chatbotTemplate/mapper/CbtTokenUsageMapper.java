package com.devlog.project.chatbotTemplate.mapper;


import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.devlog.project.chatbotTemplate.dto.CbtTokenUsage;


@Mapper
public interface CbtTokenUsageMapper {
    
    /**
     * 토큰 사용 기록 삽입
     * @param tokenUsage
     * @return 삽입된 행 수
     */
    int insertTokenUsage(CbtTokenUsage tokenUsage);
    
    /**
     * 특정 회원의 총 토큰 사용량 조회
     * @param memberNo
     * @return 총 토큰 수
     */
    int getTotalTokensByMemberNo(Long memberNo);
    
    /**
     * 특정 세션의 총 토큰 사용량 조회
     * @param cbSessionId
     * @return 총 토큰 수
     */
    int getTotalTokensBySessionId(Long cbSessionId);
    
    /**
     * 특정 회원의 토큰 사용 내역 조회
     * @param memberNo
     * @return 토큰 사용 내역 리스트
     */
    List<CbtTokenUsage> getTokenUsageListByMemberNo(Long memberNo);
    
    /**
     * 회원의 총 사용 커피콩 조회
     * @param memberNo
     * @return 총 커피콩 포인트
     */
    int getTotalBeansByMemberNo(Long memberNo);
}
