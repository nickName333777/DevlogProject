package com.devlog.project.manager.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.devlog.project.manager.model.dto.ReportManagerDTO;
import com.devlog.project.report.enums.ReportStatus;
import com.devlog.project.report.enums.ReportTargetEnums;

@Mapper
public interface ManagerReportMapper {

	// 게시글 조회
    int selectBoardCode(@Param("boardNo") Long boardNo);
    
    // 게시글 삭제
    void deleteBoard(Long boardNo);
    
    // 게시글 삭제 여부 조회
    int isBoardDeleted(Long boardNo);

    // 신고 검색
    List<ReportManagerDTO> searchForManager(
        @Param("query") String query,
        @Param("reportType") String reportType,
        @Param("status") ReportStatus status,
        @Param("targetType") ReportTargetEnums targetType
    );
    
}