package com.devlog.project.board.freeboard.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CommentFB {

	@JsonProperty("commentNo") 
	private Long commentNo;
	
	@JsonProperty("memberNo")
	private Long memberNo;
	
	@JsonProperty("boardNo")
	private Long boardNo;
	
	@JsonProperty("parentsCommentNo")
	private Long parentsCommentNo;
	
	@JsonProperty("cCreateDate")
	private String cCreateDate;
	
	@JsonProperty("commentContent")
	private String commentContent;
	
	@JsonProperty("commentDelFl")
	private String commentDelFl;
	
	@JsonProperty("secretYn")
	private String secretYn;
	
	@JsonProperty("modifyYn")
	private String modifyYn;
	
	@JsonProperty("memberNickname")
	private String memberNickname;
	
	@JsonProperty("profileImg")
	private String profileImg;
}
