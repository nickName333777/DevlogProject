package com.devlog.project.member.model.dto;

import java.time.LocalDateTime;

import com.devlog.project.member.enums.CommonEnums.Status;
import com.devlog.project.member.model.entity.Level;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class MemberInfoResponseDTO {
	
	@JsonProperty("memberNo") 
    private Long memberNo;
	
	@JsonProperty("memberEmail") 
    private String memberEmail;
	
	@JsonProperty("memberName") 
    private String memberName;
	
	@JsonProperty("memberNickname") 
    private String memberNickname;
	
	@JsonProperty("memberTel") 
    private String memberTel;
	
	@JsonProperty("role") 
    private String role; // ROLE_USER / ROLE_ADMIN; auto by spring-security
	
    ////////
	@JsonProperty("memberAdmin") 
    private Status memberAdmin; // 'N': 일반회원,  'Y':관리자
    
	@JsonProperty("memberSubscribe") 
	private Status memberSubscribe;
	
	@JsonProperty("memberDelFl") 
    private Status memberDelFl; // 'N': 회원,      'Y':탈퇴회원 
	
    //////////
	@JsonProperty("memberCareer") 
	private String memberCareer;
	
	@JsonProperty("profileImg") 
    private String profileImg;

	@JsonProperty("myInfoIntro") 
	private String myInfoIntro;
	
	@JsonProperty("myInfoGit") 
    private String myInfoGit;
	
	@JsonProperty("myInfoHomepage") 
    private String myInfoHomepage;
	
	@JsonProperty("subscriptionPrice") 
    private Integer subscriptionPrice;
	
	@JsonProperty("beansAmount") 
    private Integer beansAmount;
	
	@JsonProperty("currentExp") 
    private Integer currentExp;

	@JsonProperty("mCreateDate") 
	private LocalDateTime mCreateDate;
    
	@JsonProperty("memberLevel") 
	private LevelDTO memberLevel; // Level 엔티티말고, LevelDTO를 넣어줘야함 => 매핑은 Service에서 해주어야함
    
}
