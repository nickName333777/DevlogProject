package com.devlog.project.member.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberLoginRequestDTO {
    private String memberEmail;
    private String memberPw;
}