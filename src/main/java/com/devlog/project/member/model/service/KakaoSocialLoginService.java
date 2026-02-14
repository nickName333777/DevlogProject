package com.devlog.project.member.model.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.devlog.project.member.enums.CommonEnums.Status;
import com.devlog.project.member.model.dto.KakaoSocialLoginResponseDTO;
import com.devlog.project.member.model.dto.LevelDTO;
import com.devlog.project.member.model.dto.MemberKakaoSocialLoginResponseDTO;
import com.devlog.project.member.model.dto.MemberLoginResponseDTO;
import com.devlog.project.member.model.entity.Level;
import com.devlog.project.member.model.entity.Member;
import com.devlog.project.member.model.entity.SocialLogin;
import com.devlog.project.member.model.repository.MemberRepository;
import com.devlog.project.member.model.repository.KakaoSocialLoginRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoSocialLoginService {

    private final KakaoSocialLoginRepository socialLoginRepository;
    private final MemberRepository memberRepository;
    private final RestTemplate restTemplate = new RestTemplate();	
	
    @Value("${KAKAO_REDIRECT_URI}") // pring 파일에서는 @Value를 통해서 application.yml에 ${KAKAO_REDIRECT_URI}로 정의된 환경변수, env 값을 가져올 수 있다.
    private String kakao_redirect_uri;    
    
    @Value("${KAKAO_REST_API_KEY}")
    private String kakao_rest_api_key;    
    
    @Value("${KAKAO_CLIENT_SECRET}")
    private String kakao_client_secret;       
    
    
    public String getKakaoAuthUrl() {
        return "https://kauth.kakao.com/oauth/authorize" +
               "?response_type=code" +
               "&client_id=" + kakao_rest_api_key +
               "&redirect_uri=" + URLEncoder.encode(kakao_redirect_uri, StandardCharsets.UTF_8);
    }
    
    
    @Transactional(readOnly = true)
    public MemberKakaoSocialLoginResponseDTO processKakaoLogin(String code) {
        // 1. 카카오 access token 요청
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=authorization_code" +
                "&client_id="+ kakao_rest_api_key +
                "&client_secret=" + kakao_client_secret +
                "&redirect_uri=" + kakao_redirect_uri +
                "&code=" + code;

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class); // 실제요청보내고/응답받기

        JSONObject json = null;
		try {
			json = new JSONObject(response.getBody());
		} catch (JSONException e) {
			e.printStackTrace();
		}
        String accessToken = null;
		try {
			accessToken = json.getString("access_token");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		log.info("[ accessToken ] =>  { } ", accessToken);
		
        // 2. 사용자 정보 가져오기
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

        ResponseEntity<String> userResponse = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                userRequest,
                String.class
        ); // 실제요청보내고/응답받기

        JSONObject userJson = null;
		try {
			userJson = new JSONObject(userResponse.getBody());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
        String kakaoId = null;
		try {
			kakaoId = String.valueOf(userJson.getLong("id"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		log.info("[ kakaoId ] =>  { } ", kakaoId);

        // 3. SOCIAL_LOGIN DB 조회
        Optional<SocialLogin> socialOpt = socialLoginRepository.findByProviderAndProviderId("kakao", kakaoId);
        if (socialOpt.isPresent()) { // SOCIAL_LOGIN DB에 존재
        	Member member = socialOpt.get().getMemberNo(); // socialOpt.get().getMemberNo()는 실제로 Member Entity 
        	
        	// for double-checking: member vs. loginMemberKakao
        	Long memberNo = member.getMemberNo(); // memberNo만 꺼내서 Long memberNo를 반환할수도 있다
        	Optional<Member> loginMemberKakao = memberRepository.findById(memberNo); // loginMember_kakao는 socialOpt.get().getMemberNo()와 같아야함
        	log.info("[ member checking ] =>  { } ", member.getMemberEmail().equals(loginMemberKakao.get().getMemberEmail()));
        	
        	// 이제 MemberLoginResponseDTO 만들자.
            String role =  member.getMemberAdmin() == Status.N ? "ROLE_USER" : "ROLE_ADMIN";
        	Level level = member.getMemberLevel(); // LAZY 초기화 (트랜잭션 안)

            LevelDTO levelDTO = new LevelDTO(
                level.getLevelNo(),
                level.getTitle(),
                level.getRequiredTotalExp()
            );        	
            
            MemberLoginResponseDTO memberDTO = new MemberLoginResponseDTO(
                    member.getMemberNo(),
                    member.getMemberEmail(),
                    member.getMemberNickname(),
                    role,
                    member.getMemberAdmin(),
                    member.getMemberSubscribe(),
                    member.getMemberDelFl(),
                    member.getMemberCareer(),
                    member.getProfileImg(),
                    member.getMyInfoIntro(),
                    member.getMyInfoGit(),
                    member.getMyInfoHomepage(),
                    member.getSubscriptionPrice(),
                    member.getBeansAmount(),
                    member.getCurrentExp(),
                    member.getMCreateDate(),
                    levelDTO
                    );
            
            MemberKakaoSocialLoginResponseDTO memberKakaoDTO = new MemberKakaoSocialLoginResponseDTO(
            		memberDTO,
                    accessToken, // for kakao social login
                    kakaoId // for kakao social login
        			);
        	
        	return memberKakaoDTO; // kakao 소셜 로그인한 기존 회원 정보반환 
        	
        } else { // SOCIAL_LOGIN DB에 존재하지 않을 때
        	
            MemberKakaoSocialLoginResponseDTO memberKakaoDTO = new MemberKakaoSocialLoginResponseDTO(
            		null,
                    accessToken, // for kakao social login
                    kakaoId // for kakao social login
        			);        	   	
           	
            return memberKakaoDTO; // 신규 회원이면 memberKakaoDTO.memberDTO = null 반환 -> 컨트롤러에서 signUp으로 redirect
        }
    }
    
}
