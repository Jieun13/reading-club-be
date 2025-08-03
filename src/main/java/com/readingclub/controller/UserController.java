package com.readingclub.controller;

import com.readingclub.dto.ApiResponse;
import com.readingclub.dto.UserDto;
import com.readingclub.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    
    /**
     * 내 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto.Response>> getMyInfo() {
        try {
            Long userId = getCurrentUserId();
            UserDto.Response user = userService.getUserById(userId);
            return ResponseEntity.ok(ApiResponse.success(user, "사용자 정보 조회 성공"));
        } catch (Exception e) {
            log.error("사용자 정보 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자 정보 조회에 실패했습니다."));
        }
    }
    
    /**
     * 내 정보 수정
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDto.Response>> updateMyInfo(
            @RequestBody UserDto.UpdateRequest request) {
        try {
            Long userId = getCurrentUserId();
            UserDto.Response updatedUser = userService.updateUser(userId, request);
            return ResponseEntity.ok(ApiResponse.success(updatedUser, "사용자 정보 수정 성공"));
        } catch (Exception e) {
            log.error("사용자 정보 수정 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자 정보 수정에 실패했습니다."));
        }
    }
    
    /**
     * 내 독서 통계 조회
     */
    @GetMapping("/me/statistics")
    public ResponseEntity<ApiResponse<UserDto.Statistics>> getMyStatistics() {
        try {
            Long userId = getCurrentUserId();
            UserDto.Statistics statistics = userService.getUserStatistics(userId);
            return ResponseEntity.ok(ApiResponse.success(statistics, "독서 통계 조회 성공"));
        } catch (Exception e) {
            log.error("독서 통계 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("독서 통계 조회에 실패했습니다."));
        }
    }
    
    /**
     * SecurityContext에서 현재 사용자 ID 추출
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }
        return (Long) authentication.getPrincipal();
    }
}
