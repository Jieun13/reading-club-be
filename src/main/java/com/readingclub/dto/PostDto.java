package com.readingclub.dto;

import com.readingclub.entity.PostType;
import com.readingclub.entity.PostVisibility;
import com.readingclub.entity.RecommendationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class PostDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookInfo {
        private String isbn;
        private String title;
        private String author;
        private String publisher;
        private String cover;
        private String pubDate;
        private String description;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private BookInfo bookInfo;
        private PostType postType;
        private PostVisibility visibility;
        
        // 독후감 필드
        private String title;
        private String content;
        
        // 추천/비추천 필드
        private RecommendationType recommendationType;
        private String reason;
        
        // 문장 수집 필드 (새로운 방식)
        private List<QuoteDto> quotes;
        
        // 하위 호환성을 위한 기존 필드들
        private String quote;
        private Integer pageNumber;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private PostVisibility visibility;
        
        // 독후감 필드
        private String title;
        private String content;
        
        // 추천/비추천 필드
        private RecommendationType recommendationType;
        private String reason;
        
        // 문장 수집 필드 (새로운 방식)
        private List<QuoteDto> quotes;
        
        // 하위 호환성을 위한 기존 필드들
        private String quote;
        private Integer pageNumber;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long userId;
        private String userName;
        private String userProfileImage;
        private BookInfo bookInfo;
        private PostType postType;
        private PostVisibility visibility;
        
        // 독후감 필드
        private String title;
        private String content;
        
        // 추천/비추천 필드
        private RecommendationType recommendationType;
        private String reason;
        
        // 문장 수집 필드 (새로운 방식)
        private List<QuoteDto> quotes;
        
        // 하위 호환성을 위한 기존 필드들
        private String quote;
        private Integer pageNumber;
        
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListResponse {
        private java.util.List<Response> posts;
        private int totalCount;
        private int currentPage;
        private int totalPages;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchFilter {
        private PostType postType;
        private PostVisibility visibility;
        private Long userId;
        private int page = 0;
        private int size = 10;
    }
}
