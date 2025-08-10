package com.readingclub.service;

import com.readingclub.dto.DroppedBookDto;
import com.readingclub.dto.UserDto;
import com.readingclub.entity.DroppedBook;
import com.readingclub.entity.User;
import com.readingclub.repository.DroppedBookRepository;
import com.readingclub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DroppedBookService {
    
    private final DroppedBookRepository droppedBookRepository;
    private final UserRepository userRepository;
    
    /**
     * 읽다 만 책 목록 조회 (페이징)
     */
    public Page<DroppedBookDto.Response> getDroppedBooks(Long userId, Pageable pageable, String search) {
        Page<DroppedBook> droppedBooks;
        
        if (search != null && !search.trim().isEmpty()) {
            // 검색어가 있는 경우 제목이나 저자로 검색
            droppedBooks = droppedBookRepository.findByUserIdAndTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
                    userId, search.trim(), search.trim(), pageable);
        } else {
            // 검색어가 없는 경우 전체 조회
            droppedBooks = droppedBookRepository.findByUserIdOrderByDroppedDateDesc(userId, pageable);
        }
        
        return droppedBooks.map(this::convertToDto);
    }
    
    /**
     * 읽다 만 책 목록 조회 (전체)
     */
    public List<DroppedBookDto.Response> getDroppedBooks(Long userId) {
        List<DroppedBook> droppedBooks = droppedBookRepository.findByUserIdOrderByDroppedDateDesc(userId);
        return droppedBooks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 읽다 만 책 상세 조회
     */
    public DroppedBookDto.Response getDroppedBook(Long droppedBookId, Long userId) {
        DroppedBook droppedBook = droppedBookRepository.findByIdAndUserId(droppedBookId, userId)
                .orElseThrow(() -> new IllegalArgumentException("읽다 만 책을 찾을 수 없습니다."));
        return convertToDto(droppedBook);
    }
    
    /**
     * 읽다 만 책 추가
     */
    @Transactional
    public DroppedBookDto.Response createDroppedBook(Long userId, DroppedBookDto.CreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 중복 체크
        checkDuplicateBook(userId, request);
        
        // 하차 날짜가 없으면 오늘 날짜로 설정
        LocalDate droppedDate = request.getDroppedDate() != null ? request.getDroppedDate() : LocalDate.now();
        
        DroppedBook droppedBook = DroppedBook.builder()
                .user(user)
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .coverImage(request.getCoverImage())
                .publisher(request.getPublisher())
                .publishedDate(request.getPublishedDate())
                .description(request.getDescription())
                .readingType(request.getReadingType())
                .progressPercentage(request.getProgressPercentage())
                .dropReason(request.getDropReason())
                .startedDate(request.getStartedDate())
                .droppedDate(droppedDate)
                .memo(request.getMemo())
                .build();
        
        DroppedBook savedDroppedBook = droppedBookRepository.save(droppedBook);
        return convertToDto(savedDroppedBook);
    }
    
    /**
     * 읽다 만 책 수정
     */
    @Transactional
    public DroppedBookDto.Response updateDroppedBook(Long droppedBookId, Long userId, DroppedBookDto.UpdateRequest request) {
        DroppedBook droppedBook = droppedBookRepository.findByIdAndUserId(droppedBookId, userId)
                .orElseThrow(() -> new IllegalArgumentException("읽다 만 책을 찾을 수 없습니다."));
        
        // 수정 시 중복 체크 (자기 자신은 제외)
        if (request.getTitle() != null || request.getAuthor() != null || request.getIsbn() != null) {
            checkDuplicateBookForUpdate(userId, droppedBookId, request);
        }
        
        // 수정 가능한 필드들만 업데이트
        if (request.getTitle() != null) {
            droppedBook.setTitle(request.getTitle());
        }
        if (request.getAuthor() != null) {
            droppedBook.setAuthor(request.getAuthor());
        }
        if (request.getIsbn() != null) {
            droppedBook.setIsbn(request.getIsbn());
        }
        if (request.getCoverImage() != null) {
            droppedBook.setCoverImage(request.getCoverImage());
        }
        if (request.getPublisher() != null) {
            droppedBook.setPublisher(request.getPublisher());
        }
        if (request.getPublishedDate() != null) {
            droppedBook.setPublishedDate(request.getPublishedDate());
        }
        if (request.getDescription() != null) {
            droppedBook.setDescription(request.getDescription());
        }
        if (request.getReadingType() != null) {
            droppedBook.setReadingType(request.getReadingType());
        }
        if (request.getProgressPercentage() != null) {
            droppedBook.setProgressPercentage(request.getProgressPercentage());
        }
        if (request.getDropReason() != null) {
            droppedBook.setDropReason(request.getDropReason());
        }
        if (request.getStartedDate() != null) {
            droppedBook.setStartedDate(request.getStartedDate());
        }
        if (request.getDroppedDate() != null) {
            droppedBook.setDroppedDate(request.getDroppedDate());
        }
        if (request.getMemo() != null) {
            droppedBook.setMemo(request.getMemo());
        }
        
        return convertToDto(droppedBook);
    }
    
    /**
     * 읽다 만 책 삭제
     */
    @Transactional
    public void deleteDroppedBook(Long droppedBookId, Long userId) {
        DroppedBook droppedBook = droppedBookRepository.findByIdAndUserId(droppedBookId, userId)
                .orElseThrow(() -> new IllegalArgumentException("읽다 만 책을 찾을 수 없습니다."));
        
        droppedBookRepository.delete(droppedBook);
    }
    
    /**
     * 사용자별 읽다 만 책 개수 조회
     */
    public long countByUserId(Long userId) {
        return droppedBookRepository.countByUserId(userId);
    }
    
    /**
     * 사용자별 특정 기간 읽다 만 책 개수 조회
     */
    public long countByUserIdAndDroppedDateBetween(Long userId, LocalDate startDate, LocalDate endDate) {
        return droppedBookRepository.countByUserIdAndDroppedDateBetween(userId, startDate, endDate);
    }
    
    /**
     * 읽다 만 책 중복 체크
     */
    public DroppedBookDto.DuplicateCheckResponse checkDuplicate(Long userId, String title, String author) {
        DroppedBookDto.DuplicateCheckResponse.DuplicateCheckResponseBuilder responseBuilder = 
            DroppedBookDto.DuplicateCheckResponse.builder();
        
        if (title != null && !title.trim().isEmpty()) {
            // 제목으로 중복 체크
            if (droppedBookRepository.existsByUserIdAndTitle(userId, title.trim())) {
                // 중복된 책을 찾아서 반환
                List<DroppedBook> duplicates = droppedBookRepository.findByUserIdAndTitleContainingIgnoreCase(userId, title.trim());
                if (!duplicates.isEmpty()) {
                    return responseBuilder
                        .isDuplicate(true)
                        .existingBook(convertToDto(duplicates.get(0)))
                        .message("이미 읽다 만 책으로 등록된 제목입니다: " + title)
                        .build();
                }
            }
            
            // 제목과 저자가 모두 있는 경우 제목+저자로 중복 체크
            if (author != null && !author.trim().isEmpty()) {
                if (droppedBookRepository.existsByUserIdAndTitleAndAuthor(userId, title.trim(), author.trim())) {
                    // 중복된 책을 찾아서 반환
                    List<DroppedBook> duplicates = droppedBookRepository.findByUserIdAndTitleContainingIgnoreCase(userId, title.trim());
                    DroppedBook duplicateBook = duplicates.stream()
                        .filter(book -> book.getAuthor() != null && book.getAuthor().equalsIgnoreCase(author.trim()))
                        .findFirst()
                        .orElse(duplicates.get(0));
                    
                    return responseBuilder
                        .isDuplicate(true)
                        .existingBook(convertToDto(duplicateBook))
                        .message("이미 읽다 만 책으로 등록된 책입니다: " + title + " - " + author)
                        .build();
                }
            }
        }
        
        // 중복이 없는 경우
        return responseBuilder
            .isDuplicate(false)
            .existingBook(null)
            .message("중복된 책이 없습니다.")
            .build();
    }
    
    /**
     * Entity를 DTO로 변환
     */
    private DroppedBookDto.Response convertToDto(DroppedBook droppedBook) {
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
                .user(convertToUserDto(droppedBook.getUser()))
                .build();
    }
    
    /**
     * User를 UserDto로 변환
     */
    private UserDto.Response convertToUserDto(User user) {
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
     * 중복 책 체크 (새로 추가할 때)
     */
    private void checkDuplicateBook(Long userId, DroppedBookDto.CreateRequest request) {
        // ISBN이 있는 경우 ISBN으로 중복 체크
        if (request.getIsbn() != null && !request.getIsbn().trim().isEmpty()) {
            if (droppedBookRepository.existsByUserIdAndIsbn(userId, request.getIsbn().trim())) {
                throw new IllegalArgumentException("이미 읽다 만 책으로 등록된 ISBN입니다: " + request.getIsbn());
            }
        }
        
        // 제목과 저자가 모두 있는 경우 제목+저자로 중복 체크
        if (request.getTitle() != null && request.getAuthor() != null && 
            !request.getTitle().trim().isEmpty() && !request.getAuthor().trim().isEmpty()) {
            if (droppedBookRepository.existsByUserIdAndTitleAndAuthor(userId, request.getTitle().trim(), request.getAuthor().trim())) {
                throw new IllegalArgumentException("이미 읽다 만 책으로 등록된 책입니다: " + request.getTitle() + " - " + request.getAuthor());
            }
        }
        
        // 제목만 있는 경우 제목으로 중복 체크
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            if (droppedBookRepository.existsByUserIdAndTitle(userId, request.getTitle().trim())) {
                throw new IllegalArgumentException("이미 읽다 만 책으로 등록된 제목입니다: " + request.getTitle());
            }
        }
    }
    
    /**
     * 중복 책 체크 (수정할 때)
     */
    private void checkDuplicateBookForUpdate(Long userId, Long droppedBookId, DroppedBookDto.UpdateRequest request) {
        // ISBN이 있는 경우 ISBN으로 중복 체크 (자기 자신 제외)
        if (request.getIsbn() != null && !request.getIsbn().trim().isEmpty()) {
            // ISBN이 변경된 경우에만 중복 체크
            DroppedBook currentBook = droppedBookRepository.findById(droppedBookId).orElse(null);
            if (currentBook != null && !request.getIsbn().trim().equals(currentBook.getIsbn())) {
                if (droppedBookRepository.existsByUserIdAndIsbn(userId, request.getIsbn().trim())) {
                    throw new IllegalArgumentException("이미 읽다 만 책으로 등록된 ISBN입니다: " + request.getIsbn());
                }
            }
        }
        
        // 제목과 저자가 모두 있는 경우 제목+저자로 중복 체크 (자기 자신 제외)
        if (request.getTitle() != null && request.getAuthor() != null && 
            !request.getTitle().trim().isEmpty() && !request.getAuthor().trim().isEmpty()) {
            DroppedBook currentBook = droppedBookRepository.findById(droppedBookId).orElse(null);
            if (currentBook != null && 
                (!request.getTitle().trim().equals(currentBook.getTitle()) || 
                 !request.getAuthor().trim().equals(currentBook.getAuthor()))) {
                if (droppedBookRepository.existsByUserIdAndTitleAndAuthor(userId, request.getTitle().trim(), request.getAuthor().trim())) {
                    throw new IllegalArgumentException("이미 읽다 만 책으로 등록된 책입니다: " + request.getTitle() + " - " + request.getAuthor());
                }
            }
        }
        
        // 제목만 있는 경우 제목으로 중복 체크 (자기 자신 제외)
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            DroppedBook currentBook = droppedBookRepository.findById(droppedBookId).orElse(null);
            if (currentBook != null && !request.getTitle().trim().equals(currentBook.getTitle())) {
                if (droppedBookRepository.existsByUserIdAndTitle(userId, request.getTitle().trim())) {
                    throw new IllegalArgumentException("이미 읽다 만 책으로 등록된 제목입니다: " + request.getTitle());
                }
            }
        }
    }
}
