package com.devlog.project.main.controller.websocket;

import java.util.List;

import org.springframework.stereotype.Service;

import com.devlog.project.main.controller.websocket.mapper.OnlineMapper;
import com.devlog.project.member.model.dto.FollowDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OnlineService {
	
	
	private final OnlineMapper mapper;
	
	public List<FollowDTO> selectFollow(Long memberNo) {
		return mapper.selectFollow(memberNo);
	}

	public List<FollowDTO> selectFollows(Long targetMemberNo) {
		return mapper.selectFollowers(targetMemberNo);
	}

	
	
	
	
	
}
