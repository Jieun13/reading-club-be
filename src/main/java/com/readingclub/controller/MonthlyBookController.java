package com.readingclub.controller;

import com.readingclub.dto.ApiResponse;
import com.readingclub.dto.MonthlyBookDto;
import com.readingclub.entity.MonthlyBook;
import com.readingclub.service.MonthlyBookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reading-groups/{groupId}/monthly-books")
@RequiredArgsConstructor
@Slf4j
public class MonthlyBookController {
    
    private final MonthlyBookService monthlyBookService;
    
    /**
     * 월간 도서 선정
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MonthlyBookDto.Response>> selectMonthlyBook(
            @PathVariable Long groupId,
            @Valid @RequestBody MonthlyBookDto.CreateRequest request) {
        try {
            Long userId = getCurrentUserId();
            MonthlyBookDto.Response monthlyBook = monthlyBookService.selectMonthlyBook(groupId, userId, request);
            return ResponseEntity.ok(ApiResponse.success(monthlyBook, "월간 도서가 성공적으로 선정되었습니다."));
        } catch (Exception e) {
            log.error("월간 도서 선정 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("월간 도서 선정에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 그룹의 월간 도서 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MonthlyBookDto.Response>>> getGroupMonthlyBooks(
            @PathVariable Long groupId) {
        try {
            Long userId = getCurrentUserId();
            List<MonthlyBookDto.Response> monthlyBooks = monthlyBookService.getGroupMonthlyBooks(groupId, userId);
            return ResponseEntity.ok(ApiResponse.success(monthlyBooks, "월간 도서 목록 조회 성공"));
        } catch (Exception e) {
            log.error("월간 도서 목록 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("월간 도서 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 현재 월간 도서 조회
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<MonthlyBookDto.Response>> getCurrentMonthlyBook(
            @PathVariable Long groupId) {
        try {
            Long userId = getCurrentUserId();
            MonthlyBookDto.Response monthlyBook = monthlyBookService.getCurrentMonthlyBook(groupId, userId);
            return ResponseEntity.ok(ApiResponse.success(monthlyBook, "현재 월간 도서 조회 성공"));
        } catch (Exception e) {
            log.error("현재 월간 도서 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("현재 월간 도서 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 월간 도서 상태 업데이트
     */
    @PutMapping("/{monthlyBookId}/status")
    public ResponseEntity<ApiResponse<MonthlyBookDto.Response>> updateMonthlyBookStatus(
            @PathVariable Long groupId,
            @PathVariable Long monthlyBookId,
            @RequestParam MonthlyBook.BookStatus status) {
        try {
            Long userId = getCurrentUserId();
            MonthlyBookDto.Response monthlyBook = monthlyBookService.updateMonthlyBookStatus(monthlyBookId, userId, status);
            return ResponseEntity.ok(ApiResponse.success(monthlyBook, "월간 도서 상태가 성공적으로 변경되었습니다."));
        } catch (Exception e) {
            log.error("월간 도서 상태 변경 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("월간 도서 상태 변경에 실패했습니다: " + e.getMessage()));
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
