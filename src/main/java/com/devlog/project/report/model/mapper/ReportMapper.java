package com.devlog.project.report.model.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.devlog.project.report.model.dto.ReportRequestDTO;
@Mapper
public interface ReportMapper {

	// 중복 신고 확인
	public int checkReportExist(ReportRequestDTO req);
	
	// 신고 정보 삽입
	public int insertBoardReport(ReportRequestDTO req);
}
