package com.devlog.project.board.ITnews.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Comment {
	@JsonProperty("commentNo")
	private int commentNo;
	
	@JsonProperty("memberNo")
	private int memberNo;
	
	@JsonProperty("boardNo")
	private int boardNo;
	
	@JsonProperty("parentCommentNo")
	private int parentCommentNo;
	
	@JsonProperty("commentCreateDate")
	private String commentCreateDate;
	
	@JsonProperty("commentContent")
	private String commentContent;
	
	@JsonProperty("commentDeleteFlag")
	private String commentDeleteFlag;
	
	
	@JsonProperty("secretYN")
	private String secretYN;
	
	@JsonProperty("modifyYN")
	private String modifyYN;
	
	@JsonProperty("memberNickname")
	private String memberNickname;
	
	@JsonProperty("profileImg")
	private String profileImg;
	
	
	@JsonProperty("likeCount")
	private int likeCount;   
	
	@JsonProperty("badCount")
    private int badCount;   
	
	@JsonProperty("likeCheck")
    private int likeCheck;   
	
	@JsonProperty("badCheck")
    private int badCheck;     

}
