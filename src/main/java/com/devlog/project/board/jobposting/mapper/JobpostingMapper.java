package com.devlog.project.board.jobposting.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.devlog.project.board.jobposting.dto.JobPostingDTO;


@Mapper
public interface JobpostingMapper {

	
	// 채용 리스트 뽑아오기
	List<JobPostingDTO> selectjoblist();

	
	// 채용공고 상세 이동
	JobPostingDTO selectDetail(Long id);



}
