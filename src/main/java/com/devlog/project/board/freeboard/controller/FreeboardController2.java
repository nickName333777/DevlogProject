package com.devlog.project.board.freeboard.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.devlog.project.board.freeboard.model.dto.Freeboard;
import com.devlog.project.board.freeboard.model.service.FreeboardService;
import com.devlog.project.board.freeboard.model.service.FreeboardService2;
import com.devlog.project.member.model.dto.MemberLoginResponseDTO;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/board2")
@RequiredArgsConstructor
public class FreeboardController2 {
	
	private final FreeboardService freeboardService;
	
	private final FreeboardService2 service;
	
	// [A] 게시글 작성 화면 전환(GetMapping())
	@GetMapping("/freeboard/insert") 
	public String freeboardInsert( //@PathVariable("boardCode") int boardCode
			 HttpSession session // 
			) {
		
		
		int boardCode = 3; // for freeboard
		Map<String, Object> map = new HashMap<String, Object>(); 
		log.info("Freeboard Insert boardCode: {}", boardCode); 	
		map.put("boardCode", boardCode);	
		
		return "board/freeboard/freeboardWrite";  // forward하겠다. (리다이렉트는 "redirect:board/freeboard/freeboardWrite"로 해야함)
	}
		
	
	// [B] 게시글 작성 (작성완료 버튼 클릭시)(PostMapping())
	@PostMapping("/freeboard/insert")
	@ResponseBody
	public Map<String, Object> boardInsert(
			Freeboard freeboard // 커맨드 객체
			, @RequestParam(value="images", required=false) List<MultipartFile> images 
			, HttpSession session // 파일 저장 경로
			, RedirectAttributes ra
			) throws IllegalStateException, IOException {

		log.info("[ FreeboardController ] freeboard =  {}", freeboard); 
		//log.info("[ FreeboardController ] images.size() =  {}", images.size()); // images.size() null 방어필요
		
		MemberLoginResponseDTO loginMember = (MemberLoginResponseDTO) session.getAttribute("loginMember");
		log.info("loginMember from session.getAttribute(): {}", loginMember); 
		
		// 1. 로그인한 회원번호와 boardCode를 board에 세팅
		int boardCode = 3; // for freeboard
		freeboard.setBoardCode(boardCode);
		freeboard.setMemberNo(loginMember.getMemberNo());
		

		// 2. 게시글 삽입 서비스 호출 후 게시글 번호 반환 받기
		Long boardNo = service.freeboardInsert(freeboard, images);

		
		// 4. 게시글 삽입 서비스 호출 결과 후처리
		String message = null;
		String redirectUrl = null;
		Map<String, Object> result = new HashMap<>();
		
		if (boardNo > 0) {
			redirectUrl = "/board/freeboard/" + boardNo; // "redirect:" 가 prefix로 있어야 redirect 된다.
			message = "게시글이 등록 되었습니다.";
			
	        // 저장
	        result.put("success", true);
	        result.put("message", message);
	        result.put("redirectUrl", redirectUrl);			
		} else {
			redirectUrl = "/board2/freeboard/insert";
			message = "게시글 등록 실패. 잠시후 다시 시도해 주세요.";
			
	        // 저장
	        result.put("success", false);
	        result.put("message", message);
	        result.put("redirectUrl", redirectUrl);					
		}
		
		ra.addFlashAttribute("message", message); // alert메시지 출력

		return result; //결과데이터 JSON형식 
	}	
	
	
	// [C] 게시글 수정 화면 전환(GetMapping())
	@GetMapping("/freeboard/{boardNo}/update") 
	public String freeboardUpdate(
			@PathVariable("boardNo") int boardNo 
			, Model model ) {
		
		int boardCode = 3; // for freeboard
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("boardCode", boardCode);
		map.put("boardNo", boardNo); 
		
		// 게시글 상세 조회 서비스 호출 
		Freeboard freeboard = freeboardService.selectFreeboardDetail(map); 										 
		log.info("[ FreeboardController: Update-GET ] map for .selecFreeboardDetail(map):{}", map);
		log.info("[ FreeboardController: Update-GET ] freeboard in freeboardUpdate-GET:{}", freeboard); 
		model.addAttribute("freeboard", freeboard); 
		
		return "board/freeboard/freeboardUpdate"; 
	}	
	
	// [D] 게시글 수정중 기존이미지 삭제, AJAX 
	@DeleteMapping("/freeboard/deleteImage/{imgNo}")
	@ResponseBody
	public Map<String, Object> deleteImage(@PathVariable("imgNo") Long imgNo) {
	    boolean success = service.deleteFreeboardImage(imgNo);
	    return Map.of(
	        "success", success
	    );
	}
	
	
	
