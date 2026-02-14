package com.devlog.project.board.blog.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BlogDTO {
	
	// BOARD 테이블 공통 컬럼
    private Long boardNo;           // PK
    private int boardCode;          // 게시판 코드 (블로그: 1)
    private Long memberNo;          // FK: 작성자 회원 번호
    private String boardTitle;      // 제목
    private String boardContent;    // 내용
    private String bCreateDate;     // 작성일 (String으로 변환하여 사용)
    private int boardCount;         // 조회수
    private String boardDelFl;      // 삭제 여부 ('N', 'Y')
    
    // BLOG 테이블 전용 컬럼
    private String isPaid;          // 유료 여부 ('Y', 'N')
    private int price;              // 가격
    private String tempFl;          // 임시저장 여부 ('Y', 'N')
    
    // MEMBER 테이블 조인
    private String memberNickname;  // 작성자 닉네임 (화면에 표시될 이름)
    private String memberEmail;     // 작성자 아이디 (이메일)
    private String profileImg;      // 작성자 프로필 이미지
    
    // 썸네일
    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;	// 썸네일 (프로필 or 본문이미지)
    
    private int commentCount; 		// 댓글 수
    private int likeCount;			// 좋아요 수
    
    // 스크랩 목록 구분용 ('1':게시글(블로그/뉴스), '2':채용공고)
    private String type;
    
    private List<String> tagList;   // 태그 리스트 (저장/조회용)
    
    private String tagsStr;
    
    // 화면 깜빡임 방지용 요약 메서드 ${blog.summary}로 사용하면 자동 호출됨
    public String getSummary() {
        if (this.boardContent == null) return "";

        // 1. HTML 태그 제거 (<div>, <p>, <br> 등을 공백으로 변환)
        String text = this.boardContent.replaceAll("<[^>]*>", " ");
        
        // 2. 마크다운 특수문자 제거 (#, *, _, ~, `, [ ] 등)
        text = text.replaceAll("[#*`_~\\[\\]]", "");
        
        // 3. 이미지 및 링크 문법 제거 (![alt](url), (url))
        text = text.replaceAll("!\\[.*?\\]\\(.*?\\)", ""); // 이미지 태그 제거
        text = text.replaceAll("\\(.*?\\)", "");           // 링크 주소 제거

        // 4. 공백 정리 (엔터, 탭, 연속된 공백을 한 칸으로)
        text = text.replaceAll("\\s+", " ").trim();

        // 5. 길이 제한 (30자)
        // 화면에 보여질 길이를 조절하려면 숫자 '30'을 원하는 대로 변경하세요.
        if (text.length() > 60) {
            return text.substring(0, 60) + "...";
        }
        return text;
    }

}
