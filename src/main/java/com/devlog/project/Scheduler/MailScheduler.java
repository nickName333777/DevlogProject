package com.devlog.project.Scheduler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.devlog.project.Scheduler.dto.Hot3DTO;
import com.devlog.project.Scheduler.service.BoardService;
import com.devlog.project.Scheduler.service.MailService;
import com.devlog.project.member.model.entity.Member;
import com.devlog.project.member.model.service.MemberProfileService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MailScheduler {
	
	
	private final BoardService boardService;
	private final MailService mailService;
	private final MemberProfileService memberService;
	
	
	
	//@Scheduled(cron = "0 0 20 * * 7")  // 30초마다 실행
	//@Scheduled(cron = "*/30 * * * * *")
	public void Hot3BoardMail() {
		
		 	System.out.println("🔥 Hot3 스케줄러 실행됨");
		
		
		 	List<Hot3DTO> hotList = boardService.selectHotList();
		 	
	
		    if(hotList == null || hotList.isEmpty()) {
		        System.out.println("⚠ Hot3 결과 없음");
		        return;
		    }
	
		    for(Hot3DTO dto : hotList) {
		        System.out.println("👉 " + dto);
		    }
		    
		    //List<Member> members = memberService.findMemberList();
		    List<String> members = new ArrayList<>();
		    
		    members.add("kyusik0548@naver.com");
		    
		    members.add("gmlwns9863@gmail.com");
		    
		    mailService.sendHot3Mail(members, hotList);
	}
}
