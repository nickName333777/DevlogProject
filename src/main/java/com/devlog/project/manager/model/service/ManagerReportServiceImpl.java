package com.devlog.project.manager.model.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devlog.project.manager.model.dto.ReportManagerDTO;
import com.devlog.project.manager.model.mapper.ManagerReportMapper;
import com.devlog.project.manager.model.repository.ManagerReportRepository;
import com.devlog.project.report.enums.ReportStatus;
import com.devlog.project.report.enums.ReportTargetEnums;
import com.devlog.project.report.model.entity.Report;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ManagerReportServiceImpl implements ManagerReportService {

    private final ManagerReportRepository reportRepository;
    private final ManagerReportMapper managerReportMapper;

    @Override
    @Transactional
    public List<ReportManagerDTO> getReportList() {

        List<ReportManagerDTO> list = reportRepository.findAllForManager();

        list.forEach(dto -> {

            // 게시글 신고인 경우만 이동 URL 제공
            if (dto.getTargetType() == ReportTargetEnums.BOARD) {

                Long boardNo = dto.getTargetId();

                int boardCode = managerReportMapper.selectBoardCode(boardNo);

                dto.setTargetUrl(
                    resolveBoardUrl(boardCode, boardNo)
                );

            } else {
                // 메시지 신고는 직접 확인. 이동 URL 없음
                dto.setTargetUrl(null);
            }
        });
        return list;
    }


    @Override
    @Transactional
    public void updateReportStatuses(List<Long> reportIds, ReportStatus status) {

        for (Long reportId : reportIds) {

            Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고 내역 없음"));

            if (report.getStatus() != ReportStatus.PENDING) continue;

            // 게시글 신고 + 처리완료일 때만 삭제
            if (status == ReportStatus.RESOLVED
                && report.getTargetType() == ReportTargetEnums.BOARD) {

                managerReportMapper.deleteBoard(report.getTargetId());
            }

            report.setStatus(status);
            report.setProcessedAt(LocalDateTime.now());
        }
    }

    /**
     * 게시판 코드에서 게시글 URL 변환
     * 알림 서비스와 동일한 방식으로 진행함
     */
    private String resolveBoardUrl(int boardCode, Long boardNo) {
        return switch (boardCode) {
            case 1 -> "/blog/" + boardNo;
            case 21, 22, 23, 24, 25, 26 -> "/ITnews/" + boardNo;
            case 3 -> "/board/freeboard/" + boardNo;
            default -> throw new IllegalArgumentException(
                "Invalid boardCode: " + boardCode
            );
        };
    }

    
    @Transactional
    public void syncResolvedReports() {

        List<Report> reports = reportRepository.findPendingBoardReports();

        for (Report report : reports) {
            Long boardNo = report.getTargetId();

            int isDeleted = managerReportMapper.isBoardDeleted(boardNo);

            if (isDeleted == 1) {
                report.setStatus(ReportStatus.RESOLVED);
                report.setProcessedAt(LocalDateTime.now());
            }
        }
        reportRepository.flush();
    }


    @Override
    @Transactional(readOnly = true)
    public List<ReportManagerDTO> getReportList(
        String query,
        String reportType,
        ReportStatus status,
        ReportTargetEnums targetType) {
        List<ReportManagerDTO> list;
        if (isSearchMode(query, reportType, status, targetType)) {

            list = managerReportMapper.searchForManager(
                query,
                reportType,
                status,
                targetType
            );

        } else {
            list = reportRepository.findAllForManager();
        }
        list.forEach(dto -> {
            if (dto.getTargetType() == ReportTargetEnums.BOARD) {
                int boardCode =
                    managerReportMapper.selectBoardCode(dto.getTargetId());
                dto.setTargetUrl(resolveBoardUrl(boardCode, dto.getTargetId()));
            }
        });

        return list;
    }
    
    private boolean isSearchMode(
    	    String query,
    	    String reportType,
    	    ReportStatus status,
    	    ReportTargetEnums targetType
    	) {
    	    if (query != null && !query.trim().isEmpty()) {
    	        return true;
    	    }
    	    if (reportType != null && !reportType.trim().isEmpty()) {
    	        return true;
    	    }
    	    if (status != null) {
    	        return true;
    	    }
    	    if (targetType != null) {
    	        return true;
    	    }
    	    return false;
    	}



}

