package com.devlog.project.notification.entity;

import java.time.LocalDateTime;

import com.devlog.project.member.model.entity.Member;
import com.devlog.project.notification.NotiEnums;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "NOTIFICATION")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@ToString
@AllArgsConstructor
@Getter
@Setter
public class NotificationEntity {
	
	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "noti_seq")  
	@SequenceGenerator(																// 
	    name = "noti_seq",
	    sequenceName = "SEQ_NOTI_NO",
	    allocationSize = 1)
	private Long notificationNo;
	
    // 보낸 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEND_MEMBER_NO", nullable = false)
    private Member sender;

    // 받는 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECEIVE_MEMBER_NO", nullable = false)
    private Member receiver;

    @Column(name = "CREATE_AT")
    private LocalDateTime createAt;

    @Column(name = "IS_READ", length = 1)
    @Enumerated(EnumType.STRING)
    private NotiEnums.IsRead isRead;

    @Column(name = "CONTENT", length = 300)
    private String content;

    @Column(name = "TYPE", length = 30)
    @Enumerated(EnumType.STRING)
    private NotiEnums.NotiType type;

    @Column(name = "TARGET_TYPE", length = 30)
    @Enumerated(EnumType.STRING)
    private NotiEnums.TargetType targetType;

    @Column(name = "TARGET_ID")
    private Long targetId;
    
    @Column(name = "PREVIEW", length = 4000)
    private String preview;
    
    @PrePersist
    public void prePersist() {
    	
    	this.createAt = LocalDateTime.now();
    	
    	this.isRead = NotiEnums.IsRead.N;
    }

	
}
