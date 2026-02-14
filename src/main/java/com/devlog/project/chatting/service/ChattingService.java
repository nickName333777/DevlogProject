package com.devlog.project.chatting.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.devlog.project.chatting.dto.ChattingDTO;
import com.devlog.project.chatting.dto.ChattingDTO.GroupCreateDTO;
import com.devlog.project.chatting.dto.ChattingDTO.RoomInfoDTO;
import com.devlog.project.chatting.dto.MentionDTO;
import com.devlog.project.chatting.dto.MessageDTO.ChatMessageResponse;
import com.devlog.project.chatting.dto.ParticipantDTO;

public interface ChattingService {
	
	
	// 채팅방 목록 조회
	List<ChattingDTO.ChattingListDTO> selectChatList(Long memberNo, String query);
	
	// 팔로우 목록 조회
	List<ChattingDTO.FollowListDTO> selectFollowList(Long long1, Long roomNo);
	
	
	// 개인 채팅방 생성
	Long privateCreate(Long myMemberNo, Long targetMemberNo);
	
	
	// 그룹 채팅방 생성
	Long groupCreate(GroupCreateDTO group, Long myMemberNo) throws IOException;
	
	
	// 채팅방 정보 조회
	RoomInfoDTO roomInfoLoad(Long roomNo, Long memberNo);
	
	
	// 채팅방 마지막 메세지 업데이트
	void updateLastRead(Long roomNo, Long memberNo);
	
	
	
	
	// 채팅방 참여 회원 조회
	List<Long> selectUsers(Long roomNo);
	
	
	// 채팅방 나가기
	void roomExit(Long roomNo, Long memberNo);
	
	
	// 유저 초대
	void userInvite(Map<String, Object> paramMap);
	
	// 채팅방 주인 여부
	boolean isOwner(Long roomNo, Long memberNo);
	
	
	// 멘션 후보 조회
	List<MentionDTO> mentionUsersSelect(Long roomNo, String keyword, Long memberNo);
	
	
	// 텍스트 내용중에 @닉네임 있나 검색
	void processMention(ChatMessageResponse res);

	Long selectLastReadNo(Long roomNo, Long memberNo);
	
	
	// 방 이름 변경
	void roomNameChange(Map<String, Object> paramMap);
	
	// 고정 여부 변경
	void pinUpdate(Map<String, Object> paramMap);
	
	
	// 채팅방 참여 유저 변경
	Long countParticipant(Long roomNo);

	
	

}
