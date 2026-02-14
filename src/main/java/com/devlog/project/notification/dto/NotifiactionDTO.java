package com.devlog.project.notification.dto;

import com.devlog.project.common.utility.Util;
import com.devlog.project.notification.NotiEnums;
import com.devlog.project.notification.entity.NotificationEntity;

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
@Builder
public class NotifiactionDTO {
	
	private Long sender;
	private Long receiver;
	
	private String content; // 님이 ~~ 했습니다.
	private String preview;
	
	private NotiEnums.NotiType type;
	private NotiEnums.TargetType targetType;
	
	private Long targetId;
	
	
	
	@Getter
	@Setter
	@NoArgsConstructor
	@Builder
	@AllArgsConstructor
	@ToString
	public static class ResponseDTO {
		
		private Long notiNo; 
		private Long sender;
		
		private String content; // 님이 ~~ 했습니다.
		private String preview;
		
		private String type;
		private String targetType;
		private String isRead;
		
		private Long targetId;
		
		private String formatTime;
		
		
		
		
		public static ResponseDTO toDTO(NotificationEntity noti) {
			
			ResponseDTO dto = ResponseDTO.builder()
							.notiNo(noti.getNotificationNo())
							.sender(noti.getSender().getMemberNo())
							.content(noti.getContent())
							.preview(noti.getPreview())
							.type(noti.getType().toString())
							.targetType(noti.getTargetType().toString())
							.isRead(noti.getIsRead().toString())
							.targetId(noti.getTargetId())
							.formatTime(Util.formatNotiTime(noti.getCreateAt()))
							.build();
			
			return dto;
			
		}
		
		
	}

}
