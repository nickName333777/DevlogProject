package com.devlog.project.manager.model.service;

import java.util.List;

import com.devlog.project.manager.model.dto.ReportManagerDTO;
import com.devlog.project.report.enums.ReportStatus;
import com.devlog.project.report.enums.ReportTargetEnums;

public interface ManagerReportService {

    List<ReportManagerDTO> getReportList();

    // 신고 목록 조회
    void updateReportStatuses(List<Long> reportIds, ReportStatus status);
    
    // 처리 상태 변경하기
    void syncResolvedReports();

    // 검색 기반 신고 목록 조회
	List<ReportManagerDTO> getReportList(String query, String reportType, ReportStatus status,
			ReportTargetEnums targetType);
}