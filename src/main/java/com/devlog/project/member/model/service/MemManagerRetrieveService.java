package com.devlog.project.member.model.service;

import java.util.Collection;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devlog.project.member.enums.CommonEnums.Status;
import com.devlog.project.member.model.dto.LevelDTO;
import com.devlog.project.member.model.dto.MemberInfoResponseDTO;
import com.devlog.project.member.model.dto.MemberKakaoSocialLoginResponseDTO;
import com.devlog.project.member.model.dto.MemberLoginResponseDTO;
import com.devlog.project.member.model.entity.Level;
import com.devlog.project.member.model.entity.Member;
import com.devlog.project.member.model.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemManagerRetrieveService {

	private final MemberRepository memberRepository;
	
    @Transactional(readOnly = true)
    public MemberInfoResponseDTO selectMemberByMemberEmail(
    						String memberEmail
                            ) {

	    	Optional<Member> member2look = memberRepository.findByMemberEmail(memberEmail); // loginMember_kakao는 socialOpt.get().getMemberNo()와 같아야함
	    	// for double-checking
	    	log.info("[ memberEmail checking ] =>  { } ", memberEmail.equals(member2look.get().getMemberEmail()));
	    	
	    	// 이제 MemberInfoResponseDTO 만들자.
	        String role =  member2look.get().getMemberAdmin() == Status.N ? "ROLE_USER" : "ROLE_ADMIN";
	    	Level level = member2look.get().getMemberLevel(); // LAZY 초기화 (트랜잭션 안)
	
	        LevelDTO levelDTO = new LevelDTO(
	            level.getLevelNo(),
	            level.getTitle(),
	            level.getRequiredTotalExp()
	        );        	
	        
	        MemberInfoResponseDTO memberDTO = new MemberInfoResponseDTO(
	        		member2look.get().getMemberNo(),
	        		member2look.get().getMemberEmail(),
	        		member2look.get().getMemberName(),
	        		member2look.get().getMemberNickname(),
	        		member2look.get().getMemberTel(),
	                role,
	                member2look.get().getMemberAdmin(),
	                member2look.get().getMemberSubscribe(),
	                member2look.get().getMemberDelFl(),
	                member2look.get().getMemberCareer(),
	                member2look.get().getProfileImg(),
	                member2look.get().getMyInfoIntro(),
	                member2look.get().getMyInfoGit(),
	                member2look.get().getMyInfoHomepage(),
	                member2look.get().getSubscriptionPrice(),
	                member2look.get().getBeansAmount(),
	                member2look.get().getCurrentExp(),
	                member2look.get().getMCreateDate(),
	                levelDTO
	                );
	    	return memberDTO; // 조회하는 회원의 정보반환 
    }
    
    
    @Transactional(readOnly = true)
    public MemberInfoResponseDTO selectMemberByMemberNickname(
    						String memberNickname 
    						) {

	    	Optional<Member> member2look = memberRepository.findByMemberNickname(memberNickname); // loginMember_kakao는 socialOpt.get().getMemberNo()와 같아야함
	    	// for double-checking
	    	log.info("[ memberNickname checking ] =>  { } ", memberNickname.equals(member2look.get().getMemberNickname()));
	    	
	    	// 이제 MemberInfoResponseDTO 만들자.
	        String role =  member2look.get().getMemberAdmin() == Status.N ? "ROLE_USER" : "ROLE_ADMIN";
	    	Level level = member2look.get().getMemberLevel(); // LAZY 초기화 (트랜잭션 안)
	
	        LevelDTO levelDTO = new LevelDTO(
	            level.getLevelNo(),
	            level.getTitle(),
	            level.getRequiredTotalExp()
	        );        	
	        
	        MemberInfoResponseDTO memberDTO = new MemberInfoResponseDTO(
	        		member2look.get().getMemberNo(),
	        		member2look.get().getMemberEmail(),
	        		member2look.get().getMemberName(),
	        		member2look.get().getMemberNickname(),
	        		member2look.get().getMemberTel(),
	                role,
	                member2look.get().getMemberAdmin(),
	                member2look.get().getMemberSubscribe(),
	                member2look.get().getMemberDelFl(),
	                member2look.get().getMemberCareer(),
	                member2look.get().getProfileImg(),
	                member2look.get().getMyInfoIntro(),
	                member2look.get().getMyInfoGit(),
	                member2look.get().getMyInfoHomepage(),
	                member2look.get().getSubscriptionPrice(),
	                member2look.get().getBeansAmount(),
	                member2look.get().getCurrentExp(),
	                member2look.get().getMCreateDate(),
	                levelDTO
	                );
	    	return memberDTO; // 조회하는 회원의 정보반환 
    }    
    
    @Transactional(readOnly = true)
    public MemberInfoResponseDTO selectMemberByMemberNo(
    					Long memberNo
                        ) {

	    	Optional<Member> member2look = memberRepository.findById(memberNo); // loginMember_kakao는 socialOpt.get().getMemberNo()와 같아야함
	    	// for double-checking
	    	log.info("[ memberNo checking ] =>  { } ", memberNo == member2look.get().getMemberNo() );
	    	
	    	// 이제 MemberInfoResponseDTO 만들자.
	        String role =  member2look.get().getMemberAdmin() == Status.N ? "ROLE_USER" : "ROLE_ADMIN";
	    	Level level = member2look.get().getMemberLevel(); // LAZY 초기화 (트랜잭션 안)
	
	        LevelDTO levelDTO = new LevelDTO(
	            level.getLevelNo(),
	            level.getTitle(),
	            level.getRequiredTotalExp()
	        );        	
	        
	        MemberInfoResponseDTO memberDTO = new MemberInfoResponseDTO(
	        		member2look.get().getMemberNo(),
	        		member2look.get().getMemberEmail(),
	        		member2look.get().getMemberName(),
	        		member2look.get().getMemberNickname(),
	        		member2look.get().getMemberTel(),
	                role,
	                member2look.get().getMemberAdmin(),
	                member2look.get().getMemberSubscribe(),
	                member2look.get().getMemberDelFl(),
	                member2look.get().getMemberCareer(),
	                member2look.get().getProfileImg(),
	                member2look.get().getMyInfoIntro(),
	                member2look.get().getMyInfoGit(),
	                member2look.get().getMyInfoHomepage(),
	                member2look.get().getSubscriptionPrice(),
	                member2look.get().getBeansAmount(),
	                member2look.get().getCurrentExp(),
	                member2look.get().getMCreateDate(),
	                levelDTO
	                );
	    	return memberDTO; // 조회하는 회원의 정보반환 
    }    
    
}
