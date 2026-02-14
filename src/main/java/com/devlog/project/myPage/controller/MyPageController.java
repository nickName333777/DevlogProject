package com.devlog.project.myPage.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;

import com.devlog.project.board.blog.service.BlogService;
import com.devlog.project.member.enums.CommonEnums;
import com.devlog.project.member.model.dto.MemberLoginResponseDTO;
import com.devlog.project.member.model.entity.Member;
import com.devlog.project.member.model.repository.MemberRepository;
import com.devlog.project.myPage.dto.MemberUpdateDto;
import com.devlog.project.myPage.dto.MyActivityDto;
import com.devlog.project.myPage.service.MyPageService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
@RequiredArgsConstructor
public class MyPageController {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final BlogService blogService;
    private final MyPageService myPageService;

    // 1. 내 활동 페이지
    @GetMapping("/myPage/activity")
    public String myActivity() {
        return "myPage/myActivity"; // templates/myPage/myActivity.html
    }

    // 2. 비밀번호 확인 페이지 (프로필 설정 전 단계)
    @GetMapping("/myPage/check-password")
    public String checkPasswordPage() {
        return "myPage/pwCheck"; // templates/myPage/pwCheck.html
    }

    // 3. 프로필 설정 페이지 (내 정보 가져오기)
    @GetMapping("/myPage/settings")
    public String mySettings(Model  model) {
        
        // 1. 로그인한 사용자 ID(이메일) 가져오기
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // 2. DB에서 회원 정보 조회
        Member member = memberRepository.findByMemberEmailAndMemberDelFl(email, CommonEnums.Status.N)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보 없음"));
        
        // 3. 모델에 담아서 HTML로 보냄
        model.addAttribute("member", member);
        
        return "myPage/mySetting"; 
    }

    // [API] 비밀번호 검증 (JS에서 호출)
    @PostMapping("/api/myPage/verify-password")
    @ResponseBody
    public boolean verifyPassword(@RequestBody Map<String, String> request) {
        String inputPw = request.get("password");
        
        // 현재 로그인한 사용자 이메일 가져오기
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Member member = memberRepository.findByMemberEmailAndMemberDelFl(email, CommonEnums.Status.N)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보 없음"));

        // 입력한 비밀번호와 DB 비밀번호 비교
        return passwordEncoder.matches(inputPw, member.getMemberPw());
    }
    
    // [API] 내 정보 변경 (MemberService -> MyPageService로 교체)
    @PostMapping("/api/myPage/update/info")
    @ResponseBody
    public Map<String, Object> updateMemberInfo(@RequestBody MemberUpdateDto dto) {
        
    	System.out.println(">>> 프론트에서 받은 DTO: " + dto.toString());
    	
    	String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return myPageService.updateMemberInfo(email, dto);
    }

    // [API] 닉네임 중복 확인
    @GetMapping("/api/myPage/check/nickname")
    @ResponseBody
    public int checkNickname(@RequestParam("nickname") String nickname) {
        return myPageService.checkNicknameDup(nickname);
    }
    
    // [API] 프로필 이미지 변경 (MemberService -> MyPageService로 교체)
    @PostMapping("/api/myPage/update/image")
    @ResponseBody
    public String updateProfileImage(@RequestParam("image") MultipartFile image) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // 1. 파일 업로드
        String imageUrl = blogService.uploadImage(image);
        
        // 2. DB 업데이트 (MyPageService 사용)
        myPageService.updateProfileImage(email, imageUrl);
        
        return imageUrl;
    }
    
    
    @PostMapping("/api/myPage/subscribe")
    @ResponseBody
    public ResponseEntity<Void>  setSubscribePrice(
    		@RequestBody Map<String, Object> paramMap,
    		@SessionAttribute("loginMember") MemberLoginResponseDTO loginMember
    		) {
    	
    	
    	Long memberNo = loginMember.getMemberNo();
    	
    	paramMap.put("memberNo", memberNo);
    	
    	myPageService.setSubscribePrice(paramMap);
    	
    	return ResponseEntity.ok().build();
    	
    }
    
    // [API] 내 활동 리스트 조회
    @GetMapping("/api/myPage/activity-list")
    @ResponseBody
    public List<MyActivityDto> getActivityList(@RequestParam("type") String type) {
        
        // 현재 로그인한 사용자 찾기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        // 회원 번호(memberNo) 구하기
        Member member = memberRepository.findByMemberEmailAndMemberDelFl(email, CommonEnums.Status.N)
                .orElseThrow(() -> new IllegalArgumentException("로그인 정보가 없습니다."));
        
        // 서비스 호출 및 결과 반환
        return myPageService.getMyActivityList(member.getMemberNo(), type);
    }
    
    // 비밀번호 변경
    @PostMapping("/api/changePw")
    @ResponseBody
    public ResponseEntity<Integer> changePw(
    		@RequestBody Map<String, Object> paramMap,
    		@SessionAttribute("loginMember") MemberLoginResponseDTO loginMember
    		) {
    	
    	paramMap.put("memberNo", loginMember.getMemberNo());
    	
    	Integer result = myPageService.changePw(paramMap);
    	
    	return ResponseEntity.ok(result);
    }
    
    // 회원 탈퇴
    @DeleteMapping("/api/withdraw")
    @ResponseBody
    public ResponseEntity<Integer> withdraw(
    		@RequestBody Map<String, String> paramMap,
            @SessionAttribute("loginMember") MemberLoginResponseDTO loginMember,
            HttpSession session
    		){
    	
    	int result = myPageService.withdraw(loginMember.getMemberNo(), paramMap.get("checkPw"));

        if (result == 1) {  // 성공
            session.invalidate();
        }
    	
    	return ResponseEntity.ok(result);
    }
    	
    
    
    
}