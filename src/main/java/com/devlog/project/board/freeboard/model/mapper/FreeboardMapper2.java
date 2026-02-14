package com.devlog.project.board.freeboard.model.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.devlog.project.board.freeboard.model.dto.BoardImgDB;
import com.devlog.project.board.freeboard.model.dto.Freeboard;

@Mapper
public interface FreeboardMapper2 {

	// 
	/** 게시글 삽입 (로직은 BoardServiceImpl2.java에 작성)
	 * @param freeboard
	 * @return boardNo
	 */
	public Long freeboardInsert(Freeboard freeboard);

	
	/** 이미지 리스트 (1~5개까지) 삽입 -> mapper에서 5번 insert안하고, mapper에서 <foreach>로 해걸 (by mybatis)
	 * @param uploadList
	 * @return insert 성공한 행의 갯수
	 */
	public int insertImageList(List<BoardImgDB> uploadList);



	/** 게시글 수정에서 이미지삭제/추가에서 기존이미지 한개 삭제시, 조회된 이미지 삭제, AJAX
	 * @param imgNo
	 * @return 성공한 행의 갯수
	 */
	public int deleteImageByImgNo(Long imgNo);


	/** 게시글 제목/내용 수정 (전체 성공을 위해서는 게시글 첨부 이미지 수정도 성공해야함)
	 * @param freeboard
	 * @return 성공한 행의 갯수
	 */
	public int freeboardUpdate(Freeboard freeboard);


	/** deleteList에 이미지들이 DB에 존재하는지 확인 (게시글에 존재하는 IMG_ORDER와 deleteList내용값 일치 확인)
	 * @param deleteMap
	 * @return 체크 성공한 행의 갯수
	 */
	public int checkImage(Map<String, Object> deleteMap);


	/** deleteList에 작성된 이미지 모두 삭제	
	 * @param deleteMap
	 * @return 성공한 행의 갯수
	 */
	public int imageDelete(Map<String, Object> deleteMap);


	/** 실제로 업로드된 파일만 분류 후 uploadList에 추가된 이미지 하나씩 업데이트
	 * @param img
	 * @return 성공한 행의 갯수
	 */
	public int imageUpdate(BoardImgDB img);


	/** 위에 하나씩 UPDATE실패 시: DB에 이미지가 없는 경우 -> 이미지 삽입 진행
	 * @param img
	 * @return 성공한 행의 갯수
	 */
	public int imageInsert(BoardImgDB img);

	/////////////////////////////////////////////////////
	/** 삭제한 기존 이미지 DB에서 제거 
	 * @param boardNo
	 * @param keepImgNos
	 */
	public void deleteImagesNotInList(Long boardNo, List<Long> keepImgNos);


	/** 남아있는 기존 이미지들 IMG_ORDER 업데이트
	 * @param orderMap
	 */
	public void updateImageOrder(Map<String, Object> orderMap);


	/** existingImgNos가 null이거나 빈 문자열이면 모든 기존 이미지 삭제
	 * @param boardNo
	 */
	public void deleteAllImagesByBoardNo(Long boardNo);


	/** 게시글 삭제
	 * @param boardNo
	 * @return
	 */
	public int freeboardDelete(Long boardNo);
	        
	
}
