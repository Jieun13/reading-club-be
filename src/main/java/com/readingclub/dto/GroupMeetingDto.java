package com.readingclub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.readingclub.entity.GroupMeeting;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class GroupMeetingDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private String description;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime meetingDateTime;
        
        private String location;
        private String agenda;
        private GroupMeeting.MeetingStatus status;
        private UserDto.Response createdBy;
        private MonthlyBookDto.Response monthlyBook;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime updatedAt;
        
        // 편의 필드
        private Boolean isPast;
        private Boolean isUpcoming;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "모임 제목은 필수입니다")
        @Size(max = 200, message = "모임 제목은 200자 이하여야 합니다")
        private String title;
        
        @Size(max = 1000, message = "모임 설명은 1000자 이하여야 합니다")
        private String description;
        
        @NotNull(message = "모임 일시는 필수입니다")
        @Future(message = "모임 일시는 현재 시간 이후여야 합니다")
        private LocalDateTime meetingDateTime;
        
        @Size(max = 500, message = "모임 장소는 500자 이하여야 합니다")
        private String location;
        
        @Size(max = 1000, message = "모임 안건은 1000자 이하여야 합니다")
        private String agenda;
        
        private Long monthlyBookId; // 선택사항
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @NotBlank(message = "모임 제목은 필수입니다")
        @Size(max = 200, message = "모임 제목은 200자 이하여야 합니다")
        private String title;
        
        @Size(max = 1000, message = "모임 설명은 1000자 이하여야 합니다")
        private String description;
        
        @NotNull(message = "모임 일시는 필수입니다")
        private LocalDateTime meetingDateTime;
        
        @Size(max = 500, message = "모임 장소는 500자 이하여야 합니다")
        private String location;
        
        @Size(max = 1000, message = "모임 안건은 1000자 이하여야 합니다")
        private String agenda;
        
        private GroupMeeting.MeetingStatus status;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleResponse {
        private Long id;
        private String title;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime meetingDateTime;
        
        private String location;
        private GroupMeeting.MeetingStatus status;
        private Boolean isPast;
        private Boolean isUpcoming;
    }
}
