package com.devlog.project.ai.service;

import org.springframework.stereotype.Service;

import com.devlog.project.ai.dto.SpellCheckRequest;
import com.devlog.project.ai.dto.SpellCheckResponse;

public interface SpellCheckService {
    SpellCheckResponse check(SpellCheckRequest request);
}