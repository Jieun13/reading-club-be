package com.readingclub.service;

import com.readingclub.dto.GroupMemberDto;
import com.readingclub.dto.UserDto;
import com.readingclub.entity.GroupMember;
import com.readingclub.entity.ReadingGroup;
import com.readingclub.entity.User;
import com.readingclub.repository.GroupMemberRepository;
import com.readingclub.repository.ReadingGroupRepository;
import com.readingclub.repository.UserRepository;
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
public class GroupMemberService {
    
    private final GroupMemberRepository groupMemberRepository;
    private final ReadingGroupRepository readingGroupRepository;
    private final UserRepository userRepository;
    
    /**
     * 모임 가입 (공개 모임)
     */
    @Transactional
    public GroupMemberDto.Response joinGroup(Long groupId, Long userId, GroupMemberDto.JoinRequest request) {
        ReadingGroup group = readingGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 가입 가능 여부 검증
        validateJoinEligibility(group, user);
        
        // 공개 모임만 직접 가입 가능
        if (!group.getIsPublic()) {
            throw new IllegalArgumentException("비공개 모임은 초대 코드가 필요합니다.");
        }
        
        // 멤버 생성
        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .role(GroupMember.MemberRole.MEMBER)
                .status(GroupMember.MemberStatus.ACTIVE)
                .introduction(request.getIntroduction())
                .build();
        
        GroupMember savedMember = groupMemberRepository.save(member);
        
        log.info("모임 가입 성공: groupId={}, userId={}", groupId, userId);
        
        return convertToDto(savedMember);
    }
    
    /**
     * 초대 코드로 모임 가입
     */
    @Transactional
    public GroupMemberDto.Response joinByInviteCode(Long groupId, Long userId, GroupMemberDto.JoinByCodeRequest request) {
        ReadingGroup group = readingGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 가입 가능 여부 검증
        validateJoinEligibility(group, user);
        
        // 초대 코드 검증
        if (!group.getInviteCode().equals(request.getInviteCode())) {
            throw new IllegalArgumentException("잘못된 초대 코드입니다.");
        }
        
        // 멤버 생성
        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .role(GroupMember.MemberRole.MEMBER)
                .status(GroupMember.MemberStatus.ACTIVE)
                .introduction(request.getIntroduction())
                .build();
        
        GroupMember savedMember = groupMemberRepository.save(member);
        
        log.info("초대 코드 가입 성공: groupId={}, userId={}", groupId, userId);
        
        return convertToDto(savedMember);
    }
    
    /**
     * 멤버 목록 조회
     */
    public List<GroupMemberDto.Response> getGroupMembers(Long groupId, Long userId) {
        // 모임 존재 여부 확인
        if (!readingGroupRepository.existsById(groupId)) {
            throw new IllegalArgumentException("존재하지 않는 모임입니다.");
        }
        
        // 요청자가 멤버인지 확인 (비공개 모임의 경우)
        ReadingGroup group = readingGroupRepository.findById(groupId).get();
        if (!group.getIsPublic() && !groupMemberRepository.isUserMemberOfGroup(groupId, userId)) {
            throw new IllegalArgumentException("멤버만 멤버 목록을 조회할 수 있습니다.");
        }
        
        List<GroupMember> members = groupMemberRepository.findByGroupIdAndStatusOrderByJoinedAtAsc(
                groupId, GroupMember.MemberStatus.ACTIVE);
        
        return members.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 멤버 삭제 (모임장만)
     */
    @Transactional
    public void removeMember(Long groupId, Long targetUserId, Long currentUserId) {
        // 권한 확인 (모임장만 가능)
        if (!groupMemberRepository.isUserAdminOfGroup(groupId, currentUserId)) {
            throw new IllegalArgumentException("모임장만 멤버를 삭제할 수 있습니다.");
        }
        
        // 대상 멤버 조회
        GroupMember targetMember = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("해당 멤버를 찾을 수 없습니다."));
        
        // 모임장은 삭제할 수 없음
        if (targetMember.getRole() == GroupMember.MemberRole.CREATOR) {
            throw new IllegalArgumentException("모임장은 삭제할 수 없습니다.");
        }
        
        // 멤버 삭제
        groupMemberRepository.delete(targetMember);
        
        log.info("멤버 삭제 성공: groupId={}, targetUserId={}, currentUserId={}", 
                groupId, targetUserId, currentUserId);
    }
    
    /**
     * 모임 탈퇴 (본인만)
     */
    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모임의 멤버가 아닙니다."));
        
        // 모임장은 탈퇴할 수 없음
        if (member.getRole() == GroupMember.MemberRole.CREATOR) {
            throw new IllegalArgumentException("모임장은 탈퇴할 수 없습니다. 모임을 삭제하거나 다른 멤버에게 모임장을 위임하세요.");
        }
        
        // 멤버 삭제
        groupMemberRepository.delete(member);
        
        log.info("모임 탈퇴 성공: groupId={}, userId={}", groupId, userId);
    }
    
    /**
     * 가입 가능 여부 검증
     */
    private void validateJoinEligibility(ReadingGroup group, User user) {
        // 이미 가입했는지 확인
        if (groupMemberRepository.findByGroupIdAndUserId(group.getId(), user.getId()).isPresent()) {
            throw new IllegalArgumentException("이미 가입한 모임입니다.");
        }
        
        // 최대 인원 초과 여부 확인
        long currentMemberCount = groupMemberRepository.countByGroupIdAndStatus(
                group.getId(), GroupMember.MemberStatus.ACTIVE);
        
        if (currentMemberCount >= group.getMaxMembers()) {
            throw new IllegalArgumentException("모임 정원이 가득 찼습니다.");
        }
        
        // 모임 상태 확인 (ACTIVE만 가입 가능)
        if (group.getStatus() != ReadingGroup.GroupStatus.ACTIVE) {
            throw new IllegalArgumentException("현재 가입할 수 없는 모임입니다.");
        }
    }
    
    /**
     * Entity를 DTO로 변환
     */
    private GroupMemberDto.Response convertToDto(GroupMember member) {
        return GroupMemberDto.Response.builder()
                .id(member.getId())
                .user(convertUserToDto(member.getUser()))
                .role(member.getRole().name())
                .status(member.getStatus().name())
                .introduction(member.getIntroduction())
                .joinedAt(member.getJoinedAt())
                .updatedAt(member.getUpdatedAt())
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
