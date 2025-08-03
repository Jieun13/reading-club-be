package com.readingclub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class WishlistDto {
    
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
        private String memo;
        private Integer priority;
        
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
        
        @Size(max = 20, message = "출간일은 20자 이하여야 합니다")
        private String publishedDate;
        
        private String description;
        
        private String memo;
        
        @Min(value = 1, message = "우선순위는 1 이상이어야 합니다")
        @Max(value = 5, message = "우선순위는 5 이하여야 합니다")
        private Integer priority = 3;
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
        
        @Size(max = 20, message = "출간일은 20자 이하여야 합니다")
        private String publishedDate;
        
        private String description;
        
        private String memo;
        
        @Min(value = 1, message = "우선순위는 1 이상이어야 합니다")
        @Max(value = 5, message = "우선순위는 5 이하여야 합니다")
        private Integer priority = 3;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DuplicateCheckResponse {
        private boolean duplicate;
        private java.util.List<DuplicateWishlist> duplicateWishlists;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DuplicateWishlist {
            private Long id;
            private String title;
            private String author;
            private Integer priority;
            
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            private LocalDateTime createdAt;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriorityStats {
        private Integer priority;
        private Long count;
    }
}
