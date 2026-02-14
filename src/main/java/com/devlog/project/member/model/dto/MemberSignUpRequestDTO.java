package com.devlog.project.member.model.dto;

import com.devlog.project.member.enums.CommonEnums.Status;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MemberSignUpRequestDTO {

    // 로그인 정보 
    private String memberEmail;
    private String memberPw;

    // 기본 정보 
    private String memberName;
    private String memberNickname;
    private String memberTel;
    private String memberCareer;

    // 가입 시 선택/필수 옵션
    private Status memberAdmin;      // Y / N; Service에서 반드시 권한 체크 필요
    private Status memberSubscribe;  // Y / N
}