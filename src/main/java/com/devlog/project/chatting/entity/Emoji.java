package com.devlog.project.chatting.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "EMOJI")
public class Emoji {
	
	@Id
	@Column(name = "EMOJI_CODE" )
	private Long emojiCode;
	
	@Column(name = "EMOJI")
	private String emoji;

}
