package com.devlog.project.chatting.entity;

import java.time.LocalDateTime;

import com.devlog.project.chatting.chatenums.ChatEnums;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "CHATTING_ROOM")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoom {
	
	/*
	 *  DB테이블 미리 생성해 두어서 IDENTITY 전략이 안 먹힘 
	 *  IDENTITY 전략은 JPA가 테이블 만다는 경우에 안전
	 *  Oracle 사전 생성 테이블 사용할 경우
	 *  SEQUENCE + @SequenceGenerator 전략을 사용
	 */
	@Id
	@Column(name = "CHATTING_ROOM_NO")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_room_seq")  
	@SequenceGenerator(																// 
	    name = "chat_room_seq",
	    sequenceName = "SEQ_ROOM_NO",
	    allocationSize = 1)
	private Long roomNo;
	
	@Column(name = "CHATTING_ROOM_NAME", nullable = true, length = 50)
    private String chattingRoomName;

    @Column(name = "CREATE_DATE", nullable = false)
    private LocalDateTime createDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "ROOM_TYPE", nullable = false, length = 30)
    // private RoomType roomType;
    private ChatEnums.RoomType roomType;
    
    @Column(name = "ROOM_IMG", nullable = true, length = 255)
    private String roomImg;
    
    
    @PrePersist
    public void prePersist() {
    	this.createDate = LocalDateTime.now();
    }

    
    
}
