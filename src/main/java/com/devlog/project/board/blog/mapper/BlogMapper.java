package com.devlog.project.board.blog.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

import com.devlog.project.board.blog.dto.BlogDTO;
import com.devlog.project.board.blog.dto.TagDto;
import com.devlog.project.board.blog.dto.UserProfileDto;

@Mapper
public interface BlogMapper {

    // 게시글 등록 
    int insertBoard(BlogDTO blogDTO);
    int insertBlog(BlogDTO blogDTO);
    
    // 게시글 삭제
    int deleteBoard(Long boardNo);
    
    // 태그 등록 및 연결
    int insertTag(String tagName);
    Long selectTagNoByName(String tagName);
    int insertBlogTag(Map<String, Object> params);

    // 목록 조회 (페이징)
    List<BlogDTO> selectBlogList(Map<String, Object> params);
    int countBlogList(Map<String, Object> params);

    // 내 블로그 목록 (특정 유저)
    List<BlogDTO> selectMyBlogList(Map<String, Object> params);
    int countMyBlogList(Map<String, Object> params);

    // 상세 조회
    BlogDTO selectBlogDetail(Long boardNo);

    // 조회수 증가
    int updateViewCount(Long boardNo);
    
    // 유저 PICK 인기 글 조회
    BlogDTO selectPopularPost(String blogId);
    
    // 블로그 전체 태그 목록 조회
    List<TagDto> selectBlogTagList(String blogId);
    
    // 상세 게시글 전용 태그 조회
    List<String> selectBoardTags(Long boardNo);
    
    // 팔로우
    int checkFollowStatus(Map<String, Object> params);
    int insertFollow(Map<String, Object> params);
    int deleteFollow(Map<String, Object> params);
    
    // 통계
    int countFollower(Long memberNo);
    int countFollowing(Long memberNo);
    int countTotalPosts(String blogId);

    // 방문자
    Map<String, Object> selectVisitCount(Long memberNo);
    int insertVisitCount(Long memberNo);
    int updateVisitCount(Long memberNo);
    
    // 팔로워, 팔로잉 목록 조회
	List<UserProfileDto> selectFollowerList(Map<String, Object> params);
	List<UserProfileDto> selectFollowingList(Map<String, Object> params);
	
	// 게시글 좋아요
	int checkBoardLike(Map<String, Object> params);
	void deleteBoardLike(Map<String, Object> params);
	void insertBoardLike(Map<String, Object> params);
	
	// 게시글 수정 관련
    int updateBoard(BlogDTO blogDTO);
    int updateBlog(BlogDTO blogDTO);
    int deleteBlogTags(Long boardNo);
    
    // [추가] 특정 게시글의 구매 횟수 조회 (삭제 방지용)
    int countPostPurchases(Long boardNo);
    
    // [추가] 구독자가 조회한 횟수 조회 (삭제 방지용)
    int countSubscriberViews(Long boardNo);
    
    // 실시간 알림
	Long selectReceiverNo(Long boardNo);
	String selectBoardTitle(Long boardNo);
	String selectMemberNickname(Long receiver);
	
    // 게시글 스크랩 관련
    int checkScrapStatus(Map<String, Object> params);
    int insertScrap(Map<String, Object> params);
    int deleteScrap(Map<String, Object> params);
    
    // 썸네일(BOARD_IMG) 관련
    int insertBoardImg(Map<String, Object> params);
    
    int deleteThumbnail(Long boardNo); // IMG_ORDER = 0인 것만 삭제
    
    // 썸네일 조회 (for 수정)
	int selectBoardImg(Long boardNo);
	void updateBoardImg(Map<String, Object> imgMap);
	
	// 구독 관련
    int countSubscriber(Long memberNo);
    int checkSubscribeStatus(Map<String, Object> params);
    List<UserProfileDto> selectSubscriberList(Map<String, Object> params);
    
	List<BlogDTO> searchBlogByTitle(Map<String, Object> params);
	int countSearchBlogByTitle(Map<String, Object> params);
    
    
}