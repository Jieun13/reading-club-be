package com.readingclub.repository;

import com.readingclub.entity.GroupMeeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMeetingRepository extends JpaRepository<GroupMeeting, Long> {
    
    /**
     * 그룹의 모임 목록 조회 (최신순)
     */
    List<GroupMeeting> findByGroupIdOrderByMeetingDateTimeDesc(Long groupId);
    
    /**
     * 그룹의 예정된 모임 목록 조회
     */
    @Query("SELECT gm FROM GroupMeeting gm WHERE gm.group.id = :groupId " +
           "AND gm.meetingDateTime > :now AND gm.status = 'SCHEDULED' " +
           "ORDER BY gm.meetingDateTime ASC")
    List<GroupMeeting> findUpcomingMeetings(@Param("groupId") Long groupId, @Param("now") LocalDateTime now);
    
    /**
     * 그룹의 다음 모임 조회
     */
    @Query("SELECT gm FROM GroupMeeting gm WHERE gm.group.id = :groupId " +
           "AND gm.meetingDateTime > :now AND gm.status = 'SCHEDULED' " +
           "ORDER BY gm.meetingDateTime ASC LIMIT 1")
    Optional<GroupMeeting> findNextMeeting(@Param("groupId") Long groupId, @Param("now") LocalDateTime now);
    
    /**
     * 월간 도서별 모임 목록 조회
     */
    List<GroupMeeting> findByMonthlyBookIdOrderByMeetingDateTimeDesc(Long monthlyBookId);
    
    /**
     * 특정 상태의 모임 목록 조회
     */
    List<GroupMeeting> findByGroupIdAndStatusOrderByMeetingDateTimeDesc(
            Long groupId, GroupMeeting.MeetingStatus status);
    
    /**
     * 사용자가 생성한 모임 목록 조회
     */
    List<GroupMeeting> findByCreatedByIdOrderByMeetingDateTimeDesc(Long userId);
    
    /**
     * 특정 기간의 모임 목록 조회
     */
    @Query("SELECT gm FROM GroupMeeting gm WHERE gm.group.id = :groupId " +
           "AND gm.meetingDateTime BETWEEN :startDate AND :endDate " +
           "ORDER BY gm.meetingDateTime ASC")
    List<GroupMeeting> findMeetingsBetweenDates(
            @Param("groupId") Long groupId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
