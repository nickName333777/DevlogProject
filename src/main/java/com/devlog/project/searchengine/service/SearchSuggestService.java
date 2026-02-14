package com.devlog.project.searchengine.service;

import java.util.List;

public interface SearchSuggestService {

    // 검색어 기반 연관검색어 조회
    List<String> getRelatedKeywords(String keyword);
}
