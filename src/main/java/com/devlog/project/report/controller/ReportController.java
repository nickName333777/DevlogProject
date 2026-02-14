package com.devlog.project.report.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.devlog.project.member.model.dto.MemberLoginResponseDTO;
import com.devlog.project.report.enums.ReportTargetEnums;
import com.devlog.project.report.model.dto.ReportRequestDTO;
import com.devlog.project.report.model.dto.ReportTargetDTO;
import com.devlog.project.report.model.dto.ReportTypeDTO;
import com.devlog.project.report.model.service.ReportService;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ReportController {
	
	private final ReportService service;
	
	@GetMapping("/report/modal")
	public String openReportModal(
			@RequestParam("memberNo") Long targetMemberNo,
			Model model
			) {
		
		ReportTargetDTO taregetMember = service.findMember(targetMemberNo);
		
		List<ReportTypeDTO> reportType = service.findReportType();
		
		model.addAttribute("targetMember", taregetMember);
		model.addAttribute("reportType", reportType);
		
		return "common/report :: #reportModal" ;
	}
	
	@PostMapping("/report")
	@ResponseBody
	public String reportInsert(
			@RequestBody ReportRequestDTO req,
			@SessionAttribute("loginMember") MemberLoginResponseDTO loginMember
			) {
		
		req.setMemberNo(loginMember.getMemberNo());
		
		System.out.println(req);
		
		String result = null;
		
		if(req.getTargetType() == ReportTargetEnums.MESSAGE) {
			
			result = service.reportInsertJpa(req);
			
		} else {
			// 여기서 mybatis 흐름으로 짜시면 아마 될 거 같아요
			 result = service.reportInsertBoard(req);
			 
			
		}
		
		
		return result;
		
		
		
		
	}
	
	

}
