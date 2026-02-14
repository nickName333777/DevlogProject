package com.devlog.project.manager.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.devlog.project.manager.model.dto.ReportManagerDTO;
import com.devlog.project.manager.model.service.ManagerReportService;
import com.devlog.project.member.model.dto.MemberLoginResponseDTO;
import com.devlog.project.report.enums.ReportStatus;
import com.devlog.project.report.enums.ReportTargetEnums;
import com.devlog.project.pay.dto.PayDTO;
import com.devlog.project.pay.service.PayService;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
@RequestMapping("/manager")
public class ManagerController {
	
    private final ManagerReportService managerReportService;
    
	@Autowired PayService payService;
	
	
	
    // 관리자 홈
    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "manager/manager-home";
    }
    
    @GetMapping("/dashboard/customer")
    public String adminCustomerDashboard() {
        return "manager/manager-customer";
    }
    
    // 신고 목록 조히
    @GetMapping("/dashboard/report")
    public String reportList(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) String reportType,
        @RequestParam(required = false) ReportStatus status,
        @RequestParam(required = false) ReportTargetEnums targetType,
        Model model
    ) {

        managerReportService.syncResolvedReports();

        List<ReportManagerDTO> reportList =
            managerReportService.getReportList(query, reportType, status, targetType);

        model.addAttribute("reportList", reportList);
        model.addAttribute("reportTypes", List.of(
            "스팸 / 광고", "욕설 / 비방 / 혐오", "음란 / 선정적 내용", "개인정보 노출",
            "불법 정보", "폭력적 / 잔혹한 내용", "기타 커뮤니티 규칙 위반"
        ));

        return "manager/manager-report";
    }


    
    // 신고 처리 - 삭제
    @PostMapping("/dashboard/report/resolve")
    @ResponseBody
    public void resolveReports(@RequestBody Map<String, Object> body) {

        List<Long> reportIds =
            ((List<?>) body.get("reportIds"))
                .stream()
                .map(id -> Long.valueOf(id.toString()))
                .toList();

        ReportStatus status = ReportStatus.valueOf(
            body.get("status").toString()
        );

        managerReportService.updateReportStatuses(reportIds, status);
    }
    
    // 신고 반려
    @PostMapping("/dashboard/report/reject")
    @ResponseBody
    public void rejectReports(@RequestBody Map<String, Object> body) {

        List<Long> reportIds =
            ((List<?>) body.get("reportIds"))
                .stream()
                .map(id -> Long.valueOf(id.toString()))
                .toList();

        managerReportService.updateReportStatuses(
            reportIds,
            ReportStatus.REJECTED
        );
    }

    
    // 결제 관리
    @GetMapping("/dashboard/pay")
    public String adminPay(Model model,
    		@RequestParam(value="query", required=false) String query,
    		@RequestParam(value="type", required=false) String type,
    		@RequestParam(value="cp", required=false, defaultValue ="1") int cp,
    		@SessionAttribute(value = "loginMember", required = false) MemberLoginResponseDTO loginMember) {
        
    	Map<String, Object> paramMap = new HashMap<>();
    	paramMap.put("query", query);
    	System.out.println(query);
    	paramMap.put("type", type); 
    	
    	
    	PageHelper.startPage(cp, 10);
    	PageInfo<PayDTO> pageInfo = payService.selectAllBeansHistory(paramMap, cp);
    	model.addAttribute("payList", pageInfo.getList()); // 실제 목록
        model.addAttribute("pagination", pageInfo);      // 페이지 정보 전체
        
        return "manager/manager-pay";
    }
    
    
    // 환전 ok
    @PostMapping("/pay/approve")
    @ResponseBody
    public int approveExchange(@RequestBody PayDTO data) {
        System.out.println("전달받은 번호 : " + data.getExchangeNo()); 
        return payService.okExchange(data.getExchangeNo());
    }
    
  
    
}
