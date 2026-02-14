package com.devlog.project.member.model.dto;

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
@AllArgsConstructor
@Builder
public class MemberProfileDTO {
	
	private Long memberNo;
	private Integer level;
	private String levelTitle;
	private String profileImg;
	private String memberNickname;
	private String email;

}
