package com.devlog.project.searchengine.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.devlog.project.searchengine.service.SearchSuggestService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchSuggestController {

    private final SearchSuggestService searchSuggestService;

    @GetMapping("/suggest")
    public List<String> suggest(@RequestParam String keyword) {
        return searchSuggestService.getRelatedKeywords(keyword);
    }

}
