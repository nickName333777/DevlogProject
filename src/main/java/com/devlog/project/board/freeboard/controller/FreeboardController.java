package com.devlog.project.board.freeboard.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.devlog.project.board.freeboard.model.dto.Freeboard;
import com.devlog.project.board.freeboard.model.service.FreeboardService;
import com.devlog.project.member.model.dto.MemberLoginResponseDTO;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/board")  
@RequiredArgsConstructor
public class FreeboardController {

	private final FreeboardService freeboardService;
	
	// 게시글 목록조회
	@GetMapping("/freeboard")   
	public String selectFreeboardList(
			@RequestParam(value="cp", required=false, defaultValue ="1") int cp 
			, Model model 
			, @RequestParam Map<String, Object> paramMap 
			, HttpSession session //// session에 담긴 "loginMember" 꺼내오기용
			) {  
		int boardCode = 3; // boardCode = 3:  freeboard in BoardType 테이블
		
		log.info("[ FreeboardController ] boardCode: {}, cp: {}", boardCode, cp); 			
		
		// 게시글 목록 조회 서비스 호출
		Map<String, Object> map = freeboardService.selectFreeboardList(boardCode, cp);		
		
		//log.info("Freeboard DB 목록조회, map.pagination, map.freeboardList : {}", map);
		
		// 조회 결과를 request scope에 세팅 후 forward
		model.addAttribute("map", map); //model : spring에서 사용하는 데이터 전달 객체 => js에서 이걸 받아 사용 (@PathVariable에 담긴 boardCode와 cp도 담겨져 넘어감)
				
		return "board/freeboard/freeboardList"; 
	}	
	
	
	// 게시글 상세조회
	@GetMapping("/freeboard/{boardNo}")
	public String selectFreeboardDetail( 
			//@PathVariable("boardNo") int boardNo
			@PathVariable("boardNo") Long boardNo
			, Model model 
			, RedirectAttributes ra 
			, @SessionAttribute(value = "loginMember", required=false) MemberLoginResponseDTO loginMember
			, HttpServletRequest req
			, HttpServletResponse resp
			) throws ParseException {
		
		int boardCode = 3; //  boardCode = 3 for freeboard
		Map<String, Object> map = new HashMap<String, Object>(); 
		log.info("Freeboard detail boardCode: {}", boardCode); 
		log.info("Freeboard detail boardNo: {}", boardNo);
		
		map.put("boardCode", boardCode);
		map.put("boardNo", boardNo);
			
		// 게시글 상세 조회 서비스 호출
		Freeboard freeboard = freeboardService.selectFreeboardDetail(map); 
		log.info("Freeboard detail (boardNo= {}): {}", boardNo, freeboard);
		model.addAttribute("freeboard", freeboard); 
		
		
		String path = null;
		if(freeboard != null) {  // boardNo의 게시글 존재하는 경우

			// 1) 현재 로그인한 상태인 경우
			// 로그인한 회원이 해당 게시글에 좋아요를 눌렀는지 확인
			if (loginMember != null) { // boardNo, memberNo
				// 회원 번호를 기존에 만들어둔 map에 추가
				map.put("memberNo", loginMember.getMemberNo()); // 담아 가서 필요없으면 않쓰면 됨
				
				// 좋아요 여부 확인 서비스 호출
				int result = freeboardService.boardLikeCheck(map);
				
				// 좋아요를 누른 적이 있을 경우
				if(result > 0) { // 화면에 하트 보여주기위해 누른적 있는지 알려주기 위해 Model 전달객체 사용
					model.addAttribute("likeCheck", "yes");
				}
			}

			// 2) 쿠키를 이용한 조회수 증가 
			//
			// 1) 비회원 또는 로그인한 회원의 글이 아닌 경우
			if(loginMember == null || 
				loginMember.getMemberNo() != freeboard.getMemberNo()) {
				
				// 2) 쿠키 얻어오기
				Cookie c = null;
				
				// 요청에 담겨있는 모든 쿠키 얻어오기
				Cookie[] cookies = req.getCookies();
				
				// 쿠키가 존재하는 경우
				if(cookies != null) {
					
					// 쿠키 중 "readBoardNo" 이름을 가진 쿠키를 찾아서 c에 대입
					for (Cookie cookie : cookies) {
						if(cookie.getName().equals("readBoardNo")) {
							c = cookie; // 기존에 쿠키가 존재 하면 그거 그냥 가져다 쓴다.
							break;
						}
					}
				} 
				
				// 3) 기존에 쿠키가 없거나
				//    존재는 하지만 현재 게시글 번호가 쿠기에 저장되지 않은 경우
				//    (오늘 해당 게시글을 본적이 없는 경우)
				
				int result = 0; // 결과값 저장 변수
				
				if (c==null) {
					// 쿠키 존재 X -> 하나 새로 생성
					c = new Cookie("readBoardNo", "|" + boardNo + "|");   
					
					// 조회수 증가 서비스 호출
					result = freeboardService.updateBoardCount(boardNo);
					
				} else { // 쿠키가 존재 O : 위에서 찾아 c에 담아 놓은 쿠키
					// 현재 게시글 번호가 있는지 확인
					// cookie.getValue() : 쿠키에 저장된 모든 값을 읽어와서 String으로 반환
					
					// String.indexOf("문자열")
					// -> 찾는 문자열이 몇번 째 인덱스에 존재하는지 반환
					//    단, 없는 경우 -1 반환
					
					if(c.getValue().indexOf("|" + boardNo + "|") == -1) {
						// 쿠키에 현재 게시글 번호가 없다면					
						// 기존 쿠키 값에 게시글 번호를 추가해서 다시 세팅
						c.setValue(c.getValue() + "|" + boardNo + "|");
						
						// 조회수 증가 서비스 호출
						result = freeboardService.updateBoardCount(boardNo);
					}
				}
				
				// 4) 조회수 증가 성공 시 ( readCount 업데이트 필요)
				//    쿠키가 적용되는 경로, 수명(당일 23시 59분 59초) 지정
				if (result != 0 ) {
					// 조회된 board의 조회수와 DB의 조회수 동기화 
					freeboard.setBoardCount(freeboard.getBoardCount() + 1);
					
					// [ 쿠키 적용 경로 설정 ]
					c.setPath("/"); // "/" 이하 경로 요청 시 쿠키 서버로 전달 (모든 요청할 때 마다 쿠키가 담긴다)
					
					// [ 쿠키 수명 지정 (Date보다 Calendar가 개선된 시간관련 클래스) ]
					Calendar cal = Calendar.getInstance();  // 클래스명.메소드명 -> static 메소드
															
					cal.add(Calendar.DATE, 1); // 1일
					
					// 날짜 표기법 변경 객체
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					
					// java.util.Date
					Date current = new Date(); // 현재 시간
					
					Date temp = new Date(cal.getTimeInMillis()); 
					
					Date tmr = sdf.parse(sdf.format(temp)); // temp를 "yyyy-MM-dd" 형식
					 
					// 내일 0시 0분 0초 - 현재 시간 -> 쿠키 수명
					long diff = (tmr.getTime() - current.getTime()) / 1000; // .getTime() 반환형이 long 타입
					
					c.setMaxAge((int)diff); //.setMaxAge 파라미터는 int이므로 강제 형변환
					
					// [ 쿠키를 resp에 담아서 보낸다 ]
					resp.addCookie(c); // 응답 객체를 이용하여 클라이언트에게 전달
					
				}
				
			}
			
			//---------------------------------------------------------
			path = "board/freeboard/freeboardDetail";
			
			
		} else { // boardNo의 게시글 없는 경우
			path = "redirect:/board/freeboard"; // ==> 게시글 목록조회 로  
			ra.addFlashAttribute("message",  "해당 게시글이 존재하지 않습니다." ); 
		}
		
		return path;
	}	
	
	
	
	// 좋아요 처리
	@PostMapping("/freeboard/like")
	@ResponseBody // 반환되는 값이 비동기 요청한 곳으로 돌아가게 함; AJAX 처리
	public int like(@RequestBody Map<String, Integer> paramMap) { // Map<k, v> Object대신 Integer로 받으면 down-casting해줄 필요 없음
		System.out.println(paramMap); // {memberNo=1, boardNo=7, check=0}
		
		return freeboardService.like(paramMap);
	}		
	
	
}
