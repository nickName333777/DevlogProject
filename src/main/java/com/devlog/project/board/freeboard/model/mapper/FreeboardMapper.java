package com.devlog.project.board.freeboard.model.mapper;


import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.RowBounds;

import com.devlog.project.board.freeboard.model.dto.BoardImgDB;
import com.devlog.project.board.freeboard.model.dto.Freeboard;


@Mapper
public interface FreeboardMapper {
	
	// 게시판 종류 조회
	public List<Map<String, Object>> selectBoardTypeList();	
	
    // Freeboard 게시판(boardCode=3)의 삭제되지 않은 게시글 수 조회 
	public int getFreeboardListCount(int boardCode);

	// Freeboard 게시판(boardCode=3)에서 현재 페이지에 해당하는 부분에 대한 게시글 목록 조회
	public List<Freeboard> selectFreeboardList(int boardCode, RowBounds rowBounds);

	// Freeboard 게시판(boardCode=3)에서 boardNo 에 해당하는 게시글 상세 조회
	public Freeboard selectFreeboardDetail(Map<String, Object> map);	

	// 상세 게시글 조회수 증가(BOARD_COUNT = READ_COUNT)
	public int updateBoardCount(Long boardNo);
	
	// 상세 게시글 좋아요 여부 확인
	public int boardLikeCheck(Map<String, Object> map);

	// 상세 게시글 좋아요 처리: 좋아요 추가
	public int insertBoardLike(Map<String, Integer> paramMap);

	// 상세 게시글 좋아요 처리: 좋아요 취소	
	public int deleteBoardLike(Map<String, Integer> paramMap);

	// 상세 게시글 좋아요 처리: 좋아요 수 조회
	public int countBoardLike(Integer integer);	
	
	/** 게시글 수정에서 이미지삭제/추가에서 기존이미지 한개 삭제시, 먼저 해당 이미지 있는지 조회, AJAX
	 * @param imgNo
	 * @return
	 */
	public BoardImgDB selectImageByImgNo(Long imgNo);
	
	
	// 게시글 주인 조회
	public Long selectReceiverNo(Integer boardNo);
	
	
	// 작성자 닉네임 조회
	public String selectMemberNickname(Long receiver);
	
	// 보드 타이틀 조회
	public String selectBoardTitle(Integer integer);	
	
}
