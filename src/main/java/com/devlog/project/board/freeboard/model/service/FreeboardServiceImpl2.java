package com.devlog.project.board.freeboard.model.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.devlog.project.board.freeboard.model.dto.BoardImgDB;
import com.devlog.project.board.freeboard.model.dto.Freeboard;
import com.devlog.project.board.freeboard.model.mapper.FreeboardMapper;
import com.devlog.project.board.freeboard.model.mapper.FreeboardMapper2;
import com.devlog.project.common.exception.FileUploadException;
import com.devlog.project.common.exception.ImageDeleteException;
import com.devlog.project.common.utility.Util;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@PropertySource("classpath:/config.properties")
@RequiredArgsConstructor
public class FreeboardServiceImpl2 implements FreeboardService2 {

	@Value("${my.freeboard.webpath}") 
	private String webPath;
	
	@Value("${my.freeboard.location}")  
	private String filePath;
	
	private final FreeboardMapper mapperService; 
	
	private final FreeboardMapper2 mapper; 
	
	// 게시글 삽입
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Long freeboardInsert(Freeboard freeboard, List<MultipartFile> images) throws IllegalStateException, IOException{
		
		// 0. XSS 방지 처리: <script> 집어넣을 경우 무력화 

		// 제목만 XSS 방지처리:
		freeboard.setBoardTitle(Util.XSSHandling(freeboard.getBoardTitle() ) ); // title 변환시킨후 DB에 저장 ==> 타이틀을 읽어야하는 태그가 BoardDetail.html에 있기 때문
		//freeboard.setBoardContent(Util.XSSHandling(freeboard.getBoardContent() ) ); // content 변환 시킨후 DB에 저장
		
		// 1.  따로 따로 insert
		// -> boardNo (시퀀스로 생성한 번호) 반환 받기
		Long boardNo = mapper.freeboardInsert(freeboard); 
		
		// 실패시 서비스 종료 (밑에 코드 수행할 필요 없다
		if (boardNo == 0) return Long.valueOf(0);
		
		// mapper.xml에서 selectKey 태그로 인해 boardNo에 세팅된 값
		boardNo = freeboard.getBoardNo();
		log.info("[ FreeboardServiceImpl ] Insertion boardNo: {}", boardNo); 	
		
		// 2. 게시글 삽입 성공 시
		if (boardNo != 0) {
			// 실제로 업로드된 파일의 정보를 기록할 List
			List<BoardImgDB> uploadList = new ArrayList<BoardImgDB>();
			
			if (images != null && images.size() > 0) {	// null-방어
				log.info("[ FreeboardServiceImpl ] images.size() =  {}", images.size()); 
				
				for(int i=0; i<images.size(); i++) { // 이미지 파일 있으나 없으나, images.size()=5가 기본 ==> devlog는 추가한 것만큼만 js에서 동적으로 만듦 (즉, 모두 images.get(i).getSize() > 0)
					log.info("[ FreeboardServiceImpl ] images.get({}).getSize() =  {}", i, images.get(i).getSize()); 
					// i번째 요소에 업로드한 파일이 있다면
					if (images.get(i).getSize() > 0) { // 업로드한 이미지 있다.
						// img에 파일 정보를 담아서 uploadList에 추가
						BoardImgDB img = new BoardImgDB();
						
						img.setImgPath(webPath); // 웹 접근 경로
						
						// 파일 원본명
						String fileName = images.get(i).getOriginalFilename(); // 파일 원본명 from 리스트
						log.info("[ FreeboardServiceImpl ] 원본 파일명 fileName =  {}", fileName); 
						
						// 파일 변경명 img에 세팅
						img.setImgRename(Util.fileRename(fileName));
						
						// 파일 원본명 img에 세팅
						img.setImgOrig(fileName);
						
						// 다른 필요한 값들 img에 세팅
						img.setImgOrder(i); 	 // 이미지 순서
						img.setBoardNo(boardNo); // 게시글 번호
						
						uploadList.add(img);
						
					}
					
				} // 분류 for문 종료 
				log.info("[ FreeboardServiceImpl ] uploadList.size() =  {}", uploadList.size()); 
				
				// 분류 작업 후 uploadList가 비어있지 않은 경우
				// == 업로드한 파일이 존재
				if(!uploadList.isEmpty()) {
					
					// BOARD_IMG 테이블에 insert 하기
					int result = mapper.insertImageList(uploadList); // 이것까지 성공해야 commit by @Transactional()
					// result == 성공한 행의 개수
					//
					// 삽입된 행의 갯수(result)와 uploadList의 개수(uploadList.size())가 같다면
					// == 전체 insert 성공
					if (result == uploadList.size()) { // 전체 성공 or 부분 성공/전체 실패
						
						for (int i=0; i<uploadList.size(); i++) {
							// 이미지 순서
							int index = uploadList.get(i).getImgOrder(); //
							
							// 변경명
							String rename = uploadList.get(i).getImgRename();
							images.get(index).transferTo(new File(filePath + rename));  // index에 해당하는 images[index]만 서버로 옮겨준다(서버에 저장한다)
							
							
						}
						
						
						
					} else { // 일부 또는 전체 insert 실패
						// * 웹 서비스 수행 중 1개라도 실패하면 전체 실패 *
						// -> rollback 필요 (but, @Transactional rollback은 exception이 발생해야만 rollback진행
						// @Transactional (rollbackFor = Exception.class)
						// -> 예외가 발생해야만 롤백한다.
						// -> 사용자 정의 예외 (강제)생성 by "throw"
						throw new FileUploadException(); // 강제 예외 발생 시키는 구문 -> 이제 @Transactional에서 rollback한다.
						
					}
					
				}
			}//
		}

		//return 0;
		return boardNo;
	}

