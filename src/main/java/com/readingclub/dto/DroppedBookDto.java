package com.readingclub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.readingclub.entity.DroppedBook;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DroppedBookDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private String author;
        private String isbn;
        private String coverImage;
        private String publisher;
        private String publishedDate;
        private String description;
        private DroppedBook.ReadingType readingType;
        private String readingTypeDisplay;
        private Integer progressPercentage;
        private String dropReason;
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startedDate;
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate droppedDate;
        
        private String memo;
        
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
        
        @Size(max = 20, message = "ISBN은 20자 이하여야 합니다")
        private String isbn;
        
        @Size(max = 500, message = "표지 이미지 URL은 500자 이하여야 합니다")
        private String coverImage;
        
        @Size(max = 100, message = "출판사명은 100자 이하여야 합니다")
        private String publisher;
        
        @Size(max = 20, message = "출판일은 20자 이하여야 합니다")
        private String publishedDate;
        
        private String description;
        
        @NotNull(message = "읽기 형태는 필수입니다")
        private DroppedBook.ReadingType readingType;
        
        @Min(value = 0, message = "진행률은 0 이상이어야 합니다")
        @Max(value = 100, message = "진행률은 100 이하여야 합니다")
        private Integer progressPercentage;
        
        @NotBlank(message = "하차 이유는 필수입니다")
        @Size(max = 1000, message = "하차 이유는 1000자 이하여야 합니다")
        private String dropReason;
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startedDate;
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate droppedDate;
        
        @Size(max = 1000, message = "메모는 1000자 이하여야 합니다")
        private String memo;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Size(max = 200, message = "책 제목은 200자 이하여야 합니다")
        private String title;
        
        @Size(max = 100, message = "저자명은 100자 이하여야 합니다")
        private String author;
        
        @Size(max = 20, message = "ISBN은 20자 이하여야 합니다")
        private String isbn;
        
        @Size(max = 500, message = "표지 이미지 URL은 500자 이하여야 합니다")
        private String coverImage;
        
        @Size(max = 100, message = "출판사명은 100자 이하여야 합니다")
        private String publisher;
        
        @Size(max = 20, message = "출판일은 20자 이하여야 합니다")
        private String publishedDate;
        
        private String description;
        
        private DroppedBook.ReadingType readingType;
        
        @Min(value = 0, message = "진행률은 0 이상이어야 합니다")
        @Max(value = 100, message = "진행률은 100 이하여야 합니다")
        private Integer progressPercentage;
        
        @Size(max = 1000, message = "하차 이유는 1000자 이하여야 합니다")
        private String dropReason;
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startedDate;
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate droppedDate;
        
        @Size(max = 1000, message = "메모는 1000자 이하여야 합니다")
        private String memo;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DuplicateCheckResponse {
        private boolean isDuplicate;
        private DroppedBookDto.Response existingBook;
        private String message;
    }
}
