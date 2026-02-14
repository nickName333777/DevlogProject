package com.devlog.project.member.model.dto;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class KakaoSocialLoginResponseDTO {
    private Long socialNo;
	private String provider;
	private String providerId; // 카카오 사용자 고유 번호
	private Long memberNo; // memberNo = member.getMemberNo()
}
