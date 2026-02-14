package com.devlog.project.member.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class LevelDTO {
    private int levelNo;
    private String title;
    private int requiredTotalExp;
}

