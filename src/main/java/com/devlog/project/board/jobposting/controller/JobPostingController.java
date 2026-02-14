package com.devlog.project.board.jobposting.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.devlog.project.board.ITnews.service.ITnewsService;
import com.devlog.project.board.jobposting.dto.JobPostingDTO;
import com.devlog.project.board.jobposting.service.JobPostingService;
import com.devlog.project.member.model.dto.MemberLoginResponseDTO;

@Controller
public class JobPostingController {
	
	@Autowired
	private JobPostingService jobPostingService;
	
	@Autowired
	private ITnewsService itnewsService;
	
	
	// 채용공고 캘린더 화면 전환
	@GetMapping("/jobposting")
	public String jobposting(
			Model model) {
		List<JobPostingDTO> jobcalender = jobPostingService.selectjoblist();
//		System.out.println(jobcalender);
		model.addAttribute("jobcalender", jobcalender);
		
		return "board/Jobposting/calender";
	}
	
	// 채용공고 크롤링
	@GetMapping("/job-crawler")
	@ResponseBody
	public void JobCrawler() {
		jobPostingService.JobCrawler();
	}
	
	
	// 채용공고 상세 이동
	@GetMapping("/jobposting/{id}")
	public String jobPostingDetail(
			@PathVariable Long id,
			Model model,
			@SessionAttribute(value = "loginMember", required = false) MemberLoginResponseDTO loginMember) {
		
		JobPostingDTO detail = jobPostingService.selectDetail(id);
		model.addAttribute("job", detail);
		System.out.println(detail);
		
		if (loginMember != null) {
	        Map<String, Object> scrapMap = new HashMap<>();
	        scrapMap.put("targetNo", id);                       // 채용공고 번호
	        scrapMap.put("memberNo", loginMember.getMemberNo()); // 내 회원 번호
	        scrapMap.put("type", "2");                           //채용공고 타입 2번

	        int scrapCheck = itnewsService.checkScrap(scrapMap); 
	        model.addAttribute("scrapCheck", scrapCheck);
	    }
		
		
		return "board/Jobposting/jobpostDetail"; 
	}
	
	
	
	
	
	
	
	
}
