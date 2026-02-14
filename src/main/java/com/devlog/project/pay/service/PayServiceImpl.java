package com.devlog.project.pay.service;

import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.devlog.project.board.blog.mapper.ReplyMapper;
import com.devlog.project.pay.dto.PayDTO;
import com.devlog.project.pay.mapper.PayMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service
public class PayServiceImpl implements PayService {

	
	@Autowired
	private PayMapper paymapper;
	
	@Autowired
	private ReplyMapper replyMapper;
	


	@Override
	public PayDTO selectMyBeans(Long memberNo) {
		return paymapper.selectMyBeans(memberNo);
	}



	@Override
	public List<PayDTO> selectBeansHistory(Long memberNo) {
		return paymapper.selectBeansHistory(memberNo);
	}


	@Transactional(rollbackFor = Exception.class)
	@Override
	public int insertPayment(PayDTO payment) {
		// 결제 정보 저장
		int result = paymapper.insertPayment(payment);
		
		if(result > 0) {
			payment.setPayAmount(payment.getPrice());
			paymapper.insertHistory(payment);
			paymapper.updateMemberBeans(payment);
		}
		return result;
	}



	@Transactional(rollbackFor = Exception.class)
	@Override
	public boolean cancelPayment(PayDTO payDTO, String secretKey) {

	    // 결제 정보 조회
	    PayDTO payment = paymapper.selectPaymentByNo(payDTO.getBeansPayNo());
	    if (payment == null) {
	        throw new RuntimeException("결제 정보가 존재하지 않습니다.");
	    }

	    // 이미 사용된 결제인지 확인
	    if (payment.getUsedAmount() != null && payment.getUsedAmount() > 0) {
	        throw new RuntimeException("이미 사용된 결제는 취소할 수 없습니다.");
	    }

	    // PortOne 결제 취소 요청
	    RestTemplate restTemplate = new RestTemplate();

	    HttpHeaders headers = new HttpHeaders();
	    headers.add("Authorization", "PortOne " + secretKey);
	    headers.setContentType(MediaType.APPLICATION_JSON);


	    Map<String, Object> body = new HashMap<>();
	    body.put("reason", "고객 요청에 의한 결제 취소");

	    HttpEntity<Map<String, Object>> entity =
	            new HttpEntity<>(body, headers);

	    String url = "https://api.portone.io/payments/"
	            + payment.getPaymentId() + "/cancel";

	    try {
	    	ResponseEntity<Map> response =
	    	        restTemplate.postForEntity(url, entity, Map.class);

	        // 성공 시 DB 상태만 변경
	        if (response.getStatusCode().is2xxSuccessful()) {
	            paymapper.updatePayStatusCancel(payment.getBeansPayNo());
	         
	            // DTO의 금액을 음수로 전환 (잔액 차감 및 히스토리 기록용)
	            payment.setPrice(-payment.getPrice()); 
	            
	            payment.setPayAmount(payment.getPrice());

	            // 회원 테이블의 보유 콩 잔액 차감
	            int updateResult = paymapper.updateMemberBeans(payment);

	            // 히스토리 테이블에 '취소' 내역 추가
	            int historyResult = paymapper.insertHistory(payment);

	            return updateResult > 0 && historyResult > 0;
	        }

	    } catch (Exception e) {
	        throw new RuntimeException("포트원 결제 취소 실패", e);
	        
	    }

	    return false;
	}


	// 환전
	@Transactional(rollbackFor = Exception.class)
	@Override
	public int insertExchange(PayDTO pay) {

	    // 현재 보유 콩 확인 
	    PayDTO myBeans = paymapper.selectMyBeans(pay.getMemberNo());
	    int currentBeans = myBeans.getBeansAmount(); 
	    System.out.println(currentBeans);
	    
	    // 사용자가 입력한 원본 금액
	    int originAmount = pay.getRequestAmount();

	    // 검증 (최소 금액 및 잔액 확인)
	    if (originAmount < 5000) return -3;
	    if (currentBeans < originAmount) return -2;
	    if (pay.getReturnBank() == null || pay.getReturnBank().isEmpty()) return -4;

	    // 수수료 10% 제외한 금액 세팅 및 내역 삽입
	    pay.setRequestAmount((int)(originAmount * 0.9));
	    int result = paymapper.insertExchange(pay);

	    // 성공 시 원본 금액만큼 회원 잔액 차감
	    if (result > 0) {
	        // 기존 필드인 price에 차감할 액수를 음수로 세팅
	        pay.setPrice(-originAmount); 
	        
	        pay.setPayAmount(-originAmount);
	        result = paymapper.updateMemberBeans(pay);
	        if(result > 0) {
	            paymapper.insertHistory(pay); 
	        }
	        
	    }

	    return result;
	}



