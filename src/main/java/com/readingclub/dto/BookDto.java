package com.readingclub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BookDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private String author;
        private String coverImage;
        private Integer rating;
        private String review;
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate finishedDate;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime updatedAt;
        
        private UserDto.Response user;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "책 제목은 필수입니다")
        @Size(max = 200, message = "책 제목은 200자 이하여야 합니다")
        private String title;
        
        @Size(max = 100, message = "저자명은 100자 이하여야 합니다")
        private String author;
        
        @Size(max = 500, message = "표지 이미지 URL은 500자 이하여야 합니다")
        private String coverImage;
        
        @NotNull(message = "별점은 필수입니다")
        @Min(value = 1, message = "별점은 1점 이상이어야 합니다")
        @Max(value = 5, message = "별점은 5점 이하여야 합니다")
        private Integer rating;
        
        private String review;
        
        @NotNull(message = "완독일은 필수입니다")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate finishedDate;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @NotBlank(message = "책 제목은 필수입니다")
        @Size(max = 200, message = "책 제목은 200자 이하여야 합니다")
        private String title;
        
        @Size(max = 100, message = "저자명은 100자 이하여야 합니다")
        private String author;
        
        @Size(max = 500, message = "표지 이미지 URL은 500자 이하여야 합니다")
        private String coverImage;
        
        @NotNull(message = "별점은 필수입니다")
        @Min(value = 1, message = "별점은 1점 이상이어야 합니다")
        @Max(value = 5, message = "별점은 5점 이하여야 합니다")
        private Integer rating;
        
        private String review;
        
        @NotNull(message = "완독일은 필수입니다")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate finishedDate;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResult {
        private String title;
        private String author;
        private String publisher;
        private String pubDate;
        private String description;
        private String cover;
        private String isbn;
        private String categoryName;
        private Integer priceStandard;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStats {
        private int year;
        private int month;
        private long count;
        private double averageRating;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DuplicateCheckResponse {
        private boolean duplicate; // isDuplicate -> duplicate로 수정
        private List<DuplicateBook> duplicateBooks;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DuplicateBook {
            private Long id;
            private String title;
            private String author;
            private Integer rating;
            
            @JsonFormat(pattern = "yyyy-MM-dd")
            private LocalDate finishedDate;
            
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            private LocalDateTime createdAt;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CombinedBookResponse {
        private Page<Response> books;
        private List<CurrentlyReadingDto.Response> currentlyReading;
        private int totalCurrentlyReading;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllBooksResponse {
        private Page<Response> finishedBooks; // 완독한 책
        private List<CurrentlyReadingDto.Response> currentlyReading; // 읽고 있는 책
        private List<DroppedBookDto.Response> droppedBooks; // 읽다 만 책
        private List<WishlistDto.Response> wishlistBooks; // 읽고 싶은 책
        
        private int totalFinishedBooks;
        private int totalCurrentlyReading;
        private int totalDroppedBooks;
        private int totalWishlistBooks;
    }
    
    /**
     * 이번 달 등록한 책들의 표지 URL 목록
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyBookCoversResponse {
        private List<String> completedBookCovers;      // 완독한 책 표지 URL
        private List<String> currentlyReadingCovers;  // 읽고 있는 책 표지 URL
        private List<String> droppedBookCovers;       // 읽다 만 책 표지 URL
        private List<String> wishlistCovers;          // 위시리스트 책 표지 URL
        private int totalCount;                       // 전체 책 개수
    }

}
