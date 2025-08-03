package com.readingclub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reading_progresses",
       uniqueConstraints = @UniqueConstraint(columnNames = {"monthly_book_id", "user_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ReadingProgress {
    
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
    private Integer currentPage = 0; // 현재 읽은 페이지
    
    @Column(nullable = false)
    private Integer totalPages = 0; // 전체 페이지 수
    
    @Column(nullable = false)
    private Integer progressPercentage = 0; // 진행률 (0-100)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReadingStatus status = ReadingStatus.NOT_STARTED;
    
    @Column
    private LocalDate startedAt; // 읽기 시작일
    
    @Column
    private LocalDate completedAt; // 완독일
    
    @Column(nullable = false)
    private Integer rating = 0; // 별점 (0-5)
    
    @Column(columnDefinition = "TEXT")
    private String review; // 리뷰
    
    @Column(columnDefinition = "TEXT")
    private String notes; // 개인 메모
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum ReadingStatus {
        NOT_STARTED,    // 시작 안함
        READING,        // 읽는 중
        COMPLETED,      // 완독
        DROPPED         // 중단
    }
    
    // 편의 메서드
    public void updateProgress(int currentPage, int totalPages) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        
        if (totalPages > 0) {
            this.progressPercentage = Math.min(100, (currentPage * 100) / totalPages);
        }
        
        // 상태 자동 업데이트
        if (currentPage == 0) {
            this.status = ReadingStatus.NOT_STARTED;
        } else if (currentPage >= totalPages && totalPages > 0) {
            this.status = ReadingStatus.COMPLETED;
            if (this.completedAt == null) {
                this.completedAt = LocalDate.now();
            }
        } else {
            this.status = ReadingStatus.READING;
            if (this.startedAt == null) {
                this.startedAt = LocalDate.now();
            }
        }
    }
    
    public boolean isCompleted() {
        return status == ReadingStatus.COMPLETED;
    }
    
    public boolean isReading() {
        return status == ReadingStatus.READING;
    }
}
