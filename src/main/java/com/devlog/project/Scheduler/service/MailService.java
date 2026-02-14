package com.devlog.project.Scheduler.service;

import java.util.List;

import com.devlog.project.Scheduler.dto.Hot3DTO;

public interface MailService {
	
	void sendHot3Mail(List<String> members, List<Hot3DTO> hotList);

}
