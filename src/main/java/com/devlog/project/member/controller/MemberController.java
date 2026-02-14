package com.devlog.project.member.controller;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.devlog.project.member.model.dto.MemberLoginResponseDTO;
import com.devlog.project.member.model.dto.MemberProfileDTO;
import com.devlog.project.member.model.dto.MemberSignUpRequestDTO;
import com.devlog.project.member.model.security.CustomUserDetails;
import com.devlog.project.member.model.service.MemberProfileService;
import com.devlog.project.member.model.service.MemberService;
import com.devlog.project.member.model.service.MemberService2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller 
@RequestMapping("/member")  
@RequiredArgsConstructor 
public class MemberController {

	private final MemberService memberService; //
	private final MemberService2 service; //
	private final MemberProfileService profileService;
	
	private final AuthenticationManager authenticationManager; // spring-security
	
	// -------------------------- [ 로그인 ] 
	// 로그인 페이지(전용 화면) 이동
	@GetMapping("/login")
	public String login(HttpServletRequest request, Model model) {  
		
		// 쿠키 설정
	    Cookie[] cookies = request.getCookies();
	    if (cookies != null) {
	        for (Cookie c : cookies) {
	            if ("saveId".equals(c.getName())) {
	                model.addAttribute("cookie", Map.of("saveId", Map.of("value", c.getValue())));
	            }
	        }
	    }
	    	    
	    return "member/login";
	}
	
	
	// 로그인 요청처리
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(
            @RequestParam("memberEmail") String memberEmail,  
            @RequestParam("memberPw") String memberPw,
            /////
            @RequestParam(value="saveId", required=false) String saveId,
			HttpServletResponse resp,          
			HttpServletRequest request,
			SessionStatus status   // @SessionAttributes 기반 old loginMember 제거용, placeholder
    ) {
    	
    	try {
	        // 인증 토큰 생성 
	        UsernamePasswordAuthenticationToken authToken =
	                new UsernamePasswordAuthenticationToken(memberEmail, memberPw);   	
	    	
	
	        // 인증 시도 (여기서 Security가 모든 검증 수행) -> 실패시 인증실패 exception발생
	        Authentication authentication =
	                authenticationManager.authenticate(authToken);
	
	        // 인증 성공 → SecurityContext에 저장 
	        SecurityContextHolder.getContext().setAuthentication(authentication);
	
	        // 인증된 사용자 정보 꺼내기 (spring-security가 Member 엔티티에서 꺼내오는 회원정보)
	        CustomUserDetails userDetails =
	                (CustomUserDetails) authentication.getPrincipal();
	        System.out.println(saveId);
	        System.out.println("===== 인증 성공 =====");
	        System.out.println("memberNo: " + userDetails.getMember().getMemberNo());
	        System.out.println("memberEmail: " + userDetails.getMember().getMemberEmail());
	        System.out.println("memberNickname: " + userDetails.getMember().getMemberNickname());
	        System.out.println("authorities: " + userDetails.getAuthorities()); // ?
	        System.out.println("profileImg;: " + userDetails.getMember().getProfileImg());
	        System.out.println("====================");        
	         
	        // ------------------------------------------------
	        // 응답 DTO 생성: 서비스에서 처리
	        MemberLoginResponseDTO response =
	                memberService.toLoginResponse( // toLoginResponse
	                    userDetails.getMember(),
	                    authentication.getAuthorities()
	                );        
	        
	        // 탈퇴 회원 체크
	        if ("Y".equals(response.getMemberDelFl())) {
	        	SecurityContextHolder.clearContext();
	            throw new BadCredentialsException("탈퇴한 회원입니다.");
	        }	        
	        
	        System.out.println("##### 응답 DTO (MemberLoginResponseDTO): ");
	        System.out.println(response);  
	        
	        // --------------------------------------------------
			// 로그인 성공 시 response DTO에 로그인회원정보 담겨있다
			// 1) 세션에 로그인한 회원 정보 추가
	        
	        // ===== [추가] 하루 1회 로그인 경험치 지급 (쿠키 기반) =====
	        String today = LocalDate.now().toString();
	        String cookieName = "EXP_" + today;
	        Long memberNo = response.getMemberNo();

	        Cookie target = null;

	        if (request.getCookies() != null) {
	            for (Cookie c : request.getCookies()) {
	                if (cookieName.equals(c.getName())) {
	                    target = c;
	                    break;
	                }
	            }
	        }

	        boolean canGain = false;

	        if (target == null) {
	            target = new Cookie(cookieName, "|" + memberNo + "|");
	            canGain = true;
	        } else {
	            String value = target.getValue();
	            if (!value.contains("|" + memberNo + "|")) {
	                target.setValue(value + memberNo + "|");
	                canGain = true;
	            }
	        }

	        if (canGain) {
	            memberService.increaseExp(memberNo, 50);

	            // 자정 만료
	            LocalDateTime now = LocalDateTime.now();
	            LocalDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay();
	            int secondsUntilMidnight = (int) Duration.between(now, nextMidnight).getSeconds();

	            target.setPath("/");
	            target.setMaxAge(secondsUntilMidnight);
	            resp.addCookie(target);
	        }
	        
	        System.out.println("경험치 이후 ");
	        // ==================================================
	        
	        // 세션 고정 공격 방지 + 이전 사용자 정보 제거
	        HttpSession oldSession = request.getSession(false);
	        if (oldSession != null) {
	            oldSession.invalidate();
	        }
	        
	        // 12-31 YHJ 추가
	        HttpSession newSession = request.getSession(true);
	        newSession.setAttribute("loginMember", response);
	        
	        newSession.setAttribute(
	        		HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
	        		SecurityContextHolder.getContext()
	        		);
	        
	        
	        
			// 2) 아이디 저장(쿠키에)
			// 쿠키 생성(K:V로 해당 쿠키에 담을 (로그인멤버의 이메일) 데이터 지정)
			Cookie cookie = new Cookie("saveId", response.getMemberEmail()); // 로그인 성공시
			if(saveId != null) { // 체크 되었을 때
				cookie.setMaxAge(60*60*24*30); // 초 단위; => 한달동안 유지되는 쿠키 생성
			} else { // 체크 않되었을 때
				cookie.setMaxAge(0); // 기존 쿠키 삭제 -> 0초 동안 유지되는 쿠키 생성
			}
			
			// 클라이언트가 어떤 요청을 할 때 쿠키가 첨부될지 경로(주소)를 지정
			cookie.setPath("/"); // localhost/ 이하의 모든 주소 ex) /, /member/login, /member/logout 등 모든 요청에 쿠키 첨부
			
			// 응답 객체(HttpServletResponse)을 이용해서 만들어진 쿠키를 클라이언트에게 전달
			resp.addCookie(cookie); 
			
	        return ResponseEntity.ok(response);
    	} catch (BadCredentialsException ex) {
            // 로그인 실패 → 401 Unauthorized + 메시지 전달
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "아이디 또는 비밀번호가 일치하지 않습니다."));
        } catch (Exception ex) {
            // 기타 서버 오류
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "서버 오류가 발생했습니다."));
        }
    	
    }
	
	
	@ResponseBody // Postman API test용 
    @GetMapping("/loginTest")
    public String loginTest(Authentication authentication) {
        
        if (authentication == null) {
            System.out.println("###%%%@@@ authentication is null");
        } else {
            System.out.println("###%%%@@@ login user = " + authentication.getName());
        }
              
        return "ok";
    }	
	
	
	// -------------------------- [ 로그아웃 ] 
	// 로그아웃 요청처리
	// GET - 리다이렉트 방식
	@GetMapping("/logout")
	public String logoutGet(HttpServletRequest request, SessionStatus status) {
		status.setComplete(); // @SessionAttributes 제거
	    logout(request);
	    System.out.println("###%%%@@@ 로그아웃 성공 (GET)");
	    //return "redirect:/member/login"; // 테스트용
	    return "redirect:/"; // 메인페이지와 통합시
	}

	// POST - REST API 방식
	@ResponseBody
	@PostMapping("/logout")
	public ResponseEntity<Map<String, String>> logoutPost(
			HttpServletRequest request
			, SessionStatus status
			) {
		status.setComplete(); // @SessionAttributes 제거
	    logout(request);
	    System.out.println("###%%%@@@ 로그아웃 성공 (POST)");
	    
	    Map<String, String> response = new HashMap<>();
	    response.put("message", "로그아웃 성공");
	    return ResponseEntity.ok(response);
	}

	// 공통 로그아웃 로직
	private void logout(HttpServletRequest request) {
	    HttpSession session = request.getSession(false);
	    if (session != null) {
	        session.invalidate();
	    }
	    SecurityContextHolder.clearContext();
	}
	
	
	
	// -------------------------- [ 회원 가입 ] 
	// 회원가입 페이지(전용화면) 이동: GET방식
	@GetMapping("/signUp")
	public String signUp() {
		
		return "member/signUp";
	}	
	
	
	
	// 회원 가입 진행 // 아이디(이메일), 비밀번호, 이름, 닉네임, 전화번호, 경력사항, 이메일 수신동의, 관리자 계정 신청 
	@PostMapping("/signUp")  
    public String signUp( 
    		 @ModelAttribute  MemberSignUpRequestDTO request  
    		 , RedirectAttributes ra
    ) {
		log.info("signUp email = {}", request.getMemberEmail());
		log.info("###@@@%%% CONTROLLER DTO = {}", request);

		String path = "redirect:";
		String message = null;
		int result=0; // placeholder
		
		try { // 회원가입 성공
			service.signUp(request); // MemberService2에서 signUp 처리, signUp 실패시 예외 발생 -> controller에서 성공(1) 실패(0)처리 반환
			result = 1;
			
			path += "/"; //메인페이지로 (JS에서?)
			message = request.getMemberNickname() + "님의 가입을 환영합니다.\n 로그인 후 서비스를 이용해 주세요.";	 // (JS에서?)
		} catch(Exception e) { // 회원가입 실패
			log.error("회원가입 실패", e);
			result = 0;
			path += "/member/signUp"; //다시 회원가입 페이지로 
			message = "서버 오류로 회원 가입에 실패했습니다.\n 잠시후 다시 이용해 주세요.";				
		}
		
		ra.addFlashAttribute("message", message);
		
		return path;
    }		
	

	
	// 필수 회원정보 입력 페이지(전용화면) 이동: GET방식
	@GetMapping("/signUpKakao")
	public String signUpKakaoPage() {
	    return "member/signUpKakao"; //  Thymeleaf
	}
	
	
	// 필수 회원정보 입력 진행 // 아이디(이메일), 비밀번호, 이름, 닉네임, 전화번호, 경력사항, 이메일 수신동의
	// 카카오 로그인한 유저가 SOCIAL_LOGIN DB에 레코드없을 경우(최초 카카오로그인경우), 회원가입 절차진행 
	@PostMapping("/signUpKakao")  
    public String signUpKakao( 
    		 @ModelAttribute  MemberSignUpRequestDTO request  
    		 , RedirectAttributes ra
    		 , HttpSession session
    ) {
		log.info("signUp email = {}", request.getMemberEmail());
		log.info("###@@@%%% CONTROLLER DTO = {}", request);

		String path = "redirect:";
		String message = null;
		int result=0; // placeholder
		
		try { // 회원가입 성공
			String kakaoId = (String)session.getAttribute("kakaoId");
			MemberLoginResponseDTO loginMemberKakao = service.signUpKakao(request, kakaoId); // MemberService2에서 signUp 처리, signUp 실패시 예외 발생 -> controller에서 성공(1) 실패(0)처리 반환
			session.setAttribute("loginMember", loginMemberKakao);
			result = 1;
			
			path += "/"; //메인페이지로 (JS에서?)
			message = request.getMemberNickname() + "님, 회원정보를 입력해 주셔서 감사합니다.";	 // (JS에서?)
		} catch(Exception e) { // 회원가입 실패
			log.error("회원가입 실패", e);
			result = 0;
			path += "/member/signUpKakao"; //다시 회원가입 페이지로 
			message = "서버 오류로 회원 정보입력에 실패했습니다.\n 잠시후 다시 이용해 주세요.";				
		}
		
		ra.addFlashAttribute("message", message);
		
		return path;
    }	
	
	
	@GetMapping("/profile")
	@ResponseBody
	public MemberProfileDTO selectProfile(
			Long memberNo
			) {
		MemberProfileDTO resp = profileService.selectProfile(memberNo);
		
		if(resp.getProfileImg() == null) {
			resp.setProfileImg("/images/logo.png");
		}
		
		
		return resp;
	}
		
	
}
