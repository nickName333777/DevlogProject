package com.devlog.project.board.ITnews.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.devlog.project.board.ITnews.dto.ITnewsDTO;

public interface ITnewsService {

	//IT뉴스 화면 전환
	List<ITnewsDTO> selectITnewsList(Integer boardCode);

	//IT뉴스 상세
	ITnewsDTO selectNewsDetail(int boardNo);

	// IT뉴스 크롤링
	void ITnewsCrawler();

	
	// 좋아요 여부 확인
	int newsLikeCheck(Map<String, Object> map);

	
	// 조회수 증가
	int updateReadCount(int boardNo);

	
	// 좋아요 처리
	int like(Map<String, Object> paramMap);

	// 좋아요 수
	int countBoardLike(int boardNo);

	
	// 게시글 삭제
	int boardDelete(int boardNo);

	
	// 게시글 수정
	int boardUpdate(ITnewsDTO itnews, MultipartFile imageFile) throws IllegalStateException, IOException;

	// 스크랩
	int toggleScrap(Map<String, Object> paramMap);
	
	// 스크랩 확인
	int checkScrap(Map<String, Object> scrapMap);

	// 메인

	



}
