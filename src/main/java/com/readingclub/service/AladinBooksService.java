package com.readingclub.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readingclub.dto.BookDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class AladinBooksService {

    private final WebClient aladinWebClient;

    @Value("${aladin.api.ttb-key}")
    private String ttbKey;

    public List<BookDto.SearchResult> searchBooks(String query, int maxResults) {
        log.info("알라딘 책 검색 시작: query={}, maxResults={}", query, maxResults);
        List<BookDto.SearchResult> results = new ArrayList<>();

        results.addAll(searchByType(query, "Title", maxResults));
        if (results.size() < maxResults) {
            results.addAll(searchByType(query, "Keyword", maxResults - results.size()));
        }
        if (results.size() < maxResults) {
            results.addAll(searchByType(query, "Author", maxResults - results.size()));
        }

        log.info("알라딘 책 검색 완료: 총 {}개 결과", results.size());
        return results.stream()
                .distinct()
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    private List<BookDto.SearchResult> searchByType(String query, String queryType, int maxResults) {
        try {
            log.info("알라딘 API 호출 시작: query={}, queryType={}, maxResults={}", query, queryType, maxResults);
            String processedQuery = query.replaceAll("\\s+", "");

            String responseBody = aladinWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/ItemSearch.aspx")
                            .queryParam("ttbkey", ttbKey)
                            .queryParam("Query", processedQuery)
                            .queryParam("QueryType", queryType)
                            .queryParam("MaxResults", maxResults)
                            .queryParam("start", 1)
                            .queryParam("SearchTarget", "Book")
                            .queryParam("output", "js")
                            .queryParam("Version", "20131101")
                            .queryParam("Cover", "Big")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (responseBody == null || responseBody.isBlank()) {
                log.warn("알라딘 API 응답 없음: query={}, queryType={}", query, queryType);
                return Collections.emptyList();
            }

            log.info("알라딘 API 응답 받음: 길이={}", responseBody.length());
            log.debug("알라딘 API 응답 내용: {}", responseBody);

            AladinResponse response = parseAladinResponse(responseBody);
            if (response == null || response.getItem() == null) {
                log.warn("알라딘 응답 파싱 실패 또는 아이템 없음");
                return Collections.emptyList();
            }

            List<BookDto.SearchResult> results = response.getItem().stream()
                    .map(this::convertToSearchResult)
                    .collect(Collectors.toList());
            
            log.info("알라딘 검색 결과 변환 완료: {}개", results.size());
            return results;

        } catch (Exception e) {
            log.error("알라딘 API 호출 실패 ({}): {}", queryType, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private AladinResponse parseAladinResponse(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, AladinResponse.class);
        } catch (Exception e) {
            log.error("응답 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    private BookDto.SearchResult convertToSearchResult(AladinItem item) {
        return BookDto.SearchResult.builder()
                .title(Optional.ofNullable(item.getTitle()).orElse("제목 없음"))
                .author(item.getAuthor())
                .publisher(item.getPublisher())
                .pubDate(item.getPubDate())
                .description(item.getDescription())
                .cover(item.getCover())
                .isbn(item.getIsbn())
                .categoryName(item.getCategoryName())
                .priceStandard(item.getPriceStandard())
                .build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AladinResponse {
        private int totalResults;
        private List<AladinItem> item;

        public int getTotalResults() { return totalResults; }
        public void setTotalResults(int totalResults) { this.totalResults = totalResults; }

        public List<AladinItem> getItem() { return item; }
        public void setItem(List<AladinItem> item) { this.item = item; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AladinItem {
        private String title;
        private String author;
        private String publisher;
        private String pubDate;
        private String description;
        private String cover;
        private String isbn;
        private String categoryName;
        private Integer priceStandard;

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        public String getPublisher() { return publisher; }
        public void setPublisher(String publisher) { this.publisher = publisher; }
        public String getPubDate() { return pubDate; }
        public void setPubDate(String pubDate) { this.pubDate = pubDate; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getCover() { return cover; }
        public void setCover(String cover) { this.cover = cover; }
        public String getIsbn() { return isbn; }
        public void setIsbn(String isbn) { this.isbn = isbn; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public Integer getPriceStandard() { return priceStandard; }
        public void setPriceStandard(Integer priceStandard) { this.priceStandard = priceStandard; }
    }
}