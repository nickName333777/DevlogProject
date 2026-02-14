package com.devlog.project.chatting.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.devlog.project.chatting.chatenums.MsgEnums;
import com.devlog.project.member.model.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "MESSAGE")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Message {
	
	
	@Id
    @Column(name = "MESSAGE_NO")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "msg_seq")  
	@SequenceGenerator(																// 
	    name = "msg_seq",
	    sequenceName = "SEQ_MSG_NO",
	    allocationSize = 1)
    private Long messageNo;
	
	
    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CHATTING_ROOM_NO")
    private ChatRoom chattingRoom;
    
    
//    @Column(name = "MEMBER_NO", nullable = false)
//    private Long memberNo;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_NO")
    private Member member;

    @Column(name = "SEND_TIME", nullable = false)
    private LocalDateTime sendTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false, length = 20)
    private MsgEnums.MsgType type; // IMG / SYSTEM / TEXT

    @Column(name = "MESSAGE_CONTENT", nullable = true, length = 3000)
    private String messageContent;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "MESSAGE_STATUS", nullable = true, length = 30)
    private MsgEnums.MsgStatus status; // 수정 / 삭제
    
    
    @OneToOne(mappedBy = "message", fetch = FetchType.LAZY)
    private MessageImage messageImg;
    
    
    @PrePersist
    public void prePersist() {
    	this.sendTime = LocalDateTime.now();
    }
    
}
