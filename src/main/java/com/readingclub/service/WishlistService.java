package com.readingclub.service;

import com.readingclub.dto.UserDto;
import com.readingclub.dto.WishlistDto;
import com.readingclub.entity.User;
import com.readingclub.entity.Wishlist;
import com.readingclub.repository.UserRepository;
import com.readingclub.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WishlistService {
    
    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    
    /**
     * 사용자별 위시리스트 조회 (페이징)
     */
    public Page<WishlistDto.Response> getUserWishlists(Long userId, Pageable pageable, 
                                                      Integer priority, String search) {
        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        
        // TODO: 필터링 로직 구현 (현재는 기본 페이징만)
        Page<Wishlist> wishlists = wishlistRepository.findByUserIdOrderByPriorityAscCreatedAtDesc(userId, pageable);
        
        return wishlists.map(this::convertToDto);
    }
    
    /**
     * 위시리스트 상세 조회
     */
    public WishlistDto.Response getWishlistById(Long wishlistId, Long userId) {
        Wishlist wishlist = wishlistRepository.findByIdAndUserId(wishlistId, userId)
                .orElseThrow(() -> new IllegalArgumentException("위시리스트를 찾을 수 없거나 접근 권한이 없습니다."));
        
        return convertToDto(wishlist);
    }
    
    /**
     * 위시리스트 추가
     */
    @Transactional
    public WishlistDto.Response createWishlist(Long userId, WishlistDto.CreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .title(request.getTitle())
                .author(request.getAuthor())
                .coverImage(request.getCoverImage())
                .publisher(request.getPublisher())
                .publishedDate(request.getPublishedDate())
                .description(request.getDescription())
                .memo(request.getMemo())
                .priority(request.getPriority())
                .build();
        
        Wishlist savedWishlist = wishlistRepository.save(wishlist);
        log.info("새 위시리스트 추가: {} (사용자: {})", savedWishlist.getTitle(), userId);
        
        return convertToDto(savedWishlist);
    }
    
    /**
     * 위시리스트 수정
     */
    @Transactional
    public WishlistDto.Response updateWishlist(Long wishlistId, Long userId, WishlistDto.UpdateRequest request) {
        Wishlist wishlist = wishlistRepository.findByIdAndUserId(wishlistId, userId)
                .orElseThrow(() -> new IllegalArgumentException("위시리스트를 찾을 수 없거나 접근 권한이 없습니다."));
        
        wishlist.setTitle(request.getTitle());
        wishlist.setAuthor(request.getAuthor());
        wishlist.setCoverImage(request.getCoverImage());
        wishlist.setPublisher(request.getPublisher());
        wishlist.setPublishedDate(request.getPublishedDate());
        wishlist.setDescription(request.getDescription());
        wishlist.setMemo(request.getMemo());
        wishlist.setPriority(request.getPriority());
        
        Wishlist updatedWishlist = wishlistRepository.save(wishlist);
        log.info("위시리스트 수정: {} (ID: {})", updatedWishlist.getTitle(), wishlistId);
        
        return convertToDto(updatedWishlist);
    }
    
    /**
     * 위시리스트 삭제
     */
    @Transactional
    public void deleteWishlist(Long wishlistId, Long userId) {
        Wishlist wishlist = wishlistRepository.findByIdAndUserId(wishlistId, userId)
                .orElseThrow(() -> new IllegalArgumentException("위시리스트를 찾을 수 없거나 접근 권한이 없습니다."));
        
        wishlistRepository.delete(wishlist);
        log.info("위시리스트 삭제: {} (ID: {})", wishlist.getTitle(), wishlistId);
    }
    
    /**
     * 위시리스트 중복 체크
     */
    public WishlistDto.DuplicateCheckResponse checkDuplicate(Long userId, String title, String author) {
        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        
        List<Wishlist> duplicateWishlists;
        
        if (author != null && !author.trim().isEmpty()) {
            // 제목과 저자로 검색
            duplicateWishlists = wishlistRepository.findByUserIdAndTitleContainingIgnoreCaseAndAuthorContainingIgnoreCase(
                    userId, title.trim(), author.trim());
        } else {
            // 제목만으로 검색
            duplicateWishlists = wishlistRepository.findByUserIdAndTitleContainingIgnoreCase(userId, title.trim());
        }
        
        List<WishlistDto.DuplicateCheckResponse.DuplicateWishlist> duplicateWishlistDtos = duplicateWishlists.stream()
                .map(wishlist -> WishlistDto.DuplicateCheckResponse.DuplicateWishlist.builder()
                        .id(wishlist.getId())
                        .title(wishlist.getTitle())
                        .author(wishlist.getAuthor())
                        .priority(wishlist.getPriority())
                        .createdAt(wishlist.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        
        return WishlistDto.DuplicateCheckResponse.builder()
                .duplicate(!duplicateWishlists.isEmpty())
                .duplicateWishlists(duplicateWishlistDtos)
                .build();
    }
    
    /**
     * 우선순위별 통계 조회
     */
    public List<WishlistDto.PriorityStats> getPriorityStatistics(Long userId) {
        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        
        List<Object[]> results = wishlistRepository.findPriorityStatsByUserId(userId);
        
        return results.stream()
                .map(result -> WishlistDto.PriorityStats.builder()
                        .priority((Integer) result[0])
                        .count((Long) result[1])
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * Entity를 DTO로 변환
     */
    private WishlistDto.Response convertToDto(Wishlist wishlist) {
        UserDto.Response userDto = UserDto.Response.builder()
                .id(wishlist.getUser().getId())
                .kakaoId(wishlist.getUser().getKakaoId())
                .nickname(wishlist.getUser().getNickname())
                .profileImage(wishlist.getUser().getProfileImage())
                .createdAt(wishlist.getUser().getCreatedAt())
                .updatedAt(wishlist.getUser().getUpdatedAt())
                .build();
        
        return WishlistDto.Response.builder()
                .id(wishlist.getId())
                .title(wishlist.getTitle())
                .author(wishlist.getAuthor())
                .coverImage(wishlist.getCoverImage())
                .publisher(wishlist.getPublisher())
                .publishedDate(wishlist.getPublishedDate())
                .description(wishlist.getDescription())
                .memo(wishlist.getMemo())
                .priority(wishlist.getPriority())
                .createdAt(wishlist.getCreatedAt())
                .updatedAt(wishlist.getUpdatedAt())
                .user(userDto)
                .build();
    }
}
