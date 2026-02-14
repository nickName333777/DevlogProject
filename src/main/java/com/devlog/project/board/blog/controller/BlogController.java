package com.devlog.project.board.blog.controller;

import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.devlog.project.board.blog.dto.BlogDTO;
import com.devlog.project.board.blog.dto.MyBlogResponseDto;
import com.devlog.project.board.blog.dto.UserProfileDto;
import com.devlog.project.board.blog.service.BlogService;
import com.devlog.project.board.blog.service.BlogServiceImpl;
import com.devlog.project.board.blog.service.ReplyService;
import com.devlog.project.member.enums.CommonEnums;
import com.devlog.project.member.model.entity.Member;
import com.devlog.project.member.model.repository.MemberRepository;
import com.devlog.project.member.model.security.CustomUserDetails;
import com.devlog.project.member.model.service.MemberService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BlogController {

	private final BlogService blogService;
	private final ReplyService replyService;
	private final MemberRepository memberRepository;
	private final MemberService memberService;

	// 1. 블로그 목록 화면
	@GetMapping("/blog/list")
	public String blogList(Model model,
			@PageableDefault(size = 12, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

		Map<String, Object> result = blogService.getBlogList(pageable.getPageNumber(), pageable.getPageSize(), "id");
		model.addAttribute("blogList", result.get("content"));
		// 마지막 페이지 여부 넘기기 (희준 추가)
		model.addAttribute("isLast", result.get("last"));
		
		return "board/blog/blogList";
	}

	// 2. 블로그 목록 데이터 (API)
	@GetMapping("/api/blog/list")
	@ResponseBody
	public Map<String, Object> getBlogListApi(
			@PageableDefault(size = 12, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

		String sortProp = pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse("id");
		return blogService.getBlogList(pageable.getPageNumber(), pageable.getPageSize(), sortProp);
	}

	// 3. 글 작성 화면
	@GetMapping("/blog/write")
	public String blogWrite(@RequestParam(required = false) Long no, Model model) {
		// 로그인 체크
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
			// 로그인 안 했으면 로그인 페이지로 리다이렉트 (alert 띄우고 보내고 싶으면 JS 처리 필요하지만 일단 리다이렉트)
			return "redirect:/member/login";
		}

		if (no != null) {
			// 기존 상제 조회 메서드 재사용 (DTO만 뽑아서 전달)
			BlogDTO post = blogService.getBoardDetail(no);
			// 본인 글인지 체크
			String loginEmail = auth.getName();
			if (post != null && post.getMemberEmail().equals(loginEmail)) {
				model.addAttribute("post", post); // HTML로 데이터 전달
			}
		}

		return "board/blog/blogWrite";
	}

	// 4. 글 작성 처리 (API)
	@PostMapping("/api/blog/write")
	@ResponseBody
	public ResponseEntity<String> writeBlog(@RequestBody BlogDTO blogDTO) {
		blogService.writeBlog(blogDTO);
		memberService.increaseExp(blogDTO.getMemberNo(), 100);

		return ResponseEntity.ok("저장 성공");
	}

	// 5. 이미지 업로드
	@PostMapping("/api/blog/imageUpload")
	@ResponseBody
	public String imageUpload(@RequestParam("image") MultipartFile image) {
		return blogService.uploadImage(image);
	}

	@GetMapping("/blog/{blogId:.+}")
	public String blogMain(@PathVariable("blogId") String blogId, Model model,
			HttpServletRequest request, HttpServletResponse response) {

		// 1. 현재 로그인한 사용자 ID 가져오기
		String currentLoginId = null;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
			currentLoginId = auth.getName();
		}

		try {
			// 블로그 주인 찾기
			Member owner = memberRepository.findByMemberEmailAndMemberDelFl(blogId, CommonEnums.Status.N).orElse(null);

			if (owner != null) {
				
				// 내 블로그가 아닐 때만 카운트 증가 시도
				if (currentLoginId == null || !currentLoginId.equals(blogId)) {
					
					Cookie oldCookie = null;
					Cookie[] cookies = request.getCookies();
					
					if (cookies != null) {
						for (Cookie cookie : cookies) {
							// "blogVisit"라는 이름의 쿠키가 있는지 확인
							if (cookie.getName().equals("blogVisit")) {
								oldCookie = cookie;
							}
						}
					}

					// 쿠키가 있고, 해당 블로그 방문 기록이 없으면 -> 카운트 증가 & 쿠키 업데이트
					if (oldCookie != null) {
						if (!oldCookie.getValue().contains("[" + owner.getMemberNo() + "]")) {
							blogService.increaseVisitCount(owner.getMemberNo());
							
							oldCookie.setValue(oldCookie.getValue() + "_[" + owner.getMemberNo() + "]");
							oldCookie.setPath("/");
							oldCookie.setMaxAge(60 * 60 * 24); // 24시간 유지
							response.addCookie(oldCookie);
						}
					} 
					// 쿠키가 아예 없으면 -> 카운트 증가 & 새 쿠키 생성
					else {
						blogService.increaseVisitCount(owner.getMemberNo());
						
						Cookie newCookie = new Cookie("blogVisit", "[" + owner.getMemberNo() + "]");
						newCookie.setPath("/");
						newCookie.setMaxAge(60 * 60 * 24); // 24시간 유지
						response.addCookie(newCookie);
					}
				}
			}

			// 2. 서비스에 내 블로그 화면 데이터 요청
			MyBlogResponseDto myBlogData = blogService.getMyBlogPageData(blogId, currentLoginId);

			// 3. 모델에 담기
			model.addAttribute("userProfile", myBlogData.getUserProfile());
			model.addAttribute("isOwner", myBlogData.isOwner());
			// 인기글 정보
			model.addAttribute("pickPost", myBlogData.getPickPost());
			// 태그 리스트
			model.addAttribute("tags", myBlogData.getTags());

			// 통계 데이터들
			model.addAttribute("followerCount", myBlogData.getFollowerCount());
			model.addAttribute("followingCount", myBlogData.getFollowingCount());
			model.addAttribute("subscriberCount", myBlogData.getSubscriberCount());
			model.addAttribute("totalVisit", myBlogData.getTotalVisit());
			model.addAttribute("postCount", myBlogData.getPostCount());
			model.addAttribute("todayVisit", myBlogData.getTodayVisit());

			model.addAttribute("subPrice", myBlogData.getSubPrice());
			model.addAttribute("subscriberCount", myBlogData.getSubscriberCount());

			model.addAttribute("memberLevel", myBlogData.getMemberLevel());
			model.addAttribute("currentExp", myBlogData.getCurrentExp());
			model.addAttribute("nextExp", myBlogData.getNextExp());
			model.addAttribute("levelTitle", myBlogData.getLevelTitle());

			// 4. 내가 팔로우 중인지 여부 확인
			boolean isFollowing = false;
			
			boolean isSubscribed = false; // 구독 여부 변수
			
			if (currentLoginId != null && owner != null) {
				Member me = memberRepository.findByMemberEmailAndMemberDelFl(currentLoginId, CommonEnums.Status.N)
						.orElse(null);
				if (me != null) {
					isFollowing = blogService.isFollowing(me.getMemberNo(), owner.getMemberNo());
					
					if (!me.getMemberNo().equals(owner.getMemberNo())) {
                        // 형변환해서 ServiceImpl의 메서드 호출
                        isSubscribed = blogService.isSubscribed(me.getMemberNo(), owner.getMemberNo());
                    }
				}
			}
			model.addAttribute("isFollowed", isFollowing);
			model.addAttribute("isSubscribed", isSubscribed);

		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/blog/list";
		}

		model.addAttribute("blogId", blogId); // JS용 ID
		return "board/blog/myDev";
	}

	// 팔로우 토글 API
	@PostMapping("/api/blog/follow/{targetId}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> toggleFollow(@PathVariable String targetId) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
			return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
		}

		String myEmail = auth.getName();
		Member me = memberRepository.findByMemberEmailAndMemberDelFl(myEmail, CommonEnums.Status.N).orElse(null);
		Member target = memberRepository.findByMemberEmailAndMemberDelFl(targetId, CommonEnums.Status.N).orElse(null);

		if (me == null || target == null)
			return ResponseEntity.badRequest().build();

		// ServiceImpl 캐스팅 또는 인터페이스 수정 필요
		boolean isFollowed = blogService.toggleFollow(me.getMemberNo(), target.getMemberNo());

		return ResponseEntity.ok(Map.of("success", true, "isFollowed", isFollowed));
	}

	// 7. 내 블로그 목록 (API)
	@GetMapping("/api/blog/{blogId:.+}/list")
	@ResponseBody
	public Map<String, Object> getMyBlogListApi(@PathVariable("blogId") String blogId,
			@RequestParam(value = "type", required = false, defaultValue = "all") String type,
			@RequestParam(value = "query", required = false) String query,
			@RequestParam(value = "tag", required = false) String tag,
			@PageableDefault(size = 6, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

		String sortProp = pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse("id");

		// Service로 검색어(query)와 태그(tag)를 함께 전달
		return blogService.getMyBlogList(blogId, type, query, tag, pageable.getPageNumber(), pageable.getPageSize(),
				sortProp);
	}

	// 8. 블로그 상세 게시글 조회 (수정됨)
	@GetMapping("/blog/detail/{boardNo}")
	public String blogDetail(@PathVariable Long boardNo, Model model, HttpServletRequest request,
			HttpServletResponse response, @AuthenticationPrincipal CustomUserDetails loginUser // (1) 로그인 유저 정보 받기
	) {

		// 1. 로그인 정보 가져오기 (문자열)
		String loginEmail = (loginUser != null) ? loginUser.getUsername() : null;

		// 2. 조회수 중복 방지 (쿠키) - 기존 코드 유지
		Cookie oldCookie = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("postView")) {
					oldCookie = cookie;
				}
			}
		}

		if (oldCookie != null) {
			if (!oldCookie.getValue().contains("[" + boardNo + "]")) {
				blogService.increaseViewCount(boardNo);
				oldCookie.setValue(oldCookie.getValue() + "_[" + boardNo + "]");
				oldCookie.setPath("/");
				oldCookie.setMaxAge(60 * 60 * 24);
				response.addCookie(oldCookie);
			}
		} else {
			blogService.increaseViewCount(boardNo);
			Cookie newCookie = new Cookie("postView", "[" + boardNo + "]");
			newCookie.setPath("/");
			newCookie.setMaxAge(60 * 60 * 24);
			response.addCookie(newCookie);
		}

		// 3. 게시글 데이터 가져오기
		BlogDTO post = blogService.getBoardDetail(boardNo);
		
		// 팔로우 여부 확인(상세 게시글 옆에 팔로우버튼)
        boolean isFollowed = false;
        if (loginUser != null) {
            Member me = loginUser.getMember();
            // 게시글 작성자(post.getMemberNo())를 내가 팔로우했는지 확인
            isFollowed = blogService.isFollowing(me.getMemberNo(), post.getMemberNo());
        }
        model.addAttribute("isFollowed", isFollowed); // HTML로 전달
		
		if (post == null) {
			return "redirect:/blog/list";
		}
		
		// [추가] 게시글 좋아요 여부 확인 (로그인 한 경우만)
        boolean isLiked = false;
        if (loginUser != null) {
            Member me = loginUser.getMember(); // 혹은 repository에서 조회한 me 객체 사용
            // BlogService에 이 메서드가 없으면 아래 2단계 참고해서 추가하세요
            isLiked = blogService.isBoardLiked(boardNo, me.getMemberNo());
        }
        model.addAttribute("isLiked", isLiked); // HTML로 전달

		// 4. 유료 글 잠금 체크 - 기존 코드 유지
		boolean isLocked = false;
		if (post.getPrice() > 0 && (loginEmail == null || !post.getMemberEmail().equals(loginEmail))) {
			isLocked = true; // 일단 유료글이고 남의 글이면 잠가버림!

		    if (loginEmail != null) {
		        Member me = memberRepository.findByMemberEmailAndMemberDelFl(loginEmail, CommonEnums.Status.N).orElse(null);
		        if (me != null) {
		            boolean isPurchased = replyService.isPurchased(boardNo, me.getMemberNo());
		            boolean isSubscribed = blogService.isSubscribed(me.getMemberNo(), post.getMemberNo());

		            // 둘 중 하나라도 해당되면 잠금을 해제(false) 함
		            if (isPurchased || isSubscribed) {
		                isLocked = false; 
		            }
		        }
		    }
		}
		    
		// 스크랩 체크
		boolean isScraped = false;
		if (loginEmail != null) {
			Member me = memberRepository.findByMemberEmailAndMemberDelFl(loginEmail, CommonEnums.Status.N).orElse(null);
			if (me != null) {
				isScraped = blogService.isScraped(boardNo, me.getMemberNo());
			}
		}
		model.addAttribute("isScraped", isScraped);

		// 5. 로그인 유저 정보 (잔액 등) - 기존 코드 유지
		if (loginEmail != null) {
			Member me = memberRepository.findByMemberEmailAndMemberDelFl(loginEmail, CommonEnums.Status.N).orElse(null);
			if (me != null) {
				model.addAttribute("loginUser", me);
			}
		}

		// 6. 데이터 전송
		model.addAttribute("post", post);
		model.addAttribute("isLocked", isLocked);

		// 최근 본 게시물 로그 저장
		if (loginUser != null) {
			// CustomUserDetails에서 Member 객체를 꺼내거나, 위에서 조회한 me 변수 사용
			Long memberNo = loginUser.getMember().getMemberNo();
			blogService.insertViewLog(memberNo, boardNo);
		}
		return "board/blog/blogDetail";
	}

	
	// 팔로워 / 팔로잉 목록 API (모달 연동용)
	// 9. 팔로워 목록 조회
	@GetMapping("/api/blog/{blogId}/followers")
    @ResponseBody
    public ResponseEntity<List<UserProfileDto>> getFollowers(@PathVariable String blogId) {
        // 1. 내 정보 찾기
        Member me = getMyMember(); // *하단 헬퍼 메소드 참고 
        // 2. 서비스 호출 (내 정보 전달)
        List<UserProfileDto> list = blogService.getFollowList(blogId, "follower", me);
        return ResponseEntity.ok(list);
    }

	// 10. 팔로잉 목록 조회
	@GetMapping("/api/blog/{blogId}/followings")
	@ResponseBody
	public ResponseEntity<List<UserProfileDto>> getFollowings(@PathVariable String blogId) {
		Member me = getMyMember();
		List<UserProfileDto> list = blogService.getFollowList(blogId, "following", me);
		return ResponseEntity.ok(list);
	}

	// 11. 구독자 목록 조회
	@GetMapping("/api/blog/{blogId}/subscribers")
	@ResponseBody
	public ResponseEntity<List<UserProfileDto>> getSubscribers(@PathVariable String blogId) {
	    // 1. 현재 로그인한 내 정보 가져오기 (구독자 목록에 있는 사람을 내가 팔로우했는지 체크하기 위함)
	    Member me = getMyMember();
	    
	    // 2. 서비스 호출하여 실제 구독자 목록 가져오기
	    List<UserProfileDto> list = blogService.getSubscriberList(blogId, me);
	    
	    return ResponseEntity.ok(list);
	}

	// 게시글 스크랩 API
	@PostMapping("/api/blog/scrap/{boardNo}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> toggleBoardScrap(@PathVariable Long boardNo) {
		// 헬퍼 메서드 getMyMember()를 사용하여 현재 로그인 유저 정보 가져오기
		Member me = getMyMember();
		if (me == null)
			return ResponseEntity.status(401).body(Map.of("message", "로그인 필요"));

		// 서비스 호출 (게시글 번호와 유저 번호 전달)
		boolean isScraped = blogService.toggleBoardScrap(boardNo, me.getMemberNo());

		return ResponseEntity.ok(Map.of("success", true, "isScraped", isScraped));
	}

	// 게시글 좋아요 API
	@PostMapping("/api/blog/like/{boardNo}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> toggleBoardLike(@PathVariable Long boardNo) {
		Member me = getMyMember(); // 헬퍼 메서드 사용
		if (me == null)
			return ResponseEntity.status(401).body(Map.of("message", "로그인 필요"));

		boolean isLiked = blogService.toggleBoardLike(boardNo, me.getMemberNo());

		// 최신 좋아요 개수 가져오기 (UI 갱신용)
		BlogDTO post = blogService.getBoardDetail(boardNo);

		return ResponseEntity.ok(Map.of("success", true, "isLiked", isLiked, "count", post.getLikeCount()));
	}

	// 게시글 수정 화면으로 이동하는 메서드
	@GetMapping("/blog/edit/{boardNo}")
	public String blogEdit(@PathVariable Long boardNo, Model model) {

		// 1. 로그인 체크
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
			return "redirect:/member/login";
		}

		// 2. 수정할 게시글 데이터 가져오기
		BlogDTO post = blogService.getBoardDetail(boardNo);

		// 3. 게시글이 없으면 목록으로 리턴
		if (post == null) {
			return "redirect:/blog/list";
		}

		// 4. 본인 글인지 확인 (다르면 상세 페이지로 튕겨내기)
		String loginEmail = auth.getName();
		if (!post.getMemberEmail().equals(loginEmail)) {
			return "redirect:/blog/detail/" + boardNo;
		}

		// 5. 모델에 데이터 담기 (이게 있어야 화면에 글 내용이 채워짐)
		System.out.println("수정 화면 진입 - 글 번호: " + post.getBoardNo());
		model.addAttribute("post", post);

		// 6. 작성 페이지(blogWrite.html)를 재활용
		return "board/blog/blogWrite";
	}

	// 게시글 수정 처리 API
	@PostMapping("/api/blog/update")
	@ResponseBody
	public ResponseEntity<String> updateBlog(@RequestBody BlogDTO blogDTO) {
		// 1. 로그인 체크
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
			return ResponseEntity.status(401).body("로그인이 필요합니다.");
		}

		// 2. 서비스 호출 (본인 확인은 Service 내부에서 처리하는 것이 안전)
		// 현재 로그인한 사용자 이메일을 DTO에 담아서 보냄 (검증용)
		blogDTO.setMemberEmail(auth.getName());

		try {
			blogService.updateBlog(blogDTO);
			return ResponseEntity.ok("수정 성공");
			
		} catch (AccessDeniedException e) {
            // "유료 게시글은 수정할 수 없습니다" 메시지를 그대로 전달
            return ResponseEntity.status(403).body(e.getMessage());
			
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body("수정 중 오류 발생");
		}
	}

	// 게시글 삭제 API
	@DeleteMapping("/api/blog/delete/{boardNo}")
	@ResponseBody
	public ResponseEntity<String> deletePost(@PathVariable Long boardNo) {

		// 1. 로그인 체크 (혹은 Service 내부에서 체크)
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			return ResponseEntity.status(401).body("로그인이 필요합니다.");
		}

		// 2. 서비스 호출 (Service에서 본인 확인 로직이 있다고 가정)
		try {
			blogService.deletePost(boardNo);
			return ResponseEntity.ok("삭제되었습니다.");
			
		} catch (IllegalStateException e) {
            // [추가] "구매자가 존재하는..." 메시지를 클라이언트로 반환 (400 Bad Request)
            return ResponseEntity.status(400).body(e.getMessage());
			
		} catch (Exception e) {
			return ResponseEntity.status(500).body("삭제 실패");
		}
	}

	// 헬퍼 메서드 (반복해서 쓰는거 이걸로 씀)
	private Member getMyMember() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
			String email = auth.getName();
			return memberRepository.findByMemberEmailAndMemberDelFl(email, CommonEnums.Status.N).orElse(null);
		}
		return null;
	}
	
	// 소연 - 메인화면 검색창을 통해 일치하는 블로그 목록 조회
	@GetMapping("/search/blog")
	public String searchBlog(
	    @RequestParam("keyword") String keyword,
	    @PageableDefault(size = 12, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
	    Model model
	) {

	    Map<String, Object> result = blogService.searchBlogByTitle(
	        keyword,
	        pageable.getPageNumber(),
	        pageable.getPageSize(),
	        "id"
	    );

	    model.addAttribute("blogList", result.get("content"));
	    model.addAttribute("keyword", keyword);
	    // 마지막 페이지 여부 넘기기 (희준 추가)
	    model.addAttribute("isLast", result.get("last"));

	    return "board/blog/blogList"; // 기존 화면에서 로직만 조금 변경하면 된다..
	}
	

}