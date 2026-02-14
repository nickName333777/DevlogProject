package com.devlog.project.es.model.document;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberStatsDoc {

    private Long memberNo;
    private String delFl;
    private LocalDateTime joinedAt;
}