package com.devlog.project.searchengine.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class SearchSuggestServiceImpl implements SearchSuggestService {

    private final WebClient webClient;

    public SearchSuggestServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:9200") // docker 외부 접근
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getRelatedKeywords(String keyword) {

        // Elasticsearch Query DSL
    	Map<String, Object> requestBody = Map.of(
    		    "size", 0,
    		    "query", Map.of(
    		        "match_phrase_prefix", Map.of(
    		            "search_keyword", keyword
    		        )
    		    ),
    		    "aggs", Map.of(
    		        "related_keywords", Map.of(
    		            "terms", Map.of(
    		                "field", "search_keyword.keyword",
    		                "size", 10
    		            )
    		        )
    		    )
    		);

        Map<String, Object> response = webClient.post()
            .uri("/search-engine-*/_search")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Map.class)
            .block();

        Map<String, Object> aggregations =
            (Map<String, Object>) response.get("aggregations");

        Map<String, Object> relatedKeywords =
            (Map<String, Object>) aggregations.get("related_keywords");

        List<Map<String, Object>> buckets =
            (List<Map<String, Object>>) relatedKeywords.get("buckets");

        return buckets.stream()
            .map(bucket -> (String) bucket.get("key"))
            .toList();
    }
}
