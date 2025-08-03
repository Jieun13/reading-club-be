package com.readingclub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_members", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "user_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class GroupMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ReadingGroup group;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role = MemberRole.MEMBER;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;
    
    @Column(columnDefinition = "TEXT")
    private String introduction; // 자기소개
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum MemberRole {
        CREATOR,    // 생성자
        ADMIN,      // 관리자
        MEMBER      // 일반 멤버
    }
    
    public enum MemberStatus {
        ACTIVE,     // 활성
        INACTIVE,   // 비활성
        BANNED      // 차단됨
    }
    
    // 편의 메서드
    public boolean isAdmin() {
        return role == MemberRole.CREATOR || role == MemberRole.ADMIN;
    }
    
    public boolean isActive() {
        return status == MemberStatus.ACTIVE;
    }
}
