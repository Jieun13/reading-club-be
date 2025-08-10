package com.readingclub.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dropped_books")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DroppedBook {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // 책 정보
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "author")
    private String author;
    
    @Column(name = "isbn")
    private String isbn;
    
    @Column(name = "cover_image")
    private String coverImage;
    
    @Column(name = "publisher")
    private String publisher;
    
    @Column(name = "published_date")
    private String publishedDate;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    // 읽기 정보
    @Column(name = "reading_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReadingType readingType;
    
    @Column(name = "progress_percentage")
    private Integer progressPercentage; // 읽다 만 시점의 진행률
    
    @Column(name = "drop_reason", columnDefinition = "TEXT")
    private String dropReason; // 하차 이유
    
    @Column(name = "started_date")
    private LocalDate startedDate; // 읽기 시작일
    
    @Column(name = "dropped_date", nullable = false)
    private LocalDate droppedDate; // 하차한 날짜
    
    @Column(name = "memo")
    private String memo; // 기타 메모
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum ReadingType {
        PAPER_BOOK("종이책 소장"),
        LIBRARY_RENTAL("도서관 대여"),
        MILLIE("밀리의 서재"),
        E_BOOK("전자책 소장");
        
        private final String displayName;
        
        ReadingType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
