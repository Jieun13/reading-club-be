package com.readingclub.repository;

import com.readingclub.entity.ReadingProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, Long> {
    
    /**
     * 월간 도서와 사용자로 진행상황 조회
     */
    Optional<ReadingProgress> findByMonthlyBookIdAndUserId(Long monthlyBookId, Long userId);
    
    /**
     * 월간 도서의 모든 진행상황 조회
     */
    List<ReadingProgress> findByMonthlyBookIdOrderByProgressPercentageDesc(Long monthlyBookId);
    
    /**
     * 사용자의 모든 진행상황 조회 (최신순)
     */
    List<ReadingProgress> findByUserIdOrderByUpdatedAtDesc(Long userId);
    
    /**
     * 월간 도서의 완독자 목록 조회
     */
    List<ReadingProgress> findByMonthlyBookIdAndStatusOrderByCompletedAtAsc(
            Long monthlyBookId, ReadingProgress.ReadingStatus status);
    
    /**
     * 그룹의 사용자별 완독 통계
     */
    @Query("SELECT rp.user.id, COUNT(rp) FROM ReadingProgress rp " +
           "JOIN rp.monthlyBook mb " +
           "WHERE mb.group.id = :groupId AND rp.status = 'COMPLETED' " +
           "GROUP BY rp.user.id " +
           "ORDER BY COUNT(rp) DESC")
    List<Object[]> findCompletionStatsByGroupId(@Param("groupId") Long groupId);
    
    /**
     * 월간 도서의 평균 진행률 조회
     */
    @Query("SELECT AVG(rp.progressPercentage) FROM ReadingProgress rp " +
           "WHERE rp.monthlyBook.id = :monthlyBookId")
    Double findAverageProgressByMonthlyBookId(@Param("monthlyBookId") Long monthlyBookId);
    
    /**
     * 월간 도서의 완독률 조회
     */
    @Query("SELECT COUNT(rp) * 100.0 / " +
           "(SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.status = 'ACTIVE') " +
           "FROM ReadingProgress rp " +
           "JOIN rp.monthlyBook mb " +
           "WHERE mb.id = :monthlyBookId AND rp.status = 'COMPLETED'")
    Double findCompletionRateByMonthlyBookId(
            @Param("monthlyBookId") Long monthlyBookId, 
            @Param("groupId") Long groupId);
    
    /**
     * 사용자의 그룹별 완독 수 조회
     */
    @Query("SELECT mb.group.id, COUNT(rp) FROM ReadingProgress rp " +
           "JOIN rp.monthlyBook mb " +
           "WHERE rp.user.id = :userId AND rp.status = 'COMPLETED' " +
           "GROUP BY mb.group.id")
    List<Object[]> findUserCompletionStatsByGroups(@Param("userId") Long userId);
    
    /**
     * 월간 도서의 평균 별점 조회
     */
    @Query("SELECT AVG(rp.rating) FROM ReadingProgress rp " +
           "WHERE rp.monthlyBook.id = :monthlyBookId AND rp.rating > 0")
    Double findAverageRatingByMonthlyBookId(@Param("monthlyBookId") Long monthlyBookId);
}
