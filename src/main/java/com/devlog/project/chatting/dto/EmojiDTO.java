package com.devlog.project.chatting.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EmojiDTO {
	
	private Long messageNo;
	private String emoji;
	private Long count;
	
	
	
	@Getter
	@Setter
	@ToString
	@NoArgsConstructor
	@AllArgsConstructor
	public static class updateEmojiDTO {
		
		private Long messageNo;
		
		private String type;
		
		private Map<String, Long> reactions;
		
		
	}
	
}
