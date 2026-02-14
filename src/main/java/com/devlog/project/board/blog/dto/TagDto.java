package com.devlog.project.board.blog.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TagDto {
	private String name; 	// 태그 이름
	private int count; 		// 사용 횟수
}
