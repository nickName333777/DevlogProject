package com.devlog.project.ai.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class SpellCheckResponse {

    private List<Fix> fixes;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Fix {
        private String before;
        private String after;
    }
}
