package com.readingclub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.readingclub.entity.GroupMember;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class GroupMemberDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private UserDto.Response user;
        private String role;
        private String status;
        private String introduction;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime joinedAt;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime updatedAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinRequest {
        @Size(max = 500, message = "자기소개는 500자 이하여야 합니다")
        private String introduction;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinByCodeRequest {
        private String inviteCode;
        
        @Size(max = 500, message = "자기소개는 500자 이하여야 합니다")
        private String introduction;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberSummary {
        private Long userId;
        private String nickname;
        private String profileImage;
        private String role;
        private LocalDateTime joinedAt;
    }
}
