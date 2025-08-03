package com.readingclub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_reviews",
       uniqueConstraints = @UniqueConstraint(columnNames = {"monthly_book_id", "user_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BookReview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monthly_book_id", nullable = false)
    private MonthlyBook monthlyBook;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer rating = 0; // 별점 (1-5)
    
    @Column(nullable = false, length = 100)
    private String title; // 리뷰 제목
    
    @Column(columnDefinition = "TEXT")
    private String content; // 리뷰 내용
    
    @Column(columnDefinition = "TEXT")
    private String favoriteQuote; // 인상 깊은 구절
    
    @Column(columnDefinition = "TEXT")
    private String recommendation; // 추천 이유
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.DRAFT;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isPublic = true; // 다른 멤버들에게 공개 여부
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum ReviewStatus {
        DRAFT,      // 임시저장
        PUBLISHED,  // 게시됨
        HIDDEN      // 숨김
    }
    
    // 편의 메서드
    public boolean isPublished() {
        return status == ReviewStatus.PUBLISHED;
    }
    
    public boolean isVisible() {
        return isPublic && status == ReviewStatus.PUBLISHED;
    }
}
