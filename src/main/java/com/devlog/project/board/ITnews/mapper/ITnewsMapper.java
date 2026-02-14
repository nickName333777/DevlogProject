package com.devlog.project.board.ITnews.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartFile;

import com.devlog.project.board.ITnews.dto.ITnewsDTO;

@Mapper
public interface ITnewsMapper {

	// IT뉴스 리스트 이동
	public List<ITnewsDTO> selectITnewsList(Integer boardCode);

	// IT뉴스 상세
	public ITnewsDTO selectNewsDetail(int boardNo);

	// 좋아요 여부 확인
	public int newsLikeCheck(Map<String, Object> map);

	// 조회수 증가
	public int updateReadCount(int boardNo);

	
	// 좋아요 테이블 삽입
	public int insertBoardLike(Map<String, Object> paramMap);

	
	// 좋아요 테이블 삭제
	public int deleteBoardLike(Map<String, Object> paramMap);

	
	// 좋아요 수 조회
	public int countBoardLike(Object object);

	// 게시글 삭제
	public int boardDelete(int boardNo);

	// 게시글 수정
	public int boardUpdate(ITnewsDTO itnews);
	
	// 이미지 삽입
	public void imageInsert(ITnewsDTO itnews);
	
	// 이미지 수정 
	public int imageUpdate(ITnewsDTO itnews);
	
	// 스크랩 확인
	public int checkScrap(Map<String, Object> scrapMap);

	// 스크랩 삽입
	public void insertScrap(Map<String, Object> paramMap);

	// 스크랩 삭제
	public void deleteScrap(Map<String, Object> paramMap);



}
