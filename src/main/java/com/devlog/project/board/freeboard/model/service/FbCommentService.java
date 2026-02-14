package com.devlog.project.board.freeboard.model.service;


import com.devlog.project.board.freeboard.model.dto.CommentFB;

import java.util.List;

public interface FbCommentService {

	/** 댓글 목록 조회
	 * @param boardNo
	 * @return List<CommentDB>
	 */
	List<CommentFB> select(Long boardNo);

	
	/** 댓글 삽입
	 * @param comment
	 * @return result :성공한 경우 commentNo
	 */
	Long insert(CommentFB comment);


	/** 댓글 삭제
	 * @return result: 성공한 행의 갯수
	 */
	int delete(CommentFB comment);


	/** 댓글 수정
	 * @param comment
	 * @return result: 성공한 행의 갯수
	 */
	int update(CommentFB comment); 
}

