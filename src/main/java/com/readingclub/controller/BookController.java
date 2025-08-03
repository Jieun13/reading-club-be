package com.readingclub.controller;

import com.readingclub.dto.ApiResponse;
import com.readingclub.dto.BookDto;
import com.readingclub.service.BookService;
import com.readingclub.service.AladinBooksService;
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
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {
    
    private final BookService bookService;
    private final AladinBooksService aladinBooksService;
    
    /**
     * 내 책 목록 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookDto.Response>>> getMyBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String search) {
        try {
            Long userId = getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size);
            
            Page<BookDto.Response> books = bookService.getUserBooks(userId, pageable, year, month, rating, search);
            return ResponseEntity.ok(ApiResponse.success(books, "책 목록 조회 성공"));
        } catch (Exception e) {
            log.error("책 목록 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("책 목록 조회에 실패했습니다."));
        }
    }
    
    /**
     * 책 상세 조회
     */
    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BookDto.Response>> getBook(@PathVariable Long bookId) {
        try {
            Long userId = getCurrentUserId();
            BookDto.Response book = bookService.getBookById(bookId, userId);
            return ResponseEntity.ok(ApiResponse.success(book, "책 조회 성공"));
        } catch (Exception e) {
            log.error("책 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("책 조회에 실패했습니다."));
        }
    }
    
    /**
     * 책 등록
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookDto.Response>> createBook(
            @Valid @RequestBody BookDto.CreateRequest request) {
        try {
            Long userId = getCurrentUserId();
            BookDto.Response book = bookService.createBook(userId, request);
            return ResponseEntity.ok(ApiResponse.success(book, "책 등록 성공"));
        } catch (Exception e) {
            log.error("책 등록 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("책 등록에 실패했습니다."));
        }
    }
    
    /**
     * 책 수정
     */
    @PutMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BookDto.Response>> updateBook(
            @PathVariable Long bookId,
            @Valid @RequestBody BookDto.UpdateRequest request) {
        try {
            Long userId = getCurrentUserId();
            BookDto.Response book = bookService.updateBook(bookId, userId, request);
            return ResponseEntity.ok(ApiResponse.success(book, "책 수정 성공"));
        } catch (Exception e) {
            log.error("책 수정 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("책 수정에 실패했습니다."));
        }
    }
    
    /**
     * 책 삭제
     */
    @DeleteMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long bookId) {
        try {
            Long userId = getCurrentUserId();
            bookService.deleteBook(bookId, userId);
            return ResponseEntity.ok(ApiResponse.success(null, "책 삭제 성공"));
        } catch (Exception e) {
            log.error("책 삭제 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("책 삭제에 실패했습니다."));
        }
    }
    
    /**
     * 책 검색 (알라딘 API)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BookDto.SearchResult>>> searchBooks(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int maxResults) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("검색어를 입력해주세요."));
            }
            
            List<BookDto.SearchResult> results = aladinBooksService.searchBooks(query, maxResults);
            return ResponseEntity.ok(ApiResponse.success(results, "책 검색 완료"));
        } catch (Exception e) {
            log.error("책 검색 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("책 검색에 실패했습니다."));
        }
    }
    
    /**
     * 책 중복 체크
     */
    @GetMapping("/check-duplicate")
    public ResponseEntity<ApiResponse<BookDto.DuplicateCheckResponse>> checkDuplicate(
            @RequestParam String title,
            @RequestParam(required = false) String author) {
        try {
            Long userId = getCurrentUserId();
            BookDto.DuplicateCheckResponse response = bookService.checkDuplicate(userId, title, author);
            return ResponseEntity.ok(ApiResponse.success(response, "중복 체크 완료"));
        } catch (Exception e) {
            log.error("중복 체크 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("중복 체크에 실패했습니다."));
        }
    }
    
    /**
     * 월별 독서 통계
     */
    @GetMapping("/statistics/monthly")
    public ResponseEntity<ApiResponse<List<BookDto.MonthlyStats>>> getMonthlyStatistics() {
        try {
            Long userId = getCurrentUserId();
            List<BookDto.MonthlyStats> stats = bookService.getMonthlyStatistics(userId);
            return ResponseEntity.ok(ApiResponse.success(stats, "월별 통계 조회 성공"));
        } catch (Exception e) {
            log.error("월별 통계 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("월별 통계 조회에 실패했습니다."));
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
