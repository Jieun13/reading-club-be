package com.readingclub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.readingclub.entity.BookReview;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class BookReviewDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private UserDto.Response user;
        private MonthlyBookDto.Response monthlyBook;
        private Integer rating;
        private String title;
        private String content;
        private String favoriteQuote;
        private String recommendation;
        private BookReview.ReviewStatus status;
        private Boolean isPublic;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime updatedAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull(message = "월간 도서 ID는 필수입니다")
        private Long monthlyBookId;
        
        @Min(value = 1, message = "별점은 1 이상이어야 합니다")
        @Max(value = 5, message = "별점은 5 이하여야 합니다")
        private Integer rating = 5;
        
        @NotBlank(message = "리뷰 제목은 필수입니다")
        @Size(max = 100, message = "리뷰 제목은 100자 이하여야 합니다")
        private String title;
        
        @NotBlank(message = "리뷰 내용은 필수입니다")
        @Size(max = 2000, message = "리뷰 내용은 2000자 이하여야 합니다")
        private String content;
        
        @Size(max = 500, message = "인상 깊은 구절은 500자 이하여야 합니다")
        private String favoriteQuote;
        
        @Size(max = 1000, message = "추천 이유는 1000자 이하여야 합니다")
        private String recommendation;
        
        @Builder.Default
        private Boolean isPublic = true;
        
        @Builder.Default
        private BookReview.ReviewStatus status = BookReview.ReviewStatus.PUBLISHED;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Min(value = 1, message = "별점은 1 이상이어야 합니다")
        @Max(value = 5, message = "별점은 5 이하여야 합니다")
        private Integer rating;
        
        @NotBlank(message = "리뷰 제목은 필수입니다")
        @Size(max = 100, message = "리뷰 제목은 100자 이하여야 합니다")
        private String title;
        
        @NotBlank(message = "리뷰 내용은 필수입니다")
        @Size(max = 2000, message = "리뷰 내용은 2000자 이하여야 합니다")
        private String content;
        
        @Size(max = 500, message = "인상 깊은 구절은 500자 이하여야 합니다")
        private String favoriteQuote;
        
        @Size(max = 1000, message = "추천 이유는 1000자 이하여야 합니다")
        private String recommendation;
        
        private Boolean isPublic;
        private BookReview.ReviewStatus status;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleResponse {
        private Long id;
        private UserDto.Response user;
        private Integer rating;
        private String title;
        private String content;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime updatedAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticsResponse {
        private Double averageRating;
        private Long totalReviews;
        private Long[] ratingDistribution; // [1점 수, 2점 수, 3점 수, 4점 수, 5점 수]
    }
}
