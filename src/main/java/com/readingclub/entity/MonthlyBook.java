package com.readingclub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "monthly_books",
       uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "year", "month"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MonthlyBook {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ReadingGroup group;
    
    @Column(nullable = false)
    private Integer year;
    
    @Column(nullable = false)
    private Integer month;
    
    @Column(nullable = false, length = 200)
    private String bookTitle;
    
    @Column(length = 100)
    private String bookAuthor;
    
    @Column(length = 500)
    private String bookCoverImage;
    
    @Column(length = 100)
    private String bookPublisher;
    
    @Column(length = 20)
    private String bookPublishedDate;
    
    @Column(columnDefinition = "TEXT")
    private String bookDescription;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_by_user_id", nullable = false)
    private User selectedBy; // 책을 선정한 사용자
    
    @Column(columnDefinition = "TEXT")
    private String selectionReason; // 선정 이유
    
    @Column(nullable = false)
    private LocalDate startDate; // 읽기 시작일
    
    @Column(nullable = false)
    private LocalDate endDate; // 읽기 종료일
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookStatus status = BookStatus.UPCOMING;
    
    @OneToMany(mappedBy = "monthlyBook", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReadingProgress> readingProgresses = new ArrayList<>();
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum BookStatus {
        UPCOMING,   // 예정됨
        READING,    // 읽는 중
        COMPLETED,  // 완료됨
        CANCELLED   // 취소됨
    }
    
    // 편의 메서드
    public String getYearMonth() {
        return String.format("%d년 %d월", year, month);
    }
    
    public boolean isCurrentMonth() {
        LocalDate now = LocalDate.now();
        return year.equals(now.getYear()) && month.equals(now.getMonthValue());
    }
    
    public boolean isActive() {
        return status == BookStatus.READING || status == BookStatus.UPCOMING;
    }
}
