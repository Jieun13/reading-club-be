package com.readingclub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class UserProfileDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String nickname;
        private String profileImage;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime updatedAt;
        
        // 통계 정보
        private UserStatistics statistics;
        
        // 현재 읽고 있는 책 정보
        private List<CurrentlyReadingDto.Response> currentlyReading;
        
        // 최근 공개 게시글 정보
        private List<PostDto.Response> recentPublicPosts;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStatistics {
        private Long totalBooks; // 총 완독한 책 권수
        private Long currentlyReadingCount; // 읽고 있는 책 권수
        private Long wishlistCount; // 읽고 싶은 책 권수
        private Long droppedBooksCount; // 총 읽다 만 책 권수
        private Long totalPosts; // 총 게시글 개수
        private Long thisMonthPosts; // 이번 달 게시글 개수
        private Long thisMonthBooks; // 이번 달 완독한 책 권수
        private Long thisMonthDroppedBooks; // 이번 달 읽다 만 책 권수
    }
} 