package com.devlog.project.board.jobposting.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class JobPostingDTO extends CompanyDTO {
	
	private Long postingNo;
	private String postingTitle;
	private String postingContent;
	private String recField;
	private String recCount;
	private String empType;
	private String salary;
	private String reqCareer;
	private String reqEducation;
	private String applyStart;
	private String applyEnd;
	private String applyMethod;
	private Long companyCode;
	
	
	// 여러개 이미지 url 리스트
	private List<String> imageList;

}
