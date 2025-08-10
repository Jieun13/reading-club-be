package com.readingclub.controller;

import com.readingclub.dto.ApiResponse;
import com.readingclub.dto.DroppedBookDto;
import com.readingclub.service.DroppedBookService;
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
@RequestMapping("/api/dropped-books")
@RequiredArgsConstructor
@Slf4j
public class DroppedBookController {
    
    private final DroppedBookService droppedBookService;
    
    /**
     * 내 읽다 만 책 목록 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<DroppedBookDto.Response>>> getMyDroppedBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        try {
            Long userId = getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size);
            Page<DroppedBookDto.Response> droppedBooks = droppedBookService.getDroppedBooks(userId, pageable, search);
            return ResponseEntity.ok(ApiResponse.success(droppedBooks, "읽다 만 책 목록 조회 성공"));
        } catch (Exception e) {
            log.error("읽다 만 책 목록 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("읽다 만 책 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 내 읽다 만 책 상세 조회
     */
    @GetMapping("/{droppedBookId}")
    public ResponseEntity<ApiResponse<DroppedBookDto.Response>> getMyDroppedBook(@PathVariable Long droppedBookId) {
        try {
            Long userId = getCurrentUserId();
            DroppedBookDto.Response droppedBook = droppedBookService.getDroppedBook(droppedBookId, userId);
            return ResponseEntity.ok(ApiResponse.success(droppedBook, "읽다 만 책 조회 성공"));
        } catch (Exception e) {
            log.error("읽다 만 책 조회 실패: droppedBookId={}", droppedBookId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("읽다 만 책 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 읽다 만 책 추가
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DroppedBookDto.Response>> createDroppedBook(
            @RequestBody DroppedBookDto.CreateRequest request) {
        try {
            Long userId = getCurrentUserId();
            DroppedBookDto.Response createdDroppedBook = droppedBookService.createDroppedBook(userId, request);
            return ResponseEntity.ok(ApiResponse.success(createdDroppedBook, "읽다 만 책 추가 성공"));
        } catch (Exception e) {
            log.error("읽다 만 책 추가 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("읽다 만 책 추가에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 읽다 만 책 수정
     */
    @PutMapping("/{droppedBookId}")
    public ResponseEntity<ApiResponse<DroppedBookDto.Response>> updateDroppedBook(
            @PathVariable Long droppedBookId,
            @RequestBody DroppedBookDto.UpdateRequest request) {
        try {
            Long userId = getCurrentUserId();
            DroppedBookDto.Response updatedDroppedBook = droppedBookService.updateDroppedBook(droppedBookId, userId, request);
            return ResponseEntity.ok(ApiResponse.success(updatedDroppedBook, "읽다 만 책 수정 성공"));
        } catch (Exception e) {
            log.error("읽다 만 책 수정 실패: droppedBookId={}", droppedBookId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("읽다 만 책 수정에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 읽다 만 책 삭제
     */
    @DeleteMapping("/{droppedBookId}")
    public ResponseEntity<ApiResponse<Void>> deleteDroppedBook(@PathVariable Long droppedBookId) {
        try {
            Long userId = getCurrentUserId();
            droppedBookService.deleteDroppedBook(droppedBookId, userId);
            return ResponseEntity.ok(ApiResponse.success(null, "읽다 만 책 삭제 성공"));
        } catch (Exception e) {
            log.error("읽다 만 책 삭제 실패: droppedBookId={}", droppedBookId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("읽다 만 책 삭제에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 읽다 만 책 중복 체크
     */
    @GetMapping("/check-duplicate")
    public ResponseEntity<ApiResponse<DroppedBookDto.DuplicateCheckResponse>> checkDuplicate(
            @RequestParam String title,
            @RequestParam(required = false) String author) {
        try {
            Long userId = getCurrentUserId();
            DroppedBookDto.DuplicateCheckResponse response = droppedBookService.checkDuplicate(userId, title, author);
            return ResponseEntity.ok(ApiResponse.success(response, "중복 체크 완료"));
        } catch (Exception e) {
            log.error("중복 체크 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("중복 체크에 실패했습니다."));
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
