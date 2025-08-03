package com.readingclub.controller;

import com.readingclub.dto.ApiResponse;
import com.readingclub.dto.GroupMemberDto;
import com.readingclub.service.GroupMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reading-groups/{groupId}/members")
@RequiredArgsConstructor
@Slf4j
public class GroupMemberController {
    
    private final GroupMemberService groupMemberService;
    
    /**
     * 모임 가입 (공개 모임 또는 초대 링크)
     */
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<GroupMemberDto.Response>> joinGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody GroupMemberDto.JoinRequest request) {
        try {
            Long userId = getCurrentUserId();
            GroupMemberDto.Response member = groupMemberService.joinGroup(groupId, userId, request);
            return ResponseEntity.ok(ApiResponse.success(member, "모임에 성공적으로 가입했습니다."));
        } catch (Exception e) {
            log.error("모임 가입 실패: groupId={}, error={}", groupId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("모임 가입에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 초대 코드로 모임 가입
     */
    @PostMapping("/join-by-code")
    public ResponseEntity<ApiResponse<GroupMemberDto.Response>> joinByInviteCode(
            @PathVariable Long groupId,
            @Valid @RequestBody GroupMemberDto.JoinByCodeRequest request) {
        try {
            Long userId = getCurrentUserId();
            GroupMemberDto.Response member = groupMemberService.joinByInviteCode(groupId, userId, request);
            return ResponseEntity.ok(ApiResponse.success(member, "모임에 성공적으로 가입했습니다."));
        } catch (Exception e) {
            log.error("초대 코드 가입 실패: groupId={}, error={}", groupId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("모임 가입에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 멤버 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupMemberDto.Response>>> getGroupMembers(
            @PathVariable Long groupId) {
        try {
            Long userId = getCurrentUserId();
            List<GroupMemberDto.Response> members = groupMemberService.getGroupMembers(groupId, userId);
            return ResponseEntity.ok(ApiResponse.success(members, "멤버 목록 조회 성공"));
        } catch (Exception e) {
            log.error("멤버 목록 조회 실패: groupId={}, error={}", groupId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("멤버 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 멤버 삭제 (모임장만)
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        try {
            Long currentUserId = getCurrentUserId();
            groupMemberService.removeMember(groupId, userId, currentUserId);
            return ResponseEntity.ok(ApiResponse.success(null, "멤버가 성공적으로 삭제되었습니다."));
        } catch (Exception e) {
            log.error("멤버 삭제 실패: groupId={}, userId={}, error={}", groupId, userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("멤버 삭제에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 모임 탈퇴 (본인만)
     */
    @DeleteMapping("/leave")
    public ResponseEntity<ApiResponse<Void>> leaveGroup(@PathVariable Long groupId) {
        try {
            Long userId = getCurrentUserId();
            groupMemberService.leaveGroup(groupId, userId);
            return ResponseEntity.ok(ApiResponse.success(null, "모임에서 성공적으로 탈퇴했습니다."));
        } catch (Exception e) {
            log.error("모임 탈퇴 실패: groupId={}, userId={}, error={}", groupId, getCurrentUserId(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("모임 탈퇴에 실패했습니다: " + e.getMessage()));
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
        return Long.parseLong(authentication.getName());
    }
}
