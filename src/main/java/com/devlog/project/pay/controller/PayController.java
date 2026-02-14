package com.devlog.project.pay.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.devlog.project.member.model.dto.MemberLoginResponseDTO;
import com.devlog.project.pay.dto.PayDTO;
import com.devlog.project.pay.service.PayService;

import jakarta.servlet.http.HttpSession;

@Controller
@PropertySource("classpath:config.properties")
public class PayController {

	@Autowired PayService payService;
	
	@Value("${storeId}")
	private String storeId;
	
	@Value("${channelKey}")
	private String channelKey;
	
	@Value("${secretKey}")
	private String secretKey;
	
	
	// 화면 전환
	@GetMapping("/coffeebeans") 
	public String coffeebeans(Model model,
			@SessionAttribute(value = "loginMember", required = false) MemberLoginResponseDTO loginMember) {
		if (loginMember == null) return "redirect:/member/login"; 
		Long memberNo = loginMember.getMemberNo();
		
		// 현재 내 커피콩 잔액
		PayDTO myBeans = payService.selectMyBeans(memberNo);
		
		// 내 결제 내역 
		List<PayDTO> historyList = payService.selectBeansHistory(memberNo);
		
		model.addAttribute("myBeans", myBeans);
		model.addAttribute("historyList", historyList);
		
		model.addAttribute("channelKey", channelKey);
		model.addAttribute("storeId", storeId);

		return "/coffeebeans/mybeans";
	}
	
	// 결제 요청
	@PostMapping("/payment/complete")
	@ResponseBody
	public ResponseEntity<?> completePayment(@RequestBody PayDTO payment,
			@SessionAttribute(value = "loginMember", required = false) MemberLoginResponseDTO loginMember){
		Long memberNo = loginMember.getMemberNo();
		payment.setMemberNo(memberNo);
		
		
		System.out.println("결제 데이터" + payment);
		
		int result = payService.insertPayment(payment);
		
		return ResponseEntity.ok().body(
				java.util.Map.of("result", result));
	}
	
	
	// 결제 취소
	@PostMapping("/payment/cancel")
	@ResponseBody
	public ResponseEntity<?> cancelPayment(@RequestBody PayDTO payDTO, HttpSession session) {
		MemberLoginResponseDTO loginMember = (MemberLoginResponseDTO) session.getAttribute("loginMember");
		System.out.println("전달받은 ID: " + payDTO.getPaymentId()); // pay-c6329ad7
	    System.out.println("전달받은 번호: " + payDTO.getBeansPayNo()); // 37
	    if (loginMember == null) return ResponseEntity.status(401).body("로그인 필요");

	    try {
	        payDTO.setMemberNo(loginMember.getMemberNo());
	        
	        // Controller에 있는 secretKey를 서비스로 전달
	        boolean success = payService.cancelPayment(payDTO, secretKey); 
	        
	        return success ? ResponseEntity.ok("success") 
	                       : ResponseEntity.status(500).body("fail");
	    } catch (RuntimeException e) {
	        return ResponseEntity.badRequest().body(e.getMessage());
	    } catch (Exception e) {
	        return ResponseEntity.status(500).body("서버 오류");
	    }

	}
	
	// 환전
	@PostMapping("/payment/exchange")
	@ResponseBody
	public int insertExchange(
			@RequestBody PayDTO exchange,
			@SessionAttribute(value = "loginMember", required = false) MemberLoginResponseDTO loginMember) {
	
		if (loginMember == null) {
			return -1;
		}
		
		exchange.setMemberNo(loginMember.getMemberNo());
		
		try {
            return payService.insertExchange(exchange);
        } catch (Exception e) {
        	e.printStackTrace();
            return 0; 
        }
	
	}
	
	// 은행 코드
	@GetMapping("/payment/bankList")
	@ResponseBody
	public List<Map<String, Object>> getBankList() {
	    return payService.selectBankList();
	}
	
	
	// 커피콩 거래
	@PostMapping("/payment/trade")
	@ResponseBody
	public ResponseEntity<?> processTrade(@RequestBody Map<String, Object> request,
	        @SessionAttribute("loginMember") MemberLoginResponseDTO loginMember) {
	    
	    try {
	        // DTO 객체 생성 및 데이터 세팅
	        PayDTO trade = new PayDTO();
	        trade.setBuyerNo(loginMember.getMemberNo());
	        trade.setContentType((String) request.get("contentType"));
	        trade.setContentId(Long.parseLong(request.get("contentId").toString()));
	        trade.setPrice(Integer.parseInt(request.get("price").toString()));

	        // 통합 거래 서비스 호출 (DTO를 넘김)
	        int result = payService.insertTrade(trade);
	        
	        return ResponseEntity.ok(Map.of("result", result));
	    } catch (Exception e) {
	        e.printStackTrace(); // 에러 로그 확인용
	        return ResponseEntity.badRequest().body(e.getMessage());
	    }
	}
	
	@GetMapping("/payment/myBeans")
	@ResponseBody
	public PayDTO getMyBeans(@SessionAttribute(name="loginMember", required=false) MemberLoginResponseDTO loginMember) {
	    
	    // 1. 로그인 체크
	    if (loginMember == null) {
	        // 로그인이 안 되어 있으면 null이나 빈 객체를 반환
	        return null; 
	    }
	    
	    // 2. 콩 잔액 조회
	    PayDTO result = payService.selectMyBeans(loginMember.getMemberNo());
	    
	    // 3. 로그 찍어보기 (서버 콘솔에서 확인용)
	    System.out.println("조회된 콩: " + (result != null ? result.getBeansAmount() : "데이터 없음"));
	    
	    return result;
	}
	// 구독
	@PostMapping("/payment/subscribe")
	@ResponseBody
	public int insertSubscription(
	    @RequestBody PayDTO trade, 
	    @SessionAttribute("loginMember") MemberLoginResponseDTO loginMember) {
	    
	    // 구매자(구독자) 번호 세팅
	    trade.setBuyerNo(loginMember.getMemberNo());
	    
	    // 서비스에서 잔액 체크 -> 콩 차감 -> 구독 테이블 인서트 -> 히스토리 인서트 수행
	    return payService.insertSubscription(trade);
	}
}
	
	
	
	
	
	
	
	
	
	
	
	


