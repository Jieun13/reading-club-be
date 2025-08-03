package com.readingclub.service;

import com.readingclub.dto.BookDto;
import com.readingclub.dto.UserDto;
import com.readingclub.entity.Book;
import com.readingclub.entity.User;
import com.readingclub.repository.BookRepository;
import com.readingclub.repository.UserRepository;
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
}
