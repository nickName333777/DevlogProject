package com.devlog.project.board.freeboard.model.service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;

import com.devlog.project.board.freeboard.model.dto.Freeboard;
import com.devlog.project.board.freeboard.model.dto.PaginationFB;
import com.devlog.project.board.freeboard.model.mapper.FreeboardMapper;
import com.devlog.project.notification.NotiEnums;
import com.devlog.project.notification.dto.NotifiactionDTO;
import com.devlog.project.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FreeboardServiceImpl implements FreeboardService {

	private final FreeboardMapper mapper;
	private final NotificationService notiService;
	
	@Override
	public List<Map<String, Object>> selectBoardTypeList() {
		return mapper.selectBoardTypeList();
	}
	
	// 게시글 목록 조회	
	@Override                  
	public Map<String, Object> selectFreeboardList(int boardCode, int cp) {
		// 1. 특정 게시판의 삭제되지 않은 게시글 수 조회
		int listCount = mapper.getFreeboardListCount(boardCode);
		
		
	    // 2. 1번의 조회 결과 + cp를 이용해서 Pagination 객체 생성
	    PaginationFB pagination = new PaginationFB(cp, listCount);

	    // 3. 특정 게시판에서 현재 페이지에 해당하는 부분에 대한 게시글 목록 조회
	    // -> 어떤 게시판(boardCode)에서
	    //    몇 페이지(pagination.currentPage)에 대한
	    //    게시글 몇 개 (pagination.limit) 조회

	    // 1) offset 계산
	    int offset = (pagination.getCurrentPage() - 1) * pagination.getLimit();

	    // 2) RowBounds 객체 생성
	    RowBounds rowBounds = new RowBounds(offset, pagination.getLimit());

	    List<Freeboard> freeboardList = mapper.selectFreeboardList(boardCode, rowBounds); 

	    // 4. pagination, boardList를 Map에 담아서 반환
	    Map<String, Object> map = new HashMap<String, Object>();
	    map.put("pagination", pagination);
	    map.put("freeboardList", freeboardList);
	    
	    return map;
	}


	// 게시글 상세 조회	
	@Override
	public Freeboard selectFreeboardDetail(Map<String, Object> map) {
		
	    return mapper.selectFreeboardDetail(map);
	}

	// 조회수 증가
	@Override
	//public int updateBoardCount(int boardNo) {
	public int updateBoardCount(Long boardNo) {
		return mapper.updateBoardCount(boardNo);
	}	
	
	// 상세 게시글 좋아요 여부 확인
	@Override
	public int boardLikeCheck(Map<String, Object> map) {
		return mapper.boardLikeCheck(map);
	}

	// 상세 게시글 좋아요 처리 서비스
	@Override
	public int like(Map<String, Integer> paramMap) {
		int result = 0;

		if(paramMap.get("check") == 0) { // 좋아요 X 상태
			// BOARD_LIKE 테이블 INSERT
			result = mapper.insertBoardLike(paramMap);
			
			// 본인 게시글 좋아요 아닐 경우에 알림
			Long receiver = mapper.selectReceiverNo(paramMap.get("boardNo"));
			Long sender = ((Number)paramMap.get("memberNo")).longValue();
			if(result != 0 && !sender.equals(receiver)) {
				
				String boardTitle = mapper.selectBoardTitle(paramMap.get("boardNo"));
				
				
				String memberNickname = mapper.selectMemberNickname(receiver);
				
				NotifiactionDTO notification = NotifiactionDTO.builder()
						.sender(((Number)paramMap.get("memberNo")).longValue())
						.receiver(receiver)
						.content(memberNickname +"님이 회원님의 게시글에 좋아요를 눌렀습니다.")
						.preview(boardTitle)
						.type(NotiEnums.NotiType.LIKE)
						.targetType(NotiEnums.TargetType.BOARD)
						.targetId(((Number)paramMap.get("boardNo")).longValue())
						.build();
				
				notiService.sendNotification(notification);
			}
			
			

		} else { // 좋아요 O 상태
			// BOARD_LIKE 테이블 DELETE
			result = mapper.deleteBoardLike(paramMap);
		}

		// SQL 수행 결과가 0 == DB 또는 파라미터에 문제가 있음
		// -> 에러를 나타내는 임의의 값을 반환(-1)
		if(result == 0) return -1;

		// 현재 게시글의 좋아요 개수 조회
		return mapper.countBoardLike(paramMap.get("boardNo"));
	}
	
}

