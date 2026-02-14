package com.devlog.project.chatting.dto;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.devlog.project.chatting.chatenums.MsgEnums;
import com.devlog.project.chatting.chatenums.MsgEnums.MsgStatus;
import com.devlog.project.chatting.entity.Message;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class MessageDTO {
	
	private Long messageNo;
	private Long memberNo;
	private String senderName;
	private String senderProfile;
	
	private String type;
	private String content;
	
	private String imgPath;
	
	private LocalDateTime sendTime;
	
	private String status;
	
	private int unreadCount;
	private boolean mine;
	
	private Map<String, Long> reactions;
	
	
	public MessageDTO(
		    Long messageNo,
		    Long memberNo,
		    String senderName,
		    String senderProfile,
		    MsgEnums.MsgType type,
		    String content,
		    String imgPath,
		    LocalDateTime sendTime,
		    MsgEnums.MsgStatus status,
		    Long unreadCount,
		    Integer mine
		) {
		    this.messageNo = messageNo;
		    this.memberNo = memberNo;
		    this.senderName = senderName;
		    this.senderProfile = senderProfile;
		    this.type = type.name();               // enum → String
		    this.content = content;
		    this.imgPath = imgPath;
		    this.sendTime = sendTime;
		    this.status = status != null ? status.name() : null;
		    this.unreadCount = unreadCount != null ? unreadCount.intValue() : 0;
		    this.mine = mine != null && mine == 1;
		}
	
	public MessageDTO(Map<String, Long> reactions) {
		this.reactions = reactions;
	}
	
	
	
	@Getter
	@Setter
	@ToString
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ChatMessage {
		
		@JsonProperty("chatRoomNo")
		private Long chatRoomNo;
		private Long sender;
		private String content;
		private int totalCount;
	}
	
	
	@Getter
	@Setter
	@ToString
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ChatMessageResponse {
		
		private Long roomNo;
		private Long senderNo;
		private String senderName;
		private String content;
		private LocalDateTime sendtime;
		private Long messageNo;
		private String profileImg;
		private MsgEnums.MsgType type;
		
		private int unreadCount;
		private String imgPath;
		
		
		
		public static ChatMessageResponse toDto(Message m) {
			
			return ChatMessageResponse.builder()
					.roomNo(m.getChattingRoom().getRoomNo())
					.senderNo(m.getMember().getMemberNo())
					.senderName(m.getMember().getMemberNickname())
					.content(m.getMessageContent())
					.sendtime(m.getSendTime())
					.messageNo(m.getMessageNo())
					.profileImg(m.getMember().getProfileImg())
					.type(m.getType())
					.build();
			
		}
		
		
	}
	
	
	@Getter
	@Setter
	@ToString
	@NoArgsConstructor
	@AllArgsConstructor
	public static class messageReadRequest {
		private Long roomNo;
		private Long memberNo;
	}
	
	
	
	@Getter
	@Setter
	@ToString
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MessageEdit {
		
		private Long messageNo;
		private String content;
		
	}
	
	
	@Getter
	@Setter
	@ToString
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MessageEditResp {
		private Long messageNo;
		private MsgEnums.MsgStatus status;
		private String content;
	}
	
	
	@Getter
	@Setter
	@ToString
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ImageRequest {
		
		private Long roomNo;
		private MultipartFile img;
		private Long memberNo;
		private int totalCount;
		
	}
	
	
	@Getter
	@Setter
	@ToString
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class systemMessage {
		private String content;
		private MsgEnums.MsgType type;
	}
	
	
	@Getter
	@Setter
	@ToString
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class DeleteMessageResponse {
		
		private Long messageNo;
		private MsgEnums.MsgStatus status;
		
	}
	
	
	

}
