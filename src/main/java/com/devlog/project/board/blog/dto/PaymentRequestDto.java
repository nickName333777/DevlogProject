package com.devlog.project.board.blog.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PaymentRequestDto {
	private Long postId; // 구매할 게시글 번호
	private int amount;  // 결제 금액
	
}
