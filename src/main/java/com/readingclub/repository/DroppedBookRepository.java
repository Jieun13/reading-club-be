package com.readingclub.repository;

import com.readingclub.entity.DroppedBook;
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
public interface DroppedBookRepository extends JpaRepository<DroppedBook, Long> {
    
    /**
     * 사용자별 읽다 만 책 목록 조회 (페이징)
     */
    Page<DroppedBook> findByUserIdOrderByDroppedDateDesc(Long userId, Pageable pageable);
    
    /**
     * 사용자별 읽다 만 책 목록 조회 (하차일 기준 정렬)
     */
    List<DroppedBook> findByUserIdOrderByDroppedDateDesc(Long userId);
    
    /**
     * 사용자별 특정 연도 읽다 만 책 목록 조회
     */
    @Query("SELECT d FROM DroppedBook d WHERE d.user.id = :userId AND YEAR(d.droppedDate) = :year ORDER BY d.droppedDate DESC")
    List<DroppedBook> findByUserIdAndYear(@Param("userId") Long userId, @Param("year") int year);
    
    /**
     * 사용자별 특정 연월 읽다 만 책 목록 조회
     */
    @Query("SELECT d FROM DroppedBook d WHERE d.user.id = :userId AND YEAR(d.droppedDate) = :year AND MONTH(d.droppedDate) = :month ORDER BY d.droppedDate DESC")
    List<DroppedBook> findByUserIdAndYearAndMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);
    
    /**
     * 사용자별 특정 기간 읽다 만 책 목록 조회
     */
    @Query("SELECT d FROM DroppedBook d WHERE d.user.id = :userId AND d.droppedDate BETWEEN :startDate AND :endDate ORDER BY d.droppedDate DESC")
    List<DroppedBook> findByUserIdAndDroppedDateBetween(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * 사용자별 책 제목 검색
     */
    @Query("SELECT d FROM DroppedBook d WHERE d.user.id = :userId AND LOWER(d.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY d.droppedDate DESC")
    List<DroppedBook> findByUserIdAndTitleContainingIgnoreCase(@Param("userId") Long userId, @Param("title") String title);
    
    /**
     * 사용자별 저자 검색
     */
    @Query("SELECT d FROM DroppedBook d WHERE d.user.id = :userId AND LOWER(d.author) LIKE LOWER(CONCAT('%', :author, '%')) ORDER BY d.droppedDate DESC")
    List<DroppedBook> findByUserIdAndAuthorContainingIgnoreCase(@Param("userId") Long userId, @Param("author") String author);
    
    /**
     * 사용자별 제목 또는 저자로 검색 (페이징)
     */
    @Query("SELECT d FROM DroppedBook d WHERE d.user.id = :userId AND (LOWER(d.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(d.author) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY d.droppedDate DESC")
    Page<DroppedBook> findByUserIdAndTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
            @Param("userId") Long userId, @Param("search") String search, @Param("search") String search2, Pageable pageable);
    
    /**
     * 사용자별 총 읽다 만 책 수
     */
    long countByUserId(Long userId);
    
    /**
     * 사용자별 특정 기간 읽다 만 책 개수 조회
     */
    long countByUserIdAndDroppedDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 사용자와 책 ID로 읽다 만 책 조회 (권한 확인용)
     */
    Optional<DroppedBook> findByIdAndUserId(Long droppedBookId, Long userId);
    
    /**
     * 사용자별 동일한 책이 이미 읽다 만 책으로 등록되어 있는지 확인
     */
    boolean existsByUserIdAndTitleAndAuthor(Long userId, String title, String author);
    
    /**
     * 사용자별 동일한 책이 이미 읽다 만 책으로 등록되어 있는지 확인 (ISBN 기준)
     */
    boolean existsByUserIdAndIsbn(Long userId, String isbn);
    
    /**
     * 사용자별 동일한 책이 이미 읽다 만 책으로 등록되어 있는지 확인 (제목만으로)
     */
    boolean existsByUserIdAndTitle(Long userId, String title);
}
