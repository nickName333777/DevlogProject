package com.devlog.project.main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.devlog.project.board.ITnews.dto.ITnewsDTO;
import com.devlog.project.board.ITnews.service.ITnewsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {

	@Autowired
	private ITnewsService itnewsService;
	
	@GetMapping("/")
	public String mainPage(HttpSession session, Model model) {
		
	    model.addAttribute("loginMember", session.getAttribute("loginMember"));

	    Integer boardCode = null;
		List<ITnewsDTO> itnews = itnewsService.selectITnewsList(boardCode);
		model.addAttribute("itnews", itnews);
	    
	    return "common/main";
	}
	
	
	@GetMapping("/loginError")
	public String loginError(RedirectAttributes ra) {
		ra.addFlashAttribute("message", "로그인 후 이용해 주세요.");
		
		return "redirect:/";
	}	
	
}
