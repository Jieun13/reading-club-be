package com.readingclub.service;

import com.readingclub.dto.BookReviewDto;
import com.readingclub.dto.MonthlyBookDto;
import com.readingclub.dto.UserDto;
import com.readingclub.entity.*;
import com.readingclub.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookReviewService {
    
    private final BookReviewRepository bookReviewRepository;
    private final MonthlyBookRepository monthlyBookRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    
    /**
     * 리뷰 작성
     */
    @Transactional
    public BookReviewDto.Response createReview(Long userId, BookReviewDto.CreateRequest request) {
        MonthlyBook monthlyBook = monthlyBookRepository.findById(request.getMonthlyBookId())
                .orElseThrow(() -> new IllegalArgumentException("월간 도서를 찾을 수 없습니다."));
        
        // 그룹 멤버 여부 확인
        if (!groupMemberRepository.isUserMemberOfGroup(monthlyBook.getGroup().getId(), userId)) {
            throw new IllegalArgumentException("해당 그룹의 멤버가 아닙니다.");
        }
        
        // 이미 리뷰를 작성했는지 확인
        if (bookReviewRepository.existsByMonthlyBookIdAndUserId(request.getMonthlyBookId(), userId)) {
            throw new IllegalArgumentException("이미 이 책에 대한 리뷰를 작성했습니다.");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        BookReview review = BookReview.builder()
                .monthlyBook(monthlyBook)
                .user(user)
                .rating(request.getRating())
                .title(request.getTitle())
                .content(request.getContent())
                .favoriteQuote(request.getFavoriteQuote())
                .recommendation(request.getRecommendation())
                .isPublic(request.getIsPublic())
                .status(request.getStatus())
                .build();
        
        BookReview savedReview = bookReviewRepository.save(review);
        
        log.info("리뷰 작성: {} (사용자: {}, 책: {})", savedReview.getTitle(), userId, monthlyBook.getBookTitle());
        
        return convertToDto(savedReview);
    }
    
    /**
     * 월간 도서의 공개 리뷰 목록 조회
     */
    public List<BookReviewDto.Response> getPublicReviews(Long monthlyBookId, Long userId) {
        MonthlyBook monthlyBook = monthlyBookRepository.findById(monthlyBookId)
                .orElseThrow(() -> new IllegalArgumentException("월간 도서를 찾을 수 없습니다."));
        
        // 그룹 멤버 여부 확인
        if (!groupMemberRepository.isUserMemberOfGroup(monthlyBook.getGroup().getId(), userId)) {
            throw new IllegalArgumentException("해당 그룹의 멤버가 아닙니다.");
        }
        
        List<BookReview> reviews = bookReviewRepository.findPublicReviewsByMonthlyBookId(monthlyBookId);
        
        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 내 리뷰 조회
     */
    public BookReviewDto.Response getMyReview(Long monthlyBookId, Long userId) {
        return bookReviewRepository.findByMonthlyBookIdAndUserId(monthlyBookId, userId)
                .map(this::convertToDto)
                .orElse(null);
    }
    
    /**
     * 리뷰 수정
     */
    @Transactional
    public BookReviewDto.Response updateReview(Long reviewId, Long userId, BookReviewDto.UpdateRequest request) {
        BookReview review = bookReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        
        // 작성자 확인
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("리뷰 수정 권한이 없습니다.");
        }
        
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setContent(request.getContent());
        review.setFavoriteQuote(request.getFavoriteQuote());
        review.setRecommendation(request.getRecommendation());
        
        if (request.getIsPublic() != null) {
            review.setIsPublic(request.getIsPublic());
        }
        if (request.getStatus() != null) {
            review.setStatus(request.getStatus());
        }
        
        BookReview updatedReview = bookReviewRepository.save(review);
        
        log.info("리뷰 수정: {} (ID: {})", updatedReview.getTitle(), reviewId);
        
        return convertToDto(updatedReview);
    }
    
    /**
     * 리뷰 삭제
     */
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        BookReview review = bookReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        
        // 작성자 확인
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("리뷰 삭제 권한이 없습니다.");
        }
        
        bookReviewRepository.delete(review);
        
        log.info("리뷰 삭제: {} (ID: {})", review.getTitle(), reviewId);
    }
    
    /**
     * 월간 도서 리뷰 통계 조회
     */
    public BookReviewDto.StatisticsResponse getReviewStatistics(Long monthlyBookId, Long userId) {
        MonthlyBook monthlyBook = monthlyBookRepository.findById(monthlyBookId)
                .orElseThrow(() -> new IllegalArgumentException("월간 도서를 찾을 수 없습니다."));
        
        // 그룹 멤버 여부 확인
        if (!groupMemberRepository.isUserMemberOfGroup(monthlyBook.getGroup().getId(), userId)) {
            throw new IllegalArgumentException("해당 그룹의 멤버가 아닙니다.");
        }
        
        Double averageRating = bookReviewRepository.findAverageRatingByMonthlyBookId(monthlyBookId);
        Long totalReviews = bookReviewRepository.countPublishedReviewsByMonthlyBookId(monthlyBookId);
        
        // 별점 분포 계산 (실제로는 더 복잡한 쿼리가 필요하지만 간단히 구현)
        Long[] ratingDistribution = new Long[5];
        for (int i = 0; i < 5; i++) {
            ratingDistribution[i] = 0L; // 임시값
        }
        
        return BookReviewDto.StatisticsResponse.builder()
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalReviews(totalReviews)
                .ratingDistribution(ratingDistribution)
                .build();
    }
    
    /**
     * Entity를 DTO로 변환
     */
    private BookReviewDto.Response convertToDto(BookReview review) {
        return BookReviewDto.Response.builder()
                .id(review.getId())
                .user(convertUserToDto(review.getUser()))
                .monthlyBook(convertMonthlyBookToDto(review.getMonthlyBook()))
                .rating(review.getRating())
                .title(review.getTitle())
                .content(review.getContent())
                .favoriteQuote(review.getFavoriteQuote())
                .recommendation(review.getRecommendation())
                .status(review.getStatus())
                .isPublic(review.getIsPublic())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
    
    private UserDto.Response convertUserToDto(User user) {
        return UserDto.Response.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    
    private MonthlyBookDto.Response convertMonthlyBookToDto(MonthlyBook monthlyBook) {
        return MonthlyBookDto.Response.builder()
                .id(monthlyBook.getId())
                .year(monthlyBook.getYear())
                .month(monthlyBook.getMonth())
                .yearMonth(monthlyBook.getYearMonth())
                .bookTitle(monthlyBook.getBookTitle())
                .bookAuthor(monthlyBook.getBookAuthor())
                .bookCoverImage(monthlyBook.getBookCoverImage())
                .bookPublisher(monthlyBook.getBookPublisher())
                .bookPublishedDate(monthlyBook.getBookPublishedDate())
                .bookDescription(monthlyBook.getBookDescription())
                .selectedBy(convertUserToDto(monthlyBook.getSelectedBy()))
                .selectionReason(monthlyBook.getSelectionReason())
                .startDate(monthlyBook.getStartDate())
                .endDate(monthlyBook.getEndDate())
                .status(monthlyBook.getStatus())
                .createdAt(monthlyBook.getCreatedAt())
                .build();
    }
}
