package com.devlog.project.common.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LoginFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		// down casting
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse)response;
		
		HttpSession session = req.getSession();
		
		// 로그인이 되어있지 않은 경우
		if (session.getAttribute("loginMember")==null) {
			resp.sendRedirect("/loginError"); //"/loginError" 주소로 재요청 보내서 처리
			
		} else {
			// 다음 필터 또는 컨트롤러 이동
			chain.doFilter(request, response);
		}
	}

}