package com.readingclub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public class CommentDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String content;
        private Boolean isDeleted;
        private Boolean isReply;
        private Long parentId;
        private Integer replyCount;
        private Boolean canDelete; // 현재 사용자가 삭제할 수 있는지
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime updatedAt;
        
        private UserDto.Response user;
        private List<Response> replies; // 대댓글 목록
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "댓글 내용은 필수입니다")
        @Size(max = 1000, message = "댓글은 1000자 이하여야 합니다")
        private String content;
        
        private Long parentId; // 대댓글인 경우 부모 댓글 ID
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentListResponse {
        private Page<Response> comments;
        private long totalComments;
        private long activeComments;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserCommentResponse {
        private Long id;
        private String content;
        private Boolean isDeleted;
        private Long postId;
        private String postTitle;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
    }
} 