package com.devlog.project.myPage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class MemberUpdateDto {
	
	@JsonProperty("memberNickname")
	private String memberNickname;   // 닉네임

	@JsonProperty("memberCareer")
    private String memberCareer;     // 경력
	
	@JsonProperty("myInfoIntro")
    private String myInfoIntro;      // 소개글
	
	@JsonProperty("memberTel")
    private String memberTel;        // 전화번호
	
	@JsonProperty("myInfoGit")
    private String myInfoGit;        // 깃허브 주소
	
	@JsonProperty("myInfoHomepage")
    private String myInfoHomepage;   // 홈페이지 주소
}
