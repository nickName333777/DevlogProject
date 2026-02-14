package com.devlog.project.chatting.entity;

import java.time.LocalDateTime;

import com.devlog.project.member.model.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "MESSAGE_IMG")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MessageImage {
	
	@Id
	@Column(name = "MSG_IMG_NO")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "msg_image_seq")  
	@SequenceGenerator(																// 
	    name = "msg_image_seq",
	    sequenceName = "SEQ_MSG_IMG_NO",
	    allocationSize = 1)
	private Long msgImageNo;
	
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MESSAGE_NO")
    private Message message;
	
	
	@Column(name = "IMG_PATH")
	private String imgPath;
	
	@Column(name = "ORIGIN_NAME")
	private String original;
	
	@Column(name = "RENAME_NAME")
	private String rename;

}
