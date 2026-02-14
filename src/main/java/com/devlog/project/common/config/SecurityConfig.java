package com.devlog.project.common.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.devlog.project.member.model.security.CustomUserDetailsService;
import com.devlog.project.member.model.security.handler.CustomAuthenticationEntryPoint;
import com.devlog.project.member.model.security.handler.CustomAuthenticationFailureHandler;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration  // 설정용 Bean 생성 클래스
@RequiredArgsConstructor
public class SecurityConfig {


	private final CustomUserDetailsService userDetailsService;
	
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	
	private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
	

	@Bean
	@Order(1)
	public SecurityFilterChain authFilterChain(HttpSecurity http) throws Exception { 
	    // [ 인증전용 체인(로그인/로그아웃 전용) ]

	    http
	        .securityMatcher("/member/login", "/member/logout")
	        .csrf(csrf -> csrf.disable())
	        .authorizeHttpRequests(auth -> auth
	            .anyRequest().permitAll()  // 이 경로들은 인증 없이 접근 가능
	        );
	        // .formLogin() 설정 제거 - Controller에서 직접 인증 처리        
	    	// .logout() 설정 제거 - 컨트롤러에서 처리        

	    return http.build();
	}	
	
	
	@Bean
	@Order(2)
	public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception { // [ API / 회원가입체인 (JSON body 안전) ]

	    http
	        .csrf(csrf -> csrf.disable())
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers(
	                "/",
	                "/css/**",
	                "/js/**",
	                "/images/**",
	                "/payment/**",
	                "/favicon.ico",
	                "/member/signUp",
	                "/member/signUp-debug",
	                "/member/signUpTest",
	                "/error",
	                "/api/search/**",
	                "/search/blog/**",
	                
	                "/devtalk/**",
					"/ws-chat/**",
	                "/topic/**",
	                "/queue/**",
	                "/report/**", // ??
	                
	                //// KJY ////
	                "/jobposting",
	                "/jobposting/**",
	                "/job-crawler",
	                "/ITnews/**",
	                "/ITnews",
	                "/coffeebeans/**",
	                "/online/**"
	     
	                
	                
	                
	                ///// PYY API Addition Start: /////
	                ,"/dupCheck/**"
	                ,"/sendEmail/**"
	                ,"/checkCode/**"
	                ,"/board/freeboard/**"
	                ,"/app/login/**"
	                ,"/member/signUpKakao"	       
	                ,"/board2/freeboard/**" ///
	                ,"/api/chatbot/**"
	                ,"/api/ai/**"
	                ,"/api/manager/**" 	                
	                ///// PYY API Addition End: /////	          
	                
	                ,"/board/qna/**"
                
	                ////* YHJ *////  
	                ,"/blog/list"
	                ,"/blog/detail/**"
	                ,"/api/blog/list"
	                ,"/blog/*"
	                ,"/api/blog/*/list"
	                
	                
	                ,"/api/ai/writing/**"
	                
	            ).permitAll()
	            .anyRequest().authenticated()
	        )
	        .exceptionHandling(ex ->
	            ex.authenticationEntryPoint(customAuthenticationEntryPoint)
	        )
	        .formLogin(form -> form.disable()); // 로그인 vs API/회원가입 분리용

	    return http.build();
	}
	

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }	
	
}
