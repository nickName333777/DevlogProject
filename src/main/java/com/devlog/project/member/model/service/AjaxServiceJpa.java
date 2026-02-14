package com.devlog.project.member.model.service;


import org.springframework.stereotype.Service;

import com.devlog.project.member.model.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service // 비즈니스 로직 처리 + bean 등록
@RequiredArgsConstructor
public class AjaxServiceJpa {

    private final MemberRepository memberRepository;
    
    // 이메일 중복 유효성 검사 by ajax
    public int dupCheckEmail(String email) {
    	int result = 0;
    	if (memberRepository.existsByMemberEmail(email)) { // 이미 사용중인 이메일 경우
    		result = 1;
    	}
    	return result;
    }
    
    // 닉네임 중복 유효성 검사 by ajax
    public int dupCheckNickname(String nickname) {
    	int result = 0;
    	if (memberRepository.existsByMemberNickname(nickname)) { // 이미 사용중인 닉네임 경우
    		result = 1;
    	}
    	return result;
    }    
    
}
