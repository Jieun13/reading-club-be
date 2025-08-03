package com.readingclub.service;

import com.readingclub.dto.ReadingGroupDto;
import com.readingclub.dto.UserDto;
import com.readingclub.entity.*;
import com.readingclub.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReadingGroupService {
    
    private final ReadingGroupRepository readingGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MonthlyBookRepository monthlyBookRepository;
    private final ReadingProgressRepository readingProgressRepository;
    private final UserRepository userRepository;
    
    private static final String INVITE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_CODE_LENGTH = 8;
    
    /**
     * 독서 모임 생성
     */
    @Transactional
    public ReadingGroupDto.Response createGroup(Long userId, ReadingGroupDto.CreateRequest request) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // ✅ 중복 그룹명 자동 해결 로직
        String baseName = request.getName(); // 사용자가 입력한 그룹명
        String uniqueName = baseName;
        int suffix = 1;

        while (readingGroupRepository.existsByNameAndStatus(uniqueName, ReadingGroup.GroupStatus.ACTIVE)) {
            uniqueName = baseName + " " + suffix;
            suffix++;
        }

        request.setName(uniqueName); // 유일한 이름으로 덮어씌움

        // 초대 코드 생성
        String inviteCode = generateUniqueInviteCode();

        // 독서 모임 엔티티 생성
        ReadingGroup group = ReadingGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .creator(creator)
                .maxMembers(request.getMaxMembers())
                .isPublic(request.getIsPublic())
                .inviteCode(inviteCode)
                .status(ReadingGroup.GroupStatus.ACTIVE)
                .hasAssignment(request.getHasAssignment())

                .bookTitle(request.getBookTitle())
                .author(request.getBookAuthor())
                .publisher(request.getBookPublisher())
                .bookCoverImage(request.getBookCoverImage())
                .meetingDateTime(request.getMeetingDateTime())
                .durationHours(request.getDurationHours())
                .hasAssignment(request.getHasAssignment())

                .meetingType(request.getMeetingType())
                .location(request.getLocation())
                .meetingUrl(request.getMeetingUrl())
                .build();

        ReadingGroup savedGroup = readingGroupRepository.save(group);

        // 생성자를 첫 번째 멤버로 추가
        GroupMember creatorMember = GroupMember.builder()
                .group(savedGroup)
                .user(creator)
                .role(GroupMember.MemberRole.CREATOR)
                .status(GroupMember.MemberStatus.ACTIVE)
                .build();

        groupMemberRepository.save(creatorMember);

        log.info("새 독서 모임 생성: {} (생성자: {})", savedGroup.getName(), userId);

        return convertToDto(savedGroup);
    }
    
    /**
     * 공개 독서 모임 목록 조회
     */
    public Page<ReadingGroupDto.ListResponse> getPublicGroups(Pageable pageable, String search) {
        Page<ReadingGroup> groups;
        
        if (search != null && !search.trim().isEmpty()) {
            groups = readingGroupRepository.findPublicGroupsByNameContaining(
                    search.trim(), ReadingGroup.GroupStatus.ACTIVE, pageable);
        } else {
            groups = readingGroupRepository.findByIsPublicTrueAndStatusOrderByCreatedAtDesc(
                    ReadingGroup.GroupStatus.ACTIVE, pageable);
        }
        
        return groups.map(this::convertToListDto);
    }
    
    /**
     * 사용자가 속한 독서 모임 목록 조회
     */
    public List<ReadingGroupDto.ListResponse> getUserGroups(Long userId) {
        List<ReadingGroup> groups = readingGroupRepository.findGroupsByUserId(
                userId, ReadingGroup.GroupStatus.ACTIVE);
        
        return groups.stream()
                .map(this::convertToListDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 독서 모임 상세 조회
     */
    public ReadingGroupDto.Response getGroup(Long groupId, Long userId) {
        ReadingGroup group = readingGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("독서 모임을 찾을 수 없습니다."));
        
        // 비공개 그룹인 경우 멤버 여부 확인
        if (!group.getIsPublic() && !groupMemberRepository.isUserMemberOfGroup(groupId, userId)) {
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }
        
        return convertToDto(group);
    }
    
    /**
     * 초대 코드로 독서 모임 가입
     */
    @Transactional
    public ReadingGroupDto.Response joinGroupByInviteCode(Long userId, ReadingGroupDto.JoinRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        ReadingGroup group = readingGroupRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 코드입니다."));
        
        // 이미 멤버인지 확인
        if (groupMemberRepository.findByGroupIdAndUserId(group.getId(), userId).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 모임입니다.");
        }
        
        // 그룹이 가득 찬지 확인
        if (group.isFull()) {
            throw new IllegalArgumentException("모임 정원이 가득 찼습니다.");
        }
        
        // 새 멤버 추가
        GroupMember newMember = GroupMember.builder()
                .group(group)
                .user(user)
                .role(GroupMember.MemberRole.MEMBER)
                .status(GroupMember.MemberStatus.ACTIVE)
                .introduction(request.getIntroduction())
                .build();
        
        groupMemberRepository.save(newMember);
        
        // 현재 진행 중인 월간 도서가 있다면 진행상황 생성
        LocalDate now = LocalDate.now();
        monthlyBookRepository.findCurrentMonthlyBook(group.getId(), now.getYear(), now.getMonthValue())
                .ifPresent(monthlyBook -> {
                    ReadingProgress progress = ReadingProgress.builder()
                            .monthlyBook(monthlyBook)
                            .user(user)
                            .build();
                    readingProgressRepository.save(progress);
                });
        
        log.info("사용자 {}가 독서 모임 {}에 가입했습니다.", userId, group.getName());
        
        return convertToDto(group);
    }
    
    /**
     * 독서 모임 수정
     */
    @Transactional
    public ReadingGroupDto.Response updateGroup(Long groupId, Long userId, ReadingGroupDto.UpdateRequest request) {
        ReadingGroup group = readingGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("독서 모임을 찾을 수 없습니다."));
        
        // 관리자 권한 확인
        if (!groupMemberRepository.isUserAdminOfGroup(groupId, userId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }
        
        // 그룹명 중복 체크 (현재 그룹 제외)
        if (!group.getName().equals(request.getName()) && 
            readingGroupRepository.existsByNameAndStatus(request.getName(), ReadingGroup.GroupStatus.ACTIVE)) {
            throw new IllegalArgumentException("이미 존재하는 그룹명입니다.");
        }
        
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setMaxMembers(request.getMaxMembers());
        group.setIsPublic(request.getIsPublic());
        
        ReadingGroup updatedGroup = readingGroupRepository.save(group);
        
        log.info("독서 모임 수정: {} (수정자: {})", updatedGroup.getName(), userId);
        
        return convertToDto(updatedGroup);
    }
    
    /**
     * 독서 모임 탈퇴
     */
    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("모임 멤버가 아닙니다."));
        
        // 생성자는 탈퇴할 수 없음
        if (member.getRole() == GroupMember.MemberRole.CREATOR) {
            throw new IllegalArgumentException("모임 생성자는 탈퇴할 수 없습니다. 모임을 삭제하거나 관리자 권한을 이양하세요.");
        }
        
        member.setStatus(GroupMember.MemberStatus.INACTIVE);
        groupMemberRepository.save(member);
        
        log.info("사용자 {}가 독서 모임 {}에서 탈퇴했습니다.", userId, groupId);
    }
    
    /**
     * 독서 모임 삭제 (생성자만 가능)
     */
    @Transactional
    public void deleteGroup(Long groupId, Long userId) {
        ReadingGroup group = readingGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("독서 모임을 찾을 수 없습니다."));
        
        if (!group.isCreator(userRepository.findById(userId).orElse(null))) {
            throw new IllegalArgumentException("삭제 권한이 없습니다. 생성자만 모임을 삭제할 수 있습니다.");
        }
        
        group.setStatus(ReadingGroup.GroupStatus.ARCHIVED);
        readingGroupRepository.save(group);
        
        log.info("독서 모임 삭제: {} (삭제자: {})", group.getName(), userId);
    }
    
    /**
     * 초대 코드 재생성
     */
    @Transactional
    public String regenerateInviteCode(Long groupId, Long userId) {
        ReadingGroup group = readingGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("독서 모임을 찾을 수 없습니다."));
        
        if (!groupMemberRepository.isUserAdminOfGroup(groupId, userId)) {
            throw new IllegalArgumentException("초대 코드 재생성 권한이 없습니다.");
        }
        
        String newInviteCode = generateUniqueInviteCode();
        group.setInviteCode(newInviteCode);
        readingGroupRepository.save(group);
        
        log.info("독서 모임 {} 초대 코드 재생성", group.getName());
        
        return newInviteCode;
    }
    
    /**
     * 유니크한 초대 코드 생성
     */
    private String generateUniqueInviteCode() {
        SecureRandom random = new SecureRandom();
        String inviteCode;
        
        do {
            StringBuilder sb = new StringBuilder(INVITE_CODE_LENGTH);
            for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
                sb.append(INVITE_CODE_CHARS.charAt(random.nextInt(INVITE_CODE_CHARS.length())));
            }
            inviteCode = sb.toString();
        } while (readingGroupRepository.existsByInviteCode(inviteCode));
        
        return inviteCode;
    }
    
    /**
     * Entity를 Response DTO로 변환
     */
    private ReadingGroupDto.Response convertToDto(ReadingGroup group) {
        return ReadingGroupDto.Response.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .creator(convertUserToDto(group.getCreator()))
                .maxMembers(group.getMaxMembers())
                .isPublic(group.getIsPublic())
                .inviteCode(group.getInviteCode())
                .status(group.getStatus())
                .hasAssignment(group.isHasAssignment())
                .currentMemberCount(group.getCurrentMemberCount())

                // 일정 관련 필드 추가
                .startDateTime(group.getMeetingDateTime())
                .endDateTime(group.getMeetingDateTime().plusHours(group.getDurationHours()))
                .durationHours(group.getDurationHours())
                .location(group.getLocation())
                .meetingType(group.getMeetingType())
                .meetingUrl(group.getMeetingUrl())

                // 도서 관련 필드
                .bookTitle(group.getBookTitle())
                .bookAuthor(group.getAuthor())
                .bookDescription(group.getDescription())
                .bookCoverImage(group.getBookCoverImage())
                .bookPublisher(group.getPublisher())
                .build();
    }
    
    /**
     * Entity를 ListResponse DTO로 변환
     */
    private ReadingGroupDto.ListResponse convertToListDto(ReadingGroup group) {
        return ReadingGroupDto.ListResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .creator(convertUserToDto(group.getCreator()))
                .maxMembers(group.getMaxMembers())
                .currentMemberCount(group.getCurrentMemberCount())
                .isPublic(group.getIsPublic())
                .status(group.getStatus())
                .createdAt(group.getCreatedAt())
                .hasAssignment(group.isHasAssignment())
                .bookAuthor(group.getAuthor())
                .bookCoverImage(group.getBookCoverImage())
                .startDateTime(group.getMeetingDateTime())
                .endDateTime(group.getMeetingDateTime().plusHours(group.getDurationHours()))
                .durationHours(group.getDurationHours())
                .location(group.getLocation())
                .meetingType(group.getMeetingType())
                .meetingUrl(group.getMeetingUrl())
                .build();
    }

    /**
     * 초대 코드로 그룹 정보 조회 (미리보기용)
     */
    public ReadingGroupDto.ListResponse getGroupByInviteCode(String inviteCode) {
        ReadingGroup group = readingGroupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 초대 코드입니다."));
        
        // 현재 멤버 수 조회
        long currentMemberCount = groupMemberRepository.countByGroupIdAndStatus(
                group.getId(), GroupMember.MemberStatus.ACTIVE);
        
        return ReadingGroupDto.ListResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .bookTitle(group.getBookTitle())
                .bookAuthor(group.getAuthor())
                .bookCoverImage(group.getBookCoverImage())
                .creator(convertUserToDto(group.getCreator()))
                .maxMembers(group.getMaxMembers())
                .currentMemberCount((int) currentMemberCount)
                .isPublic(group.getIsPublic())
                .status(group.getStatus())
                .hasAssignment(group.isHasAssignment())
                .startDateTime(group.getMeetingDateTime())
                .endDateTime(group.getMeetingDateTime().plusHours(group.getDurationHours()))
                .durationHours(group.getDurationHours())
                .location(group.getLocation())
                .meetingType(group.getMeetingType())
                .meetingUrl(group.getMeetingUrl())
                .createdAt(group.getCreatedAt())
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
