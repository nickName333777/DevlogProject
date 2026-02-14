package com.devlog.project.board.freeboard.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BoardDB {
	
    // 게시글번호 
    private Long boardNo;

    // 게시글 제목 
    private String boardTitle;

    // 게시글 내용 
    private String boardContent;

    // 작성일   
    private String bCreateDate;

    // 수정일 
    private String bUpdateDate;

    // 조회수 
    private int boardCount;

    // 삭제 여부 (Y/N) 
    private String boardDelFl;

    // 게시판 코드 
    private Integer boardCode;

    // 작성자 회원번호 
    private Long memberNo;

    // 기자명 (뉴스 게시판용) 
    private String newsReporter;

}
