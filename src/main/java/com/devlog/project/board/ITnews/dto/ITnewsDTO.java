package com.devlog.project.board.ITnews.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ITnewsDTO {
	
	private int boardNo;
	private String boardTitle;
	private String boardContent;
	private String boardName;
	private String bCreateDate;
	private String bUpdateDate;
	private int boardCount;
	private String boardDelFl;
	private int boardCode;
	private int memberNo;
	
	// 뉴스 관련
	private String newsReporter;
	
	// 이미지 관련
	private int imgNo;       
    private String imgPath;  
    private String imgRename;
    
    
    
    // 상세 페이지용 추가 필드
    private int likeCount;         // 좋아요 개수
    private int scrapCount;        // 스크랩 개수
    private int commentCount;      // 댓글 개수
    
    // 로그인한 사용자의 상태 체크 (1: 체크됨, 0: 안됨)
    private int likeCheck;         
    private int scrapCheck;
}
