package com.devlog.project.chatting.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.devlog.project.chatting.dto.ChattingDTO.ChattingListDTO;
import com.devlog.project.chatting.dto.ChattingDTO.FollowListDTO;

@Mapper
public interface ChattingMapper {
	
	
	// 채팅방 목록 조회
	List<ChattingListDTO> selectChatList(Long memberNo);
	
	// 검색어 일치하는 채팅방 목록 조회
	List<ChattingListDTO> selectQueryChatList(Long memberNo, String query);
	
	// 팔로우 회원 목록 조회
	List<FollowListDTO> selectFollowList(Long memberNo, Long roomNo);


}
