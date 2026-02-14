package com.devlog.project.report.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportTargetDTO {
	
	private Long memberNo;
	private String memberNickname;
	private String profilePath;
	
}
