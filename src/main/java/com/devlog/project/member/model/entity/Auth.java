package com.devlog.project.member.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name="AUTH")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor 
@ToString
public class Auth {

	// PK
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AUTH")
    @SequenceGenerator(
            name = "SEQ_AUTH",
            sequenceName = "SEQ_AUTH_NO",
            allocationSize = 1
    )
    @Column(name = "AUTH_NO")
    private Long authNo;
    
    
    // 이메일 인증 코드
    @Column(name = "CODE", nullable = false, length = 100)
    private String code;

    @Column(name = "EMAIL", nullable = false, length = 100, unique = true)
    private String email;
    
    // 이메일 인증 코드 발급 날짜
    @Column(name = "CREATE_AT", nullable = false)
    private LocalDateTime createAt;
    
    // 생성자 (이메일 인증 코드 발급용)
    @Builder // 
    public Auth(String authKey, String email) {
        this.code = authKey;
        this.email = email;
        this.createAt = LocalDateTime.now(); // 생성 시점 초기화
    }
    
    // Auth Entity 메서드
    // 코드 변경
	public void setCode(String authKey) {
		this.code = authKey; 
		this.createAt = LocalDateTime.now();
	}
	
}
