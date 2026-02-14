package com.devlog.project.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig { 

    @Bean
    public PasswordEncoder passwordEncoder() { // 이 Bean 없으면 로그인 무조건 실패, Password비교는 Security가 자동처리
        return new BCryptPasswordEncoder();
    }
}
