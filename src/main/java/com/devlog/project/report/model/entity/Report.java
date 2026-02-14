package com.devlog.project.report.model.entity;

import java.time.LocalDateTime;

import com.devlog.project.member.model.entity.Member;
import com.devlog.project.report.enums.ReportStatus;
import com.devlog.project.report.enums.ReportTargetEnums;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="REPORT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class Report {


	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_seq")
	@SequenceGenerator(
			name = "report_seq",
			sequenceName = "SEQ_REPORT_NO",
			allocationSize = 1
			)
	@Column(name = "REPORT_ID")
	private Long reportId;


	@Column(name = "BOARD_CODE")
	private Integer boardCode;
	
	// 게시글 / 메시지
	@Column(name = "TARGET_TYPE", length = 15, nullable = false)
	@Enumerated(EnumType.STRING)
	private ReportTargetEnums targetType;


	// 신고 사유
	@Column(name = "CONTENT", length = 300)
	private String content;


	// 신고 대상 번호
	@Column(name = "TARGET_ID", nullable = false)
	private Long targetId;


	// 신고된 메세지 번호
	@Column(name = "MESSAGE_NO")
	private Long messageNo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MESSAGE_NO", insertable = false, updatable = false)
	private com.devlog.project.chatting.entity.Message message;


	@Column(name = "CREATED_AT", nullable = false, updatable = false)
	private LocalDateTime createdAt;


	@Column(name = "PROCESSED_AT")
	private LocalDateTime processedAt;


	@Column(name = "STATUS", length = 15, nullable = false)
	@Enumerated(EnumType.STRING)
	private ReportStatus status;


	// 신고한 회원
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "REPORTER_NO", nullable = false)
	private Member reporter;


	// 신고 당한 회원
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "REPORTED_NO", nullable = false)
	private Member reported;


	// 신고 유형 코드
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "REPORT_CODE", nullable = false)
	private ReportCode reportCode;


	@PrePersist
	protected void onCreate() {
	    this.createdAt = LocalDateTime.now();
	    if (this.status == null) {
	        this.status = ReportStatus.PENDING;
	    }
	}

}
