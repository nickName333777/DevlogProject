package com.devlog.project.member.model.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.devlog.project.member.enums.CommonEnums.Status;
import com.devlog.project.member.model.entity.Member;
import com.devlog.project.member.model.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService { 	// Spring Security가 로그인 시 자동 호출
																		// DB에서 회원 조회, 없으면 예외 발생
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String memberEmail) 	// Security 내부 자동 호출 메서드
    															// 'memberEmail' as username
            throws UsernameNotFoundException {

        Member member = memberRepository
                .findByMemberEmailAndMemberDelFl(memberEmail, Status.N)
                .orElseThrow(() ->
                        new UsernameNotFoundException("회원이 존재하지 않습니다.")
                );

        return new CustomUserDetails(member);
    }
}
