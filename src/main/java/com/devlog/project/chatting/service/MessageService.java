package com.devlog.project.chatting.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.devlog.project.chatting.dto.MessageDTO;
import com.devlog.project.chatting.dto.MessageDTO.ChatMessage;
import com.devlog.project.chatting.dto.MessageDTO.ChatMessageResponse;
import com.devlog.project.chatting.dto.MessageDTO.ImageRequest;
import com.devlog.project.chatting.dto.MessageDTO.MessageEdit;
import com.devlog.project.chatting.dto.QueryMessageResponseDTO;

public interface MessageService {
	
	
	// 채팅 메세지 삽입
	ChatMessageResponse insertMsg(ChatMessage msg);
	
	// 안 읽은 메세지 계산
	Long countUnreadMsg(Long memberNo, Long roomNo);
	
	
	// 메세지 수정
	void editMessage(MessageEdit editDto);
	
	
	// 이미지 삽입
	ChatMessageResponse sendImg(ImageRequest dto) throws IllegalStateException, IOException;
	
	// 메세지 삭제
	void deleteMessage(Long messageNo);
	
	
	// 메세지 공감 삽입
	void sendEmoji(Map<String, Object> paramMap);
	
	
	// 메세지 리스트 조회
	List<QueryMessageResponseDTO> searchMessageList(Map<String, Object> paramMap);
	
	// 채팅방 마지막 메세지 조회
	Integer selectLastMessageNo(Long roomNo);

}
