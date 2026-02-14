package com.devlog.project.Scheduler.dto;

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
public class Hot3DTO {
	
	private Long boardNo;
	
	private String boardTitle;
	
	private String thumnail;
}
