package com.devlog.project.board.ITnews.service;

import java.util.List;
import java.util.Map;

import com.devlog.project.board.ITnews.dto.Comment;

public interface CommentService {

	// 댓글 목록 조회
	public List<Comment> select(int boardNo);

	
	// 댓글 삽입
	public int insert(Comment comment);


	// 댓글 수정 
	public int update(Comment comment);

	// 댓글 삭제
	public int delete(Comment comment);

	
	// 댓글 좋아요 & 싫어요 통합 처리
	public Map<String, Object> updateLikeDislike(Map<String, Integer> param);

}
