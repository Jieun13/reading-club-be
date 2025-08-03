package com.readingclub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_meetings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class GroupMeeting {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ReadingGroup group;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monthly_book_id")
    private MonthlyBook monthlyBook; // 해당 월간 도서 (선택사항)
    
    @Column(nullable = false, length = 200)
    private String title; // 모임 제목
    
    @Column(columnDefinition = "TEXT")
    private String description; // 모임 설명
    
    @Column(nullable = false)
    private LocalDateTime meetingDateTime; // 모임 일시
    
    @Column(length = 500)
    private String location; // 모임 장소 (온라인/오프라인)
    
    @Column(length = 1000)
    private String agenda; // 모임 안건
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MeetingStatus status = MeetingStatus.SCHEDULED;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy; // 모임 생성자
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum MeetingStatus {
        SCHEDULED,  // 예정됨
        ONGOING,    // 진행 중
        COMPLETED,  // 완료됨
        CANCELLED   // 취소됨
    }
    
    // 편의 메서드
    public boolean isPast() {
        return meetingDateTime.isBefore(LocalDateTime.now());
    }
    
    public boolean isUpcoming() {
        return meetingDateTime.isAfter(LocalDateTime.now());
    }
}
