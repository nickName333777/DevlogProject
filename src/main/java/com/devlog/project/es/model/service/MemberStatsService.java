package com.devlog.project.es.model.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.devlog.project.es.mapper.MemberStatsMapper;
import com.devlog.project.es.model.document.MemberStatsDoc;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberStatsService {

    private final MemberStatsMapper memberStatsMapper;
    private final WebClient webClient;

    public void syncMemberStats() {

        List<MemberStatsDoc> members =
                memberStatsMapper.selectAllForStats();

        for (MemberStatsDoc m : members) {

            Map<String, Object> doc = Map.of(
                "member_no", m.getMemberNo(),
                "del_fl", m.getDelFl(),
                "joined_at", m.getJoinedAt()
            );

            webClient.put()
                .uri("/member-stats/_doc/" + m.getMemberNo()) // ★ 문서 ID = memberNo
                .bodyValue(doc)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        }
    }
}
