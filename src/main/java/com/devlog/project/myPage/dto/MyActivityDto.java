package com.devlog.project.myPage.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor // 기본 생성자 (MyBatis나 JSON 변환 시 필수)
public class MyActivityDto {
    
    // BOARD 테이블 컬럼 매핑
    private Long boardNo;           
    private String boardTitle;      
    private String boardContent;   
    private int boardCount;         
    
    // MEMBER 테이블 컬럼 매핑
    private String memberNickname;  
    private String memberEmail;     
    
    // BLOG 테이블 및 기타 매핑
    private String isPaid;         
    private String thumbnailUrl;  
    
    // 별칭(Alias) 매핑 (쿼리에서 AS로 지정한 이름)
    private String activityDate;    // 활동 날짜 (YYYY.MM.DD 등)
    private String categoryName;    // 카테고리 (BLOG, NEWS 등)
}