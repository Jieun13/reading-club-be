package com.readingclub.controller;

import com.readingclub.dto.ApiResponse;
import com.readingclub.dto.WishlistDto;
import com.readingclub.service.WishlistService;
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

@RestController
@RequestMapping("/api/wishlists")
@RequiredArgsConstructor
@Slf4j
public class WishlistController {
    
    private final WishlistService wishlistService;
    
    /**
     * 내 위시리스트 목록 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<WishlistDto.Response>>> getMyWishlists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) String search) {
        try {
            Long userId = getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size);
            
            Page<WishlistDto.Response> wishlists = wishlistService.getUserWishlists(userId, pageable, priority, search);
            return ResponseEntity.ok(ApiResponse.success(wishlists, "위시리스트 목록 조회 성공"));
        } catch (Exception e) {
            log.error("위시리스트 목록 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("위시리스트 목록 조회에 실패했습니다."));
        }
    }
    
    /**
     * 위시리스트 상세 조회
     */
    @GetMapping("/{wishlistId}")
    public ResponseEntity<ApiResponse<WishlistDto.Response>> getWishlist(@PathVariable Long wishlistId) {
        try {
            Long userId = getCurrentUserId();
            WishlistDto.Response wishlist = wishlistService.getWishlistById(wishlistId, userId);
            return ResponseEntity.ok(ApiResponse.success(wishlist, "위시리스트 조회 성공"));
        } catch (Exception e) {
            log.error("위시리스트 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("위시리스트 조회에 실패했습니다."));
        }
    }
    
    /**
     * 위시리스트 추가
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WishlistDto.Response>> createWishlist(
            @Valid @RequestBody WishlistDto.CreateRequest request) {
        try {
            Long userId = getCurrentUserId();
            WishlistDto.Response wishlist = wishlistService.createWishlist(userId, request);
            return ResponseEntity.ok(ApiResponse.success(wishlist, "위시리스트 추가 성공"));
        } catch (Exception e) {
            log.error("위시리스트 추가 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("위시리스트 추가에 실패했습니다."));
        }
    }
    
    /**
     * 위시리스트 수정
     */
    @PutMapping("/{wishlistId}")
    public ResponseEntity<ApiResponse<WishlistDto.Response>> updateWishlist(
            @PathVariable Long wishlistId,
            @Valid @RequestBody WishlistDto.UpdateRequest request) {
        try {
            Long userId = getCurrentUserId();
            WishlistDto.Response wishlist = wishlistService.updateWishlist(wishlistId, userId, request);
            return ResponseEntity.ok(ApiResponse.success(wishlist, "위시리스트 수정 성공"));
        } catch (Exception e) {
            log.error("위시리스트 수정 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("위시리스트 수정에 실패했습니다."));
        }
    }
    
    /**
     * 위시리스트 삭제
     */
    @DeleteMapping("/{wishlistId}")
    public ResponseEntity<ApiResponse<Void>> deleteWishlist(@PathVariable Long wishlistId) {
        try {
            Long userId = getCurrentUserId();
            wishlistService.deleteWishlist(wishlistId, userId);
            return ResponseEntity.ok(ApiResponse.success(null, "위시리스트 삭제 성공"));
        } catch (Exception e) {
            log.error("위시리스트 삭제 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("위시리스트 삭제에 실패했습니다."));
        }
    }
    
    /**
     * 위시리스트 중복 체크
     */
    @GetMapping("/check-duplicate")
    public ResponseEntity<ApiResponse<WishlistDto.DuplicateCheckResponse>> checkDuplicate(
            @RequestParam String title,
            @RequestParam(required = false) String author) {
        try {
            Long userId = getCurrentUserId();
            WishlistDto.DuplicateCheckResponse response = wishlistService.checkDuplicate(userId, title, author);
            return ResponseEntity.ok(ApiResponse.success(response, "중복 체크 완료"));
        } catch (Exception e) {
            log.error("중복 체크 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("중복 체크에 실패했습니다."));
        }
    }
    
    /**
     * 우선순위별 통계
     */
    @GetMapping("/statistics/priority")
    public ResponseEntity<ApiResponse<List<WishlistDto.PriorityStats>>> getPriorityStatistics() {
        try {
            Long userId = getCurrentUserId();
            List<WishlistDto.PriorityStats> stats = wishlistService.getPriorityStatistics(userId);
            return ResponseEntity.ok(ApiResponse.success(stats, "우선순위별 통계 조회 성공"));
        } catch (Exception e) {
            log.error("우선순위별 통계 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("우선순위별 통계 조회에 실패했습니다."));
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
