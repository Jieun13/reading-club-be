package com.readingclub.service;

import com.readingclub.dto.ApiResponse;
import com.readingclub.dto.CurrentlyReadingDto;
import com.readingclub.dto.UserDto;
import com.readingclub.entity.CurrentlyReading;
import com.readingclub.entity.User;
import com.readingclub.repository.CurrentlyReadingRepository;
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
@Transactional
public class CurrentlyReadingService {
    
    private final CurrentlyReadingRepository currentlyReadingRepository;
    private final UserRepository userRepository;
    
    /**
     * 사용자의 읽고 있는 책 목록 조회 (페이징)
     */
    public Page<CurrentlyReadingDto.Response> getUserCurrentlyReading(Long userId, Pageable pageable, String search) {
        Page<CurrentlyReading> currentlyReadingPage;
        
        if (search != null && !search.trim().isEmpty()) {
            currentlyReadingPage = currentlyReadingRepository.findByUserIdAndTitleOrAuthorContaining(userId, search.trim(), pageable);
        } else {
            currentlyReadingPage = currentlyReadingRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        
        return currentlyReadingPage.map(this::convertToResponse);
    }
    
    /**
     * 읽고 있는 책 상세 조회
     */
    public CurrentlyReadingDto.Response getCurrentlyReadingById(Long id, Long userId) {
        CurrentlyReading currentlyReading = currentlyReadingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("읽고 있는 책을 찾을 수 없습니다."));
        
        return convertToResponse(currentlyReading);
    }
    
    /**
     * 읽고 있는 책 추가
     */
    public CurrentlyReadingDto.Response createCurrentlyReading(Long userId, CurrentlyReadingDto.CreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 중복 체크
        if (currentlyReadingRepository.existsByUserIdAndTitleAndAuthor(userId, request.getTitle(), request.getAuthor())) {
            throw new IllegalArgumentException("이미 읽고 있는 책입니다.");
        }
        
        CurrentlyReading currentlyReading = CurrentlyReading.builder()
                .user(user)
                .title(request.getTitle())
                .author(request.getAuthor())
                .coverImage(request.getCoverImage())
                .publisher(request.getPublisher())
                .publishedDate(request.getPublishedDate())
                .description(request.getDescription())
                .readingType(CurrentlyReading.ReadingType.valueOf(request.getReadingType().name()))
                .dueDate(request.getDueDate())
                .progressPercentage(request.getProgressPercentage())
                .memo(request.getMemo())
                .build();
        
        // 진행률 설정
        currentlyReading.updateProgress(request.getProgressPercentage());
        
        CurrentlyReading saved = currentlyReadingRepository.save(currentlyReading);
        return convertToResponse(saved);
    }
    
    /**
     * 읽고 있는 책 수정
     */
    public CurrentlyReadingDto.Response updateCurrentlyReading(Long id, Long userId, CurrentlyReadingDto.UpdateRequest request) {
        CurrentlyReading currentlyReading = currentlyReadingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("읽고 있는 책을 찾을 수 없습니다."));
        
        currentlyReading.setTitle(request.getTitle());
        currentlyReading.setAuthor(request.getAuthor());
        currentlyReading.setCoverImage(request.getCoverImage());
        currentlyReading.setPublisher(request.getPublisher());
        currentlyReading.setPublishedDate(request.getPublishedDate());
        currentlyReading.setDescription(request.getDescription());
        currentlyReading.setReadingType(CurrentlyReading.ReadingType.valueOf(request.getReadingType().name()));
        currentlyReading.setDueDate(request.getDueDate());
        currentlyReading.setMemo(request.getMemo());
        
        // 진행률이 변경된 경우 업데이트
        if (request.getProgressPercentage() != null) {
            currentlyReading.updateProgress(request.getProgressPercentage());
        }
        
        CurrentlyReading saved = currentlyReadingRepository.save(currentlyReading);
        return convertToResponse(saved);
    }
    
    /**
     * 읽고 있는 책 진행률 업데이트
     */
    public CurrentlyReadingDto.Response updateProgress(Long id, Long userId, CurrentlyReadingDto.ProgressUpdateRequest request) {
        CurrentlyReading currentlyReading = currentlyReadingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("읽고 있는 책을 찾을 수 없습니다."));
        
        if (request.getProgressPercentage() != null) {
            currentlyReading.updateProgress(request.getProgressPercentage());
        }
        
        if (request.getMemo() != null) {
            currentlyReading.setMemo(request.getMemo());
        }
        
        CurrentlyReading saved = currentlyReadingRepository.save(currentlyReading);
        return convertToResponse(saved);
    }
    
    /**
     * 읽고 있는 책 삭제
     */
    public void deleteCurrentlyReading(Long id, Long userId) {
        CurrentlyReading currentlyReading = currentlyReadingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("읽고 있는 책을 찾을 수 없습니다."));
        
        currentlyReadingRepository.delete(currentlyReading);
    }
    
    /**
     * 중복 체크
     */
    public CurrentlyReadingDto.DuplicateCheckResponse checkDuplicate(Long userId, String title, String author) {
        boolean duplicate = currentlyReadingRepository.existsByUserIdAndTitleAndAuthor(userId, title, author);
        
        List<CurrentlyReadingDto.DuplicateCheckResponse.DuplicateBook> duplicateBooks = null;
        if (duplicate) {
            duplicateBooks = currentlyReadingRepository.findByUserIdAndTitleOrAuthorContaining(userId, title, Pageable.unpaged())
                    .getContent()
                    .stream()
                    .filter(book -> book.getTitle().equals(title) && 
                            (author == null || book.getAuthor() == null || book.getAuthor().equals(author)))
                    .map(this::convertToDuplicateBook)
                    .collect(Collectors.toList());
        }
        
        return CurrentlyReadingDto.DuplicateCheckResponse.builder()
                .duplicate(duplicate)
                .duplicateBooks(duplicateBooks)
                .build();
    }
    
    /**
     * 연체된 책 목록 조회
     */
    public List<CurrentlyReadingDto.Response> getOverdueBooks(Long userId) {
        List<CurrentlyReading> overdueBooks = currentlyReadingRepository.findOverdueBooks(userId, LocalDate.now());
        return overdueBooks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 엔티티를 Response DTO로 변환
     */
    private CurrentlyReadingDto.Response convertToResponse(CurrentlyReading currentlyReading) {
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
                .user(UserDto.Response.builder()
                        .id(currentlyReading.getUser().getId())
                        .kakaoId(currentlyReading.getUser().getKakaoId())
                        .nickname(currentlyReading.getUser().getNickname())
                        .profileImage(currentlyReading.getUser().getProfileImage())
                        .createdAt(currentlyReading.getUser().getCreatedAt())
                        .updatedAt(currentlyReading.getUser().getUpdatedAt())
                        .build())
                .build();
    }
    
    /**
     * 엔티티를 DuplicateBook DTO로 변환
     */
    private CurrentlyReadingDto.DuplicateCheckResponse.DuplicateBook convertToDuplicateBook(CurrentlyReading currentlyReading) {
        return CurrentlyReadingDto.DuplicateCheckResponse.DuplicateBook.builder()
                .id(currentlyReading.getId())
                .title(currentlyReading.getTitle())
                .author(currentlyReading.getAuthor())
                .readingType(CurrentlyReadingDto.ReadingType.valueOf(currentlyReading.getReadingType().name()))
                .progressPercentage(currentlyReading.getProgressPercentage())
                .createdAt(currentlyReading.getCreatedAt())
                .build();
    }
} 