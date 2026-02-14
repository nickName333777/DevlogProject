package com.devlog.project.chatbotTemplate.dto;


import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class CbSession {

	@JsonProperty("cbSessionId") 
    private Long cbSessionId;           // 챗봇세션식별자
	
	@JsonProperty("startedAt") 
    private LocalDateTime startedAt;    // 챗봇팝업시작시간
	
	@JsonProperty("endedAt") 
    private LocalDateTime endedAt;      // 챗봇팝업마침시간
	
	@JsonProperty("cbSessionType") 
    private String cbSessionType;       // 챗봇세션유형(BASIC, KONG)
	
	@JsonProperty("cbBoardType") 
    private String cbBoardType;         // 게시글유형(INSERT, UPDATE)
	
	@JsonProperty("memberNo") 
    private Long memberNo;              // 회원번호
	
	@JsonProperty("boardNo") 
    private Long boardNo;               // 게시글번호(글쓰기:NULL, 글수정:BOARD_NO)
    
}
