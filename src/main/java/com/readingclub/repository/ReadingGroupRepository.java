package com.readingclub.repository;

import com.readingclub.entity.ReadingGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingGroupRepository extends JpaRepository<ReadingGroup, Long> {
    
    /**
     * 초대 코드로 그룹 조회
     */
    Optional<ReadingGroup> findByInviteCode(String inviteCode);
    
    /**
     * 공개 그룹 목록 조회 (페이징)
     */
    Page<ReadingGroup> findByIsPublicTrueAndStatusOrderByCreatedAtDesc(
            ReadingGroup.GroupStatus status, Pageable pageable);
    
    /**
     * 그룹명으로 검색 (공개 그룹만)
     */
    @Query("SELECT rg FROM ReadingGroup rg WHERE rg.isPublic = true AND rg.status = :status " +
           "AND LOWER(rg.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY rg.createdAt DESC")
    Page<ReadingGroup> findPublicGroupsByNameContaining(
            @Param("name") String name, 
            @Param("status") ReadingGroup.GroupStatus status, 
            Pageable pageable);
    
    /**
     * 사용자가 속한 그룹 목록 조회
     */
    @Query("SELECT DISTINCT rg FROM ReadingGroup rg " +
           "JOIN rg.members gm " +
           "WHERE gm.user.id = :userId AND gm.status = 'ACTIVE' AND rg.status = :status " +
           "ORDER BY rg.updatedAt DESC")
    List<ReadingGroup> findGroupsByUserId(
            @Param("userId") Long userId, 
            @Param("status") ReadingGroup.GroupStatus status);
    
    /**
     * 사용자가 생성한 그룹 목록 조회
     */
    List<ReadingGroup> findByCreatorIdAndStatusOrderByCreatedAtDesc(
            Long creatorId, ReadingGroup.GroupStatus status);
    
    /**
     * 그룹명 중복 체크
     */
    boolean existsByNameAndStatus(String name, ReadingGroup.GroupStatus status);
    
    /**
     * 초대 코드 중복 체크
     */
    boolean existsByInviteCode(String inviteCode);
    
    /**
     * 활성 그룹 수 조회
     */
    long countByStatus(ReadingGroup.GroupStatus status);
    
    /**
     * 사용자별 그룹 수 조회
     */
    @Query("SELECT COUNT(DISTINCT rg) FROM ReadingGroup rg " +
           "JOIN rg.members gm " +
           "WHERE gm.user.id = :userId AND gm.status = 'ACTIVE' AND rg.status = :status")
    long countGroupsByUserId(@Param("userId") Long userId, @Param("status") ReadingGroup.GroupStatus status);
}
