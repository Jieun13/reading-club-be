package com.readingclub.controller;

import com.readingclub.dto.ApiResponse;
import com.readingclub.dto.GroupMeetingDto;
import com.readingclub.service.GroupMeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reading-groups/{groupId}/meetings")
@RequiredArgsConstructor
@Slf4j
public class GroupMeetingController {
    
    private final GroupMeetingService groupMeetingService;
    
    /**
     * 모임 일정 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<GroupMeetingDto.Response>> createMeeting(
            @PathVariable Long groupId,
            @Valid @RequestBody GroupMeetingDto.CreateRequest request) {
        try {
            Long userId = getCurrentUserId();
            GroupMeetingDto.Response meeting = groupMeetingService.createMeeting(groupId, userId, request);
            return ResponseEntity.ok(ApiResponse.success(meeting, "모임 일정이 성공적으로 생성되었습니다."));
        } catch (Exception e) {
            log.error("모임 일정 생성 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("모임 일정 생성에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 그룹의 모임 일정 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupMeetingDto.Response>>> getGroupMeetings(
            @PathVariable Long groupId) {
        try {
            Long userId = getCurrentUserId();
            List<GroupMeetingDto.Response> meetings = groupMeetingService.getGroupMeetings(groupId, userId);
            return ResponseEntity.ok(ApiResponse.success(meetings, "모임 일정 목록 조회 성공"));
        } catch (Exception e) {
            log.error("모임 일정 목록 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("모임 일정 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 예정된 모임 목록 조회
     */
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<GroupMeetingDto.SimpleResponse>>> getUpcomingMeetings(
            @PathVariable Long groupId) {
        try {
            Long userId = getCurrentUserId();
            List<GroupMeetingDto.SimpleResponse> meetings = groupMeetingService.getUpcomingMeetings(groupId, userId);
            return ResponseEntity.ok(ApiResponse.success(meetings, "예정된 모임 목록 조회 성공"));
        } catch (Exception e) {
            log.error("예정된 모임 목록 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("예정된 모임 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 다음 모임 조회
     */
    @GetMapping("/next")
    public ResponseEntity<ApiResponse<GroupMeetingDto.SimpleResponse>> getNextMeeting(
            @PathVariable Long groupId) {
        try {
            Long userId = getCurrentUserId();
            GroupMeetingDto.SimpleResponse meeting = groupMeetingService.getNextMeeting(groupId, userId);
            return ResponseEntity.ok(ApiResponse.success(meeting, "다음 모임 조회 성공"));
        } catch (Exception e) {
            log.error("다음 모임 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("다음 모임 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 모임 일정 수정
     */
    @PutMapping("/{meetingId}")
    public ResponseEntity<ApiResponse<GroupMeetingDto.Response>> updateMeeting(
            @PathVariable Long groupId,
            @PathVariable Long meetingId,
            @Valid @RequestBody GroupMeetingDto.UpdateRequest request) {
        try {
            Long userId = getCurrentUserId();
            GroupMeetingDto.Response meeting = groupMeetingService.updateMeeting(meetingId, userId, request);
            return ResponseEntity.ok(ApiResponse.success(meeting, "모임 일정이 성공적으로 수정되었습니다."));
        } catch (Exception e) {
            log.error("모임 일정 수정 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("모임 일정 수정에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 모임 일정 삭제
     */
    @DeleteMapping("/{meetingId}")
    public ResponseEntity<ApiResponse<Void>> deleteMeeting(
            @PathVariable Long groupId,
            @PathVariable Long meetingId) {
        try {
            Long userId = getCurrentUserId();
            groupMeetingService.deleteMeeting(meetingId, userId);
            return ResponseEntity.ok(ApiResponse.success(null, "모임 일정이 성공적으로 삭제되었습니다."));
        } catch (Exception e) {
            log.error("모임 일정 삭제 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("모임 일정 삭제에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * SecurityContext에서 현재 사용자 ID 추출
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            // 개발 테스트용: 임시 사용자 ID 반환
            log.warn("인증되지 않은 사용자 - 테스트용 사용자 ID(1) 반환");
            return 1L;
        }
        return (Long) authentication.getPrincipal();
    }
}
