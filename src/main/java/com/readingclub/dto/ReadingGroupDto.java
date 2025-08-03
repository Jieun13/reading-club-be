package com.readingclub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.readingclub.entity.MeetingType;
import com.readingclub.entity.ReadingGroup;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ReadingGroupDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private UserDto.Response creator;
        private Integer maxMembers;
        private Boolean isPublic;
        private String inviteCode;
        private ReadingGroup.GroupStatus status;
        private Integer currentMemberCount;
        private boolean hasAssignment;
        private MeetingType meetingType;

        // 일정 관련 필드 추가
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime startDateTime;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime endDateTime;
        
        private Integer durationHours;
        private String location;
        private String meetingUrl;

        // 도서 관련 필드 추가
        private String bookTitle;
        private String bookAuthor;
        private String bookPublisher;
        private String bookCoverImage;
        private String bookDescription;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleResponse {
        private Long id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "그룹명은 필수입니다")
        private String name;

        private String description;

        @NotBlank(message = "책 제목은 필수입니다")
        private String bookTitle;

        @NotBlank(message = "저자는 필수입니다")
        private String bookAuthor;

        @NotBlank(message = "출판사는 필수입니다")
        private String bookPublisher;

        private String bookCoverImage;
        private String bookDescription;

        @NotNull(message = "모임 일시는 필수입니다")
        private LocalDateTime meetingDateTime;

        @Min(value = 1, message = "모임 진행 시간은 1시간 이상이어야 합니다")
        private int durationHours;

        @Min(value = 2)
        @Max(value = 50)
        @Builder.Default
        private Integer maxMembers = 20;

        @Builder.Default
        private Boolean isPublic = true;

        @Builder.Default
        private Boolean hasAssignment = false;

        @NotNull(message = "모임 방식(ONLINE/OFFLINE)은 필수입니다")
        private MeetingType meetingType;

        private String location;   // 오프라인 장소
        private String meetingUrl; // 온라인 주소
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @NotBlank(message = "그룹명은 필수입니다")
        @Size(max = 100, message = "그룹명은 100자 이하여야 합니다")
        private String name;
        
        @Size(max = 1000, message = "그룹 설명은 1000자 이하여야 합니다")
        private String description;
        
        @Min(value = 2, message = "최대 멤버 수는 2명 이상이어야 합니다")
        @Max(value = 50, message = "최대 멤버 수는 50명 이하여야 합니다")
        private Integer maxMembers;
        
        private Boolean isPublic;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinRequest {
        @NotBlank(message = "초대 코드는 필수입니다")
        private String inviteCode;
        
        @Size(max = 500, message = "자기소개는 500자 이하여야 합니다")
        private String introduction;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListResponse {
        private Long id;
        private String name;
        private String description;

        private String bookTitle;
        private String bookAuthor;
        private String bookCoverImage;

        private UserDto.Response creator;
        private Integer maxMembers;
        private Integer currentMemberCount;

        private MeetingType meetingType;
        private String meetingUrl;

        private Boolean isPublic;
        private ReadingGroup.GroupStatus status;
        private Boolean hasAssignment;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime startDateTime;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime endDateTime;

        private Integer durationHours;
        private String location;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
    }
}
