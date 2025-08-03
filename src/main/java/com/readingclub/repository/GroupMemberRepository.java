package com.readingclub.repository;

import com.readingclub.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    
    /**
     * 그룹과 사용자로 멤버 조회
     */
    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);
    
    /**
     * 그룹의 활성 멤버 목록 조회
     */
    List<GroupMember> findByGroupIdAndStatusOrderByJoinedAtAsc(
            Long groupId, GroupMember.MemberStatus status);
    
    /**
     * 사용자가 속한 그룹의 멤버십 목록 조회
     */
    List<GroupMember> findByUserIdAndStatusOrderByJoinedAtDesc(
            Long userId, GroupMember.MemberStatus status);
    
    /**
     * 그룹의 활성 멤버 수 조회
     */
    long countByGroupIdAndStatus(Long groupId, GroupMember.MemberStatus status);
    
    /**
     * 그룹의 관리자 목록 조회
     */
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId " +
           "AND gm.status = 'ACTIVE' AND gm.role IN ('CREATOR', 'ADMIN') " +
           "ORDER BY gm.role ASC, gm.joinedAt ASC")
    List<GroupMember> findAdminsByGroupId(@Param("groupId") Long groupId);
    
    /**
     * 사용자가 특정 그룹의 관리자인지 확인
     */
    @Query("SELECT COUNT(gm) > 0 FROM GroupMember gm WHERE gm.group.id = :groupId " +
           "AND gm.user.id = :userId AND gm.status = 'ACTIVE' " +
           "AND gm.role IN ('CREATOR', 'ADMIN')")
    boolean isUserAdminOfGroup(@Param("groupId") Long groupId, @Param("userId") Long userId);
    
    /**
     * 사용자가 특정 그룹의 멤버인지 확인
     */
    @Query("SELECT COUNT(gm) > 0 FROM GroupMember gm WHERE gm.group.id = :groupId " +
           "AND gm.user.id = :userId AND gm.status = 'ACTIVE'")
    boolean isUserMemberOfGroup(@Param("groupId") Long groupId, @Param("userId") Long userId);
}
