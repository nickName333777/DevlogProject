package com.devlog.project.board.freeboard.controller;


import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.devlog.project.board.freeboard.model.dto.CommentFB;
import com.devlog.project.board.freeboard.model.service.FbCommentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController 
@RequiredArgsConstructor
public class FbCommentController { 

	private final FbCommentService service;
	
	// 댓글 목록 조회
	@GetMapping(value = "/board/freeboard/comment", produces="application/json; charset=UTF-8") 
	public List<CommentFB> select(Long boardNo ) { 
		return service.select(boardNo); 
	}
	
	
	// 댓글 삽입(INSERT) 
	@PostMapping("/board/freeboard/comment")
	public Long insert(@RequestBody CommentFB comment) { 
		return service.insert(comment);
	}
	
	
	// 댓글 삭제 (DELETE, but 내부적으로는 UPDATE)
	@DeleteMapping("/board/freeboard/comment")
	public int delete(@RequestBody CommentFB comment) {
		return service.delete(comment);
	}
	
	// 댓글 수정 (UPDATE)
	@PutMapping("/board/freeboard/comment")
	public int update(@RequestBody CommentFB comment) {
		return service.update(comment);
	}
	
}
