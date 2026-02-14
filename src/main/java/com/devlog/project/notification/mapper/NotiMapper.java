package com.devlog.project.notification.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotiMapper {
	
	
	// 보드코드 조회
	int selectBoardCode(Long boardNo);
	
	// 보드 번호 조회
	Long selectBoardNo(Long targetId);
	

}
