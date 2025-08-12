package com.readingclub.repository;

import com.readingclub.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    
    /**
     * 사용자별 위시리스트 조회 (페이징, 우선순위 및 생성일 기준 정렬)
     */
    Page<Wishlist> findByUserIdOrderByPriorityAscCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 사용자별 위시리스트 조회 (우선순위 및 생성일 기준 정렬)
     */
    List<Wishlist> findByUserIdOrderByPriorityAscCreatedAtDesc(Long userId);
    
    /**
     * 사용자별 특정 우선순위 위시리스트 조회
     */
    List<Wishlist> findByUserIdAndPriorityOrderByCreatedAtDesc(Long userId, Integer priority);
    
    /**
     * 사용자별 책 제목 검색
     */
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND LOWER(w.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY w.priority ASC, w.createdAt DESC")
    List<Wishlist> findByUserIdAndTitleContainingIgnoreCase(@Param("userId") Long userId, @Param("title") String title);
    
    /**
     * 사용자별 저자 검색
     */
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND LOWER(w.author) LIKE LOWER(CONCAT('%', :author, '%')) ORDER BY w.priority ASC, w.createdAt DESC")
    List<Wishlist> findByUserIdAndAuthorContainingIgnoreCase(@Param("userId") Long userId, @Param("author") String author);
    
    /**
     * 사용자별 제목과 저자로 중복 체크
     */
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND LOWER(w.title) LIKE LOWER(CONCAT('%', :title, '%')) AND LOWER(w.author) LIKE LOWER(CONCAT('%', :author, '%'))")
    List<Wishlist> findByUserIdAndTitleContainingIgnoreCaseAndAuthorContainingIgnoreCase(@Param("userId") Long userId, @Param("title") String title, @Param("author") String author);
    
    /**
     * 사용자별 총 위시리스트 수
     */
    long countByUserId(Long userId);
    
    /**
     * 사용자별 우선순위별 통계
     */
    @Query("SELECT w.priority, COUNT(w) FROM Wishlist w WHERE w.user.id = :userId GROUP BY w.priority ORDER BY w.priority")
    List<Object[]> findPriorityStatsByUserId(@Param("userId") Long userId);
    
    /**
     * 사용자와 위시리스트 ID로 조회 (권한 확인용)
     */
    Optional<Wishlist> findByIdAndUserId(Long wishlistId, Long userId);
    
    /**
     * 이번 달에 등록한 위시리스트 책들 조회
     */
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND YEAR(w.createdAt) = :year AND MONTH(w.createdAt) = :month ORDER BY w.priority ASC, w.createdAt DESC")
    List<Wishlist> findByUserIdAndCreatedAtYearAndCreatedAtMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);
}
