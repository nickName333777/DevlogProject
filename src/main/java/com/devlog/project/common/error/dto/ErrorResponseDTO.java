package com.devlog.project.common.error.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErrorResponseDTO {

    private String code;       // 에러 코드
    private String message;    // 사용자 메시지
    private String time; // LocalDateTime → String으로 변경
    
    // 생성자
    public ErrorResponseDTO(String code, String message) {
        this.code = code;
        this.message = message;
        this.time = LocalDateTime.now().toString();  // String으로 변환
    }   
    
}