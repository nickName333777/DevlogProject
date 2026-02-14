package com.devlog.project.board.freeboard.model.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CommentDB {
	
	// 댓글 번호 
    private Long commentNo;

    // 회원 번호 
    private Long memberNo;

    // 게시글 번호 
    private Long boardNo;

    // 부모 댓글 번호 
    private Long parentsCommentNo;

    // 작성일  
    private String cCreateDate;

    // 댓글 내용 
    private String commentContent;

    // 삭제 여부 (Y/N) 
    private String commentDelFl;

    // 비밀글 여부 (Y/N) 
    private String secretYn;

    // 수정 여부 (Y/N) 
    private String modifyYn;

}
