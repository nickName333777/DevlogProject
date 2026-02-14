package com.devlog.project.board.blog.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.devlog.project.board.blog.dto.PaymentRequestDto;
import com.devlog.project.board.blog.dto.ReplyDto;
import com.devlog.project.board.blog.service.ReplyService;
import com.devlog.project.member.enums.CommonEnums;
import com.devlog.project.member.model.entity.Member;
import com.devlog.project.member.model.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ReplyApiController {
	
	private final ReplyService replyService;
	private final MemberRepository memberRepository;
	
	// 댓글 조회
	@GetMapping("/api/posts/{postId}/comments")
	public List<ReplyDto> getComments(@PathVariable Long postId) {
	    Member me = getMe();
	    Long memberNo = (me != null) ? me.getMemberNo() : null;
	    return replyService.getComments(postId, memberNo); // memberNo 전달
	}
	
	// 댓글 좋아요 토글
	@PostMapping("/api/comments/{commentId}/like")
	public ResponseEntity<?> toggleLike(@PathVariable Long commentId) {
	    Member m = getMe();
	    if (m == null) return ResponseEntity.status(401).body("로그인 필요");
	    
	    boolean liked = replyService.toggleCommentLike(commentId, m.getMemberNo());
	    return ResponseEntity.ok(Map.of("success", true, "liked", liked));
	}
    
    // 댓글 작성
    @PostMapping("/api/comments")
    public ResponseEntity<?> addComment(@RequestBody ReplyDto reply) {
        Member m = getMe();
        // 로그인 안 한 상태면 401(권한 없음)에러 보내기
        if (m == null) return ResponseEntity.status(401).body("로그인 필요");
        
        System.out.println("수신된 댓글 데이터 : " + reply.toString());
        
        reply.setMemberNo(m.getMemberNo());
        
        // 서비스 호출 전 boardNo가 있는지 한 번 더 체크
        if(reply.getBoardNo() == null) {
            return ResponseEntity.badRequest().body("게시글 번호가 누락되었습니다.");
        }
        
        replyService.writeComment(reply);
        return ResponseEntity.ok(Map.of("message", "등록 성공"));
    }
    
    // 댓글 삭제
    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        replyService.deleteComment(commentId);
        return ResponseEntity.ok("삭제 성공");
    }
    
    // 댓글 수정
    @PutMapping("/api/comments/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long commentId, @RequestBody ReplyDto dto) {
        // 1. URL의 commentId를 DTO에 담기
        dto.setCommentNo(commentId);
        
        // 2. 서비스 호출 (DTO에 commentContent가 담겨 들어옴)
        int result = replyService.updateComment(dto);
        
        if(result > 0) {
            return ResponseEntity.ok("수정 성공");
        } else {
            return ResponseEntity.status(500).body("수정 실패");
        }
    }
    
    // 결제 요청
    @PostMapping("/api/payment/purchase")
    public ResponseEntity<?> purchase(@RequestBody PaymentRequestDto req) {
        Member m = getMe();
        if (m == null) return ResponseEntity.status(401).body("로그인 필요");

        try {
            replyService.purchasePost(req.getPostId(), m.getMemberNo(), req.getAmount());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    // 로그인 유저 헬퍼 메서드
    private Member getMe() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByMemberEmailAndMemberDelFl(email, CommonEnums.Status.N).orElse(null);
    }
    
    
}
