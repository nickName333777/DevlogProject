package com.devlog.project.manager.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.devlog.project.manager.model.dto.ReportManagerDTO;
import com.devlog.project.report.enums.ReportStatus;
import com.devlog.project.report.enums.ReportTargetEnums;
import com.devlog.project.report.model.entity.Report;

public interface ManagerReportRepository extends JpaRepository<Report, Long> {

	@Query("""
		    select new com.devlog.project.manager.model.dto.ReportManagerDTO(
		        r.reportId,
		        r.targetId,
		        r.messageNo,
		        rc.reportType,
		        r.targetType,
		        r.content,
		        reporter.memberNickname,
		        reported.memberNickname,
		        r.createdAt,
		        r.processedAt,
		        r.status,
		        msg.messageContent
		    )
		    from Report r
		    join r.reportCode rc
		    join r.reporter reporter
		    join r.reported reported
		    left join r.message msg
		    order by r.createdAt desc
		""")
		List<ReportManagerDTO> findAllForManager();

    
    @Query("""
    	 select r
    	 from Report r
    	 where r.status = 'PENDING'
    	 and r.targetType = 'BOARD'
    """)
    List<Report> findPendingBoardReports();
    
    
}


