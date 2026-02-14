package com.devlog.project.member.model.repository;

import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devlog.project.member.model.entity.Auth;


public interface EmailRepository extends JpaRepository<Auth, Long> {

	 boolean existsByEmail(String email); // AUTH 테이블에 해당 이메일 존재 체크
	 
	 Optional<Auth> findByEmail(String email); // 해당 이메일 존재하면 읽어오기
	 
	 int countByCodeAndEmail(String code, String email); // checkAuthKey: 유저 입력 code&email와 일치하는 레코드 수 조회 
}
