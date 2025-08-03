package com.readingclub.repository;

import com.readingclub.entity.BookReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookReviewRepository extends JpaRepository<BookReview, Long> {
    
    /**
     * 월간 도서와 사용자로 리뷰 조회
     */
    Optional<BookReview> findByMonthlyBookIdAndUserId(Long monthlyBookId, Long userId);
    
    /**
     * 월간 도서의 공개된 리뷰 목록 조회 (최신순)
     */
    @Query("SELECT br FROM BookReview br WHERE br.monthlyBook.id = :monthlyBookId " +
           "AND br.isPublic = true AND br.status = 'PUBLISHED' " +
           "ORDER BY br.updatedAt DESC")
    List<BookReview> findPublicReviewsByMonthlyBookId(@Param("monthlyBookId") Long monthlyBookId);
    
    /**
     * 월간 도서의 모든 리뷰 목록 조회 (관리자용)
     */
    List<BookReview> findByMonthlyBookIdOrderByUpdatedAtDesc(Long monthlyBookId);
    
    /**
     * 사용자의 리뷰 목록 조회
     */
    List<BookReview> findByUserIdOrderByUpdatedAtDesc(Long userId);
    
    /**
     * 그룹의 모든 리뷰 조회
     */
    @Query("SELECT br FROM BookReview br " +
           "JOIN br.monthlyBook mb " +
           "WHERE mb.group.id = :groupId AND br.isPublic = true AND br.status = 'PUBLISHED' " +
           "ORDER BY br.updatedAt DESC")
    List<BookReview> findPublicReviewsByGroupId(@Param("groupId") Long groupId);
    
    /**
     * 월간 도서의 평균 별점 조회
     */
    @Query("SELECT AVG(br.rating) FROM BookReview br " +
           "WHERE br.monthlyBook.id = :monthlyBookId AND br.rating > 0 " +
           "AND br.status = 'PUBLISHED'")
    Double findAverageRatingByMonthlyBookId(@Param("monthlyBookId") Long monthlyBookId);
    
    /**
     * 월간 도서의 리뷰 수 조회
     */
    @Query("SELECT COUNT(br) FROM BookReview br " +
           "WHERE br.monthlyBook.id = :monthlyBookId AND br.status = 'PUBLISHED'")
    Long countPublishedReviewsByMonthlyBookId(@Param("monthlyBookId") Long monthlyBookId);
    
    /**
     * 사용자가 특정 월간 도서에 리뷰를 작성했는지 확인
     */
    @Query("SELECT COUNT(br) > 0 FROM BookReview br " +
           "WHERE br.monthlyBook.id = :monthlyBookId AND br.user.id = :userId")
    boolean existsByMonthlyBookIdAndUserId(@Param("monthlyBookId") Long monthlyBookId, @Param("userId") Long userId);
}
