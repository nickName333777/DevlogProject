package com.devlog.project.chatting.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@Setter
public class QueryMessageResponseDTO {
	private Long messageNo;
	private String messageContent;
	private LocalDateTime sendTime;
	private String memberNickname;
	private String profilePath;
	
	private String formatTime;
	
	
	public QueryMessageResponseDTO(Long messageNo,
						            String messageContent,
						            LocalDateTime sendTime,
						            String memberNickname,
						            String profilePath) {

			this.messageNo = messageNo;
			this.messageContent = messageContent;
			this.sendTime = sendTime;
			this.memberNickname = memberNickname;
			this.profilePath = profilePath;
}
}
