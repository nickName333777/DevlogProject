package com.devlog.project.member.model.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FollowDTO {
	
	private Long memberNo;
	private String memberNickname;
	private String profile;
	private String memberEmail;
}
