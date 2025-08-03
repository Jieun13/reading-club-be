package com.readingclub.controller;

import com.readingclub.dto.ApiResponse;
import com.readingclub.dto.ReadingGroupDto;
import com.readingclub.service.ReadingGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reading-groups")
@RequiredArgsConstructor
@Slf4j
public class ReadingGroupController {
    
    private final ReadingGroupService readingGroupService;
    
    /**
     * 독서 모임 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReadingGroupDto.Response>> createGroup(
            @Valid @RequestBody ReadingGroupDto.CreateRequest request) {
        try {
            Long userId = getCurrentUserId();
            ReadingGroupDto.Response group = readingGroupService.createGroup(userId, request);
            return ResponseEntity.ok(ApiResponse.success(group, "독서 모임이 성공적으로 생성되었습니다."));
        } catch (Exception e) {
            log.error("독서 모임 생성 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("독서 모임 생성에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 공개 독서 모임 목록 조회
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<Page<ReadingGroupDto.ListResponse>>> getPublicGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ReadingGroupDto.ListResponse> groups = readingGroupService.getPublicGroups(pageable, search);
            return ResponseEntity.ok(ApiResponse.success(groups, "공개 독서 모임 목록 조회 성공"));
        } catch (Exception e) {
            log.error("공개 독서 모임 목록 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("공개 독서 모임 목록 조회에 실패했습니다."));
        }
    }
    
    /**
     * 내가 속한 독서 모임 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ReadingGroupDto.ListResponse>>> getMyGroups() {
        try {
            Long userId = getCurrentUserId();
            List<ReadingGroupDto.ListResponse> groups = readingGroupService.getUserGroups(userId);
            return ResponseEntity.ok(ApiResponse.success(groups, "내 독서 모임 목록 조회 성공"));
        } catch (Exception e) {
            log.error("내 독서 모임 목록 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("내 독서 모임 목록 조회에 실패했습니다."));
        }
    }
    
    /**
     * 독서 모임 상세 조회
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<ReadingGroupDto.Response>> getGroup(@PathVariable Long groupId) {
        try {
            Long userId = getCurrentUserId();
            ReadingGroupDto.Response group = readingGroupService.getGroup(groupId, userId);
            return ResponseEntity.ok(ApiResponse.success(group, "독서 모임 조회 성공"));
        } catch (Exception e) {
            log.error("독서 모임 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("독서 모임 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 초대 코드로 독서 모임 가입
     */
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<ReadingGroupDto.Response>> joinGroup(
            @Valid @RequestBody ReadingGroupDto.JoinRequest request) {
        try {
            Long userId = getCurrentUserId();
            ReadingGroupDto.Response group = readingGroupService.joinGroupByInviteCode(userId, request);
            return ResponseEntity.ok(ApiResponse.success(group, "독서 모임에 성공적으로 가입했습니다."));
        } catch (Exception e) {
            log.error("독서 모임 가입 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("독서 모임 가입에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 독서 모임 수정
     */
    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse<ReadingGroupDto.Response>> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody ReadingGroupDto.UpdateRequest request) {
        try {
            Long userId = getCurrentUserId();
            ReadingGroupDto.Response group = readingGroupService.updateGroup(groupId, userId, request);
            return ResponseEntity.ok(ApiResponse.success(group, "독서 모임이 성공적으로 수정되었습니다."));
        } catch (Exception e) {
            log.error("독서 모임 수정 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("독서 모임 수정에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 독서 모임 탈퇴
     */
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveGroup(@PathVariable Long groupId) {
        try {
            Long userId = getCurrentUserId();
            readingGroupService.leaveGroup(groupId, userId);
            return ResponseEntity.ok(ApiResponse.success(null, "독서 모임에서 성공적으로 탈퇴했습니다."));
        } catch (Exception e) {
            log.error("독서 모임 탈퇴 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("독서 모임 탈퇴에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 초대 코드로 그룹 정보 미리보기
     */
    @GetMapping("/invite/{inviteCode}")
    public ResponseEntity<ApiResponse<ReadingGroupDto.ListResponse>> getGroupByInviteCode(
            @PathVariable String inviteCode) {
        try {
            ReadingGroupDto.ListResponse group = readingGroupService.getGroupByInviteCode(inviteCode);
            return ResponseEntity.ok(ApiResponse.success(group, "모임 정보 조회 성공"));
        } catch (Exception e) {
            log.error("초대 코드로 모임 조회 실패: inviteCode={}, error={}", inviteCode, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("모임을 찾을 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 독서 모임 삭제
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable Long groupId) {
        try {
            Long userId = getCurrentUserId();
            readingGroupService.deleteGroup(groupId, userId);
            return ResponseEntity.ok(ApiResponse.success(null, "독서 모임이 성공적으로 삭제되었습니다."));
        } catch (Exception e) {
            log.error("독서 모임 삭제 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("독서 모임 삭제에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 초대 코드 재생성
     */
    @PostMapping("/{groupId}/regenerate-invite-code")
    public ResponseEntity<ApiResponse<Map<String, String>>> regenerateInviteCode(@PathVariable Long groupId) {
        try {
            Long userId = getCurrentUserId();
            String newInviteCode = readingGroupService.regenerateInviteCode(groupId, userId);
            return ResponseEntity.ok(ApiResponse.success(
                    Map.of("inviteCode", newInviteCode), 
                    "초대 코드가 성공적으로 재생성되었습니다."));
        } catch (Exception e) {
            log.error("초대 코드 재생성 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("초대 코드 재생성에 실패했습니다: " + e.getMessage()));
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
