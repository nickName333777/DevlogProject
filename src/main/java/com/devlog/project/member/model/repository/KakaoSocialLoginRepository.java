package com.devlog.project.member.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devlog.project.member.model.entity.SocialLogin;

public interface KakaoSocialLoginRepository extends JpaRepository<SocialLogin, Long>{

	Optional<SocialLogin> findByProviderAndProviderId(String provider, String providerId);
}
