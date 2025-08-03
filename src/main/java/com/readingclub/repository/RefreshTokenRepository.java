package com.readingclub.repository;

import com.readingclub.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    /**
     * 토큰으로 RefreshToken 조회
     */
    Optional<RefreshToken> findByToken(String token);
    
    /**
     * 사용자별 RefreshToken 목록 조회
     */
    List<RefreshToken> findByUserId(Long userId);
    
    /**
     * 사용자별 유효한 RefreshToken 목록 조회
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.expiresAt > :now")
    List<RefreshToken> findValidTokensByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    /**
     * 사용자별 RefreshToken 모두 삭제 (로그아웃 시)
     */
    void deleteByUserId(Long userId);
    
    /**
     * 만료된 RefreshToken 모두 삭제
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    /**
     * 특정 토큰 삭제
     */
    void deleteByToken(String token);
    
    /**
     * 토큰 존재 여부 확인
     */
    boolean existsByToken(String token);
}
