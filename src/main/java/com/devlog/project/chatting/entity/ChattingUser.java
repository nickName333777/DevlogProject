package com.devlog.project.chatting.entity;

import java.time.LocalDateTime;

import com.devlog.project.chatting.chatenums.ChatEnums;
import com.devlog.project.member.model.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "CHATTING_USER")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder 
public class ChattingUser {
	
	
	@EmbeddedId
	private ChattingUserId chatUserId;
	
	@MapsId("roomNo")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CHATTING_ROOM_NO")
	private ChatRoom chattingRoom;
	
	@MapsId("memberNo")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MEMBER_NO")
	private Member member;
	
	
	@Enumerated(EnumType.STRING)
	@Column(name = "PINNED_YN", length = 1)
	private ChatEnums.YesNo pinnedYn;
	
	
	@Column(name = "JOIN_DATE", nullable = false)
	private LocalDateTime joinDate;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "ROLE", length = 30, nullable = false)
	// private Role role;
	private ChatEnums.Role role;
	
	@Column(name="LAST_READ_NO", nullable = true)
	private Integer lastReadNo;
	
	
	
	@PrePersist
	public void prePersist() {
		this.joinDate = LocalDateTime.now();
		
		if(pinnedYn == null)
			this.pinnedYn = ChatEnums.YesNo.N;
		 
		if (this.lastReadNo == null)
			this.lastReadNo = 0;
	}
	
	
	



}

