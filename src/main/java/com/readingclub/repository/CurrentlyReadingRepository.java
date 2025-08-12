package com.readingclub.repository;

import com.readingclub.entity.CurrentlyReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CurrentlyReadingRepository extends JpaRepository<CurrentlyReading, Long> {
    
    // 사용자의 읽고 있는 책 목록 조회 (페이징)
    Page<CurrentlyReading> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // 사용자의 읽고 있는 책 목록 조회 (전체)
    List<CurrentlyReading> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // 사용자의 특정 책이 읽고 있는 책에 있는지 확인
    boolean existsByUserIdAndTitleAndAuthor(Long userId, String title, String author);
    
    // 사용자의 특정 책이 읽고 있는 책에 있는지 확인 (저자 없이)
    boolean existsByUserIdAndTitle(Long userId, String title);
    
    // 사용자의 읽고 있는 책 개수
    long countByUserId(Long userId);
    
    // 도서관 대여 중이고 대여 종료일이 지난 책들
    @Query("SELECT cr FROM CurrentlyReading cr WHERE cr.user.id = :userId AND cr.readingType = 'LIBRARY_RENTAL' AND cr.dueDate < :today")
    List<CurrentlyReading> findOverdueBooks(@Param("userId") Long userId, @Param("today") LocalDate today);
    
    // 제목과 저자로 검색
    @Query("SELECT cr FROM CurrentlyReading cr WHERE cr.user.id = :userId AND (cr.title LIKE %:search% OR cr.author LIKE %:search%)")
    Page<CurrentlyReading> findByUserIdAndTitleOrAuthorContaining(@Param("userId") Long userId, @Param("search") String search, Pageable pageable);
    
    // 특정 읽고 있는 책 조회
    Optional<CurrentlyReading> findByIdAndUserId(Long id, Long userId);
    
    // 이번 달에 등록한 읽고 있는 책들 조회
    @Query("SELECT cr FROM CurrentlyReading cr WHERE cr.user.id = :userId AND YEAR(cr.createdAt) = :year AND MONTH(cr.createdAt) = :month ORDER BY cr.createdAt DESC")
    List<CurrentlyReading> findByUserIdAndCreatedAtYearAndCreatedAtMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);
} 