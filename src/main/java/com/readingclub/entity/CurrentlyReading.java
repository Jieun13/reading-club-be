package com.readingclub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "currently_reading")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CurrentlyReading {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(length = 100)
    private String author;
    
    @Column(length = 500)
    private String coverImage;
    
    @Column(length = 100)
    private String publisher;
    
    @Column(length = 20)
    private String publishedDate;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReadingType readingType;
    
    @Column
    private LocalDate dueDate; // 도서관 대여 종료일
    
    @Column(nullable = false)
    private Integer progressPercentage = 0; // 진행률 (0-100)
    
    @Column(columnDefinition = "TEXT")
    private String memo; // 개인 메모
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
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
    
    // 편의 메서드
    public void updateProgress(int progressPercentage) {
        this.progressPercentage = Math.min(100, Math.max(0, progressPercentage));
    }
    
    public boolean isOverdue() {
        return readingType == ReadingType.LIBRARY_RENTAL && 
               dueDate != null && 
               dueDate.isBefore(LocalDate.now());
    }
} 