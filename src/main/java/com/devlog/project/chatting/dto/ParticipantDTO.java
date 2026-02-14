package com.devlog.project.chatting.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ParticipantDTO {

	private Long memberNo;

	private String nickname;

	private String profileImage;

	private boolean owner;


	public ParticipantDTO(Long memberNo, String nickname, String profileImage, Integer ownerYn) {
		
		this.memberNo = memberNo;
		this.nickname = nickname;
		this.profileImage = profileImage;
		this.owner = ownerYn != null && ownerYn == 1;
	}
	
	
	
	@Getter
	@Setter
	@ToString
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ChatListUpdateDTO {
		
		private Long roomNo;
		private String lastMessage;
		private LocalDateTime sendtime;
		private Long unreadCount;
		
	}

}
