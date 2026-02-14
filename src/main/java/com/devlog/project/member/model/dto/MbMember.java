package com.devlog.project.member.model.dto;


import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MbMember {

    private Long memberNo;              // MEMBER_NO
    private String memberEmail;         // MEMBER_EMAIL
    private String memberPw;            // MEMBER_PW
    private String memberName;          // MEMBER_NAME
    private String memberNickname;      // MEMBER_NICKNAME
    private String memberTel;           // MEMBER_TEL
    private String memberCareer;        // MEMBER_CAREER

    private String memberSubscribe;      // MEMBER_SUBSCRIBE (Y/N)
    private String memberAdmin;          // MEMBER_ADMIN (Y/N)

    private String profileImg;           // PROFILE_IMG
    private String memberDelFl;          // MEMBER_DEL_FL (Y/N)

    private LocalDateTime mCreateDate;    // M_CREATE_DATE

    private Integer subscriptionPrice;      // SUBSCRIPTION_PRICE
    private String myInfoIntro;          // MY_INFO_INTRO
    private String myInfoGit;            // MY_INFO_GIT
    private String myInfoHomepage;       // MY_INFO_HOMEPAGE

    private Integer beansAmount;            // BEANS_AMOUNT
    private Integer currentExp;             // CURRENT_EXP

    private Integer memberLevel;         // MEMBER_LEVEL (LV1 ~ LV30)

}
