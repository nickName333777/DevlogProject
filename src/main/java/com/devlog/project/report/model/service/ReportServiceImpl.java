package com.devlog.project.report.model.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.devlog.project.chatting.entity.Message;
import com.devlog.project.chatting.repository.MessageRepository;
import com.devlog.project.member.model.entity.Member;
import com.devlog.project.member.model.repository.MemberRepository;
import com.devlog.project.report.enums.ReportTargetEnums;
import com.devlog.project.report.model.dto.ReportRequestDTO;
import com.devlog.project.report.model.dto.ReportTargetDTO;
import com.devlog.project.report.model.dto.ReportTypeDTO;
import com.devlog.project.report.model.entity.Report;
import com.devlog.project.report.model.entity.ReportCode;
import com.devlog.project.report.model.mapper.ReportMapper;
import com.devlog.project.report.model.reporitory.ReportCodeRepository;
import com.devlog.project.report.model.reporitory.ReportRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
	
	@Autowired ReportMapper reportMapper;
	private final MemberRepository memberRepository;
	private final ReportCodeRepository codeRepository;
	private final MessageRepository messageRepository;
	private final ReportRepository reportRepository;
	
	// 신고 대상 회원 조회
	@Override
	public ReportTargetDTO findMember(Long targetMemberNo) {
		
		Member member = memberRepository.findById(targetMemberNo)
						.orElseThrow();
		
		ReportTargetDTO targetMember = ReportTargetDTO.builder()
				.memberNo(member.getMemberNo())
				.memberNickname(member.getMemberNickname())
				.profilePath(member.getProfileImg())
				.build();
		
		
		
		return targetMember;
	}
	
	
	// 신고 유형 조회
	@Override
	public List<ReportTypeDTO> findReportType() {
		
		List<ReportCode> codes = codeRepository.findAll();
		
		List<ReportTypeDTO> types = codes.stream()
				.map(code -> new ReportTypeDTO( // codes엔티티 모음에서 하나 꺼내옴 반환 결과 DTO
						code.getReportCode(),
						code.getReportType()
						))
				.collect(Collectors.toList());
		
		
		return types;
	}

	
	
	// 신고 정보 삽입
	@Override
	public String reportInsertJpa(ReportRequestDTO req) {
		
		System.out.println("서비스 오는지 확인");
		
		String result = null;
		// 본인
		Member member = memberRepository.findById(req.getMemberNo())
						.orElseThrow();
		
		// 신고 대상 회원
		Member targetMember = memberRepository.findById(req.getTargetMemberNo())
						.orElseThrow();
		
			
		Message message = messageRepository.findById(req.getTargetNo())
						.orElseThrow();
		
		ReportCode reportCode = codeRepository.findById(req.getReportCode())
						.orElseThrow();
		
		// 이미 신고한 적 있는지 조회
		
		boolean exist = reportRepository
				.existsByReporter_MemberNoAndTargetTypeAndTargetId(
						req.getMemberNo(),
						req.getTargetType(),
						req.getTargetNo()
						);
				
		if(exist) {
			
			result = "이미 신고한 메시지입니다.";
			
		}else {
			
			Report report = Report.builder()
							.targetType(req.getTargetType())
							.content(req.getReportReason())
							.targetId(req.getTargetNo())
							.messageNo(req.getTargetNo())
							.reporter(member)
							.reported(targetMember)
							.reportCode(reportCode)
							.build();
			
			reportRepository.save(report);
			
			result = "신고가 성공적으로 접수되었습니다.";
		}
		
		
		
		return result;
	}


	@Override
	public String reportInsertBoard(ReportRequestDTO req) {
	    // 중복 신고 확인
	    int count = reportMapper.checkReportExist(req);
	    System.out.println("count:"+ count);
	    if (count > 0) {
	    	
	        return "이미 신고한 게시글입니다.";
	    }

	    // 신고 정보 삽입
	    int result = reportMapper.insertBoardReport(req);

	    if (result > 0) {
	        return "신고가 성공적으로 접수되었습니다.";
	    } else {
	        return "신고 접수에 실패했습니다.";
	    }
	}
	
}
