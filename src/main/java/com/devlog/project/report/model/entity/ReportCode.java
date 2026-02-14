package com.devlog.project.report.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
@Table(name = "REPORT_CODE")
public class ReportCode {
	
	@Id
	@Column(name = "REPORT_CODE")
	private Long reportCode;
	
	@Column(name = "REPORT_TYPE", length = 100, nullable = false)
	private String reportType;
}
