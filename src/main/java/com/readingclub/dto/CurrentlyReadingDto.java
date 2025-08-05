package com.readingclub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CurrentlyReadingDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private String author;
        private String coverImage;
        private String publisher;
        private String publishedDate;
        private String description;
        private ReadingType readingType;
        private String readingTypeDisplay;
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dueDate;
        
        private Integer progressPercentage;
        private String memo;
        private boolean isOverdue;
        
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
        
        @Size(max = 100, message = "출판사명은 100자 이하여야 합니다")
        private String publisher;
        
        @Size(max = 20, message = "출판일은 20자 이하여야 합니다")
        private String publishedDate;
        
        private String description;
        
        @NotNull(message = "읽기 형태는 필수입니다")
        private ReadingType readingType;
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dueDate; // 도서관 대여 종료일
        
        @Min(value = 0, message = "진행률은 0 이상이어야 합니다")
        @Max(value = 100, message = "진행률은 100 이하여야 합니다")
        private Integer progressPercentage = 0;
        
        private String memo;
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
        
        @Size(max = 100, message = "출판사명은 100자 이하여야 합니다")
        private String publisher;
        
        @Size(max = 20, message = "출판일은 20자 이하여야 합니다")
        private String publishedDate;
        
        private String description;
        
        @NotNull(message = "읽기 형태는 필수입니다")
        private ReadingType readingType;
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dueDate;
        
        @Min(value = 0, message = "진행률은 0 이상이어야 합니다")
        @Max(value = 100, message = "진행률은 100 이하여야 합니다")
        private Integer progressPercentage;
        
        private String memo;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressUpdateRequest {
        @Min(value = 0, message = "진행률은 0 이상이어야 합니다")
        @Max(value = 100, message = "진행률은 100 이하여야 합니다")
        private Integer progressPercentage;
        
        private String memo;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DuplicateCheckResponse {
        private boolean duplicate;
        private List<DuplicateBook> duplicateBooks;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DuplicateBook {
            private Long id;
            private String title;
            private String author;
            private ReadingType readingType;
            private Integer progressPercentage;
            
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            private LocalDateTime createdAt;
        }
    }
    
    public enum ReadingType {
        PAPER_BOOK("종이책 소장"),
        LIBRARY_RENTAL("도서관 대여"),
        MILLIE("밀리의 서재"),
        E_BOOK("전자책 소장");
        
        private final String displayName;
        
        ReadingType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
} 