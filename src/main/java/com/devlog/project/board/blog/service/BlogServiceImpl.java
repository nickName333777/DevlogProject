package com.devlog.project.board.blog.service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.devlog.project.board.blog.dto.BlogDTO;
import com.devlog.project.board.blog.dto.MyBlogResponseDto;
import com.devlog.project.board.blog.dto.TagDto;
import com.devlog.project.board.blog.dto.UserProfileDto;
import com.devlog.project.board.blog.mapper.BlogMapper;
import com.devlog.project.member.enums.CommonEnums;
import com.devlog.project.member.model.entity.Level;
import com.devlog.project.member.model.entity.Member;
import com.devlog.project.member.model.repository.LevelRepository;
import com.devlog.project.member.model.repository.MemberRepository;
import com.devlog.project.myPage.mapper.MyPageMapper;
import com.devlog.project.notification.NotiEnums;
import com.devlog.project.notification.dto.NotifiactionDTO;
import com.devlog.project.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogMapper blogMapper;
    private final MemberRepository memberRepository;
    private final LevelRepository levelRepository;
    private final MyPageMapper myPageMapper;

    private final NotificationService notiService;
    
    @Value("${my.blogWrite.location}")
    private String uploadLocation;

    @Value("${my.blogWrite.webpath}")
    private String uploadWebPath;

    // 1. 블로그 목록 조회
    @Override
    public Map<String, Object> getBlogList(int page, int size, String sort) {
        Map<String, Object> params = new HashMap<>();
        params.put("offset", page * size);
        params.put("limit", size);
        params.put("sort", sort);

        List<BlogDTO> list = blogMapper.selectBlogList(params);
        
        // 문자열 태그를 리스트로 변환함 (목록에 태그 보여줄라고)
        for (BlogDTO dto : list) {
            if (dto.getTagsStr() != null) {
                dto.setTagList(java.util.Arrays.asList(dto.getTagsStr().split(",")));
            }
        }
        
        int totalCount = blogMapper.countBlogList(params);

        Map<String, Object> result = new HashMap<>();
        result.put("content", list);
        result.put("totalElements", totalCount);
        result.put("totalPages", (int) Math.ceil((double) totalCount / size));
        result.put("last", (page + 1) * size >= totalCount);

        return result;
    }

    // 2. 글 작성
    @Override
    @Transactional
    public Long writeBlog(BlogDTO blogDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }
        String email = auth.getName();

        Member member = memberRepository.findByMemberEmailAndMemberDelFl(email, CommonEnums.Status.N)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보 없음"));

        blogDTO.setMemberNo(member.getMemberNo());

        blogMapper.insertBoard(blogDTO);
        
        blogMapper.insertBlog(blogDTO);
        
        // 썸네일 처리
        if (blogDTO.getThumbnailUrl() != null && !blogDTO.getThumbnailUrl().isEmpty()) {
            insertThumbnail(blogDTO.getBoardNo(), blogDTO.getThumbnailUrl());
        }
        
        
        // 태그 처리
        processTags(blogDTO);
        
        return blogDTO.getBoardNo();
    }

    // 3. 내 블로그 목록 조회
    @Override
    public Map<String, Object> getMyBlogList(String blogId, String type, String query, String tag, int page, int size, String sort) {
        Map<String, Object> params = new HashMap<>();
        params.put("blogId", blogId);
        params.put("type", type);
        params.put("query", query); 
        params.put("tag", tag);     
        params.put("offset", page * size);
        params.put("limit", size);
        params.put("sort", sort);
        
        // [추가] 현재 로그인한 사람의 번호를 가져와서 넘겨줌 (스크랩 조회용)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Member loginUser = memberRepository.findByMemberEmailAndMemberDelFl(auth.getName(), CommonEnums.Status.N).orElse(null);
            if (loginUser != null) {
                params.put("loginMemberNo", loginUser.getMemberNo()); // 쿼리에서 us.MEMBER_NO와 비교됨
            }
        }
        
        List<BlogDTO> list = blogMapper.selectMyBlogList(params);
        
        for (BlogDTO dto : list) {
            if (dto.getTagsStr() != null) {
                dto.setTagList(java.util.Arrays.asList(dto.getTagsStr().split(",")));
            }
        }
        
        int totalCount = blogMapper.countMyBlogList(params);

        System.out.println(">>> [디버깅] 탭타입: " + type + ", 조회된 글 수: " + list.size() + ", 전체 수: " + totalCount);
        
        Map<String, Object> result = new HashMap<>();
        result.put("content", list);
        result.put("last", (page + 1) * size >= totalCount);

        return result;
    }

    // 4. 이미지 업로드
    @Override
    public String uploadImage(MultipartFile image) {
        if (image == null || image.isEmpty()) return null;
        
        // 원본 파일명 가져오기
        String originalName = image.getOriginalFilename();
        
        // [수정] 파일명 안전하게 처리 (공백 -> 언더바(_)로 변경)
        // 예: "my photo.jpg" -> "my_photo.jpg"
        if (originalName != null) {
            originalName = originalName.replaceAll("\\s", "_");
        }

        // UUID와 결합하여 고유 파일명 생성
        String fileName = UUID.randomUUID().toString() + "_" + originalName;
        
        File folder = new File(uploadLocation);
        if (!folder.exists()) folder.mkdirs();

        try {
            File saveFile = new File(uploadLocation, fileName);
            image.transferTo(saveFile);
            return uploadWebPath + fileName;
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }

    // 5. 프로필 조회
    @Override
    public UserProfileDto getUserProfile(String blogId) {
        Member member = memberRepository.findByMemberEmailAndMemberDelFl(blogId, CommonEnums.Status.N)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        UserProfileDto userProfileDto = new UserProfileDto();
        userProfileDto.setId(member.getMemberEmail());
        userProfileDto.setMemberNo(member.getMemberNo());
        userProfileDto.setNickname(member.getMemberNickname());
        userProfileDto.setUsername(member.getMemberName());
        userProfileDto.setJob(member.getMemberCareer() != null ? member.getMemberCareer() : "개발자");
        userProfileDto.setBio(member.getMyInfoIntro() != null ? member.getMyInfoIntro() : "소개가 없습니다.");
        userProfileDto.setProfileImgUrl(member.getProfileImg());
        userProfileDto.setGithubUrl(member.getMyInfoGit());
        userProfileDto.setBlogUrl(member.getMyInfoHomepage());
        userProfileDto.setExp(member.getCurrentExp());

        return userProfileDto;
    }

    // 6. 상세 조회
    @Override
    public BlogDTO getBoardDetail(Long boardNo) {
    	// blogMapper.updateViewCount(boardNo);
        BlogDTO dto = blogMapper.selectBlogDetail(boardNo);
        if (dto != null) {
            List<String> tags = blogMapper.selectBoardTags(boardNo);
            dto.setTagList(tags);
        }
        return dto;
    }
    
    // 조회수 증가 전용 메서드
    @Override
    public void increaseViewCount(Long boardNo) {
        blogMapper.updateViewCount(boardNo);
    }
    
    // [추가] 게시글 삭제
    @Override
    public void deletePost(Long boardNo) {
    	// 1. 게시글 정보 조회 (유료 여부 확인용)
        BlogDTO post = blogMapper.selectBlogDetail(boardNo);
        
        if (post == null) {
             throw new IllegalArgumentException("존재하지 않는 게시글입니다.");
        }

     // [삭제 불가 로직] 2. 유료 게시글이고, 소비자가 있는지 확인
        if ("Y".equals(post.getIsPaid())) {
            
            // 2-1. 직접 구매자 확인
            int purchaseCount = blogMapper.countPostPurchases(boardNo);
            
            // 2-2. 구독자 조회 확인 (구독으로 읽은 사람)
            int subViewCount = blogMapper.countSubscriberViews(boardNo);
            
            if (purchaseCount > 0 || subViewCount > 0) {
                // 소비자가 1명이라도 있으면 삭제 불가
                throw new IllegalStateException("이미 구매하거나 구독자가 열람한 유료 게시글은 삭제할 수 없습니다.");
            }
        }

        // 3. 삭제 진행
        blogMapper.deleteBoard(boardNo);
    }

    // 7. 팔로우 여부 확인
    @Override
    public boolean isFollowing(Long followerId, Long targetId) {
        Map<String, Object> params = new HashMap<>();
        params.put("followerId", followerId);
        params.put("targetId", targetId);
        return blogMapper.checkFollowStatus(params) > 0;
    }

    // 8. 팔로우 토글
    @Override
    @Transactional
    public boolean toggleFollow(Long followerId, Long targetId) {
        Map<String, Object> params = new HashMap<>();
        params.put("followerId", followerId);
        params.put("targetId", targetId);

        int count = blogMapper.checkFollowStatus(params);
        if (count > 0) {
            blogMapper.deleteFollow(params);
            return false; // 언팔로우됨
        } else {
            blogMapper.insertFollow(params);
            
            String memberNickname = blogMapper.selectMemberNickname(followerId);
            
            
            
            
            NotifiactionDTO notification = NotifiactionDTO.builder()
					.sender(followerId)
					.receiver(targetId)
					.content(memberNickname +"님이 회원님을 팔로우 하였습니다.")
					.preview(" ")
					.type(NotiEnums.NotiType.FOLLOW)
					.targetType(NotiEnums.TargetType.USER)
					.targetId(targetId)
					.build();
    		
    		notiService.sendNotification(notification);
            
            return true; // 팔로우됨
        }
    }

    // 9. 방문자 수 증가
    @Override
    @Transactional
    public void increaseVisitCount(Long blogOwnerNo) {
        Map<String, Object> visitData = blogMapper.selectVisitCount(blogOwnerNo);
        if (visitData == null) {
            blogMapper.insertVisitCount(blogOwnerNo);
        } else {
            blogMapper.updateVisitCount(blogOwnerNo);
        }
    }

    // 10. [핵심] 화면용 전체 데이터 (통계 포함) - 중복 제거됨
    @Override
    public MyBlogResponseDto getMyBlogPageData(String blogId, String currentLoginId) {
        MyBlogResponseDto response = new MyBlogResponseDto();
        
        // 1) 프로필
        UserProfileDto profile = this.getUserProfile(blogId);
        response.setUserProfile(profile);

        // 2) 주인 찾기
        Member blogOwner = memberRepository.findByMemberEmailAndMemberDelFl(blogId, CommonEnums.Status.N).orElse(null);
        Long ownerNo = (blogOwner != null) ? blogOwner.getMemberNo() : 0L;

        // 3) 주인 여부 체크
        boolean isOwner = false;
        if (currentLoginId != null && currentLoginId.equals(blogId)) {
            isOwner = true;
        }
        response.setOwner(isOwner);

        // 4) 인기글, 태그
        response.setPickPost(blogMapper.selectPopularPost(blogId));
        response.setTags(blogMapper.selectBlogTagList(blogId));

        // 5) 통계 데이터 (DB 조회)
        if (ownerNo != 0L) {
            response.setFollowerCount(blogMapper.countFollower(ownerNo));
            response.setFollowingCount(blogMapper.countFollowing(ownerNo));
            response.setSubscriberCount(blogMapper.countSubscriber(ownerNo));

            // 방문자 수
            Map<String, Object> visitMap = blogMapper.selectVisitCount(ownerNo);
            if (visitMap != null) {
                // 오라클 숫자는 BigDecimal로 올 수 있어서 String 변환 후 파싱이 안전
                response.setTotalVisit(Integer.parseInt(String.valueOf(visitMap.get("TOTAL_VISIT"))));
                response.setTodayVisit(Integer.parseInt(String.valueOf(visitMap.get("DAILY_VISIT"))));
            } else {
                response.setTotalVisit(0);
                response.setTodayVisit(0);
            }
        }

        response.setPostCount(blogMapper.countTotalPosts(blogId));
        
        response.setSubPrice(blogOwner.getSubscriptionPrice());
        
        // 회원 현재 레벨
        response.setMemberLevel(blogOwner.getMemberLevel().getLevelNo());
        response.setCurrentExp(blogOwner.getCurrentExp());
        
        
        Integer nextLv = null;
        
        if(blogOwner.getMemberLevel().getLevelNo() < 30) {
        	nextLv = blogOwner.getMemberLevel().getLevelNo() + 1;
        } else {
        	nextLv = blogOwner.getMemberLevel().getLevelNo();
        }
        
        Level level = levelRepository.findById(nextLv).orElseThrow();
        
        response.setNextExp(level.getRequiredTotalExp());
        
        response.setLevelTitle(level.getTitle());
        

        return response;
    }
    
    // 구독 여부 확인 메서드 (Controller에서 호출)
    public boolean isSubscribed(Long me, Long target) {
        Map<String, Object> params = new HashMap<>();
        params.put("me", me);
        params.put("target", target);
        return blogMapper.checkSubscribeStatus(params) > 0;
    }
    
    // 구독자 목록 조회 (API용)
    public List<UserProfileDto> getSubscriberList(String blogId, Member me) {
        Member owner = memberRepository.findByMemberEmailAndMemberDelFl(blogId, CommonEnums.Status.N)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        Map<String, Object> params = new HashMap<>();
        params.put("memberNo", owner.getMemberNo());
        if (me != null) {
            params.put("myMemberNo", me.getMemberNo());
        }
        
        return blogMapper.selectSubscriberList(params);
    }
    
    // 팔로워/팔로잉 목록 가져오기
    @Override
    public List<UserProfileDto> getFollowList(String blogId, String type, Member me) {
        Member owner = memberRepository.findByMemberEmailAndMemberDelFl(blogId, CommonEnums.Status.N)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));
        
        Map<String, Object> params = new HashMap<>();
        params.put("memberNo", owner.getMemberNo());
        
        // 로그인 했다면 내 번호 넣기, 안했으면 null
        if (me != null) {
            params.put("myMemberNo", me.getMemberNo());
        }

        if ("follower".equals(type)) {
            return blogMapper.selectFollowerList(params);
        } else {
            return blogMapper.selectFollowingList(params);
        }
    }
    
    // 게시글 스크랩
    @Override
    @Transactional
    public boolean toggleBoardScrap(Long boardNo, Long memberNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("boardNo", boardNo);
        params.put("memberNo", memberNo);

        // 1. 이미 스크랩했는지 확인
        int count = blogMapper.checkScrapStatus(params);

        if (count > 0) {
            // 2-1. 이미 있으면 삭제 (스크랩 취소)
            blogMapper.deleteScrap(params);
            return false;
        } else {
            // 2-2. 없으면 삽입 (스크랩 등록)
            blogMapper.insertScrap(params);
            return true;
        }
    }
    
    // 게시글 스크랩
    @Override
    public boolean isScraped(Long boardNo, Long memberNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("boardNo", boardNo);
        params.put("memberNo", memberNo);
        return blogMapper.checkScrapStatus(params) > 0;
    }
    
    // 게시글 좋아요 토글
    @Override
    @Transactional
    public boolean toggleBoardLike(Long boardNo, Long memberNo) {
        Map<String, Object> params = Map.of("boardNo", boardNo, "memberNo", memberNo);
        // Mapper 메서드 필요 (checkBoardLike, deleteBoardLike, insertBoardLike)
        // *주의: Mapper 인터페이스에 해당 메서드들을 Map 파라미터로 정의해주세요.
        
        if (blogMapper.checkBoardLike(params) > 0) {
            blogMapper.deleteBoardLike(params);
            return false; // 취소됨
        } else {
            blogMapper.insertBoardLike(params);
            
            Long receiver = blogMapper.selectReceiverNo(boardNo);
			Long sender = memberNo;
			if(!sender.equals(receiver)) {
				
				String boardTitle = blogMapper.selectBoardTitle(boardNo);
				
				
				String memberNickname = blogMapper.selectMemberNickname(receiver);
				
				NotifiactionDTO notification = NotifiactionDTO.builder()
						.sender(sender)
						.receiver(receiver)
						.content(memberNickname +"님이 회원님의 게시글에 좋아요를 눌렀습니다.")
						.preview(boardTitle)
						.type(NotiEnums.NotiType.LIKE)
						.targetType(NotiEnums.TargetType.BOARD)
						.targetId(boardNo)
						.build();
				
				notiService.sendNotification(notification);
			}
            
            return true; // 등록됨
        }
    }
    
    // 게시글 좋아요
    @Override
    public boolean isBoardLiked(Long boardNo, Long memberNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("boardNo", boardNo);
        params.put("memberNo", memberNo);
        return blogMapper.checkBoardLike(params) > 0;
    }
    
    // 게시글 수정
    @Override
    @Transactional
    public void updateBlog(BlogDTO blogDTO) {
        // 1. 기존 글 조회 (존재 여부 및 본인 확인용)
        BlogDTO existPost = blogMapper.selectBlogDetail(blogDTO.getBoardNo());
        if (existPost == null) {
            throw new IllegalArgumentException("존재하지 않는 게시글입니다.");
        }

        // 2. 본인 확인 (요청자 이메일 vs 작성자 이메일)
        // blogDTO.getMemberEmail()은 컨트롤러에서 넣어줌
        if (!existPost.getMemberEmail().equals(blogDTO.getMemberEmail())) {
            throw new AccessDeniedException("본인의 글만 수정할 수 있습니다.");
        }
        
        // [수정 불가 로직] 3. 유료 게시글은 내용 수정 불가
        // DB에 저장된 값이 'Y'인지 확인
        if ("Y".equals(existPost.getIsPaid())) {
            throw new AccessDeniedException("유료로 발행된 게시글은 수정할 수 없습니다.");
        }
        
        
        
        
        System.out.println("썸네일 url : " + blogDTO.getThumbnailUrl());
        // 썸네일 처리
        if (blogDTO.getThumbnailUrl() != null && !blogDTO.getThumbnailUrl().isEmpty()) {
            updateThumbnail(blogDTO.getBoardNo(), blogDTO.getThumbnailUrl());
        }
        // 3. 내용 수정 (BOARD 테이블)
        blogMapper.updateBoard(blogDTO);

        // 4. 옵션 수정 (BLOG 테이블 - 유료여부, 가격 등)
        blogMapper.updateBlog(blogDTO);

        // 5. 태그 수정 (기존 태그 삭제 -> 새 태그 등록)
        // 5-1. 기존 태그 연결 끊기
        blogMapper.deleteBlogTags(blogDTO.getBoardNo());

        // 5-2. 새 태그 등록 (Write 로직과 동일)
        processTags(blogDTO);
    }
    
    // [헬퍼] 썸네일 저장을 위한 내부 메서드 (BOARD_IMG 테이블 사용)
    private void insertThumbnail(Long boardNo, String fullPath) {
        // 예: /images/blog/uuid_filename.png
        int lastSlash = fullPath.lastIndexOf("/");
        
        // 방어 로직: 슬래시가 없으면 전체를 파일명으로
        String imgPath = (lastSlash > -1) ? fullPath.substring(0, lastSlash + 1) : "";
        String imgRename = (lastSlash > -1) ? fullPath.substring(lastSlash + 1) : fullPath;

        Map<String, Object> imgMap = new HashMap<>();
        imgMap.put("boardNo", boardNo);
        imgMap.put("imgPath", imgPath);    
        imgMap.put("imgRename", imgRename); 
        imgMap.put("imgOrder", 0);         // 대표 이미지는 항상 0번
        
        blogMapper.insertBoardImg(imgMap); 
    }
    
    private void updateThumbnail(Long boardNo, String fullPath) {
    	// 예: /images/blog/uuid_filename.png
    	int lastSlash = fullPath.lastIndexOf("/");
    	
    	// 방어 로직: 슬래시가 없으면 전체를 파일명으로
    	String imgPath = (lastSlash > -1) ? fullPath.substring(0, lastSlash + 1) : "";
    	String imgRename = (lastSlash > -1) ? fullPath.substring(lastSlash + 1) : fullPath;
    	
    	// boardNO -> 이미지 테이블 조회
    	// 결과 있으면 (이미지테이블 컬럼에 뭐 있으면 update)
    	//
    	
    	int result = blogMapper.selectBoardImg(boardNo);
    	
    	if(result == 0) { // 없을 경우
    		insertThumbnail(boardNo, fullPath);
    	}else {
    		
    		Map<String, Object> imgMap = new HashMap<>();
    		imgMap.put("boardNo", boardNo);
    		imgMap.put("imgRename", imgRename); 
    		
    		blogMapper.updateBoardImg(imgMap); 
    		
    	}
    	
    }
    
    // 태그 처리 로직 분리
    private void processTags(BlogDTO blogDTO) {
        if (blogDTO.getTagList() != null && !blogDTO.getTagList().isEmpty()) {
            for (String tagName : blogDTO.getTagList()) {
                blogMapper.insertTag(tagName);
                Long tagNo = blogMapper.selectTagNoByName(tagName);
                
                Map<String, Object> tagParams = new HashMap<>();
                tagParams.put("boardNo", blogDTO.getBoardNo());
                tagParams.put("tagNo", tagNo);
                blogMapper.insertBlogTag(tagParams);
            }
        }
    }
    
    // 최근 본 게시물 로직
	@Override
	public void insertViewLog(Long memberNo, Long boardNo) {
		if (memberNo == null || boardNo == null) return;

        try {
            // Mapper 호출해서 DB에 저장
            myPageMapper.insertViewLog(memberNo, boardNo);
        } catch (Exception e) {
            // 로그 저장하다가 에러 나도(예: DB연결 끊김 등), 
            // 사용자가 글 보는 데는 지장 없도록 에러를 무시(로그만 찍음)합니다.
            System.out.println("조회 로그 저장 실패 (무시됨): " + e.getMessage());
        }
	}

	// 소연 - 메인화면 검색창을 통해 일치하는 블로그 목록 조회
	@Override
	public Map<String, Object> searchBlogByTitle(
		    String keyword,
		    int page,
		    int size,
		    String sort
		) {
		    Map<String, Object> params = new HashMap<>();
		    params.put("keyword", keyword);
		    params.put("offset", page * size);
		    params.put("limit", size);
		    params.put("sort", sort);

		    List<BlogDTO> list = blogMapper.searchBlogByTitle(params);

		    for (BlogDTO dto : list) {
		        if (dto.getTagsStr() != null) {
		            dto.setTagList(Arrays.asList(dto.getTagsStr().split(",")));
		        }
		    }

		    int totalCount = blogMapper.countSearchBlogByTitle(params);

		    Map<String, Object> result = new HashMap<>();
		    result.put("content", list);
		    result.put("totalElements", totalCount);
		    result.put("totalPages", (int) Math.ceil((double) totalCount / size));
		    result.put("last", (page + 1) * size >= totalCount);

		    return result;
		}

    
}