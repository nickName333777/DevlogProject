package com.devlog.project.member.model.entity;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
//import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table( // ALTER TABLE SOCIAL_LOGIN ADD CONSTRAINT UK_SOCIAL_LOGIN UNIQUE (PROVIDER, PROVIDER_ID); 에 대응되게 Entity 설정
	    name = "SOCIAL_LOGIN",
	    uniqueConstraints = {
	        @UniqueConstraint(
	            name = "UK_SOCIAL_LOGIN",
	            columnNames = {"PROVIDER", "PROVIDER_ID"}
	        )
	    }
	)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor // 객체생성에 필요
@ToString
public class SocialLogin { // 

	// PK
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SOCIAL_LOGIN")
    @SequenceGenerator(
            name = "SEQ_SOCIAL_LOGIN",
            sequenceName = "SEQ_SOCIAL_LOGIN_NO",
            allocationSize = 1
    )
    @Column(name = "SOCIAL_NO")
    private Long socialNo;
    
    
    // 소셜로그인 제공자 // "KAKAO"
    @Column(name = "PROVIDER", nullable = false, length = 30)
    private String provider;

    // 소셜로그인 제공자서비스에서의 식별자(ex: 카카오 사용자 id) // kakaoId
    @Column(name = "PROVIDER_ID", nullable = false, length = 100)
    private String providerId;
    
    // 멤버 테이블키 참조 (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_NO", nullable = false)
    private Member memberNo;	// Member Entity
	
    // 생성자 (소셜로그인용)
    @Builder // @Builder가 필드가 아닌 생성자에 붙어 있음 =>이 경우 생성자 파라미터 이름 기준으로 builder 메서드가 만들어짐
    public SocialLogin(String provider, String providerId, 
                  Member memberNo) {

        this.provider = provider;
        this.providerId = providerId;
        
        // FK (MEMBER 테이블)
        this.memberNo = memberNo; // Member Entity
    }	
}
