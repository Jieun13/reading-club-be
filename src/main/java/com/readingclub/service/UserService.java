package com.readingclub.service;

import com.readingclub.dto.UserDto;
import com.readingclub.entity.User;
import com.readingclub.repository.BookRepository;
import com.readingclub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    
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
        
        return UserDto.Statistics.builder()
                .totalBooks(totalBooks)
                .averageRating(Math.round(averageRating * 10) / 10.0) // 소수점 첫째자리까지
                .booksThisMonth(booksThisMonth)
                .booksThisYear(booksThisYear)
                .build();
    }
    
    /**
     * 카카오 ID 존재 여부 확인
     */
    public boolean existsByKakaoId(String kakaoId) {
        return userRepository.existsByKakaoId(kakaoId);
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
}
