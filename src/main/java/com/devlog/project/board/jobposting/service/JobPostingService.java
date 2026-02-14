package com.devlog.project.board.jobposting.service;

import java.util.List;

import com.devlog.project.board.jobposting.dto.JobPostingDTO;

public interface JobPostingService {
	
	// 채용공고 크롤링
	void JobCrawler();

	// 채용리스트 뽑아오기
	List<JobPostingDTO> selectjoblist();

	
	// 채용공고 상세 이동
	JobPostingDTO selectDetail(Long id);

}
