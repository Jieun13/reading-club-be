package com.readingclub.service;

import com.readingclub.dto.MonthlyBookDto;
import com.readingclub.dto.UserDto;
import com.readingclub.entity.*;
import com.readingclub.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MonthlyBookService {
    
    private final MonthlyBookRepository monthlyBookRepository;
    private final ReadingGroupRepository readingGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ReadingProgressRepository readingProgressRepository;
    private final UserRepository userRepository;
    
    /**
     * 월간 도서 선정
     */
    @Transactional
    public MonthlyBookDto.Response selectMonthlyBook(Long groupId, Long userId, MonthlyBookDto.CreateRequest request) {
        // 그룹 존재 확인
        ReadingGroup group = readingGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("독서 모임을 찾을 수 없습니다."));
        
        // 관리자 권한 확인
        if (!groupMemberRepository.isUserAdminOfGroup(groupId, userId)) {
            throw new IllegalArgumentException("월간 도서 선정 권한이 없습니다.");
        }
        
        // 해당 년월에 이미 선정된 도서가 있는지 확인
        if (monthlyBookRepository.existsByGroupIdAndYearAndMonth(groupId, request.getYear(), request.getMonth())) {
            throw new IllegalArgumentException("해당 년월에 이미 선정된 도서가 있습니다.");
        }
        
        User selectedBy = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        MonthlyBook monthlyBook = MonthlyBook.builder()
                .group(group)
                .year(request.getYear())
                .month(request.getMonth())
                .bookTitle(request.getBookTitle())
                .bookAuthor(request.getBookAuthor())
                .bookCoverImage(request.getBookCoverImage())
                .bookPublisher(request.getBookPublisher())
                .bookPublishedDate(request.getBookPublishedDate())
                .bookDescription(request.getBookDescription())
                .selectedBy(selectedBy)
                .selectionReason(request.getSelectionReason())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(MonthlyBook.BookStatus.UPCOMING)
                .build();
        
        MonthlyBook savedMonthlyBook = monthlyBookRepository.save(monthlyBook);
        
        // 모든 활성 멤버에 대해 독서 진행상황 생성
        List<GroupMember> activeMembers = groupMemberRepository.findByGroupIdAndStatusOrderByJoinedAtAsc(
                groupId, GroupMember.MemberStatus.ACTIVE);
        
        for (GroupMember member : activeMembers) {
            ReadingProgress progress = ReadingProgress.builder()
                    .monthlyBook(savedMonthlyBook)
                    .user(member.getUser())
                    .build();
            readingProgressRepository.save(progress);
        }
        
        log.info("월간 도서 선정: {} (그룹: {}, 선정자: {})", savedMonthlyBook.getBookTitle(), groupId, userId);
        
        return convertToDto(savedMonthlyBook);
    }
    
    /**
     * 그룹의 월간 도서 목록 조회
     */
    public List<MonthlyBookDto.Response> getGroupMonthlyBooks(Long groupId, Long userId) {
        // 그룹 멤버 여부 확인
        if (!groupMemberRepository.isUserMemberOfGroup(groupId, userId)) {
            throw new IllegalArgumentException("해당 그룹의 멤버가 아닙니다.");
        }
        
        List<MonthlyBook> monthlyBooks = monthlyBookRepository.findByGroupIdOrderByYearDescMonthDesc(groupId);
        
        return monthlyBooks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 현재 월간 도서 조회
     */
    public MonthlyBookDto.Response getCurrentMonthlyBook(Long groupId, Long userId) {
        // 그룹 멤버 여부 확인
        if (!groupMemberRepository.isUserMemberOfGroup(groupId, userId)) {
            throw new IllegalArgumentException("해당 그룹의 멤버가 아닙니다.");
        }
        
        LocalDate now = LocalDate.now();
        MonthlyBook monthlyBook = monthlyBookRepository.findCurrentMonthlyBook(
                groupId, now.getYear(), now.getMonthValue())
                .orElseThrow(() -> new IllegalArgumentException("현재 진행 중인 월간 도서가 없습니다."));
        
        return convertToDto(monthlyBook);
    }
    
    /**
     * 월간 도서 상태 업데이트
     */
    @Transactional
    public MonthlyBookDto.Response updateMonthlyBookStatus(Long monthlyBookId, Long userId, MonthlyBook.BookStatus status) {
        MonthlyBook monthlyBook = monthlyBookRepository.findById(monthlyBookId)
                .orElseThrow(() -> new IllegalArgumentException("월간 도서를 찾을 수 없습니다."));
        
        // 관리자 권한 확인
        if (!groupMemberRepository.isUserAdminOfGroup(monthlyBook.getGroup().getId(), userId)) {
            throw new IllegalArgumentException("월간 도서 상태 변경 권한이 없습니다.");
        }
        
        monthlyBook.setStatus(status);
        MonthlyBook updatedMonthlyBook = monthlyBookRepository.save(monthlyBook);
        
        log.info("월간 도서 상태 변경: {} -> {} (ID: {})", monthlyBook.getBookTitle(), status, monthlyBookId);
        
        return convertToDto(updatedMonthlyBook);
    }
    
    /**
     * Entity를 DTO로 변환
     */
    private MonthlyBookDto.Response convertToDto(MonthlyBook monthlyBook) {
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
}
