package com.readingclub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.readingclub.entity.MonthlyBook;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MonthlyBookDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Integer year;
        private Integer month;
        private String yearMonth;
        private String bookTitle;
        private String bookAuthor;
        private String bookCoverImage;
        private String bookPublisher;
        private String bookPublishedDate;
        private String bookDescription;
        private UserDto.Response selectedBy;
        private String selectionReason;
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
        
        private MonthlyBook.BookStatus status;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull(message = "연도는 필수입니다")
        @Min(value = 2020, message = "연도는 2020년 이후여야 합니다")
        private Integer year;
        
        @NotNull(message = "월은 필수입니다")
        @Min(value = 1, message = "월은 1 이상이어야 합니다")
        @Max(value = 12, message = "월은 12 이하여야 합니다")
        private Integer month;
        
        @NotBlank(message = "책 제목은 필수입니다")
        @Size(max = 200, message = "책 제목은 200자 이하여야 합니다")
        private String bookTitle;
        
        @Size(max = 100, message = "저자명은 100자 이하여야 합니다")
        private String bookAuthor;
        
        @Size(max = 500, message = "표지 이미지 URL은 500자 이하여야 합니다")
        private String bookCoverImage;
        
        @Size(max = 100, message = "출판사명은 100자 이하여야 합니다")
        private String bookPublisher;
        
        @Size(max = 20, message = "출간일은 20자 이하여야 합니다")
        private String bookPublishedDate;
        
        private String bookDescription;
        
        @Size(max = 1000, message = "선정 이유는 1000자 이하여야 합니다")
        private String selectionReason;
        
        @NotNull(message = "시작일은 필수입니다")
        private LocalDate startDate;
        
        @NotNull(message = "종료일은 필수입니다")
        private LocalDate endDate;
    }
}
