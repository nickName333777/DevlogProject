package com.devlog.project.pay.service;


import java.util.List;
import java.util.Map;

import com.devlog.project.pay.dto.PayDTO;
import com.github.pagehelper.PageInfo;

public interface PayService {

	// 내 커피콩 조회
	PayDTO selectMyBeans(Long memberNo);

	// 내 커피콩 내역 조회
	List<PayDTO> selectBeansHistory(Long memberNo);

	
	// 결제 요청
	int insertPayment(PayDTO payment);

	
	// 결제 취소
	boolean cancelPayment(PayDTO payDTO, String secretKey);

	// 환전
	int insertExchange(PayDTO exchange);

	// 은행 코드
	List<Map<String, Object>> selectBankList();

	// 환전 ok
	int okExchange(int exchangeNo);

	// 관리자용 조회
	PageInfo<PayDTO> selectAllBeansHistory(Map<String, Object> paramMap, int cp);
	
	// 커피콩 거래
	int insertTrade(PayDTO trade);

	// 구독 
	int insertSubscription(PayDTO trade);



}
