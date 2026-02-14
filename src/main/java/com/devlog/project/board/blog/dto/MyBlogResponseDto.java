package com.devlog.project.board.blog.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MyBlogResponseDto {
	// 프로필 정보 
	private UserProfileDto userProfile;
    
	// 유저 PICK 인기 글
	private BlogDTO pickPost;
	
	// 태그 목록
	private List<TagDto> tags;
	
    // 주인 여부
    private boolean isOwner;
    
    // 방문자/구독자 통계
    private int followerCount;
    private int followingCount;
    private int subscriberCount;
    private int totalVisit;
    private int postCount;
    private int todayVisit;
    
    private int subPrice;
    
    
    private Integer memberLevel;
    
    private Integer currentExp;
    
    private Integer nextExp;
    
    private String levelTitle;
}
