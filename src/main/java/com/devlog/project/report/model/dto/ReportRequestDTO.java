package com.devlog.project.report.model.dto;

import com.devlog.project.report.enums.ReportTargetEnums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDTO {
	
	// 신고 코드
	private Long reportCode;
	
	// 신고 대상 회원번호
	private Long targetMemberNo;
	
	// 상세 싱고 사유
	private String reportReason;
	
	// 대상 타입 (BOARD / MESSAGE)
	private ReportTargetEnums targetType;
	
	// 신고 대상 번호 (게시글 or 메시지)
	private Long targetNo;
	
	// 본인 회원 번호 세션에서 가져와서 설정
	private Long memberNo;
	
	

}