	// 게시글의 이미지 삭제: 게시글 수정에서 이미지삭제/추가에서 기존이미지 한개 삭제 (Ajax)
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteFreeboardImage(Long imgNo) {
	    BoardImgDB img = mapperService.selectImageByImgNo(imgNo); // 먼저 삭제할 이미지가 데이터베이스에 존재하는지 확인
	    if (img == null) return false;

	    // DB 삭제
	    int result = 0;
	    try {
	    	result = mapper.deleteImageByImgNo(imgNo); // 데이터베이스에 존재 확인된 이미지 삭제
	    } catch (Exception e) {
	    	log.info("error duging image deletion:{}", e);
	    	throw new ImageDeleteException(e.getMessage()); 
	    }

	    // 파일 삭제
	    if (result > 0) {
	        Path path = Paths.get(
	            img.getImgPath(),
	            img.getImgRename()
	        );

	        try {
	            Files.deleteIfExists(path);
	        } catch (IOException e) {
	            throw new RuntimeException(e);
	        }
	    }

	    return result > 0;
	}

	
	
	// 게시글 수정: 게시글과 게시글 이미지의 업데이트 둘다 성공해야 commit
	@Transactional(rollbackFor = Exception.class)
	@Override	
	public int freeboardUpdate(Freeboard freeboard, List<MultipartFile> images, String existingImgNos)
			throws IllegalStateException, IOException {
		// 0. XSS 방지 처리: <script> 집어넣을 경우 무력화 (게시글 작성때와 마찬가지)
		freeboard.setBoardTitle(Util.XSSHandling(freeboard.getBoardTitle() ) ); // title 변환시킨후 DB에 저장
		//freeboard.setBoardContent(Util.XSSHandling(freeboard.getBoardContent() ) ); // content 변환 시킨후 DB에 저장
		
		// 1. 게시글 제목/내용만 수정
		int rowCount = mapper.freeboardUpdate(freeboard);
		
		
		// 2. 게시글 수정 성공시
        if (rowCount > 0) {
            
            // 2. 기존 이미지 처리
            List<Long> keepImgNos = new ArrayList<>();
            
            if (existingImgNos != null && !existingImgNos.isEmpty()) { //null-방어
                // JSON 파싱: "[88,89,90]" -> List<Long>
                keepImgNos = parseExistingImgNos(existingImgNos);
                
                log.info("[ FreeboardServiceImpl:수정CC ]유지할 기존 이미지 번호들: {}", keepImgNos);
                
                // 2-1. 이 게시글의 기존 이미지 중 keepImgNos에 없는 것들 삭제
                if (!keepImgNos.isEmpty()) {
                    mapper.deleteImagesNotInList(freeboard.getBoardNo(), keepImgNos);
                }
                
                // 2-2. 유지할 기존 이미지들의 순서(IMG_ORDER) 업데이트
                for (int i = 0; i < keepImgNos.size(); i++) {
                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("imgNo", keepImgNos.get(i));
                    orderMap.put("imgOrder", i);
                    mapper.updateImageOrder(orderMap);
                }
            } else {
                // existingImgNos가 null이거나 빈 문자열이면 모든 기존 이미지 삭제
                mapper.deleteAllImagesByBoardNo(freeboard.getBoardNo());
            }
            
            // 3. 새 이미지 추가
            if (images != null && !images.isEmpty()) { // null-방어
                
                // 새 이미지의 시작 순서 = 기존 이미지 개수
                int startOrder = keepImgNos.size();
                
                // 실제로 업로드된 파일만 저장할 리스트
                List<BoardImgDB> uploadList = new ArrayList<>();
                
                for (int i = 0; i < images.size(); i++) {
                    MultipartFile image = images.get(i);
                    
                    // 실제로 업로드된 파일인 경우
                    if (image.getSize() > 0) {
                        
                        // 파일명 생성
                        String originalFilename = image.getOriginalFilename();
                        String rename = Util.fileRename(originalFilename);
                        
                        // BoardImgDB 객체 생성
                        BoardImgDB img = new BoardImgDB();
                        img.setImgPath(webPath);
                        img.setImgRename(rename);
                        img.setImgOrig(originalFilename);
                        img.setImgOrder(startOrder + i);
                        img.setBoardNo(freeboard.getBoardNo());
                        
                        uploadList.add(img);
                        
                        // DB에 이미지 정보 삽입
                        rowCount = mapper.imageInsert(img);
                        
                        if (rowCount == 0) {
                            throw new RuntimeException("이미지 삽입 실패");
                        }
                    }
                }
                
                // 4. 서버에 실제 파일 저장
                if (!uploadList.isEmpty()) {
                    for (int i = 0; i < uploadList.size(); i++) {
                        int index = uploadList.get(i).getImgOrder() - startOrder;
                        String rename = uploadList.get(i).getImgRename();
                        
                        // 실제 파일 저장
                        images.get(index).transferTo(new File(filePath + rename));
                    }
                }
            }
        }
        
        return rowCount;
	}	
	
	
    /**
     * JSON 문자열을 Long 리스트로 파싱
     * @param existingImgNos "[88,89,90]" 형태의 JSON 문자열
     * @return List<Long>
     */
    private List<Long> parseExistingImgNos(String existingImgNos) {
        List<Long> result = new ArrayList<>();
        
        try {
            // JSON 파싱
            ObjectMapper mapper = new ObjectMapper();
            result = mapper.readValue(existingImgNos, new TypeReference<List<Long>>(){});
        } catch (Exception e) {
            log.error("existingImgNos 파싱 실패: {}", existingImgNos, e);
        }
        
        return result;
    }	
	
	// 게시글 삭제
	@Override
	public int freeboardDelete(Long boardNo) {
		// 
		return mapper.freeboardDelete(boardNo);
	}

	@Override
	public int setBoardNoDelFl(Long boardNo) {
		return mapper.freeboardDelete(boardNo);
	}   
    
}
