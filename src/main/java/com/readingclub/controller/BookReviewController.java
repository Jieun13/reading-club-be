package com.readingclub.controller;

import com.readingclub.dto.ApiResponse;
import com.readingclub.dto.BookReviewDto;
import com.readingclub.service.BookReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book-reviews")
@RequiredArgsConstructor
@Slf4j
public class BookReviewController {
    
    private final BookReviewService bookReviewService;
    
    /**
     * 리뷰 작성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookReviewDto.Response>> createReview(
            @Valid @RequestBody BookReviewDto.CreateRequest request) {
        try {
            Long userId = getCurrentUserId();
            BookReviewDto.Response review = bookReviewService.createReview(userId, request);
            return ResponseEntity.ok(ApiResponse.success(review, "리뷰가 성공적으로 작성되었습니다."));
        } catch (Exception e) {
            log.error("리뷰 작성 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("리뷰 작성에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 월간 도서의 공개 리뷰 목록 조회
     */
    @GetMapping("/monthly-book/{monthlyBookId}")
    public ResponseEntity<ApiResponse<List<BookReviewDto.Response>>> getPublicReviews(
            @PathVariable Long monthlyBookId) {
        try {
            Long userId = getCurrentUserId();
            List<BookReviewDto.Response> reviews = bookReviewService.getPublicReviews(monthlyBookId, userId);
            return ResponseEntity.ok(ApiResponse.success(reviews, "리뷰 목록 조회 성공"));
        } catch (Exception e) {
            log.error("리뷰 목록 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("리뷰 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 내 리뷰 조회
     */
    @GetMapping("/monthly-book/{monthlyBookId}/my")
    public ResponseEntity<ApiResponse<BookReviewDto.Response>> getMyReview(
            @PathVariable Long monthlyBookId) {
        try {
            Long userId = getCurrentUserId();
            BookReviewDto.Response review = bookReviewService.getMyReview(monthlyBookId, userId);
            return ResponseEntity.ok(ApiResponse.success(review, "내 리뷰 조회 성공"));
        } catch (Exception e) {
            log.error("내 리뷰 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("내 리뷰 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 리뷰 수정
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<BookReviewDto.Response>> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody BookReviewDto.UpdateRequest request) {
        try {
            Long userId = getCurrentUserId();
            BookReviewDto.Response review = bookReviewService.updateReview(reviewId, userId, request);
            return ResponseEntity.ok(ApiResponse.success(review, "리뷰가 성공적으로 수정되었습니다."));
        } catch (Exception e) {
            log.error("리뷰 수정 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("리뷰 수정에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 리뷰 삭제
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long reviewId) {
        try {
            Long userId = getCurrentUserId();
            bookReviewService.deleteReview(reviewId, userId);
            return ResponseEntity.ok(ApiResponse.success(null, "리뷰가 성공적으로 삭제되었습니다."));
        } catch (Exception e) {
            log.error("리뷰 삭제 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("리뷰 삭제에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 월간 도서 리뷰 통계 조회
     */
    @GetMapping("/monthly-book/{monthlyBookId}/statistics")
    public ResponseEntity<ApiResponse<BookReviewDto.StatisticsResponse>> getReviewStatistics(
            @PathVariable Long monthlyBookId) {
        try {
            Long userId = getCurrentUserId();
            BookReviewDto.StatisticsResponse statistics = bookReviewService.getReviewStatistics(monthlyBookId, userId);
            return ResponseEntity.ok(ApiResponse.success(statistics, "리뷰 통계 조회 성공"));
        } catch (Exception e) {
            log.error("리뷰 통계 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("리뷰 통계 조회에 실패했습니다: " + e.getMessage()));
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