	// 은행 코드
	@Override
	public List<Map<String, Object>> selectBankList() {
		return paymapper.selectBankList();
	}

	
	
	// 환전 ok
	@Override
	public int okExchange(int exchangeNo) {
		return paymapper.okExchange(exchangeNo);
	}


	// 관리자용 
	public PageInfo<PayDTO> selectAllBeansHistory(Map<String, Object> paramMap, int cp) {
	    
	    // PageHelper.startPage(현재페이지, 한페이지당개수);
	    PageHelper.startPage(cp, 10); 
	    
	    // 전체 조회 쿼리 호출 
	    List<PayDTO> payList = paymapper.selectAllBeansHistory(paramMap);
	    
	    return new PageInfo<>(payList, 5); // 5는 하단에 보여줄 페이지 번호 개수
	}



	// 커피콩 거래
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int insertTrade(PayDTO trade) {
	    
	    // 판매자 식별 로직
	    Long sellerNo = 0L;
	    if ("POST".equals(trade.getContentType())) {
	        sellerNo = replyMapper.selectBoardWriter(trade.getContentId());
	    } else if ("SUBSCRIBE".equals(trade.getContentType())) {
	        sellerNo = trade.getContentId();
	    } else if ("CHATBOT".equals(trade.getContentType())) {
	        sellerNo = 1L; 
	    }
	    
	    if (sellerNo == null || sellerNo == 0L) throw new RuntimeException("판매자 정보를 찾을 수 없습니다.");
	    trade.setSellerNo(sellerNo);

	    // 잔액 확인
	    PayDTO myBeans = paymapper.selectMyBeans(trade.getBuyerNo());
	    if (myBeans == null || myBeans.getBeansAmount() < trade.getPrice()) {
	        throw new RuntimeException("커피콩 잔액이 부족합니다.");
	    }

	    // 구매자 차감
	    PayDTO deduction = new PayDTO();
	    deduction.setMemberNo(trade.getBuyerNo());
	    deduction.setPrice(-trade.getPrice());
	    if(paymapper.updateMemberBeans(deduction) == 0) throw new RuntimeException("차감 실패");

	    // 판매자 지급 (본인 제외)
	    if (!trade.getBuyerNo().equals(sellerNo)) {
	        PayDTO addition = new PayDTO();
	        addition.setMemberNo(sellerNo);
	        addition.setPrice(trade.getPrice());
	        if(paymapper.updateMemberBeans(addition) == 0) throw new RuntimeException("지급 실패");
	    }

	    // 거래 저장
	    int result = paymapper.insertBeansTrade(trade);
	    if(result == 0) throw new RuntimeException("거래 저장 실패");

	    // 구매자 내역
	    PayDTO buyerHistory = new PayDTO();
	    buyerHistory.setMemberNo(trade.getBuyerNo()); 
	    buyerHistory.setPayAmount(-trade.getPrice()); 
	    buyerHistory.setTradeNo(trade.getTradeNo()); 
	    
	    System.out.println("거래번호 체크: " + trade.getTradeNo());
	    
	    paymapper.insertHistory(buyerHistory); 

	    // 판매자 내역
	    if (!trade.getBuyerNo().equals(sellerNo)) {
	        PayDTO sellerHistory = new PayDTO();
	        sellerHistory.setMemberNo(sellerNo);
	        sellerHistory.setPayAmount(trade.getPrice()); 
	        sellerHistory.setTradeNo(trade.getTradeNo());
	        paymapper.insertHistory(sellerHistory);
	    }
	    return result;
	}


