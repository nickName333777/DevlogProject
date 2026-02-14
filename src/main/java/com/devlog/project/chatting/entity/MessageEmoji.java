package com.devlog.project.chatting.entity;

import com.devlog.project.member.model.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "MESSAGE_EMOJI")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class MessageEmoji {
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reaction_seq")  
	@SequenceGenerator(																// 
	    name = "reaction_seq",
	    sequenceName = "SEQ_REACTION_NO",
	    allocationSize = 1)
	private Long reactionNo;
	
	@JoinColumn(name = "MESSAGE_NO", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Message message;
	
	@JoinColumn(name = "EMOJI_CODE", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Emoji emoji;
	
	@JoinColumn(name = "MEMBER_NO", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Member member;

}
