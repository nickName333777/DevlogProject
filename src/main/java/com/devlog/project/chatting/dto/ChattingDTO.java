package com.devlog.project.chatting.dto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


public class ChattingDTO {
	
	
	
	
	@Getter
	@Setter
	@ToString
	public static class ChattingListDTO {
	    private Long chattingRoomNo;
	    private String roomType;
	    private String pinnedYn;
	    private String displayName;
	    private String roomImg;
	    private String lastMessage;
	    private LocalDateTime lastMessageAt;
	    private String formatTime;
	    private Long unreadCount;


	}
	
	
	
	@Getter
	@Setter
	@ToString
	public static class FollowListDTO {
		
		private Long memberNo;
		private String memberNickname;
		private String profileImg;
		
	}
	
	
	@Getter
	@Setter
	@ToString
	public static class GroupCreateDTO {
		
		private String roomName;
		private List<Long> memberNo;
		private MultipartFile roomImg;
		
	}
	
	
	@Getter
	@Setter
	@ToString
	public static class RoomInfoDTO {
		private String roomName; // 방이름
		private int participantCount; // 참여인원
		private String roomProfile; // 채팅방 프로필
		
		
		List<MessageDTO> messageList;
		
		List<ParticipantDTO> users;
		
	}
	
	



}

