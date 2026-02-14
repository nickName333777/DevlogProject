package com.devlog.project.member.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.devlog.project.member.model.dto.MemberKakaoSocialLoginResponseDTO;
import com.devlog.project.member.model.service.KakaoSocialLoginService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller // @Controller: 리턴값을 "뷰이름"으로 해석, @RestController: 리턴값을 HTTP Body(JSON)로 해석 
			// => ResponseEntity는 자동으로 Body(JSON)이 아님 => @RestController 또는 @Controller + @ResponseBody 이어야함
@RequestMapping("/app/login")  // GET and POST 다 처리
@RequiredArgsConstructor 
public class KakaoSocialLoginController {

    private final KakaoSocialLoginService kakaoSocialLoginService;
     	
    @GetMapping("/kakao")
    public String kakaoAuthServer() { // window.location.href = "/app/login/kakao"; 로 전달되는 query string parameter 없음
    	
    		return "redirect:" + kakaoSocialLoginService.getKakaoAuthUrl();
    } // JS 카카오 로그인 버튼 클릭 =>  콘트롤러 => 카카오 인증 서버로 리다이렉트
    
    
    @GetMapping("/kakao/callback") // 카카오 인증 서버에서 인증후 받아온 인가코드 => 콘트롤러에서 인가코드로 => 카카오에서서 accessToken받아오기 => accessToken으로 kakaoId, 사용자정보 얻어오기 => 서비스 웹사이트 로그인 처리 마무리(MEMBER, SOCIAL_LOGIN DB 작업등)
    public String kakaoCallback(@RequestParam("code") String code
    							, HttpSession session
    							, RedirectAttributes ra
    							) {
    	MemberKakaoSocialLoginResponseDTO memberKakaoDTO = kakaoSocialLoginService.processKakaoLogin(code); // SocialLogin DB에 있을시 회원정보다 받기
        log.info("[ memberKakaoDTO ] =>  { }", memberKakaoDTO);
        
        if (memberKakaoDTO.getMemberDTO() != null) { // 기존 SOCAIL_LOGIN DB에 있는 멤버
            session.setAttribute("loginMember", memberKakaoDTO.getMemberDTO());
            return "redirect:/"; // kakao 로그인 후 메인 페이지로
        } else { // 기존 SOCAIL_LOGIN DB에 없는 최초 kakao 로그인 멤버 
            session.setAttribute("kakaoId", memberKakaoDTO.getKakaoId()); // signUp에서 사용할 수 있도록 카카오 id 저장
            
            String message = "카카오 로그인에 성공했습니다.\n" + 
            				 "DevLog 서비스를 원활히 이용하시기 위해서는 필수 회원 정보가 필요합니다.\n" + 
            				 "회원 정보를 입력해 주세요. 감사합니다.";	
            ra.addFlashAttribute("message", message);
            
            return "redirect:/member/signUpKakao";
        }
    }    
    
}
