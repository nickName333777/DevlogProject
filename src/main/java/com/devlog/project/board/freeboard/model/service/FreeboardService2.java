package com.devlog.project.board.freeboard.model.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.devlog.project.board.freeboard.model.dto.Freeboard;

public interface FreeboardService2 {


	/** 게시글 삽입
	 * @param freeboard
	 * @param images
	 * @return boardNo // insert하는 보드넘버
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	Long freeboardInsert(Freeboard freeboard, List<MultipartFile> images) throws IllegalStateException, IOException;

	
	
	/** 게시글 수정에서 이미지삭제/추가에서 기존이미지 한개 삭제, AJAX
	 * @param imgNo
	 * @return 성공여부
	 */
	boolean deleteFreeboardImage(Long imgNo);


	/** 게시글 수정 
	 * @param freeboard
	 * @param images
	 * @param deleteList
	 * @return 성공한 행의 갯수
	 */
	int freeboardUpdate(Freeboard freeboard, List<MultipartFile> images, String existingImgNos) throws IllegalStateException, IOException;



	/** 게시글 삭제
	 * @param boardNo
	 * @return 성공한 행의 갯수
	 */
	int freeboardDelete(Long boardNo); 
	
	
	/** 게시글 삭제, monkey-patch
	 * @param boardNo
	 * @return 성공한 행의 갯수
	 */
	int setBoardNoDelFl(Long boardNo); 	
	
}
