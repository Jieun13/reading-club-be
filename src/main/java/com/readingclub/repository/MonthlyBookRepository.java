package com.readingclub.repository;

import com.readingclub.entity.MonthlyBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyBookRepository extends JpaRepository<MonthlyBook, Long> {
    
    /**
     * 그룹의 특정 년월 도서 조회
     */
    Optional<MonthlyBook> findByGroupIdAndYearAndMonth(Long groupId, Integer year, Integer month);
    
    /**
     * 그룹의 현재 월 도서 조회
     */
    @Query("SELECT mb FROM MonthlyBook mb WHERE mb.group.id = :groupId " +
           "AND mb.year = :year AND mb.month = :month " +
           "AND mb.status IN ('UPCOMING', 'READING')")
    Optional<MonthlyBook> findCurrentMonthlyBook(
            @Param("groupId") Long groupId, 
            @Param("year") Integer year, 
            @Param("month") Integer month);
    
    /**
     * 그룹의 월간 도서 목록 조회 (최신순)
     */
    List<MonthlyBook> findByGroupIdOrderByYearDescMonthDesc(Long groupId);
    
    /**
     * 그룹의 특정 상태 월간 도서 목록 조회
     */
    List<MonthlyBook> findByGroupIdAndStatusOrderByYearDescMonthDesc(
            Long groupId, MonthlyBook.BookStatus status);
    
    /**
     * 사용자가 선정한 월간 도서 목록 조회
     */
    List<MonthlyBook> findBySelectedByIdOrderByYearDescMonthDesc(Long userId);
    
    /**
     * 그룹의 년도별 월간 도서 목록 조회
     */
    List<MonthlyBook> findByGroupIdAndYearOrderByMonthDesc(Long groupId, Integer year);
    
    /**
     * 특정 년월에 이미 도서가 선정되었는지 확인
     */
    boolean existsByGroupIdAndYearAndMonth(Long groupId, Integer year, Integer month);
    
    /**
     * 그룹의 완료된 도서 수 조회
     */
    long countByGroupIdAndStatus(Long groupId, MonthlyBook.BookStatus status);
    
    /**
     * 현재 진행 중인 모든 월간 도서 조회 (알림용)
     */
    @Query("SELECT mb FROM MonthlyBook mb WHERE mb.status = 'READING' " +
           "AND mb.endDate >= CURRENT_DATE")
    List<MonthlyBook> findActiveMonthlyBooks();
}
