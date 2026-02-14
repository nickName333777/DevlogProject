package com.devlog.project.myPage.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.devlog.project.myPage.dto.MemberUpdateDto;
import com.devlog.project.myPage.dto.MyActivityDto;

@Mapper
public interface MyPageMapper {
    
    // 내 정보 수정 (MyBatis)
    int updateMemberInfo(@Param("email") String email, @Param("dto") MemberUpdateDto dto);

    // 프로필 이미지 수정 (MyBatis)
    int updateProfileImage(@Param("email") String email, @Param("imageUrl") String imageUrl);
    
    // 좋아요 한 게시물 조회
    List<MyActivityDto> selectLikedPosts(@Param("memberNo") Long memberNo);

    // 최근 본 게시물 조회
    List<MyActivityDto> selectViewHistory(@Param("memberNo") Long memberNo);

    // 임시 저장한 글 조회
    List<MyActivityDto> selectDrafts(@Param("memberNo") Long memberNo);

    // 구매 내역 조회
    List<MyActivityDto> selectPurchasedPosts(@Param("memberNo") Long memberNo);

    // 최근 본 게시물 기록 저장 (로그 쌓기)
    void insertViewLog(@Param("memberNo") Long memberNo, @Param("boardNo") Long boardNo);
    
    // [추가] 탈퇴 시 팔로우 삭제
    int deleteMemberFollows(Long memberNo);

    // [추가] 탈퇴 시 구독 삭제
    int deleteMemberSubscribes(Long memberNo);

}