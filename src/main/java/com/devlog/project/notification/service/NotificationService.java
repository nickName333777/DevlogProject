package com.devlog.project.notification.service;


import java.util.List;

import com.devlog.project.notification.dto.NotifiactionDTO;
import com.devlog.project.notification.dto.NotifiactionDTO.ResponseDTO;

public interface NotificationService {

	// 멘션 알림
	void sendNotification(NotifiactionDTO notification);
	
	// 안 읽은 알림 개수 조회
	Long notiCount(Long memberNo);
	
	
	// 알림 목록 조회
	List<ResponseDTO> selectList(String type, Long memberNo);
	
	// 읽음 처리 url 반환
	String readAndGet(Long notiNo);
	
	
	// 알림 삭제
	void deleteNoti(Long notiNo);
	
	// 알림 전체 삭제
	void deleteAllNotification(Long memberNo);
	
	// 알림 전체 읽기
	void readAllNotification(Long memberNo);

}
