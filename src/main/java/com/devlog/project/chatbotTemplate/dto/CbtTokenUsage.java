package com.devlog.project.chatbotTemplate.dto;

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
public class CbtTokenUsage {
    
	@JsonProperty("tkUsageId") 
    private Long tkUsageId;          // 토큰사용이력식별자
	
	@JsonProperty("promptText") 
    private String promptText;       // 사용자질문내용
	
	@JsonProperty("answerText") 
    private String answerText;       // 챗봇대답내용
	
	@JsonProperty("promptTokens") 
    private Integer promptTokens;    // 사용자질문토큰수
	
	@JsonProperty("answerTokens") 
    private Integer answerTokens;    // 챗봇대답토큰수
	
	@JsonProperty("totalTokens") 
    private Integer totalTokens;     // 전체토큰수
	
	@JsonProperty("beanSwe") 
    private Integer beanSwe;         // 과금커피콩포인트
	
	@JsonProperty("modelName") 
    private String modelName;        // 챗봇이름(GPT-4o-mini)
	
	@JsonProperty("memberNo") 
    private Long memberNo;           // 회원번호
	
	@JsonProperty("cbSessionId") 
    private Long cbSessionId;        // 챗봇세션식별자
}
