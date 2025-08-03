package com.readingclub.service;

import com.readingclub.dto.GroupMeetingDto;
import com.readingclub.dto.MonthlyBookDto;
import com.readingclub.dto.UserDto;
import com.readingclub.entity.*;
import com.readingclub.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GroupMeetingService {
    
    private final GroupMeetingRepository groupMeetingRepository;
    private final ReadingGroupRepository readingGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MonthlyBookRepository monthlyBookRepository;
    private final UserRepository userRepository;
    
    /**
     * 모임 일정 생성
     */
    @Transactional
    public GroupMeetingDto.Response createMeeting(Long groupId, Long userId, GroupMeetingDto.CreateRequest request) {
        // 그룹 존재 확인
        ReadingGroup group = readingGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("독서 모임을 찾을 수 없습니다."));
        
        // 관리자 권한 확인
        if (!groupMemberRepository.isUserAdminOfGroup(groupId, userId)) {
            throw new IllegalArgumentException("모임 일정 생성 권한이 없습니다.");
        }
        
        User createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        GroupMeeting.GroupMeetingBuilder meetingBuilder = GroupMeeting.builder()
                .group(group)
                .title(request.getTitle())
                .description(request.getDescription())
                .meetingDateTime(request.getMeetingDateTime())
                .location(request.getLocation())
                .agenda(request.getAgenda())
                .createdBy(createdBy);
        
        // 월간 도서 연결 (선택사항)
        if (request.getMonthlyBookId() != null) {
            MonthlyBook monthlyBook = monthlyBookRepository.findById(request.getMonthlyBookId())
                    .orElseThrow(() -> new IllegalArgumentException("월간 도서를 찾을 수 없습니다."));
            meetingBuilder.monthlyBook(monthlyBook);
        }
        
        GroupMeeting meeting = meetingBuilder.build();
        GroupMeeting savedMeeting = groupMeetingRepository.save(meeting);
        
        log.info("모임 일정 생성: {} (그룹: {}, 생성자: {})", savedMeeting.getTitle(), groupId, userId);
        
        return convertToDto(savedMeeting);
    }
    
    /**
     * 그룹의 모임 일정 목록 조회
     */
    public List<GroupMeetingDto.Response> getGroupMeetings(Long groupId, Long userId) {
        // 그룹 멤버 여부 확인
        if (!groupMemberRepository.isUserMemberOfGroup(groupId, userId)) {
            throw new IllegalArgumentException("해당 그룹의 멤버가 아닙니다.");
        }
        
        List<GroupMeeting> meetings = groupMeetingRepository.findByGroupIdOrderByMeetingDateTimeDesc(groupId);
        
        return meetings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 예정된 모임 목록 조회
     */
    public List<GroupMeetingDto.SimpleResponse> getUpcomingMeetings(Long groupId, Long userId) {
        // 그룹 멤버 여부 확인
        if (!groupMemberRepository.isUserMemberOfGroup(groupId, userId)) {
            throw new IllegalArgumentException("해당 그룹의 멤버가 아닙니다.");
        }
        
        List<GroupMeeting> meetings = groupMeetingRepository.findUpcomingMeetings(groupId, LocalDateTime.now());
        
        return meetings.stream()
                .map(this::convertToSimpleDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 다음 모임 조회
     */
    public GroupMeetingDto.SimpleResponse getNextMeeting(Long groupId, Long userId) {
        // 그룹 멤버 여부 확인
        if (!groupMemberRepository.isUserMemberOfGroup(groupId, userId)) {
            throw new IllegalArgumentException("해당 그룹의 멤버가 아닙니다.");
        }
        
        return groupMeetingRepository.findNextMeeting(groupId, LocalDateTime.now())
                .map(this::convertToSimpleDto)
                .orElse(null);
    }
    
    /**
     * 모임 일정 수정
     */
    @Transactional
    public GroupMeetingDto.Response updateMeeting(Long meetingId, Long userId, GroupMeetingDto.UpdateRequest request) {
        GroupMeeting meeting = groupMeetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("모임 일정을 찾을 수 없습니다."));
        
        // 관리자 권한 확인
        if (!groupMemberRepository.isUserAdminOfGroup(meeting.getGroup().getId(), userId)) {
            throw new IllegalArgumentException("모임 일정 수정 권한이 없습니다.");
        }
        
        meeting.setTitle(request.getTitle());
        meeting.setDescription(request.getDescription());
        meeting.setMeetingDateTime(request.getMeetingDateTime());
        meeting.setLocation(request.getLocation());
        meeting.setAgenda(request.getAgenda());
        
        if (request.getStatus() != null) {
            meeting.setStatus(request.getStatus());
        }
        
        GroupMeeting updatedMeeting = groupMeetingRepository.save(meeting);
        
        log.info("모임 일정 수정: {} (ID: {})", updatedMeeting.getTitle(), meetingId);
        
        return convertToDto(updatedMeeting);
    }
    
    /**
     * 모임 일정 삭제
     */
    @Transactional
    public void deleteMeeting(Long meetingId, Long userId) {
        GroupMeeting meeting = groupMeetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("모임 일정을 찾을 수 없습니다."));
        
        // 관리자 권한 확인
        if (!groupMemberRepository.isUserAdminOfGroup(meeting.getGroup().getId(), userId)) {
            throw new IllegalArgumentException("모임 일정 삭제 권한이 없습니다.");
        }
        
        groupMeetingRepository.delete(meeting);
        
        log.info("모임 일정 삭제: {} (ID: {})", meeting.getTitle(), meetingId);
    }
    
    /**
     * Entity를 DTO로 변환
     */
    private GroupMeetingDto.Response convertToDto(GroupMeeting meeting) {
        return GroupMeetingDto.Response.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .meetingDateTime(meeting.getMeetingDateTime())
                .location(meeting.getLocation())
                .agenda(meeting.getAgenda())
                .status(meeting.getStatus())
                .createdBy(convertUserToDto(meeting.getCreatedBy()))
                .monthlyBook(meeting.getMonthlyBook() != null ? convertMonthlyBookToDto(meeting.getMonthlyBook()) : null)
                .createdAt(meeting.getCreatedAt())
                .updatedAt(meeting.getUpdatedAt())
                .isPast(meeting.isPast())
                .isUpcoming(meeting.isUpcoming())
                .build();
    }
    
    private GroupMeetingDto.SimpleResponse convertToSimpleDto(GroupMeeting meeting) {
        return GroupMeetingDto.SimpleResponse.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .meetingDateTime(meeting.getMeetingDateTime())
                .location(meeting.getLocation())
                .status(meeting.getStatus())
                .isPast(meeting.isPast())
                .isUpcoming(meeting.isUpcoming())
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
