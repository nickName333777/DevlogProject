package com.devlog.project.report.model.service;

import java.util.List;

import com.devlog.project.report.model.dto.ReportRequestDTO;
import com.devlog.project.report.model.dto.ReportTargetDTO;
import com.devlog.project.report.model.dto.ReportTypeDTO;

public interface ReportService {
	
	
	// 그 그 그 그 그 그 신고 대상 회원 조회
	ReportTargetDTO findMember(Long targetMemberNo);
	
	// 신고 유형 조회
	List<ReportTypeDTO> findReportType();
	
	
	// 신고 정보 삽입
	String reportInsertJpa(ReportRequestDTO req);

	
	// 게시글 신고
	String reportInsertBoard(ReportRequestDTO req);

}
