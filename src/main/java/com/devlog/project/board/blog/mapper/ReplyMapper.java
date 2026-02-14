package com.devlog.project.board.blog.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.devlog.project.board.blog.dto.ReplyDto;

@Mapper
public interface ReplyMapper {
	
	// 댓글 기능
	List<ReplyDto> selectReplyList(Map<String, Object> params);
    int insertReply(ReplyDto reply);
    int deleteReply(Long commentNo);
    int updateReply(ReplyDto reply);
    
    // 결제 관련 (구매 내역
    // 내가 이 글을 샀는지 확인
    int checkPurchaseHistory(@Param("boardNo") Long boardNo, @Param("memberNo") Long memberNo);
    
    // 구매 내역 저장
    int insertPurchaseHistory(@Param("boardNo") Long boardNo, @Param("memberNo") Long memberNo, @Param("price") int price);
    
    // 글 작성자 번호 찾기 (판매자에게 돈 주기 위해)
    Long selectBoardWriter(Long boardNo);
    
    // 댓글 좋아요
    int checkCommentLike(@Param("commentNo") Long commentNo, @Param("memberNo") Long memberNo);
    int insertCommentLike(@Param("commentNo") Long commentNo, @Param("memberNo") Long memberNo);
    int deleteCommentLike(@Param("commentNo") Long commentNo, @Param("memberNo") Long memberNo);
    
	int getBoardMemberNo(Long boardNo);
	String selectMemberNickname(Long memberNo);
	int getParentMemberNo(Long parentCommentNo);
}
