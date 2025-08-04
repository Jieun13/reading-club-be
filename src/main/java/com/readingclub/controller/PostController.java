package com.readingclub.controller;

import com.readingclub.dto.ApiResponse;
import com.readingclub.dto.PostDto;
import com.readingclub.entity.PostType;
import com.readingclub.entity.PostVisibility;
import com.readingclub.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {
    
    private final PostService postService;
    
    /**
     * 게시글 목록 조회 (필터링 및 페이징)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PostDto.ListResponse>> getPosts(
            @RequestParam(required = false) PostType postType,
            @RequestParam(required = false) PostVisibility visibility,
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = Long.parseLong(authentication.getName());
        
        try {
            PostDto.SearchFilter filter = PostDto.SearchFilter.builder()
                .postType(postType)
                .visibility(visibility)
                .userId(userId)
                .page(page)
                .size(size)
                .build();
            
            PostDto.ListResponse response = postService.getPosts(filter, userId);
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("게시글 목록 조회 실패", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("게시글 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 내 게시글 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PostDto.ListResponse>> getMyPosts(
            Authentication authentication,
            @RequestParam(required = false) PostType postType,
            @RequestParam(required = false) PostVisibility visibility,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Long userId = Long.parseLong(authentication.getName());
            
            PostDto.SearchFilter filter = PostDto.SearchFilter.builder()
                .postType(postType)
                .visibility(visibility)
                .page(page)
                .size(size)
                .build();
            
            PostDto.ListResponse response = postService.getMyPosts(userId, filter);
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("내 게시글 목록 조회 실패", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("내 게시글 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 게시글 상세 조회
     */
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDto.Response>> getPost(@PathVariable Long postId) {
        try {
            PostDto.Response response = postService.getPost(postId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("게시글 상세 조회 실패: postId={}", postId, e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("게시글 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 게시글 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostDto.Response>> createPost(
            Authentication authentication,
            @RequestBody PostDto.CreateRequest request) {
        
        try {
            Long userId = Long.parseLong(authentication.getName());
            PostDto.Response response = postService.createPost(userId, request);
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("게시글 생성 실패", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("게시글 생성에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 게시글 수정
     */
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDto.Response>> updatePost(
            Authentication authentication,
            @PathVariable Long postId,
            @RequestBody PostDto.UpdateRequest request) {
        
        try {
            Long userId = Long.parseLong(authentication.getName());
            PostDto.Response response = postService.updatePost(userId, postId, request);
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("게시글 수정 실패: postId={}", postId, e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("게시글 수정에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 게시글 삭제
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            Authentication authentication,
            @PathVariable Long postId) {
        
        try {
            Long userId = Long.parseLong(authentication.getName());
            postService.deletePost(userId, postId);
            
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            log.error("게시글 삭제 실패: postId={}", postId, e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("게시글 삭제에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 특정 책에 대한 게시글 조회
     */
    @GetMapping("/book/{isbn}")
    public ResponseEntity<ApiResponse<List<PostDto.Response>>> getPostsByBook(@PathVariable String isbn) {
        try {
            List<PostDto.Response> response = postService.getPostsByBook(isbn);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("책별 게시글 조회 실패: isbn={}", isbn, e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("책별 게시글 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 게시글 검색 (책 제목 기준)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<PostDto.Response>>> searchPosts(
            @RequestParam(required = false) String bookTitle,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String postType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PostDto.Response> response = postService.searchPosts(bookTitle, keyword, postType, pageable);
            return ResponseEntity.ok(ApiResponse.success(response, "게시글 검색 성공"));
        } catch (Exception e) {
            log.error("게시글 검색 실패: bookTitle={}, keyword={}, postType={}", bookTitle, keyword, postType, e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("게시글 검색에 실패했습니다: " + e.getMessage()));
        }
    }
}
