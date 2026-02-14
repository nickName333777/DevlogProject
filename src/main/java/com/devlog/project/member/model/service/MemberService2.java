package com.devlog.project.member.model.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devlog.project.member.enums.CommonEnums.Status;
import com.devlog.project.member.model.dto.LevelDTO;
import com.devlog.project.member.model.dto.MemberLoginResponseDTO;
import com.devlog.project.member.model.dto.MemberSignUpRequestDTO;
import com.devlog.project.member.model.entity.Level;
import com.devlog.project.member.model.entity.Member;
import com.devlog.project.member.model.entity.SocialLogin;
import com.devlog.project.member.model.repository.KakaoSocialLoginRepository;
import com.devlog.project.member.model.repository.LevelRepository;
import com.devlog.project.member.model.repository.MemberRepository;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional // 회원가입은 하나의 트랜잭션
public class MemberService2 { // 회원가입(signUp) 서비스 전용

    private final MemberRepository memberRepository;
    private final LevelRepository levelRepository;
    private final PasswordEncoder passwordEncoder;
    //
    private final KakaoSocialLoginRepository kakaoSocialLoginRepository;
    
    public void signUp(MemberSignUpRequestDTO dto) { // devlog.com 사이트 통한 회원가입 => 이후 로그인 =>서비스 이용

    	log.info("email = {}", dto.getMemberEmail());
    	log.info("pw = {}", dto.getMemberPw());
    	System.out.println(dto);

        // 이메일 중복 체크 
        if (memberRepository.existsByMemberEmail(dto.getMemberEmail())) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }

        // 기본 레벨 조회 (LV1) 
        Level defaultLevel = levelRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("기본 레벨이 존재하지 않습니다."));

        // Member Entity 생성 
        Member member = Member.builder()
                .memberEmail(dto.getMemberEmail())
                .memberPw(passwordEncoder.encode(dto.getMemberPw())) // passwordEncoder	반드시 Service에서
                .memberName(dto.getMemberName())
                .memberNickname(dto.getMemberNickname())
                .memberTel(dto.getMemberTel())
                .memberCareer(dto.getMemberCareer())
                .memberAdmin(dto.getMemberAdmin() != null ? dto.getMemberAdmin() : Status.N) //	null 방어 처리
                .memberSubscribe(dto.getMemberSubscribe() != null ? dto.getMemberSubscribe() : Status.N) //	null 방어 처리
                .memberLevel(defaultLevel) // 기본 Level	클라이언트가 못 건드리게
                .build();

        //  저장 
        memberRepository.save(member); // 실패 시 예외 발생
    }
    
    public MemberLoginResponseDTO signUpKakao(MemberSignUpRequestDTO dto, String kakaoId) { // kakao 로그인 (최초) => devlog.com 사이트 통한 회원가입 => 바로 서비스 이용

    	log.info("email = {}", dto.getMemberEmail());
    	log.info("pw = {}", dto.getMemberPw());
    	System.out.println(dto);

        // 이메일 중복 체크 
        if (memberRepository.existsByMemberEmail(dto.getMemberEmail())) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }

        // [A] ///////////////////////////////////        
        // 기본 레벨 조회 (LV1) 
        Level defaultLevel = levelRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("기본 레벨이 존재하지 않습니다."));

        // Member Entity 생성 
        Member member = Member.builder()
                .memberEmail(dto.getMemberEmail())
                .memberPw(passwordEncoder.encode(dto.getMemberPw())) // passwordEncoder	반드시 Service에서
                .memberName(dto.getMemberName())
                .memberNickname(dto.getMemberNickname())
                .memberTel(dto.getMemberTel())
                .memberCareer(dto.getMemberCareer())
                .memberAdmin(dto.getMemberAdmin() != null ? dto.getMemberAdmin() : Status.N) //	null 방어 처리
                .memberSubscribe(dto.getMemberSubscribe() != null ? dto.getMemberSubscribe() : Status.N) //	null 방어 처리
                .memberLevel(defaultLevel) // 기본 Level	클라이언트가 못 건드리게
                .build();

        // MEMBER TABLE 저장 
        memberRepository.save(member); // 실패 시 예외 발생 ==> 저장되면 이 member의 memberNo 생성됨
        
        // [B] ///////////////////////////////////        
        // SocialLogin Entity 생성 
        //String kakaoId = (String)session.getAttribute("kakaoId");
        log.info("kakaoId = {}", kakaoId);
        SocialLogin socialLogin = SocialLogin.builder()
        		.provider("kakao")
        		.providerId(kakaoId)
        		.memberNo(member)
        		.build();

        // SOCIAL_LOGIN TABLE 저장        
        kakaoSocialLoginRepository.save(socialLogin); // 실패 시 예외 발생
        
        // [C] ///////////////////////////////////        
        // MemberLoginResponseDTO 생성해서 반환해 주기
    	// 이제 MemberLoginResponseDTO 만들자 with MemberSignUpRequestDTO dto
        
        String role =  dto.getMemberAdmin() == Status.N ? "ROLE_USER" : "ROLE_ADMIN";
  
        //Level level = member.getMemberLevel(); // LAZY 초기화 (트랜잭션 안)
        // 기본 레벨 조회 (LV1): 이거 위에서 했다. 그거 그대로 가져다 쓴다. 
        //Level defaultLevel = levelRepository.findById(1)
        //        .orElseThrow(() -> new IllegalStateException("기본 레벨이 존재하지 않습니다."));
        LevelDTO levelDTO = new LevelDTO(
        		defaultLevel.getLevelNo(),
        		defaultLevel.getTitle(),
        		defaultLevel.getRequiredTotalExp()
        );        	
        
        // 위에서 member entity 조회하여 memberNo를 포함함 모든 필드 읽어와야 MemberLoginResponseDTO 만들수 있다.
        Member member2 = memberRepository
                .findByMemberEmailAndMemberDelFl(dto.getMemberEmail(), Status.N)
                .orElseThrow(() ->
                        new UsernameNotFoundException("회원이 존재하지 않습니다.") // 바로 위에서 저장한 거라, 이게 발생하면 안됨
                );

        
        MemberLoginResponseDTO memberKakaoDTO = new MemberLoginResponseDTO(
        		member2.getMemberNo(),
        		member2.getMemberEmail(),
        		member2.getMemberNickname(),
                role,
                member2.getMemberAdmin(),
                member2.getMemberSubscribe(),
                member2.getMemberDelFl(),
                member2.getMemberCareer(),
                member2.getProfileImg(),
                member2.getMyInfoIntro(),
                member2.getMyInfoGit(),
                member2.getMyInfoHomepage(),
                member2.getSubscriptionPrice(),
                member2.getBeansAmount(),
                member2.getCurrentExp(),
                member2.getMCreateDate(),
                levelDTO
                );        
        
        return memberKakaoDTO;
    }    
    
    
    
}
