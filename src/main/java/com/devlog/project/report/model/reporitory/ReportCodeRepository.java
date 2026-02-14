package com.devlog.project.report.model.reporitory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devlog.project.report.model.entity.ReportCode;

@Repository
public interface ReportCodeRepository extends JpaRepository<ReportCode, Long> {

}
