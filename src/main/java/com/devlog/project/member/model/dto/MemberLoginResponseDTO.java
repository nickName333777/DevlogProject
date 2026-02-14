package com.devlog.project.member.model.dto;

import java.time.LocalDateTime;

import com.devlog.project.member.enums.CommonEnums.Status;
import com.devlog.project.member.model.entity.Level;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class MemberLoginResponseDTO {
	
    private Long memberNo;
    private String memberEmail;
    private String memberNickname;
    private String role; // ROLE_USER / ROLE_ADMIN; auto by spring-security
    ////////
    private Status memberAdmin; // 'N': 일반회원,  'Y':관리자
    private Status memberSubscribe; 
    private Status memberDelFl; // 'N': 회원,      'Y':탈퇴회원 
    //////////
    private String memberCareer;
    private String profileImg;
    private String myInfoIntro;
    private String myInfoGit;
    private String myInfoHomepage;
    private Integer subscriptionPrice;
    private Integer beansAmount;
    private Integer currentExp;
    private LocalDateTime mCreateDate;
    // //private Level memberLevel; //Level 엔티티를 DTO에 그대로 넣으면, "Hibernate LAZY 프록시 객체"를 그대로 JSON으로 변환하려고 해서 실패(Jackson에 걸려에러발생)    
    private LevelDTO memberLevel; // Level 엔티티말고, LevelDTO를 넣어줘야함 => 매핑은 Service에서 해주어야함
    
}
