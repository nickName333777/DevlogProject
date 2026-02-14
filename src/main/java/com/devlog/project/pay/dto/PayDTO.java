package com.devlog.project.pay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PayDTO {
	
	// 커피콩 결제
	@JsonProperty("beansPayNo")
	private int beansPayNo;
	
	private Long memberNo;
	
	@JsonProperty("paymentId")
	private String paymentId;
	
	@JsonProperty("payMethod")
	private String payMethod;
	private int price;
	private String payDate;
	private Integer usedAmount;
	private String payStatus;
	
	private int beansAmount;
	
	// 커피콩 내역
	private int beansHistory;
	private int payAmount;
	
	@JsonProperty("displayPrice")
	private int displayPrice;
	
	
	// 커피콩 거래
	private int tradeNo;
	
	@JsonProperty("buyerNo")
	private Long buyerNo; //구매자
	
	@JsonProperty("sellerNo")
	private Long sellerNo; //구매당한사람
	private String contentType; //컨텐츠, 구독, 챗봇
	private String tradeAt; // 거래 일시
	
	@JsonProperty("contentId")
	private Long contentId; // 대상 ID(게시글번호, 회원번호, 챗봇번호)
	
	// 사용 타입
	private String tradeType;
	
	// 환전     
	@JsonProperty("exchangeNo")
	private int exchangeNo;  
	
	@JsonProperty("returnBank")
    private String returnBank;      // 은행 코드
    
    @JsonProperty("requestAmount")
    private int requestAmount;      // 환전 신청한 금액 (10% 차감)
    
    @JsonProperty("exchangeHolder")
    private String exchangeHolder;  // 예금주
    
    @JsonProperty("exchangeAccount")
    private String exchangeAccount; // 계좌번호
    private String exchangeReqDate; // 신청일시
    private String exchangeOkDate;  // 처리일시
    private String status; // "요청", "완료"
    private String memberNickname;
    private String returnBankName;
    
    
    // 구독
    @JsonProperty("subscribeNo")
    private int subscribeNo;
    
    @JsonProperty("subscriberId")
    private int subscriberId;
    private int creatorId;
    private int startAt;
	
	
	
	
	
}
