package com.readingclub.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false)
    private PostType postType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private PostVisibility visibility;
    
    // 책 정보 (JSON으로 저장)
    @Column(name = "book_isbn", nullable = false)
    private String bookIsbn;
    
    @Column(name = "book_title", nullable = false)
    private String bookTitle;
    
    @Column(name = "book_author", nullable = false)
    private String bookAuthor;
    
    @Column(name = "book_publisher")
    private String bookPublisher;
    
    @Column(name = "book_cover")
    private String bookCover;
    
    @Column(name = "book_pub_date")
    private String bookPubDate;
    
    @Column(name = "book_description", columnDefinition = "TEXT")
    private String bookDescription;
    
    // 독후감 필드
    @Column(name = "title")
    private String title;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    // 추천/비추천 필드
    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation_type")
    private RecommendationType recommendationType;
    
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    // 문장 수집 필드 (JSON 배열로 저장)
    @Column(name = "quotes", columnDefinition = "JSON")
    private String quotes; // [{"page": 10, "text": "문장내용"}, {"page": 20, "text": "다른문장"}]
    
    // 하위 호환성을 위한 기존 필드들 (deprecated)
    @Column(name = "quote", columnDefinition = "TEXT")
    private String quote;
    
    @Column(name = "page_number")
    private Integer pageNumber;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();
}
