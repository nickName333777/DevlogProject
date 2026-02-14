//package com.devlog.project.searchengine.controller;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//@Slf4j
//@Controller
//public class SearchEngineController {
//
//    @GetMapping("/search/blog")
//    public String searchBlog(
//            @RequestParam String keyword,
//            Model model
//    ) {
//
//        log.info("[SEARCH_ENGINE] keyword={}", keyword);
//
//        model.addAttribute("keyword", keyword);
//        return "search/blog-search-result";
//    }
//}
