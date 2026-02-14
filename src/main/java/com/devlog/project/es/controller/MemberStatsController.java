package com.devlog.project.es.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devlog.project.es.model.service.MemberStatsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
public class MemberStatsController {

    private final MemberStatsService StatsService;

    @GetMapping("/member/stats")
    public String sync() {
    	StatsService.syncMemberStats();
        return "OK";
    }
}