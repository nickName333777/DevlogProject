package com.devlog.project.board.ITnews.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.devlog.project.board.ITnews.dto.ITnewsDTO;
import com.devlog.project.board.ITnews.mapper.ITnewsMapper;
import com.devlog.project.common.utility.Util;

@Service
@PropertySource("classpath:/config.properties")
public class ITnewsServiceImpl implements ITnewsService {

	@Autowired
	private ITnewsMapper ITnewsmapper;
	
	@Value("${my.news.webpath}")
    private String webPath;

    @Value("${my.news.location}")
    private String filePath;
	// 뉴스 목록 조회
	@Override
	public List<ITnewsDTO> selectITnewsList(Integer boardCode) {
		return ITnewsmapper.selectITnewsList(boardCode);
	}

	// 뉴스 상세 조회
	@Override
	public ITnewsDTO selectNewsDetail(int boardNo) {
		return ITnewsmapper.selectNewsDetail(boardNo);
	}

	// 뉴스 크롤링
	@Scheduled(cron = "0 0 0 * * *") // 초 분 시 일 월 요일
	public void ITnewsCrawler() {
		System.out.println(">>> ITnewsCrawler() 메서드 진입 성공!");
		try {
			String projectPath = System.getProperty("user.dir");
			String scriptPath = projectPath + File.separator + "scripts" + File.separator + "ITnews.py";

			// OS 판별
	        String os = System.getProperty("os.name").toLowerCase();
	        String pythonCmd;

	        if (os.contains("win")) {
	            // Windows
	            pythonCmd = "python";
	        } else if (os.contains("mac")) {
	            // macOS
	            pythonCmd = "/usr/bin/python3";
	        } else {
	            // Linux
	            pythonCmd = "/home/yypark/miniconda3/bin/python3";
	        }

	        System.out.println("OS: " + os);
	        System.out.println("Python Command: " + pythonCmd);
	        System.out.println("크롤링 프로세스 시작: " + scriptPath);

	        ProcessBuilder pb = new ProcessBuilder(pythonCmd, scriptPath);
	        pb.redirectErrorStream(true);

			Process process = pb.start();

			// 자바 콘솔에서 버퍼 읽기
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
				String line;
				while ((line = reader.readLine()) != null) {
					System.out.println("Python Log " + line);
				}
			}

			int exitCode = process.waitFor();
			System.out.println(" 크롤링 프로세스 종료. Exit Code: " + exitCode);

		} catch (Exception e) {
			System.err.println("에러 발생");
			e.printStackTrace();
		}
	}

	// 좋아요 여부 확인
	@Override
	public int newsLikeCheck(Map<String, Object> map) {
		return ITnewsmapper.newsLikeCheck(map);
	}

	// 조회수 증가
	@Override
	public int updateReadCount(int boardNo) {
		return ITnewsmapper.updateReadCount(boardNo);
	}

	// 좋아요 처리
	@Override
	public int like(Map<String, Object> paramMap) {
		int result = 0;

		// JS에서 온 check 값 (0: 빈하트 누름 -> 삽입 시도 / 1: 꽉찬하트 누름 -> 삭제 시도)
		int check = Integer.parseInt(String.valueOf(paramMap.get("check")));

		if (check == 0) {
			// 삽입하기 전에 혹시 데이터가 있는지 한 번 더 체크 (ORA-00001 방지)
			int count = ITnewsmapper.newsLikeCheck(paramMap);

			if (count == 0) { // 데이터가 정말 없을 때만 삽입
				result = ITnewsmapper.insertBoardLike(paramMap);
			} else {
				result = 1;
			}
		} else {
			// 좋아요 삭제
			result = ITnewsmapper.deleteBoardLike(paramMap);
		}
		if (result == 0)
			return -1;
		return ITnewsmapper.countBoardLike(paramMap.get("boardNo"));

	}

	@Override
	public int countBoardLike(int boardNo) {
		return ITnewsmapper.countBoardLike(boardNo);
	}

	@Override
	public int boardDelete(int boardNo) {
		return ITnewsmapper.boardDelete(boardNo);
	}

	
	// 수정
	@Transactional(rollbackFor = Exception.class)
	@Override
	public int boardUpdate(ITnewsDTO itnews, MultipartFile imageFile) throws IllegalStateException, IOException {

	    int result = ITnewsmapper.boardUpdate(itnews);

	    if (result > 0 && imageFile != null && !imageFile.isEmpty()) {
	        
	        String rename = Util.fileRename(imageFile.getOriginalFilename());
	        itnews.setImgPath(webPath);
	        itnews.setImgRename(rename);

	        imageFile.transferTo(new File(filePath + rename));

	        int imgResult = ITnewsmapper.imageUpdate(itnews);

	        if (imgResult == 0) {
	            ITnewsmapper.imageInsert(itnews);
	        }
	    }

	    return result;
	}

	// 스크랩
	@Transactional(rollbackFor = Exception.class)
	@Override
	public int toggleScrap(Map<String, Object> paramMap) {
		int check = ITnewsmapper.checkScrap(paramMap);
		
		if(check == 0) {
			ITnewsmapper.insertScrap(paramMap);
			return 1;
		}else {
			ITnewsmapper.deleteScrap(paramMap);
			return 0;
		}
		
	}
	
	// 스크랩 확인
	@Override
	public int checkScrap(Map<String, Object> scrapMap) {
		return ITnewsmapper.checkScrap(scrapMap);
	}


}
