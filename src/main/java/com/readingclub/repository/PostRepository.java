package com.readingclub.repository;

import com.readingclub.entity.Post;
import com.readingclub.entity.PostType;
import com.readingclub.entity.PostVisibility;
import com.readingclub.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // 모든 공개 게시글 조회 (페이징)
    Page<Post> findByVisibilityOrderByCreatedAtDesc(PostVisibility visibility, Pageable pageable);
    
    // 타입별 공개 게시글 조회 (페이징)
    Page<Post> findByPostTypeAndVisibilityOrderByCreatedAtDesc(
        PostType postType, PostVisibility visibility, Pageable pageable);
    
    // 사용자별 게시글 조회 (페이징)
    Page<Post> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // 사용자별 + 타입별 게시글 조회 (페이징)
    Page<Post> findByUserAndPostTypeOrderByCreatedAtDesc(
        User user, PostType postType, Pageable pageable);
    
    // 사용자별 + 공개설정별 게시글 조회 (페이징)
    Page<Post> findByUserAndVisibilityOrderByCreatedAtDesc(
        User user, PostVisibility visibility, Pageable pageable);
    
    // 복합 조건 검색을 위한 커스텀 쿼리
    @Query("SELECT p FROM Post p WHERE " +
           "(:postType IS NULL OR p.postType = :postType) AND " +
           "(:visibility IS NULL OR p.visibility = :visibility) AND " +
           "(:userId IS NULL OR p.user.id = :userId) " +
           "ORDER BY p.createdAt DESC")
    Page<Post> findPostsWithFilters(
        @Param("postType") PostType postType,
        @Param("visibility") PostVisibility visibility,
        @Param("userId") Long userId,
        Pageable pageable);
    
    // 특정 사용자의 게시글 수 조회
    long countByUser(User user);
    
    // 특정 사용자의 타입별 게시글 수 조회
    long countByUserAndPostType(User user, PostType postType);
    
    // 특정 책에 대한 게시글 조회
    List<Post> findByBookIsbnOrderByCreatedAtDesc(String bookIsbn);
    
    // 게시글 ID와 사용자로 조회 (권한 확인용)
    Optional<Post> findByIdAndUser(Long id, User user);
    
    // 책 제목으로 게시글 검색 (공개 게시글만)
    @Query("SELECT p FROM Post p WHERE " +
           "p.visibility = 'PUBLIC' AND " +
           "LOWER(p.bookTitle) LIKE LOWER(CONCAT('%', :bookTitle, '%')) " +
           "ORDER BY p.createdAt DESC")
    Page<Post> findByBookTitleContainingIgnoreCaseAndVisibility(
        @Param("bookTitle") String bookTitle, Pageable pageable);
    
    // 책 제목으로 게시글 검색 (모든 게시글, 관리자용)
    @Query("SELECT p FROM Post p WHERE " +
           "LOWER(p.bookTitle) LIKE LOWER(CONCAT('%', :bookTitle, '%')) " +
           "ORDER BY p.createdAt DESC")
    Page<Post> findByBookTitleContainingIgnoreCase(
        @Param("bookTitle") String bookTitle, Pageable pageable);
    
    // 책 제목 + 게시글 타입으로 검색
    @Query("SELECT p FROM Post p WHERE " +
           "p.visibility = 'PUBLIC' AND " +
           "p.postType = :postType AND " +
           "LOWER(p.bookTitle) LIKE LOWER(CONCAT('%', :bookTitle, '%')) " +
           "ORDER BY p.createdAt DESC")
    Page<Post> findByBookTitleContainingIgnoreCaseAndPostTypeAndVisibility(
        @Param("bookTitle") String bookTitle, 
        @Param("postType") PostType postType, 
        Pageable pageable);
    
    // 제목 또는 내용으로 게시글 검색
    @Query("SELECT p FROM Post p WHERE " +
           "p.visibility = 'PUBLIC' AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.bookTitle) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.createdAt DESC")
    Page<Post> findByKeywordInTitleOrContentOrBookTitle(
        @Param("keyword") String keyword, Pageable pageable);
}
