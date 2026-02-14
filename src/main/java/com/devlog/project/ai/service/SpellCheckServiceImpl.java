package com.devlog.project.ai.service;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.devlog.project.ai.dto.SpellCheckRequest;
import com.devlog.project.ai.dto.SpellCheckResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class SpellCheckServiceImpl implements SpellCheckService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    
    public SpellCheckServiceImpl(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    private String cleanJson(String aiResult) {
        return aiResult
            .replaceAll("```json", "")
            .replaceAll("```", "")
            .trim();
    }
    
    @Override
    public SpellCheckResponse check(SpellCheckRequest request) {

    	String prompt = """
    			너는 한국어 맞춤법 검사기다.

    			아래 글에서 명백한 오탈자, 잘못된 철자, 틀린 띄어쓰기를 반드시 찾아라.

    			규칙:
    			- 의미나 문장 구조는 바꾸지 않는다
    			- 오타, 철자 오류, 맞춤법 오류만 수정한다
    			- 발견한 오류가 하나라도 있으면 반드시 출력한다
    			- 수정할 것이 없으면 빈 배열 [] 을 출력한다
    			- 출력은 JSON 배열만 허용한다
    			- 설명, 주석, 문장 금지

    			출력 형식:
    			[
    			  { "before": "맞춤뻡", "after": "맞춤법" }
    			]

    			글:
    			%s
    			""".formatted(request.getContent());


        String aiResult = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        System.out.println("===== AI ANSWER START =====");
        System.out.println(aiResult);
        System.out.println("===== AI ANSWER END =====");
        
        // 여기서 aiResult 파싱
        return parse(aiResult);
    }
    
    private SpellCheckResponse parse(String aiResult) {
        try {
            String clean = cleanJson(aiResult);

            List<SpellCheckResponse.Fix> fixes =
                objectMapper.readValue(
                    clean,
                    new TypeReference<List<SpellCheckResponse.Fix>>() {}
                );

            return new SpellCheckResponse(fixes);

        } catch (Exception e) {
        	e.printStackTrace();
            return new SpellCheckResponse(List.of());
        }
    }
    
    
    
}
