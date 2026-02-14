package com.devlog.project.chatting.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.devlog.project.chatting.chatenums.MsgEnums;
import com.devlog.project.chatting.chatenums.MsgEnums.MsgStatus;
import com.devlog.project.chatting.chatenums.MsgEnums.MsgType;
import com.devlog.project.chatting.dto.EmojiDTO;
import com.devlog.project.chatting.dto.EmojiDTO.updateEmojiDTO;
import com.devlog.project.chatting.dto.MessageDTO;
import com.devlog.project.chatting.dto.MessageDTO.ChatMessage;
import com.devlog.project.chatting.dto.MessageDTO.ChatMessageResponse;
import com.devlog.project.chatting.dto.MessageDTO.ImageRequest;
import com.devlog.project.chatting.dto.MessageDTO.MessageEdit;
import com.devlog.project.chatting.dto.MessageDTO.MessageEditResp;
import com.devlog.project.chatting.dto.QueryMessageResponseDTO;
import com.devlog.project.chatting.entity.ChatRoom;
import com.devlog.project.chatting.entity.Emoji;
import com.devlog.project.chatting.entity.Message;
import com.devlog.project.chatting.entity.MessageEmoji;
import com.devlog.project.chatting.entity.MessageImage;
import com.devlog.project.chatting.repository.ChatRoomRepository;
import com.devlog.project.chatting.repository.EmojiRepository;
import com.devlog.project.chatting.repository.MessageEmojiRepository;
import com.devlog.project.chatting.repository.MessageImageRepository;
import com.devlog.project.chatting.repository.MessageRepository;
import com.devlog.project.common.utility.Util;
import com.devlog.project.member.model.entity.Member;
import com.devlog.project.member.model.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
	
	private final ChatRoomRepository roomRepository;
	private final MessageRepository msgRepository;
	private final MemberRepository memberRepository;
	private final SimpMessagingTemplate template;
	private final MessageImageRepository msgImageRepository;
	
	private final EmojiRepository emojiRepository;
	
	private final MessageEmojiRepository msgEmojiRepository;
	
	@Value("${my.chatMessage.location}")
	private String filePath;
	
	@Value("${my.chatMessage.webpath}")
	private String webPath;
	
	// 메세지 삽입
	@Override
	public ChatMessageResponse insertMsg(ChatMessage msg) {
		
		ChatRoom room = roomRepository.findById(msg.getChatRoomNo())
				.orElseThrow();
		
		Member member = memberRepository.findById(msg.getSender())
				.orElseThrow();
		
		
		
		Message message = Message.builder()
				.chattingRoom(room)
				.member(member)
				.messageContent(msg.getContent())
				.type(MsgEnums.MsgType.TEXT)
				.build();
		
		message = msgRepository.save(message);
		
		
		
		return MessageDTO.ChatMessageResponse.toDto(message);
	}
	
	
	// 안 읽은 메세지 계산
	@Override
	public Long countUnreadMsg(Long memberNo, Long roomNo) {
		return msgRepository.countUnreadMsg(memberNo, roomNo);
	}

	
	
	// 메세지 수정
	@Override
	@Transactional
	public void editMessage(MessageEdit editDto) {
		
		System.out.println("메세지 번호 확인 : " + editDto.getMessageNo());
		Message message = msgRepository.findById(editDto.getMessageNo())
					.orElseThrow();
		
		message.setMessageContent(editDto.getContent());
		message.setStatus(MsgEnums.MsgStatus.MOD);
		
		msgRepository.save(message);
		
		MessageDTO.MessageEditResp resp = new MessageEditResp();
		resp.setMessageNo(message.getMessageNo());
		resp.setStatus(message.getStatus());
		resp.setContent(message.getMessageContent());
		
		template.convertAndSend(
				"/topic/room/" + message.getChattingRoom().getRoomNo(),
				resp
				);
		
	}

	
	
	// 이미지 저장
	@Override
	@Transactional
	public ChatMessageResponse sendImg(ImageRequest dto) throws IllegalStateException, IOException {
		
		
		
		ChatRoom room = roomRepository.findById(dto.getRoomNo())
				.orElseThrow();
		
		Member member = memberRepository.findById(dto.getMemberNo())
				.orElseThrow();
		
		// 메세지 삽입
		Message msg = Message.builder()
				.chattingRoom(room)
				.member(member)
				.messageContent("사진")
				.type(MsgType.IMG)
				.build();
		msgRepository.save(msg);
		
		// 이미지 저장 
		MultipartFile img =  dto.getImg();
		
		String rename = Util.fileRename(img.getOriginalFilename());
		
		String imgPath = webPath + rename;
		
		// 해당 경로에 폴더 없으면 생성
		File dir = new File(filePath);
		if (!dir.exists()) {
		    boolean created = dir.mkdirs();
		    if (!created) {
		        throw new IOException("업로드 디렉토리 생성 실패: " + filePath);
		    }
		}
		
		File dest = new File(dir, rename);
		
		img.transferTo(dest);
		
		
		
		
		
		// 메세지 이미지 삽입
		MessageImage msgImage = MessageImage.builder()
								.message(msg)
								.imgPath(imgPath)
								.original(img.getOriginalFilename())
								.rename(rename)
								.build();
		msgImageRepository.save(msgImage);
		
		ChatMessageResponse res = MessageDTO.ChatMessageResponse.toDto(msg);
		res.setImgPath(imgPath);
		
		return res;
		
	}

	
	
	// 메세지 삭제
	@Override
	@Transactional
	public void deleteMessage(Long messageNo) {
		
		Message message = msgRepository.findById(messageNo)
						.orElseThrow();
		
		message.setStatus(MsgStatus.DEL);
		
		MessageDTO.DeleteMessageResponse delMsg = MessageDTO.DeleteMessageResponse.builder()
													.messageNo(messageNo)
													.status(message.getStatus())
													.build();
		
		System.out.println("삭제 메세지 dto 확인 : "+ delMsg);
		
		template.convertAndSend(
				"/topic/room/" + message.getChattingRoom().getRoomNo(),
					delMsg
				);
		
	}

	
	
	// 메세지 공감 삽입
	@Override
	@Transactional
	public void sendEmoji(Map<String, Object> paramMap) {
		
		Long memberNo = ((Number)paramMap.get("memberNo")).longValue();
		
		Long messageNo = ((Number)paramMap.get("messageNo")).longValue();
		
		Long emojiCode = ((Number)paramMap.get("emojiCode")).longValue();
		
		
		
		
		// 멤버
		
		Member member = memberRepository.findById(memberNo)
						.orElseThrow();
		
		
		// 메세지 
		
		Message message = msgRepository.findById(messageNo)
							.orElseThrow();
		
		// 이모지
		
		Emoji emoji = emojiRepository.findById(emojiCode)
					.orElseThrow();
		
		// 회원 + 메세지로 일치하는 것이 있는지 조회
		Optional<MessageEmoji> reaction = msgEmojiRepository.findByMessageAndMember(message, member);
		
		// 일치하는 것이 있다면 새이모지 코드 or 기존 이모지 코드로 업데이트
		if(reaction.isPresent()) {
			System.out.println("엔티티 존재 : " + emojiCode);
			reaction.get().setEmoji(emoji);
			
		} else { // 일치하는 것이 없다면 새 엔티티 생성 후 저장
			MessageEmoji newReaction = MessageEmoji.builder()
										.emoji(emoji)
										.member(member)
										.message(message)
										.build();
			
			msgEmojiRepository.save(newReaction);
			
			System.out.println("이모지 저장 성공 ! ! !");
		}
		
		List<Long> messageNos = new ArrayList<>();
		messageNos.add(messageNo);
		
		List<EmojiDTO> emojiDtos = msgEmojiRepository.findEmojiCount(messageNos);
						
		
		Map<String, Long> reactionMap = new LinkedHashMap<>();
		
		for (EmojiDTO emojiDTO : emojiDtos) {
			
			String key = emojiDTO.getEmoji();
			
			Long value = emojiDTO.getCount();
			
			reactionMap.put(key, value);
		}
		
		
		
		EmojiDTO.updateEmojiDTO updateEmoji = new EmojiDTO.updateEmojiDTO();
		
		updateEmoji.setMessageNo(messageNo);
		updateEmoji.setReactions(reactionMap);
		updateEmoji.setType("Emoji");
		
		System.out.println("updateEmoji : " + updateEmoji);
		
		template.convertAndSend(
				"/topic/room/" + message.getChattingRoom().getRoomNo(),
				updateEmoji
				);
		
	}

	
	// 검색된 메세지 조회
	@Override
	public List<QueryMessageResponseDTO> searchMessageList(Map<String, Object> paramMap) {
		
		Long roomNo = ((Number)paramMap.get("roomNo")).longValue();
		String query = (String) paramMap.get("query");
		
		System.out.println(roomNo);
		System.out.println(query);
		
		return msgRepository.searchMessage(roomNo, query);
	}

	
	
	// 채팅방 마지막 메세지 조회
	@Override
	public Integer selectLastMessageNo(Long roomNo) {
		
		return msgRepository.selectLastMessage(roomNo);
	}

}
