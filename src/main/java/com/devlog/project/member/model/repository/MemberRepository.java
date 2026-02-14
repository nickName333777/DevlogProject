package com.devlog.project.member.model.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devlog.project.member.enums.CommonEnums.Status;
import com.devlog.project.member.model.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long>  {
	
    Optional<Member> findByMemberEmailAndMemberDelFl(String memberEmail, Status memberDelFl); // memberDelFl=N 회원만 조회
    
    boolean existsByMemberEmail(String memberEmail); // 회원가입중복체크
    
    boolean existsByMemberNickname(String memberNickname); // 닉네임중복체크
    
    
    // 닉네임 일치하는 회원 반환
	List<Member> findByMemberNicknameIn(Set<String> mentionNicknames);
	

    // for 관리자 회원정보 조회기능 (findById는 jpa 기본제공)
    Optional<Member> findByMemberEmail(String memberEmail); // memberDelFl=N, Y 모든 회원 조회 by 관리자
    Optional<Member> findByMemberNickname(String memberNickname); // memberDelFl=N, Y 모든 회원 조회 by 관리자	
    
    
    // 회원 번호 조회
    Member findMemberNoByMemberEmail(String memberEmail);
	
}

