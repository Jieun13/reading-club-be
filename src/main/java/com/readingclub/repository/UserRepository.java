package com.readingclub.repository;

import com.readingclub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 카카오 ID로 사용자 조회
     */
    Optional<User> findByKakaoId(String kakaoId);
    
    /**
     * 카카오 ID 존재 여부 확인
     */
    boolean existsByKakaoId(String kakaoId);
    
    /**
     * 닉네임으로 사용자 조회
     */
    Optional<User> findByNickname(String nickname);
    
    /**
     * 닉네임 존재 여부 확인
     */
    boolean existsByNickname(String nickname);
    
    /**
     * 사용자와 함께 책 목록도 조회 (N+1 문제 해결)
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.books WHERE u.id = :userId")
    Optional<User> findByIdWithBooks(@Param("userId") Long userId);
}
