package com.devlog.project.manager.model.dto;

import java.time.LocalDateTime;

import com.devlog.project.report.enums.ReportStatus;
import com.devlog.project.report.enums.ReportTargetEnums;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReportManagerDTO {

	private String targetUrl; // 관리자가 이동할 게시글 주소
	
    private Long reportId;
    private Long targetId;
    private String reportType;
    private Long messageNo;
    
    private ReportTargetEnums targetType;
    private String reportReason;

    private String reporterNickname;
    private String targetNickname;

    private LocalDateTime reportDate;
    private LocalDateTime processDate;

    private ReportStatus status;
    
    private String messageContent;

    public ReportManagerDTO(
    	    Long reportId,
    	    Long targetId,
    	    Long messageNo,
    	    String reportType,
    	    ReportTargetEnums targetType,
    	    String reportReason,
    	    String reporterNickname,
    	    String targetNickname,
    	    LocalDateTime reportDate,
    	    LocalDateTime processDate,
    	    ReportStatus status,
    	    String messageContent
    	) {
    	    this.reportId = reportId;
    	    this.targetId = targetId;
    	    this.messageNo = messageNo;
    	    this.reportType = reportType;
    	    this.targetType = targetType;
    	    this.reportReason = reportReason;
    	    this.reporterNickname = reporterNickname;
    	    this.targetNickname = targetNickname;
    	    this.reportDate = reportDate;
    	    this.processDate = processDate;
    	    this.status = status;
    	    this.messageContent = messageContent;
    	}

}
