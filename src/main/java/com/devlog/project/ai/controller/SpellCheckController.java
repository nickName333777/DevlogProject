package com.devlog.project.ai.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devlog.project.ai.dto.SpellCheckRequest;
import com.devlog.project.ai.dto.SpellCheckResponse;
import com.devlog.project.ai.service.SpellCheckService;

@RestController
@RequestMapping("/api/ai/writing")
public class SpellCheckController {

    private final SpellCheckService spellCheckService;

    public SpellCheckController(SpellCheckService spellCheckService) {
        this.spellCheckService = spellCheckService;
    }

    @PostMapping("/spell-check")
    public SpellCheckResponse spellCheck(
            @RequestBody SpellCheckRequest request
    ) {
        return spellCheckService.check(request);
    }
}

