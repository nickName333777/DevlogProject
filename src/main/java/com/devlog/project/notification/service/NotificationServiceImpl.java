package com.devlog.project.notification.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devlog.project.board.ITnews.mapper.CommentMapper;
import com.devlog.project.chatting.repository.MessageRepository;
import com.devlog.project.member.model.entity.Member;
import com.devlog.project.member.model.repository.MemberRepository;
import com.devlog.project.notification.NotiEnums;
import com.devlog.project.notification.dto.NotifiactionDTO;
import com.devlog.project.notification.dto.NotifiactionDTO.ResponseDTO;
import com.devlog.project.notification.entity.NotificationEntity;
import com.devlog.project.notification.mapper.NotiMapper;
import com.devlog.project.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	private final NotificationRepository notificationRepository;
	private final MemberRepository memberRepository;
	private final SseService sseService;
	
	private final CommentMapper commentMapper;
	
	private final NotiMapper notiMapper;
	
	private final MessageRepository messageRepository;

	// 멘션 알림
	@Override
	public void sendNotification(NotifiactionDTO notification) {

		System.out.println("알림 저장 시도: " + notification);

		Member sender = memberRepository.findById(notification.getSender())
				.orElseThrow();

		Member recevier = memberRepository.findById(notification.getReceiver())
				.orElseThrow();


		NotificationEntity noti = NotificationEntity.builder()
				.sender(sender)
				.receiver(recevier)
				.content(notification.getContent())
				.preview(notification.getPreview())
				.type(notification.getType())
				.targetId(notification.getTargetId())
				.targetType(notification.getTargetType())
				.build();

		notificationRepository.save(noti);


		sseService.send(
				recevier.getMemberNo()
				);

	}


	// 안 읽은 메세지 개수 조회
	@Override
	public Long notiCount(Long memberNo) {
		return notificationRepository.countUnreadCount(memberNo);
	}



	// 알림 목록 조회
	@Override
	public List<ResponseDTO> selectList(String type, Long memberNo) {

		List<NotificationEntity> notis = null;

		if(type.equals("ALL")) {

			notis = notificationRepository.findAllNotiList(memberNo);
		} else {
			NotiEnums.NotiType notiType;

			notiType = NotiEnums.NotiType.valueOf(type);

			notis = notificationRepository.findOptionNotiList(memberNo, notiType);
		}

		List<ResponseDTO> listDto = new ArrayList<>();

		for (NotificationEntity noti  : notis) {

			listDto.add(ResponseDTO.toDTO(noti));  


		}

		return listDto;
	}


	// 읽음 처리 url 반환
	@Override
	@Transactional
	public String readAndGet(Long notiNo) {

		NotificationEntity noti = notificationRepository.findById(notiNo)
				.orElseThrow();

		noti.setIsRead(NotiEnums.IsRead.Y);

		String url = selectUrl(noti);

		return url;
	}


	public String selectUrl(NotificationEntity noti) {

	    return switch (noti.getTargetType()) {

	        case COMMENT -> {
	            Long boardNo = notiMapper.selectBoardNo(noti.getTargetId());

	            if (boardNo == null)
	                throw new IllegalStateException("boardNo null, commentNo = " + noti.getTargetId());

	            int boardCode = notiMapper.selectBoardCode(boardNo);

	            yield returnBoardUrl(boardCode, boardNo)
	                    + "#comment-" + noti.getTargetId();
	        }

	        case BOARD -> {
	            int boardCode = notiMapper.selectBoardCode(noti.getTargetId());

	            yield returnBoardUrl(boardCode, noti.getTargetId());
	        }

	        case MESSAGE -> {
	            Long roomNo = messageRepository.findRoomNoByMessageNo(noti.getTargetId());

	            if (roomNo == null)
	                throw new IllegalStateException("roomNo null, messageNo = " + noti.getTargetId());

	            yield "/devtalk?roomNo=" + roomNo
	            		+ "&targetMsg=" + noti.getTargetId();
	        }
	        
	        case USER -> {
	        	
	        	Member member = noti.getSender();
	        	
	        	String email = member.getMemberEmail();
	        	
	        	yield "/blog/" + email; // 블로그 상세페이지 이동 sender 회원 
	        }

	        default -> throw new IllegalArgumentException("Unknown targetType: " + noti.getTargetType());
	    };
	}

	
	private String returnBoardUrl(int boardCode, Long boardNo) {

	    return switch (boardCode) {
	        case 1 -> "/blog/detail/" + boardNo;
	        case 21, 22, 23, 24, 25, 26 -> "/ITnews/" + boardNo;
	        case 3 -> "/board/freeboard/" + boardNo;
	        default -> throw new IllegalArgumentException("Invalid boardCode: " + boardCode);
	    };
	}

	
	// 알림 삭제
	@Override
	@Transactional
	public void deleteNoti(Long notiNo) {
		
		NotificationEntity noti = notificationRepository.findById(notiNo)
									.orElseThrow();
		
		notificationRepository.deleteById(notiNo);
		
	}

	
	
	// 알림 전체 삭제
	@Override
	@Transactional
	public void deleteAllNotification(Long memberNo) {
		
		notificationRepository.deleteAllByReceiver_MemberNo(memberNo);
		
	}

	
	// 알림 전체 읽기
	@Override
	@Transactional
	public void readAllNotification(Long memberNo) {
		
		notificationRepository.readAllNotification(memberNo);
		
	}

}