	// 구독
	@Transactional(rollbackFor = Exception.class)
	@Override
	public int insertSubscription(PayDTO trade) {
	    
	    // 1. 판매자 번호 체크 로직 보완
	    // null 체크와 더불어 0인지 확인하는 방법을 더 확실하게 바꿉니다.
	    if (trade.getSellerNo() == null || trade.getSellerNo() == 0) {
	        
	        // contentId도 체크 (Long 타입이므로 0과 비교)
	        if (trade.getContentId() != null && trade.getContentId() != 0) {
	            trade.setSellerNo(trade.getContentId());
	            
	        } else if (trade.getCreatorId() != 0) { // DTO에 있는 int creatorId 체크
	            trade.setSellerNo((long)trade.getCreatorId());
	            
	        } else {
	            throw new RuntimeException("지급 대상(판매자) 번호가 없습니다.");
	        }
	    }
		
	    // 잔액 확인
	    PayDTO myBeans = paymapper.selectMyBeans(trade.getBuyerNo());
	    if (myBeans == null || myBeans.getBeansAmount() < trade.getPrice()) {
	        return -2; // 잔액 부족 코드
	    }

	    // 구매자 커피콩 차감
	    PayDTO deduction = new PayDTO();
	    deduction.setMemberNo(trade.getBuyerNo());
	    deduction.setPrice(-trade.getPrice());
	    if(paymapper.updateMemberBeans(deduction) == 0) throw new RuntimeException("차감 실패");

	    // 판매자 커피콩 지급
	    PayDTO addition = new PayDTO();
	    addition.setMemberNo(trade.getSellerNo());
	    addition.setPrice(trade.getPrice());
	    if(paymapper.updateMemberBeans(addition) == 0) throw new RuntimeException("지급 실패");

	    
	    
	    trade.setBuyerNo(trade.getBuyerNo());
	    trade.setSellerNo(trade.getSellerNo());
	    trade.setContentType("SUBSCRIBE");
	    paymapper.insertBeansTrade(trade);
	    
	    // 구독 전용 테이블 저장 
	    int subResult = paymapper.insertSubscriptionRecord(trade);
	    if(subResult == 0) throw new RuntimeException("구독 정보 기록 실패");

	    // 내역 저장 
	    // 구매자 내역
	    trade.setMemberNo(trade.getBuyerNo());
	    trade.setPayAmount(-trade.getPrice());
	    paymapper.insertHistory(trade);

	    // 판매자 내역
	    PayDTO sellerHistory = new PayDTO(); 
	    sellerHistory.setMemberNo(trade.getSellerNo()); // 판매자 번호
	    sellerHistory.setPayAmount(trade.getPrice());    // 판매자는 지급 (+) - 마이너스 없는거 확인!
	    sellerHistory.setTradeNo(trade.getTradeNo());
	    paymapper.insertHistory(sellerHistory);
	    return 1; // 성공
	}
	
	
	
	
	
	// 매일 새벽 1시 실행
	    @Scheduled(cron = "0 0 1 * * *")
	    public void autoSubscriptionRenewal() {
	        System.out.println("=== 구독 자동 갱신 프로세스 시작 ===");

	        // 오늘이 딱 구독 시작 30일째인 대상자들 조회
	        List<PayDTO> targets = paymapper.selectExpiringSubscriptions();

	        if (targets == null || targets.isEmpty()) {
	            System.out.println("오늘 갱신 대상자가 없습니다.");
	            return;
	        }

	        for (PayDTO trade : targets) {
	            try {
	                int result = this.insertSubscription(trade);

	                if (result == 1) {
	                    System.out.println("사용자 [" + trade.getBuyerNo() + "] 구독 갱신 성공");
	                } else {
	                    System.out.println("사용자 [" + trade.getBuyerNo() + "] 잔액 부족으로 갱신 실패");
	                }
	            } catch (Exception e) {
	                System.err.println("사용자 [" + trade.getBuyerNo() + "] 갱신 중 오류: " + e.getMessage());
	            }
	        }
	        System.out.println("=== 구독 자동 갱신 프로세스 종료 ===");
	    }
	
}

