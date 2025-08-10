package com.readingclub.service;

import com.readingclub.dto.UserDto;
import com.readingclub.dto.UserProfileDto;
import com.readingclub.dto.CurrentlyReadingDto;
import com.readingclub.dto.PostDto;
import com.readingclub.dto.QuoteDto;
import com.readingclub.entity.User;
import com.readingclub.entity.Post;
import com.readingclub.entity.PostVisibility;
import com.readingclub.repository.BookRepository;
import com.readingclub.repository.UserRepository;
import com.readingclub.repository.PostRepository;
import com.readingclub.repository.CurrentlyReadingRepository;
import com.readingclub.repository.WishlistRepository;
import com.readingclub.repository.DroppedBookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PostRepository postRepository;
    private final CurrentlyReadingRepository currentlyReadingRepository;
    private final WishlistRepository wishlistRepository;
    private final DroppedBookRepository droppedBookRepository;
    
    /**
     * 사용자 ID로 조회
     */
    public UserDto.Response getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        return convertToDto(user);
    }
    
    /**
     * 카카오 ID로 사용자 조회
     */
    public UserDto.Response getUserByKakaoId(String kakaoId) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        return convertToDto(user);
    }
    
    /**
     * 사용자 생성
     */
    @Transactional
    public UserDto.Response createUser(UserDto.CreateRequest request) {
        // 카카오 ID 중복 확인
        if (userRepository.existsByKakaoId(request.getKakaoId())) {
            throw new IllegalArgumentException("이미 존재하는 카카오 사용자입니다.");
        }
        
        // 닉네임 중복 확인
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        
        User user = User.builder()
                .kakaoId(request.getKakaoId())
                .nickname(request.getNickname())
                .profileImage(request.getProfileImage())
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("새 사용자 생성: {}", savedUser.getKakaoId());
        
        return convertToDto(savedUser);
    }
    
    /**
     * 사용자 정보 수정
     */
    @Transactional
    public UserDto.Response updateUser(Long userId, UserDto.UpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 닉네임 변경 시 중복 확인
        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            user.setNickname(request.getNickname());
        }
        
        // 프로필 이미지 수정
        if (request.getProfileImage() != null) {
            user.setProfileImage(request.getProfileImage());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("사용자 정보 수정: {}", updatedUser.getId());
        
        return convertToDto(updatedUser);
    }
    
    /**
     * 사용자 독서 통계 조회
     */
    public UserDto.Statistics getUserStatistics(Long userId) {
        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        
        // 총 책 수
        long totalBooks = bookRepository.countByUserId(userId);
        
        // 평균 별점
        Double averageRating = bookRepository.findAverageRatingByUserId(userId);
        if (averageRating == null) {
            averageRating = 0.0;
        }
        
        // 이번 달 읽은 책 수
        LocalDate thisMonthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate thisMonthEnd = thisMonthStart.plusMonths(1).minusDays(1);
        long booksThisMonth = bookRepository.findByUserIdAndFinishedDateBetween(
                userId, thisMonthStart, thisMonthEnd).size();
        
        // 올해 읽은 책 수
        LocalDate thisYearStart = LocalDate.now().withDayOfYear(1);
        LocalDate thisYearEnd = LocalDate.now().withMonth(12).withDayOfMonth(31);
        long booksThisYear = bookRepository.findByUserIdAndFinishedDateBetween(
                userId, thisYearStart, thisYearEnd).size();
        
        // 읽다 만 책 통계
        long droppedBooksCount = droppedBookRepository.countByUserId(userId);
        long thisMonthDroppedBooks = droppedBookRepository.countByUserIdAndDroppedDateBetween(
                userId, thisMonthStart, thisMonthEnd);
        
        return UserDto.Statistics.builder()
                .totalBooks(totalBooks)
                .averageRating(Math.round(averageRating * 10) / 10.0) // 소수점 첫째자리까지
                .booksThisMonth(booksThisMonth)
                .booksThisYear(booksThisYear)
                .droppedBooksCount(droppedBooksCount)
                .thisMonthDroppedBooks(thisMonthDroppedBooks)
                .build();
    }
    
    /**
     * 카카오 ID 존재 여부 확인
     */
    public boolean existsByKakaoId(String kakaoId) {
        return userRepository.existsByKakaoId(kakaoId);
    }
    
    /**
     * 타사용자 프로필 조회
     */
    public UserProfileDto.Response getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 기본 사용자 정보
        UserProfileDto.UserStatistics statistics = getUserProfileStatistics(userId);
        
        // 현재 읽고 있는 책 정보
        List<CurrentlyReadingDto.Response> currentlyReading = getCurrentlyReadingBooks(userId);
        
        // 최근 공개 게시글 정보 (최대 5개)
        List<PostDto.Response> recentPublicPosts = getRecentPublicPosts(userId);
        
        return UserProfileDto.Response.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .statistics(statistics)
                .currentlyReading(currentlyReading)
                .recentPublicPosts(recentPublicPosts)
                .build();
    }
    
    /**
     * 타사용자 프로필 통계 조회
     */
    private UserProfileDto.UserStatistics getUserProfileStatistics(Long userId) {
        // 총 완독한 책 권수
        long totalBooks = bookRepository.countByUserId(userId);
        
        // 읽고 있는 책 권수
        long currentlyReadingCount = currentlyReadingRepository.countByUserId(userId);
        
        // 읽고 싶은 책 권수
        long wishlistCount = wishlistRepository.countByUserId(userId);
        
        // 읽다 만 책 권수
        long droppedBooksCount = droppedBookRepository.countByUserId(userId);
        
        // 총 게시글 개수
        long totalPosts = postRepository.countByUser(userRepository.findById(userId).orElse(null));
        
        // 이번 달 게시글 개수
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = LocalDateTime.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
        long thisMonthPosts = postRepository.countByUserAndCreatedAtBetween(
                userRepository.findById(userId).orElse(null), startOfMonth, endOfMonth);
        
        // 이번 달 완독한 책 권수
        LocalDate startOfMonthDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonthDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        long thisMonthBooks = bookRepository.countByUserIdAndFinishedDateBetween(userId, startOfMonthDate, endOfMonthDate);
        
        // 이번 달 읽다 만 책 권수
        long thisMonthDroppedBooks = droppedBookRepository.countByUserIdAndDroppedDateBetween(userId, startOfMonthDate, endOfMonthDate);
        
        return UserProfileDto.UserStatistics.builder()
                .totalBooks(totalBooks)
                .currentlyReadingCount(currentlyReadingCount)
                .wishlistCount(wishlistCount)
                .droppedBooksCount(droppedBooksCount)
                .totalPosts(totalPosts)
                .thisMonthPosts(thisMonthPosts)
                .thisMonthBooks(thisMonthBooks)
                .thisMonthDroppedBooks(thisMonthDroppedBooks)
                .build();
    }
    
    /**
     * 현재 읽고 있는 책 정보 조회
     */
    private List<CurrentlyReadingDto.Response> getCurrentlyReadingBooks(Long userId) {
        return currentlyReadingRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .limit(5) // 최대 5개만
                .map(this::convertToCurrentlyReadingDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 최근 공개 게시글 조회
     */
    private List<PostDto.Response> getRecentPublicPosts(Long userId) {
        return postRepository.findByUserAndVisibilityOrderByCreatedAtDesc(
                userRepository.findById(userId).orElse(null), PostVisibility.PUBLIC, PageRequest.of(0, 5))
                .stream()
                .map(this::convertToPostDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Entity를 DTO로 변환
     */
    private UserDto.Response convertToDto(User user) {
        return UserDto.Response.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    
    /**
     * CurrentlyReading을 DTO로 변환
     */
    private CurrentlyReadingDto.Response convertToCurrentlyReadingDto(com.readingclub.entity.CurrentlyReading currentlyReading) {
        return CurrentlyReadingDto.Response.builder()
                .id(currentlyReading.getId())
                .title(currentlyReading.getTitle())
                .author(currentlyReading.getAuthor())
                .coverImage(currentlyReading.getCoverImage())
                .publisher(currentlyReading.getPublisher())
                .publishedDate(currentlyReading.getPublishedDate())
                .description(currentlyReading.getDescription())
                .readingType(CurrentlyReadingDto.ReadingType.valueOf(currentlyReading.getReadingType().name()))
                .readingTypeDisplay(currentlyReading.getReadingType().getDisplayName())
                .dueDate(currentlyReading.getDueDate())
                .progressPercentage(currentlyReading.getProgressPercentage())
                .memo(currentlyReading.getMemo())
                .isOverdue(currentlyReading.isOverdue())
                .createdAt(currentlyReading.getCreatedAt())
                .updatedAt(currentlyReading.getUpdatedAt())
                .user(convertToDto(currentlyReading.getUser()))
                .build();
    }
    
    /**
     * Post를 DTO로 변환
     */
    private PostDto.Response convertToPostDto(Post post) {
        return PostDto.Response.builder()
                .id(post.getId())
                .userId(post.getUser().getId())
                .userName(post.getUser().getNickname())
                .userProfileImage(post.getUser().getProfileImage())
                .bookInfo(PostDto.BookInfo.builder()
                        .isbn(post.getBookIsbn())
                        .title(post.getBookTitle())
                        .author(post.getBookAuthor())
                        .publisher(post.getBookPublisher())
                        .cover(post.getBookCover())
                        .pubDate(post.getBookPubDate())
                        .description(post.getBookDescription())
                        .build())
                .postType(post.getPostType())
                .visibility(post.getVisibility())
                .title(post.getTitle())
                .content(post.getContent())
                .recommendationType(post.getRecommendationType())
                .reason(post.getReason())
                .quotes(null) // JSON 파싱은 복잡하므로 여기서는 생략
                .quote(post.getQuote())
                .pageNumber(post.getPageNumber())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
