package com.devlog.project.board.blog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserProfileDto {
	private String id;            // 아이디(이메일)
    private String nickname;      // 닉네임
    private String username;      // 실명
    private String job;           // 직업
    private String bio;           // 자기소개
    private String profileImgUrl; // 프로필 이미지 경로
    private String githubUrl;
    private String blogUrl;
    private int exp;              // 경험치
	
    @JsonProperty("isFollowed")
    private boolean isFollowed; // 내가 이 사람을 팔로우 중인지 여부
    
    @JsonProperty("memberNo")
    private Long memberNo; // 구독하려고 가져올게요
}
