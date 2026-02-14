package com.devlog.project.board.freeboard.model.dto;


import java.util.ArrayList;
import java.util.List; 

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Freeboard {
	private Long boardNo;
	private String boardTitle;
	private String boardContent;
	private String bCreateDate; // LocalDateTime?
	private String bUpdateDate; // LocalDateTime?
	private int boardCount; // 조회수, readCount
	private String boardDelFl;
	private int boardCode; // 3:자유게시판
	
	
	// 서브쿼리 (상세 페이지용 추가 필드)
	private int likeCount;         // 좋아요 개수
	private int commentCount;      // 댓글 개수	
	
	
	// 회원 join
	private Long memberNo;
	private String memberNickname; 
	private String profileImg;
	private String thumbnail;
	
	// 이미지 목록
	private List<BoardImgDB> imageList;

	// 댓글 목록
	private List<CommentDB> commentList;
	
	// null 방어, 2026/01/09
	public void setImageList(List<BoardImgDB> imageList) {
	    this.imageList = (imageList == null)
	        ? new ArrayList <>()
	        : imageList;
	}	
	
	//
	public void setCommnetList(List<CommentDB> commentList) {
	    this.commentList = (commentList == null)
	        ? new ArrayList <>()
	        : commentList;
	}		
}