	// [D] 게시글 수정 (수정완료 버튼 클릭시)(PostMapping())
	@PostMapping("/freeboard/{boardNo}/update")  
	@ResponseBody
	public  Map<String, Object> boardUpdate(
			@PathVariable("boardNo") Long boardNo,
			Freeboard freeboard, 
			@RequestParam(value="cp", required=false, defaultValue="1") String cp, 
			@RequestParam(value="deleteList", required=false) String deleteList, 
			@RequestParam(value="images", required=false) List<MultipartFile> images, 
			@RequestParam(value="existingImgNos", required=false) String existingImgNos, 
			RedirectAttributes ra, 
			HttpSession session 
			) throws IllegalStateException, IOException {

		log.info("[ FreeboardController: Update-POST ] cp for 목록으로 버튼 선택시 :{}", cp);
		log.info("[ FreeboardController: Update-POST ] deleteList in freeboardUpdate-POST:{}", deleteList); 		
		//log.info("[ FreeboardController: Update-POST ] images.size() in freeboardUpdate-POST:{}", images.size()); 		
		if (images != null && images.size() > 0) {	// null-방어
			log.info("[ FreeboardController: Update-POST ] images.size() in freeboardUpdate-POST:{} for new images attached", images.size()); 					
		} else {
			log.info("[ FreeboardController: Update-POST ] images.size() in freeboardUpdate-POST == 0; no-new image attached"); 								
		}		
		log.info("[ FreeboardController: Update-POST ] existingImgNos in freeboardUpdate-POST:{}", existingImgNos); 		
			
		
		// 1. boardNo를 커맨드 객체에 세팅
		freeboard.setBoardNo(boardNo);
		
		// 2. 게시글 수정 서비스 호출 (제목/내용수정:BOARD + 이미지수정:BOARD_IMG)
		int rowCount = service.freeboardUpdate(freeboard, images, existingImgNos);
		
		// 3. 결과에 따라 message, path 설정
		String message = null;
		String redirectUrl = null;
		Map<String, Object> result = new HashMap<>();
		
		if(rowCount > 0) { // 게시글 수정 성공 시
			message = "게시글이 수정되었습니다";
			redirectUrl = "/board/freeboard/" + boardNo + "?cp=" + cp; //  boardCode, boardNo -> 
			
	        result.put("success", true);
	        result.put("message", message);
	        result.put("redirectUrl", redirectUrl);					
			
		} else { // 실패 시
			message = "게시글 수정 실패. 잠시후 다시 시도해 주세요.";
			redirectUrl = "/board2/freeboard/" + boardNo + "/update"+ "?cp=" + cp; 
			
	        result.put("success", false);
	        result.put("message", message);
	        result.put("redirectUrl", redirectUrl);					
		}
		
		ra.addFlashAttribute("message", message);
		
		return result;
	}

	// [E] 게시글 삭제 (GetMapping())
	@GetMapping("/freeboard/{boardNo}/delete") 
	public String boardDelete(
			@PathVariable("boardNo") Long boardNo 
			, @RequestParam(value="cp", required=false, defaultValue="1") String cp
			, RedirectAttributes ra 
			, @RequestHeader("referer") String referer // 이전 요청 주소
			) {
		
		log.info("[ FreeboardController: Delete-GET ] cp :{}", cp);
		log.info("[ FreeboardController: Delete-GET ] boardNo :{}", boardNo); 		
		log.info("[ FreeboardController: Delete-GET ] referer, 이전 요청 주소 :{}", referer); 		
			
		// 1. 게시글 삭제 서비스 호출
		int result = service.freeboardDelete(boardNo);
		
		// 2. 결과에 따라 message, path 설정
		String message = null;
		String path = "redirect:";
		
		if (result > 0) {
			
			message = "게시글이 삭제되었습니다.";
			path += "/board/" + "freeboard";
		} else {
			message = "게시글 삭제 실패. 잠시 후 다시 시도해 주세요.";
			path += referer; // 마찬가지
				
		}
		
		ra.addFlashAttribute("message", message);
		
		return path;
		
	}
	
	// [F] 게시글 삭제 (PostMapping())
	// /board2/freeboard/20/update/deletePOST
	@PostMapping("/freeboard/{boardNo}/update/deletePOST") // "/board2/freeboard/15" + "/deletePOST"
	@ResponseBody
	public Map<String, Object> deletePOST(@RequestBody Map<String, Object> data) {
	    // data.get("oldBoardNo")
		//Long oldBoardNo = (Long) data.get("oldBoardNo");
		//Long oldBoardNo = (Long) data.get("oldBoardNo");
		Long oldBoardNo =  ((Number) data.get("oldBoardNo")).longValue(); 
		//Long oldBoardNo = Long.valueOf( data.get("oldBoardNo"));
		// data.get("insertedBoardNo")
		//Long insertedBoardNo = (Long) data.get("insertedBoardNo");
		Long insertedBoardNo = ((Number) data.get("insertedBoardNo")).longValue(); 
	    // data.get("existingImgNos")
		
		log.info("받은 데이터 : {}", data );
		
		// 1. 게시글 삭제 서비스 호출 (해당 게시글 boardDelFl을 'Y'로 세팅)
		int res = service.setBoardNoDelFl(oldBoardNo);
		
		log.info("처리결과 res : {}", data );
		// 2. data.get("existingImgNos"): oldBoardNo 게시글의 이미지 작업은 future work

		
		// 3. 결과에 따라 message, path 설정
		String message = null;
		String redirectUrl = null;
		Map<String, Object> result = new HashMap<>();		
		if(res > 0) { // 게시글 삭제 성공 시
			message = "게시글이 삭제되었습니다";
			redirectUrl = "/board/freeboard/" + insertedBoardNo;  
			
	        result.put("success", true);
	        result.put("message", message);
	        result.put("redirectUrl", redirectUrl);					
			
		} else { // 실패 시
			message = "게시글 수정 실패. 잠시후 다시 시도해 주세요.";
			redirectUrl = "/board2/freeboard/" + oldBoardNo + "/update"; 
			
	        result.put("success", false);
	        result.put("message", message);
	        result.put("redirectUrl", redirectUrl);					
		}		
		
		return result;
	}
	
}
