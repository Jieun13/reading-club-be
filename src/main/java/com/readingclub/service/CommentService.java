package com.readingclub.service;

import com.readingclub.dto.CommentDto;
import com.readingclub.dto.UserDto;
import com.readingclub.entity.Comment;
import com.readingclub.entity.Post;
import com.readingclub.entity.User;
import com.readingclub.repository.CommentRepository;
import com.readingclub.repository.PostRepository;
import com.readingclub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    
    /**
     * 게시글의 댓글 목록 조회 (페이징)
     */
    public CommentDto.CommentListResponse getCommentsByPostId(Long postId, Long currentUserId, Pageable pageable) {
        // 게시글 존재 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        
        // 댓글 목록 조회 (대댓글 제외)
        Page<Comment> comments = commentRepository.findByPostIdAndParentIsNullOrderByCreatedAtAsc(postId, pageable);
        
        // 댓글 개수 조회
        long totalComments = commentRepository.countCommentsByPostId(postId);
        long activeComments = commentRepository.countActiveCommentsByPostId(postId);
        
        // DTO 변환
        Page<CommentDto.Response> commentResponses = comments.map(comment -> 
                convertToResponse(comment, currentUserId));
        
        return CommentDto.CommentListResponse.builder()
                .comments(commentResponses)
                .totalComments(totalComments)
                .activeComments(activeComments)
                .build();
    }
    
    /**
     * 댓글 작성
     */
    public CommentDto.Response createComment(Long postId, Long userId, CommentDto.CreateRequest request) {
        // 게시글 존재 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        Comment.CommentBuilder commentBuilder = Comment.builder()
                .post(post)
                .user(user)
                .content(request.getContent());
        
        // 대댓글인 경우 부모 댓글 확인
        if (request.getParentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));
            
            // 부모 댓글이 같은 게시글의 댓글인지 확인
            if (!parentComment.getPost().getId().equals(postId)) {
                throw new IllegalArgumentException("부모 댓글이 해당 게시글의 댓글이 아닙니다.");
            }
            
            commentBuilder.parent(parentComment);
        }
        
        Comment comment = commentBuilder.build();
        Comment savedComment = commentRepository.save(comment);
        
        log.info("댓글 작성: {} (사용자: {}, 게시글: {})", 
                savedComment.getId(), userId, postId);
        
        return convertToResponse(savedComment, userId);
    }
    
    /**
     * 댓글 삭제
     */
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        
        // 삭제 권한 확인
        if (!comment.canDeleteByUser(userId)) {
            throw new IllegalArgumentException("댓글을 삭제할 권한이 없습니다.");
        }
        
        // 이미 삭제된 댓글인지 확인
        if (comment.getIsDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 댓글입니다.");
        }
        
        // 댓글 삭제 (내용만 변경, 실제 삭제하지 않음)
        comment.delete();
        commentRepository.save(comment);
        
        log.info("댓글 삭제: {} (사용자: {})", commentId, userId);
    }
    
    /**
     * 특정 댓글의 대댓글 목록 조회
     */
    public List<CommentDto.Response> getRepliesByCommentId(Long commentId, Long currentUserId) {
        // 댓글 존재 확인
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        
        // 대댓글 목록 조회
        List<Comment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(commentId);
        
        return replies.stream()
                .map(reply -> convertToResponse(reply, currentUserId))
                .collect(Collectors.toList());
    }
    
    /**
     * 사용자가 작성한 댓글 목록 조회
     */
    public Page<CommentDto.UserCommentResponse> getUserComments(Long userId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        return comments.map(this::convertToUserCommentResponse);
    }
    
    /**
     * 댓글 엔티티를 Response DTO로 변환
     */
    private CommentDto.Response convertToResponse(Comment comment, Long currentUserId) {
        // 대댓글 목록 조회
        List<CommentDto.Response> replies = comment.getReplies().stream()
                .map(reply -> convertToResponse(reply, currentUserId))
                .collect(Collectors.toList());
        
        // 사용자 DTO 생성
        UserDto.Response userDto = UserDto.Response.builder()
                .id(comment.getUser().getId())
                .kakaoId(comment.getUser().getKakaoId())
                .nickname(comment.getUser().getNickname())
                .profileImage(comment.getUser().getProfileImage())
                .createdAt(comment.getUser().getCreatedAt())
                .updatedAt(comment.getUser().getUpdatedAt())
                .build();
        
        return CommentDto.Response.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .isDeleted(comment.getIsDeleted())
                .isReply(comment.isReply())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .replyCount(comment.getReplyCount())
                .canDelete(comment.canDeleteByUser(currentUserId))
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .user(userDto)
                .replies(replies)
                .build();
    }
    
    /**
     * 댓글 엔티티를 UserCommentResponse DTO로 변환
     */
    private CommentDto.UserCommentResponse convertToUserCommentResponse(Comment comment) {
        return CommentDto.UserCommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .isDeleted(comment.getIsDeleted())
                .postId(comment.getPost().getId())
                .postTitle(comment.getPost().getTitle())
                .createdAt(comment.getCreatedAt())
                .build();
    }
} 