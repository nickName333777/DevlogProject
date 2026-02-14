package com.devlog.project.board.blog.service;

import java.util.List;

import com.devlog.project.board.blog.dto.ReplyDto;

public interface ReplyService {
	// 댓글
	List<ReplyDto> getComments(Long postId, Long memberNo);
	
    int writeComment(ReplyDto reply);
    int deleteComment(Long commentNo);
    int updateComment(ReplyDto reply);
    
    // 댓글/답글 좋아요 토글
    boolean toggleCommentLike(Long commentNo, Long memberNo);
    
    // 결제
    void purchasePost(Long postId, Long memberNo, int amount);
    boolean isPurchased(Long postId, Long memberNo);
    
	
}
