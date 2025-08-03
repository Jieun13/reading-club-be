package com.readingclub.repository;

import com.readingclub.entity.Book;
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
public interface BookRepository extends JpaRepository<Book, Long> {
    
    /**
     * 사용자별 책 목록 조회 (페이징)
     */
    Page<Book> findByUserIdOrderByFinishedDateDesc(Long userId, Pageable pageable);
    
    /**
     * 사용자별 책 목록 조회 (완독일 기준 정렬)
     */
    List<Book> findByUserIdOrderByFinishedDateDesc(Long userId);
    
    /**
     * 사용자별 특정 연도 책 목록 조회
     */
    @Query("SELECT b FROM Book b WHERE b.user.id = :userId AND YEAR(b.finishedDate) = :year ORDER BY b.finishedDate DESC")
    List<Book> findByUserIdAndYear(@Param("userId") Long userId, @Param("year") int year);
    
    /**
     * 사용자별 특정 연월 책 목록 조회
     */
    @Query("SELECT b FROM Book b WHERE b.user.id = :userId AND YEAR(b.finishedDate) = :year AND MONTH(b.finishedDate) = :month ORDER BY b.finishedDate DESC")
    List<Book> findByUserIdAndYearAndMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);
    
    /**
     * 사용자별 특정 기간 책 목록 조회
     */
    @Query("SELECT b FROM Book b WHERE b.user.id = :userId AND b.finishedDate BETWEEN :startDate AND :endDate ORDER BY b.finishedDate DESC")
    List<Book> findByUserIdAndFinishedDateBetween(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * 사용자별 별점별 책 목록 조회
     */
    List<Book> findByUserIdAndRatingOrderByFinishedDateDesc(Long userId, Integer rating);
    
    /**
     * 사용자별 책 제목 검색
     */
    @Query("SELECT b FROM Book b WHERE b.user.id = :userId AND LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY b.finishedDate DESC")
    List<Book> findByUserIdAndTitleContainingIgnoreCase(@Param("userId") Long userId, @Param("title") String title);
    
    /**
     * 사용자별 제목과 저자로 검색 (중복 체크용)
     */
    @Query("SELECT b FROM Book b WHERE b.user.id = :userId AND LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')) AND LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%')) ORDER BY b.finishedDate DESC")
    List<Book> findByUserIdAndTitleContainingIgnoreCaseAndAuthorContainingIgnoreCase(@Param("userId") Long userId, @Param("title") String title, @Param("author") String author);
    
    /**
     * 사용자별 저자 검색
     */
    @Query("SELECT b FROM Book b WHERE b.user.id = :userId AND LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%')) ORDER BY b.finishedDate DESC")
    List<Book> findByUserIdAndAuthorContainingIgnoreCase(@Param("userId") Long userId, @Param("author") String author);
    
    /**
     * 사용자별 총 책 수
     */
    long countByUserId(Long userId);
    
    /**
     * 사용자별 평균 별점
     */
    @Query("SELECT AVG(b.rating) FROM Book b WHERE b.user.id = :userId")
    Double findAverageRatingByUserId(@Param("userId") Long userId);
    
    /**
     * 사용자별 월별 독서 통계
     */
    @Query("SELECT YEAR(b.finishedDate) as year, MONTH(b.finishedDate) as month, COUNT(b) as count, AVG(b.rating) as avgRating " +
           "FROM Book b WHERE b.user.id = :userId " +
           "GROUP BY YEAR(b.finishedDate), MONTH(b.finishedDate) " +
           "ORDER BY year DESC, month DESC")
    List<Object[]> findMonthlyStatsByUserId(@Param("userId") Long userId);
    
    /**
     * 사용자와 책 ID로 책 조회 (권한 확인용)
     */
    Optional<Book> findByIdAndUserId(Long bookId, Long userId);
}
