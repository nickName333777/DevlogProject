package com.devlog.project.Scheduler.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.devlog.project.Scheduler.dto.Hot3DTO;
import com.devlog.project.Scheduler.mapper.MailMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
	
	private final MailMapper mailMapper;
	
	
	@Override
	public List<Hot3DTO> selectHotList() {
		
	
		return mailMapper.selectHotList();
	}

}
