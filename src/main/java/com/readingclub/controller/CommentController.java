package com.readingclub.controller;

import com.readingclub.dto.ApiResponse;
import com.readingclub.dto.CommentDto;
import com.readingclub.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {
    
    private final CommentService commentService;
    
    /**
     * 게시글의 댓글 목록 조회
     */
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<CommentDto.CommentListResponse>> getCommentsByPostId(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long userId = getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size);
            
            CommentDto.CommentListResponse response = commentService.getCommentsByPostId(postId, userId, pageable);
            return ResponseEntity.ok(ApiResponse.success(response, "댓글 목록 조회 성공"));
        } catch (Exception e) {
            log.error("댓글 목록 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("댓글 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 댓글 작성
     */
    @PostMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<CommentDto.Response>> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentDto.CreateRequest request) {
        try {
            Long userId = getCurrentUserId();
            CommentDto.Response response = commentService.createComment(postId, userId, request);
            return ResponseEntity.ok(ApiResponse.success(response, "댓글 작성 성공"));
        } catch (Exception e) {
            log.error("댓글 작성 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("댓글 작성에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId) {
        try {
            Long userId = getCurrentUserId();
            commentService.deleteComment(commentId, userId);
            return ResponseEntity.ok(ApiResponse.success(null, "댓글 삭제 성공"));
        } catch (Exception e) {
            log.error("댓글 삭제 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("댓글 삭제에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 특정 댓글의 대댓글 목록 조회
     */
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<ApiResponse<List<CommentDto.Response>>> getRepliesByCommentId(
            @PathVariable Long commentId) {
        try {
            Long userId = getCurrentUserId();
            List<CommentDto.Response> replies = commentService.getRepliesByCommentId(commentId, userId);
            return ResponseEntity.ok(ApiResponse.success(replies, "대댓글 목록 조회 성공"));
        } catch (Exception e) {
            log.error("대댓글 목록 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("대댓글 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 내가 작성한 댓글 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<CommentDto.UserCommentResponse>>> getMyComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long userId = getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size);
            
            Page<CommentDto.UserCommentResponse> comments = commentService.getUserComments(userId, pageable);
            return ResponseEntity.ok(ApiResponse.success(comments, "내 댓글 목록 조회 성공"));
        } catch (Exception e) {
            log.error("내 댓글 목록 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("내 댓글 목록 조회에 실패했습니다: " + e.getMessage()));
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