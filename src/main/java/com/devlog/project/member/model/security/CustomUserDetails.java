package com.devlog.project.member.model.security;


import lombok.Getter;
import lombok.ToString;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.devlog.project.member.enums.CommonEnums.Status;
import com.devlog.project.member.model.entity.Member;

import java.util.Collection;
import java.util.List;

@Getter
@ToString(exclude = {"member"})  // Member 제외 (순환참조 방지)
public class CustomUserDetails implements UserDetails {

    private final Member member;

    // 생성자
    public CustomUserDetails(Member member) { 
        this.member = member;
    }


    // 권한
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
    	// 이걸로 SecurityContext에서 hasAuthority("ADMIN") 체크가능
        if (member.getMemberAdmin()  == Status.Y) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    // 인증 정보
    @Override
    public String getPassword() {
        return member.getMemberPw();
    }

    @Override
    public String getUsername() {
        return member.getMemberEmail();
    }

    // 계정 상태
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return member.getMemberDelFl() == Status.N;
    }
}

