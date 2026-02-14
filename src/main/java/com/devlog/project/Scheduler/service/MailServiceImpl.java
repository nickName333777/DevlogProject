package com.devlog.project.Scheduler.service;

import java.util.Iterator;
import java.util.List;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.devlog.project.Scheduler.dto.Hot3DTO;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

	private final JavaMailSender mailSender;

	private String BASE_URL = "https://nonfluent-synchronistically-melba.ngrok-free.dev";
	// ngrok http 8880			https://nonfluent-synchronistically-melba.ngrok-free.dev 

	@Override
	public void sendHot3Mail(List<String> members, List<Hot3DTO> hotList) {

		try {
			
			for (String member : members) {
				
				// 실제 이메일 객체 생성
				MimeMessage message = mailSender.createMimeMessage();
				
				// 첨부파일 + 한글 인코딩 + HTML 지원 세팅
				MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
				
				helper.setTo(member);
				helper.setFrom("kyusik0207@gmail.com", "Devlog"); // 발신자 이름을 Devlog로 변경
				helper.setSubject("🔥 Devlog 인기 게시글 TOP 3"); // 메일 타이틀
				
				
				// 게시글 목록을 HTML 뉴스레터로 변환
				String html = buildHot3Html(hotList);
				
				helper.setText(html, true);
				
				mailSender.send(message);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String buildHot3Html(List<Hot3DTO> list) {

		StringBuilder sb = new StringBuilder();

		sb.append("""
				    <html>
				    <body style="font-family:Pretendard, Arial; background:#f6f7fb; padding:20px;">
				    <div style="max-width:600px; margin:auto; background:white; border-radius:12px; padding:24px;">
				    <h2 style="color:#6b4eff;">🔥 Devlog Weekly Hot 3</h2>
				    <p>Devlog 핫 게시글을 확인해보세요 ! </p>
				""");

		for (Hot3DTO dto : list) {

			sb.append("""
					<table width="100%" cellpadding="0" cellspacing="0"
					       style="margin-top:16px; background:#f6f7ff; border-radius:12px; border:1px solid #e1e4ff;">
					  <tr>
					    <td width="130" style="padding:12px;">
					""");

			sb.append("<img src=\"")
			.append(BASE_URL)
			.append(dto.getThumnail())
			.append("\" width=\"120\" height=\"80\" style=\"border-radius:8px; object-fit:cover; display:block;\">");

			sb.append("""
					        </td>
					        <td style="padding:12px; vertical-align:middle;">
					<div style="
					    max-width: 380px;
					    font-size:16px;
					    font-weight:600;
					    color:#222;
					    margin-bottom:6px;
					    overflow:hidden;
					    text-overflow:ellipsis;
					    white-space:nowrap;
					">
					""");

			sb.append(dto.getBoardTitle());

			sb.append("</div>");

			sb.append("<a href=\"")
			.append(BASE_URL)
			.append("/blog/detail/")
			.append(dto.getBoardNo())
			.append("\" style=\"display:inline-block; padding:6px 14px; background:#6b4eff; color:white; border-radius:16px; text-decoration:none; font-size:13px;\">해당 게시글로 이동 →</a>");

			sb.append("""
					    </td>
					  </tr>
					</table>
					""");
		}

		sb.append("""
				    <div style="text-align:center; margin-top:28px;">
				""");

		sb.append("<a href=\"")
		.append(BASE_URL)
		.append("\" style=\"display:inline-block; padding:12px 26px; background:#6b4eff; color:white; border-radius:24px; text-decoration:none; font-weight:600;\">Devlog 바로가기</a>");

		sb.append("""
				    </div>
				    </div>
				    </body>
				    </html>
				""");

		return sb.toString();
	}




}
