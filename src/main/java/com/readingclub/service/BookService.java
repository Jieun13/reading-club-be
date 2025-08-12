package com.readingclub.service;

import com.readingclub.dto.BookDto;
import com.readingclub.dto.CurrentlyReadingDto;
import com.readingclub.dto.DroppedBookDto;
import com.readingclub.dto.WishlistDto;
import com.readingclub.dto.UserDto;
import com.readingclub.entity.Book;
import com.readingclub.entity.CurrentlyReading;
import com.readingclub.entity.DroppedBook;
import com.readingclub.entity.User;
import com.readingclub.entity.Wishlist;
import com.readingclub.repository.BookRepository;
import com.readingclub.repository.CurrentlyReadingRepository;
import com.readingclub.repository.DroppedBookRepository;
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
public class BookService {
    
    private final BookRepository bookRepository;
    private final CurrentlyReadingRepository currentlyReadingRepository;
    private final DroppedBookRepository droppedBookRepository;
    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    
    /**
     * 사용자별 책 목록 조회 (페이징 및 필터링)
     */
    public Page<BookDto.Response> getUserBooks(Long userId, Pageable pageable, 
                                             Integer year, Integer month, Integer rating, String search) {
        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        
        // TODO: 필터링 로직 구현 (현재는 기본 페이징만)
        Page<Book> books = bookRepository.findByUserIdOrderByFinishedDateDesc(userId, pageable);
        
        return books.map(this::convertToDto);
    }
    
    /**
     * 사용자별 책 목록 조회 (완독한 책 + 읽고 있는 책)
     */
    public BookDto.CombinedBookResponse getUserBooksWithCurrentlyReading(Long userId, Pageable pageable, 
                                                                       Integer year, Integer month, Integer rating, String search) {
        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        
        // 완독한 책 조회
        Page<Book> books = bookRepository.findByUserIdOrderByFinishedDateDesc(userId, pageable);
        Page<BookDto.Response> bookResponses = books.map(this::convertToDto);
        
        // 읽고 있는 책 조회 (페이징 없이 전체)
        List<CurrentlyReading> currentlyReading = currentlyReadingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<CurrentlyReadingDto.Response> currentlyReadingResponses = currentlyReading.stream()
                .map(this::convertCurrentlyReadingToDto)
                .collect(Collectors.toList());
        
        return BookDto.CombinedBookResponse.builder()
                .books(bookResponses)
                .currentlyReading(currentlyReadingResponses)
                .totalCurrentlyReading(currentlyReadingResponses.size())
                .build();
    }
    

    
    /**
     * 책 상세 조회
     */
    public BookDto.Response getBookById(Long bookId, Long userId) {
        Book book = bookRepository.findByIdAndUserId(bookId, userId)
                .orElseThrow(() -> new IllegalArgumentException("책을 찾을 수 없거나 접근 권한이 없습니다."));
        
        return convertToDto(book);
    }
    
    /**
     * 책 등록
     */
    @Transactional
    public BookDto.Response createBook(Long userId, BookDto.CreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        Book book = Book.builder()
                .user(user)
                .title(request.getTitle())
                .author(request.getAuthor())
                .coverImage(request.getCoverImage())
                .rating(request.getRating())
                .review(request.getReview())
                .finishedDate(request.getFinishedDate())
                .build();
        
        Book savedBook = bookRepository.save(book);
        log.info("새 책 등록: {} (사용자: {})", savedBook.getTitle(), userId);
        
        return convertToDto(savedBook);
    }
    
    /**
     * 책 수정
     */
    @Transactional
    public BookDto.Response updateBook(Long bookId, Long userId, BookDto.UpdateRequest request) {
        Book book = bookRepository.findByIdAndUserId(bookId, userId)
                .orElseThrow(() -> new IllegalArgumentException("책을 찾을 수 없거나 접근 권한이 없습니다."));
        
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setCoverImage(request.getCoverImage());
        book.setRating(request.getRating());
        book.setReview(request.getReview());
        book.setFinishedDate(request.getFinishedDate());
        
        Book updatedBook = bookRepository.save(book);
        log.info("책 정보 수정: {} (ID: {})", updatedBook.getTitle(), bookId);
        
        return convertToDto(updatedBook);
    }
    
    /**
     * 책 삭제
     */
    @Transactional
    public void deleteBook(Long bookId, Long userId) {
        Book book = bookRepository.findByIdAndUserId(bookId, userId)
                .orElseThrow(() -> new IllegalArgumentException("책을 찾을 수 없거나 접근 권한이 없습니다."));
        
        bookRepository.delete(book);
        log.info("책 삭제: {} (ID: {})", book.getTitle(), bookId);
    }
    
