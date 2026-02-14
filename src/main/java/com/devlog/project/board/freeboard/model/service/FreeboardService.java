package com.devlog.project.board.freeboard.model.service;


import java.util.List;
import java.util.Map;

import com.devlog.project.board.freeboard.model.dto.Freeboard;

public interface FreeboardService {

	/** 게시판 종류 조회
	 * @return boardTypeList
	 * 
	 */
	List<Map<String, Object>> selectBoardTypeList();


	/** 게시글 목록조회 (boardCode=3 자유 게시판)
	 * @param boardCode
	 * @param cp
	 * @return map
	 */
	Map<String, Object> selectFreeboardList(int boardCode, int cp);


	/** 게시글 상세조회 (boardCode=3 자유 게시판)
	 * @param map (여기에 boardCode, boardNo 담겨있음)
	 * @return Freeboard DTO
	 */
	Freeboard selectFreeboardDetail(Map<String, Object> map);

	
	/** 조회수 증가
	 * @param boardNo
	 * @return count
	 */
	//int updateBoardCount(int boardNo);
	int updateBoardCount(Long boardNo);

	
	/** 좋아요 여부 확인
	 * @param map
	 * @return result
	 */
	int boardLikeCheck(Map<String, Object> map);


	/** 좋아요 처리 서비스
	 * @param paramMap
	 * @return count
	 */
	int like(Map<String, Integer> paramMap);
}

