package com.devlog.project.board.jobposting.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CompanyDTO {
	
	private Long companyCode;
	private String companyName;
	private String workAddr;
	private String nearbySub;
}