    /**
     * 월별 독서 통계 조회
     */
    public List<BookDto.MonthlyStats> getMonthlyStatistics(Long userId) {
        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        
        List<Object[]> results = bookRepository.findMonthlyStatsByUserId(userId);
        
        return results.stream()
                .map(result -> BookDto.MonthlyStats.builder()
                        .year((Integer) result[0])
                        .month((Integer) result[1])
                        .count((Long) result[2])
                        .averageRating(((Double) result[3]).doubleValue())
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * 책 중복 체크
     */
    public BookDto.DuplicateCheckResponse checkDuplicate(Long userId, String title, String author) {
        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        
        List<Book> duplicateBooks;
        
        if (author != null && !author.trim().isEmpty()) {
            // 제목과 저자로 검색
            duplicateBooks = bookRepository.findByUserIdAndTitleContainingIgnoreCaseAndAuthorContainingIgnoreCase(
                    userId, title.trim(), author.trim());
        } else {
            // 제목만으로 검색
            duplicateBooks = bookRepository.findByUserIdAndTitleContainingIgnoreCase(userId, title.trim());
        }
        
        List<BookDto.DuplicateCheckResponse.DuplicateBook> duplicateBookDtos = duplicateBooks.stream()
                .map(book -> BookDto.DuplicateCheckResponse.DuplicateBook.builder()
                        .id(book.getId())
                        .title(book.getTitle())
                        .author(book.getAuthor())
                        .rating(book.getRating())
                        .finishedDate(book.getFinishedDate())
                        .createdAt(book.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        
        return BookDto.DuplicateCheckResponse.builder()
                .duplicate(!duplicateBooks.isEmpty()) // isDuplicate -> duplicate로 수정
                .duplicateBooks(duplicateBookDtos)
                .build();
    }
    
    /**
     * Entity를 DTO로 변환
     */
    private BookDto.Response convertToDto(Book book) {
        UserDto.Response userDto = UserDto.Response.builder()
                .id(book.getUser().getId())
                .kakaoId(book.getUser().getKakaoId())
                .nickname(book.getUser().getNickname())
                .profileImage(book.getUser().getProfileImage())
                .createdAt(book.getUser().getCreatedAt())
                .updatedAt(book.getUser().getUpdatedAt())
                .build();
        
        return BookDto.Response.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .coverImage(book.getCoverImage())
                .rating(book.getRating())
                .review(book.getReview())
                .finishedDate(book.getFinishedDate())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .user(userDto)
                .build();
    }
    
    /**
     * CurrentlyReading Entity를 DTO로 변환
     */
    private CurrentlyReadingDto.Response convertCurrentlyReadingToDto(CurrentlyReading currentlyReading) {
        UserDto.Response userDto = UserDto.Response.builder()
                .id(currentlyReading.getUser().getId())
                .kakaoId(currentlyReading.getUser().getKakaoId())
                .nickname(currentlyReading.getUser().getNickname())
                .profileImage(currentlyReading.getUser().getProfileImage())
                .createdAt(currentlyReading.getUser().getCreatedAt())
                .updatedAt(currentlyReading.getUser().getUpdatedAt())
                .build();
        
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
                .user(userDto)
                .build();
    }
    
    /**
     * 사용자별 모든 책 상태 조회 (완독 + 읽고 있는 책 + 읽다 만 책 + 읽고 싶은 책)
     */
    public BookDto.AllBooksResponse getAllUserBooks(Long userId, Pageable pageable, 
                                                  Integer year, Integer month, Integer rating, String search) {
        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        
        // 완독한 책 조회 (페이징)
        Page<Book> finishedBooks = bookRepository.findByUserIdOrderByFinishedDateDesc(userId, pageable);
        Page<BookDto.Response> finishedBookResponses = finishedBooks.map(this::convertToDto);
        
        // 읽고 있는 책 조회 (페이징 없이 전체)
        List<CurrentlyReading> currentlyReading = currentlyReadingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<CurrentlyReadingDto.Response> currentlyReadingResponses = currentlyReading.stream()
                .map(this::convertCurrentlyReadingToDto)
                .collect(Collectors.toList());
        
        // 읽다 만 책 조회 (페이징 없이 전체)
        List<DroppedBook> droppedBooks = droppedBookRepository.findByUserIdOrderByDroppedDateDesc(userId);
        List<DroppedBookDto.Response> droppedBookResponses = droppedBooks.stream()
                .map(this::convertDroppedBookToDto)
                .collect(Collectors.toList());
        
        // 읽고 싶은 책 조회 (페이징 없이 전체)
        List<Wishlist> wishlistBooks = wishlistRepository.findByUserIdOrderByPriorityAscCreatedAtDesc(userId);
        List<WishlistDto.Response> wishlistBookResponses = wishlistBooks.stream()
                .map(this::convertWishlistToDto)
                .collect(Collectors.toList());
        
        return BookDto.AllBooksResponse.builder()
                .finishedBooks(finishedBookResponses)
                .currentlyReading(currentlyReadingResponses)
                .droppedBooks(droppedBookResponses)
                .wishlistBooks(wishlistBookResponses)
                .totalFinishedBooks((int) finishedBooks.getTotalElements())
                .totalCurrentlyReading(currentlyReadingResponses.size())
                .totalDroppedBooks(droppedBookResponses.size())
                .totalWishlistBooks(wishlistBookResponses.size())
                .build();
    }
    
    /**
     * DroppedBook Entity를 DTO로 변환
     */
    private DroppedBookDto.Response convertDroppedBookToDto(DroppedBook droppedBook) {
        UserDto.Response userDto = UserDto.Response.builder()
                .id(droppedBook.getUser().getId())
                .kakaoId(droppedBook.getUser().getKakaoId())
                .nickname(droppedBook.getUser().getNickname())
                .profileImage(droppedBook.getUser().getProfileImage())
                .createdAt(droppedBook.getUser().getCreatedAt())
                .updatedAt(droppedBook.getUser().getUpdatedAt())
                .build();
        
        return DroppedBookDto.Response.builder()
                .id(droppedBook.getId())
                .title(droppedBook.getTitle())
                .author(droppedBook.getAuthor())
                .isbn(droppedBook.getIsbn())
                .coverImage(droppedBook.getCoverImage())
                .publisher(droppedBook.getPublisher())
                .publishedDate(droppedBook.getPublishedDate())
                .description(droppedBook.getDescription())
                .readingType(droppedBook.getReadingType())
                .readingTypeDisplay(droppedBook.getReadingType().getDisplayName())
                .progressPercentage(droppedBook.getProgressPercentage())
                .dropReason(droppedBook.getDropReason())
                .startedDate(droppedBook.getStartedDate())
                .droppedDate(droppedBook.getDroppedDate())
                .memo(droppedBook.getMemo())
                .createdAt(droppedBook.getCreatedAt())
                .updatedAt(droppedBook.getUpdatedAt())
                .user(userDto)
                .build();
    }
    
    /**
     * Wishlist Entity를 DTO로 변환
     */
    private WishlistDto.Response convertWishlistToDto(Wishlist wishlist) {
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
                .priority(wishlist.getPriority())
                .memo(wishlist.getMemo())
                .createdAt(wishlist.getCreatedAt())
                .updatedAt(wishlist.getUpdatedAt())
                .user(userDto)
                .build();
    }
    
    /**
     * 이번 달에 등록한 책들의 표지 URL 목록 조회
     */
    public BookDto.MonthlyBookCoversResponse getMonthlyBookCovers(Long userId) {
        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        
        // 현재 년월 계산
        java.time.YearMonth currentYearMonth = java.time.YearMonth.now();
        int currentYear = currentYearMonth.getYear();
        int currentMonth = currentYearMonth.getMonthValue();
        
        // 이번 달에 등록한 완독한 책들의 표지 URL
        List<String> completedBookCovers = bookRepository
                .findByUserIdAndYearAndMonth(userId, currentYear, currentMonth)
                .stream()
                .map(Book::getCoverImage)
                .filter(coverImage -> coverImage != null && !coverImage.trim().isEmpty())
                .collect(Collectors.toList());
        
        // 이번 달에 등록한 읽고 있는 책들의 표지 URL
        List<String> currentlyReadingCovers = currentlyReadingRepository
                .findByUserIdAndCreatedAtYearAndCreatedAtMonth(userId, currentYear, currentMonth)
                .stream()
                .map(CurrentlyReading::getCoverImage)
                .filter(coverImage -> coverImage != null && !coverImage.trim().isEmpty())
                .collect(Collectors.toList());
        
        // 이번 달에 등록한 읽다 만 책들의 표지 URL
        List<String> droppedBookCovers = droppedBookRepository
                .findByUserIdAndYearAndMonth(userId, currentYear, currentMonth)
                .stream()
                .map(DroppedBook::getCoverImage)
                .filter(coverImage -> coverImage != null && !coverImage.trim().isEmpty())
                .collect(Collectors.toList());
        
        // 이번 달에 등록한 위시리스트 책들의 표지 URL
        List<String> wishlistCovers = wishlistRepository
                .findByUserIdAndCreatedAtYearAndCreatedAtMonth(userId, currentYear, currentMonth)
                .stream()
                .map(Wishlist::getCoverImage)
                .filter(coverImage -> coverImage != null && !coverImage.trim().isEmpty())
                .collect(Collectors.toList());
        
        int totalCount = completedBookCovers.size() + currentlyReadingCovers.size() + 
                        droppedBookCovers.size() + wishlistCovers.size();
        
        return BookDto.MonthlyBookCoversResponse.builder()
                .completedBookCovers(completedBookCovers)
                .currentlyReadingCovers(currentlyReadingCovers)
                .droppedBookCovers(droppedBookCovers)
                .wishlistCovers(wishlistCovers)
                .totalCount(totalCount)
                .build();
    }
    
}
