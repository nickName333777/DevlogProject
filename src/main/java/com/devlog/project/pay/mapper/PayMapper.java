package com.devlog.project.pay.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.devlog.project.pay.dto.PayDTO;

@Mapper
public interface PayMapper {

	
	// 내 커피콩 조회
	public PayDTO selectMyBeans(Long memberNo);

	// 내 커피콩 내역 조회
	public List<PayDTO> selectBeansHistory(Long memberNo);

	// 결제 완료
	public int insertPayment(PayDTO payment);

	// 결제 내역
	public int insertHistory(PayDTO payment);

	// 내 커피콩 내역 업데이트
	public int updateMemberBeans(PayDTO payment);

	// 결제 내역 조회
	public PayDTO selectPaymentByNo(int beansPayNo);

	// 취소
	public int updatePayStatusCancel(int beansPayNo);

	// 현재 커피콩
	public int checkCurrentBeans(Long memberNo);

	// 환전
	public int insertExchange(PayDTO pay);

	// 은행 코드
	public List<Map<String, Object>> selectBankList();
	
	// 환전 ok
	int okExchange(int exchangeNo);

	// 관리자용 조회
	public List<PayDTO> selectAllBeansHistory(Map<String, Object> paramMap);

	// 거래
	public int insertBeansTrade(PayDTO trade);

	// 구독
	public int insertSubscriptionRecord(PayDTO trade);

	// 30일마다 조회
	public List<PayDTO> selectExpiringSubscriptions();


}
