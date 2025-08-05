package com.readingclub.controller;

import com.readingclub.dto.ApiResponse;
import com.readingclub.dto.BookDto;
import com.readingclub.dto.CurrentlyReadingDto;
import com.readingclub.service.CurrentlyReadingService;
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
@RequestMapping("/api/currently-reading")
@RequiredArgsConstructor
@Slf4j
public class CurrentlyReadingController {
    
    private final CurrentlyReadingService currentlyReadingService;
    private final AladinBooksService aladinBooksService;
    
    /**
     * 내 읽고 있는 책 목록 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CurrentlyReadingDto.Response>>> getMyCurrentlyReading(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        try {
            Long userId = getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size);
            
            Page<CurrentlyReadingDto.Response> books = currentlyReadingService.getUserCurrentlyReading(userId, pageable, search);
            return ResponseEntity.ok(ApiResponse.success(books, "읽고 있는 책 목록 조회 성공"));
        } catch (Exception e) {
            log.error("읽고 있는 책 목록 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("읽고 있는 책 목록 조회에 실패했습니다."));
        }
    }
    
    /**
     * 읽고 있는 책 상세 조회
     */
    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<CurrentlyReadingDto.Response>> getCurrentlyReading(@PathVariable Long bookId) {
        try {
            Long userId = getCurrentUserId();
            CurrentlyReadingDto.Response book = currentlyReadingService.getCurrentlyReadingById(bookId, userId);
            return ResponseEntity.ok(ApiResponse.success(book, "읽고 있는 책 조회 성공"));
        } catch (Exception e) {
            log.error("읽고 있는 책 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("읽고 있는 책 조회에 실패했습니다."));
        }
    }
    
    /**
     * 읽고 있는 책 추가
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CurrentlyReadingDto.Response>> createCurrentlyReading(
            @Valid @RequestBody CurrentlyReadingDto.CreateRequest request) {
        try {
            Long userId = getCurrentUserId();
            CurrentlyReadingDto.Response book = currentlyReadingService.createCurrentlyReading(userId, request);
            return ResponseEntity.ok(ApiResponse.success(book, "읽고 있는 책 추가 성공"));
        } catch (Exception e) {
            log.error("읽고 있는 책 추가 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("읽고 있는 책 추가에 실패했습니다."));
        }
    }
    
    /**
     * 읽고 있는 책 수정
     */
    @PutMapping("/{bookId}")
    public ResponseEntity<ApiResponse<CurrentlyReadingDto.Response>> updateCurrentlyReading(
            @PathVariable Long bookId,
            @Valid @RequestBody CurrentlyReadingDto.UpdateRequest request) {
        try {
            Long userId = getCurrentUserId();
            CurrentlyReadingDto.Response book = currentlyReadingService.updateCurrentlyReading(bookId, userId, request);
            return ResponseEntity.ok(ApiResponse.success(book, "읽고 있는 책 수정 성공"));
        } catch (Exception e) {
            log.error("읽고 있는 책 수정 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("읽고 있는 책 수정에 실패했습니다."));
        }
    }
    
    /**
     * 읽고 있는 책 진행률 업데이트
     */
    @PutMapping("/{bookId}/progress")
    public ResponseEntity<ApiResponse<CurrentlyReadingDto.Response>> updateProgress(
            @PathVariable Long bookId,
            @Valid @RequestBody CurrentlyReadingDto.ProgressUpdateRequest request) {
        try {
            Long userId = getCurrentUserId();
            CurrentlyReadingDto.Response book = currentlyReadingService.updateProgress(bookId, userId, request);
            return ResponseEntity.ok(ApiResponse.success(book, "진행률 업데이트 성공"));
        } catch (Exception e) {
            log.error("진행률 업데이트 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("진행률 업데이트에 실패했습니다."));
        }
    }
    
    /**
     * 읽고 있는 책 삭제
     */
    @DeleteMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Void>> deleteCurrentlyReading(@PathVariable Long bookId) {
        try {
            Long userId = getCurrentUserId();
            currentlyReadingService.deleteCurrentlyReading(bookId, userId);
            return ResponseEntity.ok(ApiResponse.success(null, "읽고 있는 책 삭제 성공"));
        } catch (Exception e) {
            log.error("읽고 있는 책 삭제 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("읽고 있는 책 삭제에 실패했습니다."));
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
     * 읽고 있는 책 중복 체크
     */
    @GetMapping("/check-duplicate")
    public ResponseEntity<ApiResponse<CurrentlyReadingDto.DuplicateCheckResponse>> checkDuplicate(
            @RequestParam String title,
            @RequestParam(required = false) String author) {
        try {
            Long userId = getCurrentUserId();
            CurrentlyReadingDto.DuplicateCheckResponse response = currentlyReadingService.checkDuplicate(userId, title, author);
            return ResponseEntity.ok(ApiResponse.success(response, "중복 체크 완료"));
        } catch (Exception e) {
            log.error("중복 체크 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("중복 체크에 실패했습니다."));
        }
    }
    
    /**
     * 연체된 책 목록 조회
     */
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<CurrentlyReadingDto.Response>>> getOverdueBooks() {
        try {
            Long userId = getCurrentUserId();
            List<CurrentlyReadingDto.Response> overdueBooks = currentlyReadingService.getOverdueBooks(userId);
            return ResponseEntity.ok(ApiResponse.success(overdueBooks, "연체된 책 목록 조회 성공"));
        } catch (Exception e) {
            log.error("연체된 책 목록 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("연체된 책 목록 조회에 실패했습니다."));
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