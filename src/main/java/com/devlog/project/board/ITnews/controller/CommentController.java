package com.devlog.project.board.ITnews.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.devlog.project.board.ITnews.dto.Comment;
import com.devlog.project.board.ITnews.service.CommentService;

@RestController
public class CommentController {

	@Autowired
	private CommentService service;

	// 댓글 목록 조회
	@GetMapping(value = "/ITnews/comment", produces = "application/json; charset=UTF-8")
	public List<Comment> select(int boardNo) {
		return service.select(boardNo);
	}
	
	// 댓글 삽입
	@PostMapping("/ITnews/comment")
	public int insert(@RequestBody Comment comment) { 
//	    System.out.println("DTO로 변환된 데이터: " + comment); 
	    return service.insert(comment);
	}
	
	// 댓글 수정
	@PutMapping("/ITnews/comment")
	public int update(@RequestBody Comment comment) {
		return service.update(comment);
	}
	
	// 댓글 삭제
	@DeleteMapping("/ITnews/comment")
	public int delete(@RequestBody Comment comment) {
		return service.delete(comment);
	}
	
	// 댓글 좋아요 & 싫어요 통합 처리
	@PostMapping("/ITnews/comment/like")
	public Map<String, Object> updateLikeDislike(@RequestBody Map<String, Integer> param) {
	    // commentNo, memberNo, status(1:좋아요, 2:싫어요)
	    return service.updateLikeDislike(param);
	}

	
}
