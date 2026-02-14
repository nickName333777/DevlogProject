package com.devlog.project.board.blog.dto;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty; // 추가
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ReplyDto {
    
    @JsonProperty("commentNo")
    private Long commentNo;         
    
    @JsonProperty("boardNo")
    private Long boardNo;           
    
    @JsonProperty("parentCommentNo")
    private Long parentCommentNo;   
    
    @JsonProperty("memberNo")
    private Long memberNo;          
    
    @JsonProperty("memberNickname")
    private String memberNickname;  
    
    @JsonProperty("profileImg")
    private String profileImg;      
    
    @JsonProperty("commentContent")
    private String commentContent;  
    
    @JsonProperty("cCreateDate")
    private String cCreateDate;     
    
    @JsonProperty("commentDelFl")
    private String commentDelFl;    
    
    @JsonProperty("secretYn")
    private String secretYn;        
    
    @JsonProperty("likeCount")
    private int likeCount;          
    
    @JsonProperty("isLiked")
    private boolean isLiked;
    
    private List<ReplyDto> children = new ArrayList<>();
}