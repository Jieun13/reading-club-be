package com.readingclub.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readingclub.dto.PostDto;
import com.readingclub.dto.QuoteDto;
import com.readingclub.entity.*;
import com.readingclub.repository.PostRepository;
import com.readingclub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PostService {
    
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 게시글 목록 조회 (필터링 및 페이징)
     */
    public PostDto.ListResponse getPosts(PostDto.SearchFilter filter) {
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());
        
        Page<Post> postPage = postRepository.findPostsWithFilters(
            filter.getPostType(),
            filter.getVisibility(),
            filter.getUserId(),
            pageable
        );
        
        List<PostDto.Response> posts = postPage.getContent().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        return PostDto.ListResponse.builder()
            .posts(posts)
            .totalCount((int) postPage.getTotalElements())
            .currentPage(filter.getPage())
            .totalPages(postPage.getTotalPages())
            .build();
    }
    
    /**
     * 내 게시글 목록 조회
     */
    public PostDto.ListResponse getMyPosts(Long userId, PostDto.SearchFilter filter) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());
        Page<Post> postPage;
        
        if (filter.getPostType() != null && filter.getVisibility() != null) {
            // 타입과 공개설정 모두 필터링
            postPage = postRepository.findPostsWithFilters(
                filter.getPostType(), filter.getVisibility(), userId, pageable);
        } else if (filter.getPostType() != null) {
            // 타입만 필터링
            postPage = postRepository.findByUserAndPostTypeOrderByCreatedAtDesc(
                user, filter.getPostType(), pageable);
        } else if (filter.getVisibility() != null) {
            // 공개설정만 필터링
            postPage = postRepository.findByUserAndVisibilityOrderByCreatedAtDesc(
                user, filter.getVisibility(), pageable);
        } else {
            // 필터링 없음
            postPage = postRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        }
        
        List<PostDto.Response> posts = postPage.getContent().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        return PostDto.ListResponse.builder()
            .posts(posts)
            .totalCount((int) postPage.getTotalElements())
            .currentPage(filter.getPage())
            .totalPages(postPage.getTotalPages())
            .build();
    }
    
    /**
     * 게시글 상세 조회
     */
    public PostDto.Response getPost(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        
        return convertToResponse(post);
    }
    
    /**
     * 게시글 생성
     */
    @Transactional
    public PostDto.Response createPost(Long userId, PostDto.CreateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 게시글 타입별 유효성 검사
        validatePostRequest(request);
        
        Post post = Post.builder()
            .user(user)
            .postType(request.getPostType())
            .visibility(request.getVisibility())
            // 책 정보
            .bookIsbn(request.getBookInfo().getIsbn())
            .bookTitle(request.getBookInfo().getTitle())
            .bookAuthor(request.getBookInfo().getAuthor())
            .bookPublisher(request.getBookInfo().getPublisher())
            .bookCover(request.getBookInfo().getCover())
            .bookPubDate(request.getBookInfo().getPubDate())
            .bookDescription(request.getBookInfo().getDescription())
            // 독후감 필드
            .title(request.getTitle())
            .content(request.getContent())
            // 추천/비추천 필드
            .recommendationType(request.getRecommendationType())
            .reason(request.getReason())
            // 문장 수집 필드
            .quote(request.getQuote())
            .pageNumber(request.getPageNumber())
            .quotes(quotesToJson(request.getQuotes()))
            .build();
        
        Post savedPost = postRepository.save(post);
        log.info("게시글 생성 완료: postId={}, userId={}, type={}", 
            savedPost.getId(), userId, request.getPostType());
        
        return convertToResponse(savedPost);
    }
    
    /**
     * 게시글 수정
     */
    @Transactional
    public PostDto.Response updatePost(Long userId, Long postId, PostDto.UpdateRequest request) {
        Post post = postRepository.findByIdAndUser(postId, 
            userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.")))
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없거나 수정 권한이 없습니다."));
        
        // 게시글 타입에 따른 필드 업데이트
        if (request.getVisibility() != null) {
            post.setVisibility(request.getVisibility());
        }
        
        switch (post.getPostType()) {
            case REVIEW:
                if (request.getTitle() != null) {
                    post.setTitle(request.getTitle());
                }
                if (request.getContent() != null) {
                    post.setContent(request.getContent());
                }
                break;
            case RECOMMENDATION:
                if (request.getRecommendationType() != null) {
                    post.setRecommendationType(request.getRecommendationType());
                }
                if (request.getReason() != null) {
                    post.setReason(request.getReason());
                }
                break;
            case QUOTE:
                if (request.getQuotes() != null) {
                    post.setQuotes(quotesToJson(request.getQuotes()));
                }
                // 하위 호환성을 위한 기존 필드 처리
                if (request.getQuote() != null) {
                    post.setQuote(request.getQuote());
                }
                if (request.getPageNumber() != null) {
                    post.setPageNumber(request.getPageNumber());
                }
                break;
        }
        
        Post updatedPost = postRepository.save(post);
        log.info("게시글 수정 완료: postId={}, userId={}", postId, userId);
        
        return convertToResponse(updatedPost);
    }
    
    /**
     * 게시글 삭제
     */
    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = postRepository.findByIdAndUser(postId,
            userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.")))
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없거나 삭제 권한이 없습니다."));
        
        postRepository.delete(post);
        log.info("게시글 삭제 완료: postId={}, userId={}", postId, userId);
    }
    
    /**
     * 특정 책에 대한 게시글 조회
     */
    public List<PostDto.Response> getPostsByBook(String isbn) {
        List<Post> posts = postRepository.findByBookIsbnOrderByCreatedAtDesc(isbn);
        return posts.stream()
            .filter(post -> post.getVisibility() == PostVisibility.PUBLIC)
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * 게시글 요청 유효성 검사
     */
    private void validatePostRequest(PostDto.CreateRequest request) {
        if (request.getBookInfo() == null || 
            request.getBookInfo().getIsbn() == null || 
            request.getBookInfo().getTitle() == null) {
            throw new RuntimeException("책 정보는 필수입니다.");
        }
        
        switch (request.getPostType()) {
            case REVIEW:
                if (request.getTitle() == null || request.getTitle().trim().isEmpty() ||
                    request.getContent() == null || request.getContent().trim().isEmpty()) {
                    throw new RuntimeException("독후감은 제목과 내용이 필수입니다.");
                }
                break;
            case RECOMMENDATION:
                if (request.getRecommendationType() == null ||
                    request.getReason() == null || request.getReason().trim().isEmpty()) {
                    throw new RuntimeException("추천/비추천은 타입과 이유가 필수입니다.");
                }
                break;
            case QUOTE:
                // 새로운 방식 (quotes 배열) 또는 기존 방식 (quote + pageNumber) 중 하나는 있어야 함
                boolean hasQuotes = request.getQuotes() != null && !request.getQuotes().isEmpty();
                boolean hasLegacyQuote = request.getQuote() != null && !request.getQuote().trim().isEmpty() 
                    && request.getPageNumber() != null && request.getPageNumber() > 0;
                
                if (!hasQuotes && !hasLegacyQuote) {
                    throw new RuntimeException("문장 수집은 최소 하나의 문장과 페이지 번호가 필요합니다.");
                }
                
                // quotes 배열이 있는 경우 각 항목 검증
                if (hasQuotes) {
                    for (QuoteDto quote : request.getQuotes()) {
                        if (quote.getText() == null || quote.getText().trim().isEmpty() ||
                            quote.getPage() == null || quote.getPage() < 1) {
                            throw new RuntimeException("모든 문장은 내용과 유효한 페이지 번호가 필요합니다.");
                        }
                    }
                }
                break;
        }
    }
    
    /**
     * Post 엔티티를 Response DTO로 변환
     */
    private PostDto.Response convertToResponse(Post post) {
        PostDto.BookInfo bookInfo = PostDto.BookInfo.builder()
            .isbn(post.getBookIsbn())
            .title(post.getBookTitle())
            .author(post.getBookAuthor())
            .publisher(post.getBookPublisher())
            .cover(post.getBookCover())
            .pubDate(post.getBookPubDate())
            .description(post.getBookDescription())
            .build();
        
        return PostDto.Response.builder()
            .id(post.getId())
            .userId(post.getUser().getId())
            .userName(post.getUser().getNickname())
            .userProfileImage(post.getUser().getProfileImage())
            .bookInfo(bookInfo)
            .postType(post.getPostType())
            .visibility(post.getVisibility())
            .title(post.getTitle())
            .content(post.getContent())
            .recommendationType(post.getRecommendationType())
            .reason(post.getReason())
            .quote(post.getQuote())
            .pageNumber(post.getPageNumber())
            .quotes(parseQuotes(post.getQuotes()))
            .createdAt(post.getCreatedAt())
            .updatedAt(post.getUpdatedAt())
            .build();
    }
    
    // quotes JSON 문자열을 List<QuoteDto>로 파싱 (페이지 순으로 정렬)
    private List<QuoteDto> parseQuotes(String quotesJson) {
        if (quotesJson == null || quotesJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            List<QuoteDto> quotes = objectMapper.readValue(quotesJson, new TypeReference<List<QuoteDto>>() {});
            
            // 페이지 번호 순으로 정렬
            return quotes.stream()
                .sorted((q1, q2) -> Integer.compare(q1.getPage(), q2.getPage()))
                .collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            log.error("quotes JSON 파싱 실패: {}", quotesJson, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 게시글 검색
     */
    public Page<PostDto.Response> searchPosts(String bookTitle, String keyword, String postType, Pageable pageable) {
        Page<Post> posts;
        
        if (bookTitle != null && !bookTitle.trim().isEmpty()) {
            // 책 제목으로 검색
            if (postType != null && !postType.trim().isEmpty()) {
                PostType type = PostType.valueOf(postType.toUpperCase());
                posts = postRepository.findByBookTitleContainingIgnoreCaseAndPostTypeAndVisibility(
                    bookTitle.trim(), type, pageable);
            } else {
                posts = postRepository.findByBookTitleContainingIgnoreCaseAndVisibility(
                    bookTitle.trim(), pageable);
            }
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            // 키워드로 검색 (제목, 내용, 책 제목)
            posts = postRepository.findByKeywordInTitleOrContentOrBookTitle(
                keyword.trim(), pageable);
        } else {
            // 검색어가 없으면 전체 공개 게시글 조회
            if (postType != null && !postType.trim().isEmpty()) {
                PostType type = PostType.valueOf(postType.toUpperCase());
                posts = postRepository.findByPostTypeAndVisibilityOrderByCreatedAtDesc(
                    type, PostVisibility.PUBLIC, pageable);
            } else {
                posts = postRepository.findByVisibilityOrderByCreatedAtDesc(
                    PostVisibility.PUBLIC, pageable);
            }
        }
        
        return posts.map(this::convertToResponse);
    }
    
    // List<QuoteDto>를 JSON 문자열로 변환 (페이지 순으로 정렬)
    private String quotesToJson(List<QuoteDto> quotes) {
        if (quotes == null || quotes.isEmpty()) {
            return null;
        }
        
        try {
            // 페이지 번호 순으로 정렬
            List<QuoteDto> sortedQuotes = quotes.stream()
                .sorted((q1, q2) -> Integer.compare(q1.getPage(), q2.getPage()))
                .collect(Collectors.toList());
            
            return objectMapper.writeValueAsString(sortedQuotes);
        } catch (JsonProcessingException e) {
            log.error("quotes JSON 변환 실패: {}", quotes, e);
            return null;
        }
    }
}
