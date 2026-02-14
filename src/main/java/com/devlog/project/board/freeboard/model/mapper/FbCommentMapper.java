package com.devlog.project.board.freeboard.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.devlog.project.board.freeboard.model.dto.CommentFB;

@Mapper
public interface FbCommentMapper {
	
	// 댓글 목록 조회
	public List<CommentFB> select(Long boardNo);

	// 댓글 삽입
	public Long insert(CommentFB comment);


	// 댓글 삭제
	public int delete(CommentFB comment);

	
	// 댓글 수정
	public int update(CommentFB comment);
	
	
	// 게시글 부모 주인 조회
	public int getBoardMemberNo(Long boardNo);
	
	// 댓글 작성자 닉네임 조회
	public String selectMemberNickname(Long memberNo);
}
