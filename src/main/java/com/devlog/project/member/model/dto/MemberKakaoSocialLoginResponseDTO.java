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
public class MemberKakaoSocialLoginResponseDTO {
	
	private MemberLoginResponseDTO memberDTO;
	
    // for kakao social login
    private String accessToken;
    private String kakaoId;
    
}
