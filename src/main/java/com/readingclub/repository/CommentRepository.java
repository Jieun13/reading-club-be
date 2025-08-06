package com.readingclub.repository;

import com.readingclub.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // 게시글의 댓글 목록 조회 (페이징)
    Page<Comment> findByPostIdAndParentIsNullOrderByCreatedAtAsc(Long postId, Pageable pageable);
    
    // 게시글의 댓글 목록 조회 (전체)
    List<Comment> findByPostIdAndParentIsNullOrderByCreatedAtAsc(Long postId);
    
    // 특정 댓글의 대댓글 목록 조회
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);
    
    // 게시글의 모든 댓글 개수 (삭제된 것 포함)
    long countByPostId(Long postId);
    
    // 게시글의 활성 댓글 개수 (삭제되지 않은 것만)
    long countByPostIdAndIsDeletedFalse(Long postId);
    
    // 사용자가 작성한 댓글 목록
    Page<Comment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // 특정 댓글 조회 (작성자 포함)
    Optional<Comment> findByIdAndUserId(Long id, Long userId);
    
    // 게시글의 댓글 개수 (대댓글 포함)
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    long countCommentsByPostId(@Param("postId") Long postId);
    
    // 게시글의 활성 댓글 개수 (대댓글 포함, 삭제되지 않은 것만)
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.isDeleted = false")
    long countActiveCommentsByPostId(@Param("postId") Long postId);
    
    // 특정 댓글과 그 대댓글들 조회
    @Query("SELECT c FROM Comment c WHERE c.id = :commentId OR c.parent.id = :commentId ORDER BY c.createdAt ASC")
    List<Comment> findCommentWithReplies(@Param("commentId") Long commentId);
} 