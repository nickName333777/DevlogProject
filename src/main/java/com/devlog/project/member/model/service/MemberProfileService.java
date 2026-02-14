package com.devlog.project.member.model.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.devlog.project.member.model.dto.MemberProfileDTO;
import com.devlog.project.member.model.entity.Level;
import com.devlog.project.member.model.entity.Member;
import com.devlog.project.member.model.repository.LevelRepository;
import com.devlog.project.member.model.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberProfileService {
	
	private final MemberRepository repository;
	private final LevelRepository levelRepository;
	
	public MemberProfileDTO selectProfile(Long memberNo) {
		
		Member member = repository.findById(memberNo)
						.orElseThrow();
		
		
		MemberProfileDTO resp = MemberProfileDTO.builder()
								.memberNo(memberNo)
								.memberNickname(member.getMemberNickname())
								.profileImg(member.getProfileImg())
								.level(member.getMemberLevel().getLevelNo())
								.levelTitle(member.getMemberLevel().getTitle())
								.email(member.getMemberEmail())
								.build();
		
		return resp;
	}
	
	// 멤버 리스트 조회
//	public List<Member> findMemberList() {
//		
//		return repository.findByMemberEmail(null)l;
//	}

}
