package com.devlog.project.report.model.reporitory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devlog.project.report.enums.ReportTargetEnums;
import com.devlog.project.report.model.entity.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
	
	
	
	boolean existsByReporter_MemberNoAndTargetTypeAndTargetId(Long memberNo, ReportTargetEnums targetType,
			Long taregetNo);

}
