package com.devlog.project.member.model.entity;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.devlog.project.member.enums.CommonEnums;
import com.devlog.project.member.enums.CommonEnums.Status;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "MEMBER")
@Getter
@Setter
@ToString(exclude = {"memberLevel"})  // Level 필드 제외,  연관관계 모두 제외
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor // 시제 객체생성에 필요
public class Member {

	// PK
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MEMBER")
    @SequenceGenerator(
            name = "SEQ_MEMBER",
            sequenceName = "SEQ_MEMBER_NO",
            allocationSize = 1
    )
    @Column(name = "MEMBER_NO")
    private Long memberNo;

    // 로그인 정보
    @Column(name = "MEMBER_EMAIL", nullable = false, length = 30, unique = true)
    private String memberEmail;

    @Column(name = "MEMBER_PW", length = 200)
    private String memberPw;

    // 기본 정보
    @Column(name = "MEMBER_NAME", nullable = false, length = 30)
    private String memberName;

    @Column(name = "MEMBER_NICKNAME", nullable = false, length = 30)
    private String memberNickname;

    @Column(name = "MEMBER_TEL", nullable = false, length = 13)
    private String memberTel;

    @Column(name = "MEMBER_CAREER", nullable = false, length = 50)
    private String memberCareer;

    // 상태값 (ENUM)
    @Enumerated(EnumType.STRING)
    @Column(name = "MEMBER_SUBSCRIBE", nullable = false, length = 1)
    private Status memberSubscribe;

    @Enumerated(EnumType.STRING)
    @Column(name = "MEMBER_ADMIN", nullable = false, length = 1)
    private Status memberAdmin;

    @Enumerated(EnumType.STRING)
    @Column(name = "MEMBER_DEL_FL", nullable = false, length = 1)
    private Status memberDelFl;

    // 프로필
    @Column(name = "PROFILE_IMG", length = 300)
    private String profileImg;

    @Column(name = "MY_INFO_INTRO", length = 2000)
    private String myInfoIntro;

    @Column(name = "MY_INFO_GIT", length = 200)
    private String myInfoGit;

    @Column(name = "MY_INFO_HOMEPAGE", length = 200)
    private String myInfoHomepage;

    // 활동 정보
    @Column(name = "SUBSCRIPTION_PRICE", nullable = false)
    private Integer subscriptionPrice;

    @Column(name = "BEANS_AMOUNT", nullable = false)
    private Integer beansAmount;

    @Column(name = "CURRENT_EXP", nullable = false)
    private Integer currentExp;

    // 회원 가입 날짜
    @Column(name = "M_CREATE_DATE")
    private LocalDateTime mCreateDate;

    // 레벨 테이블키 참조 (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_LEVEL", nullable = false)
    private Level memberLevel;

    // 생성자 (회원가입용)
    @Builder // @Builder가 필드가 아닌 생성자에 붙어 있음 =>이 경우 생성자 파라미터 이름 기준으로 builder 메서드가 만들어짐
    public Member(String memberEmail, String memberPw, String memberName, String memberNickname,
                  String memberTel, String memberCareer, 
                  Status memberAdmin, Status memberSubscribe,
                  Level memberLevel) {

        this.memberEmail = memberEmail;
        this.memberPw = memberPw;
        this.memberName = memberName;
        this.memberNickname = memberNickname;
        this.memberTel = memberTel;
        this.memberCareer = memberCareer;

        // 기본값 세팅
        this.memberSubscribe = memberSubscribe != null ? memberSubscribe : Status.N;
        this.memberAdmin = memberAdmin != null ? memberAdmin : Status.N;
        this.memberDelFl = Status.N;
        this.subscriptionPrice = 0;
        this.beansAmount = 0;
        this.currentExp = 0;
        this.mCreateDate = LocalDateTime.now();
        
        // FK (LEVEL 테이블)
        this.memberLevel = memberLevel;
    }

    // Member Entity 메서드
    // 비밀번호 변경
    public void changePassword(String encodedPw) {
        this.memberPw = encodedPw;
    }

    // 회원탈퇴
    public void withdraw() {
        this.memberDelFl = Status.Y;
    }

    // 일반회원 -> 관리자 변경
    public void promoteAdmin() {
        this.memberAdmin = Status.Y;
    }
    
	@PrePersist // 기본값 설정, JPA가 INSERT하기 전 자동 실행 메소드(위 Member생성자 기본값 설정의 double-check) 
	public void prePersist() {
		this.mCreateDate = LocalDateTime.now(); // 디폴트값 지정이라고 봐라.
        this.subscriptionPrice = 0;
        this.beansAmount = 0;
        this.currentExp = 0;
        
		if(this.memberSubscribe == null) {
			this.memberSubscribe = CommonEnums.Status.N;
		}
		
		if(this.memberAdmin == null) {
			this.memberAdmin = CommonEnums.Status.N;
		}
		
		if(this.memberDelFl == null) {
			this.memberDelFl = CommonEnums.Status.N;
		}		
	}
	
	// 회원 정보 수정 메소드: 닉네임 & 전화번호
	public void updateMemberInfo(String memberNickname, String memberTel) {
		this.memberNickname = memberNickname; // 필드값 세팅 -> 업데이트
		this.memberTel = memberTel;
	}    
    
	
	// 회원 정보 수정 메소드 (회원 커피콩 잔액 수정)
	/**
	 * 커피콩 잔액 업데이트
	 * @param newAmount 새로운 커피콩 잔액
	 */
	public void updateBeansAmount(Integer newAmount) {
	    if(newAmount < 0) {
	        throw new IllegalArgumentException("커피콩 잔액은 음수일 수 없습니다.");
	    }
	    this.beansAmount = newAmount;
	}
	
	/**
	 * 커피콩 차감
	 * @param amount 차감할 커피콩 수
	 */
	public void deductBeans(Integer amount) {
	    if(this.beansAmount < amount) {
	        throw new IllegalStateException("커피콩 잔액이 부족합니다.");
	    }
	    this.beansAmount -= amount;
	}
	
	/**
	 * 커피콩 충전
	 * @param amount 충전할 커피콩 수
	 */
	public void chargeBeans(Integer amount) {
	    if(amount <= 0) {
	        throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
	    }
	    this.beansAmount += amount;
	}
	
}

