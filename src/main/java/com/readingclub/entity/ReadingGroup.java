package com.readingclub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reading_groups")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ReadingGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(nullable = false)
    private Integer maxMembers = 20;

    @Column(nullable = false)
    private Boolean isPublic = true;

    @Column(unique = true, nullable = false, length = 50)
    private String inviteCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupStatus status = GroupStatus.ACTIVE;

    // 📘 추가된 도서 정보
    @Column(nullable = false)
    private String bookTitle;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String publisher;

    private String bookCoverImage;

    // 📅 모임 일시 및 진행 시간
    @Column(nullable = false)
    private LocalDateTime meetingDateTime;

    @Column(nullable = false)
    private int durationHours;

    // 📝 과제 여부
    @Column(nullable = false)
    private boolean hasAssignment;

    // 🧭 온라인/오프라인 여부
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingType meetingType;

    @Column
    private String location;    // 오프라인 장소 (meetingType == OFFLINE)

    @Column
    private String meetingUrl;  // 온라인 주소 (meetingType == ONLINE)

    // 📚 기존 연관관계
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GroupMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MonthlyBook> monthlyBooks = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum GroupStatus {
        ACTIVE,    // 모집 또는 진행 중
        INACTIVE,  // 종료됨
        ARCHIVED   // 보관됨
    }

    // ✅ 편의 메서드들
    public int getCurrentMemberCount() {
        return members != null ? (int) members.stream()
                .filter(member -> member.getStatus() == GroupMember.MemberStatus.ACTIVE)
                .count() : 0;
    }

    public boolean isFull() {
        return getCurrentMemberCount() >= maxMembers;
    }

    public boolean isCreator(User user) {
        return creator != null && creator.getId().equals(user.getId());
    }

    /**
     * 모임이 종료됐는지 확인 (일회성 모임 기준)
     */
    public boolean isExpired() {
        return meetingDateTime.plusHours(durationHours).isBefore(LocalDateTime.now());
    }
}