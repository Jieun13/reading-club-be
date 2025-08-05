package com.readingclub.controller;

import com.readingclub.dto.ApiResponse;
import com.readingclub.dto.AuthDto;
import com.readingclub.dto.UserDto;
import com.readingclub.entity.User;
import com.readingclub.service.AuthService;
import com.readingclub.service.UserService;
import com.readingclub.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 카카오 로그인 콜백 처리
     */
    @GetMapping("/kakao/callback")
    public ResponseEntity<ApiResponse<AuthDto.LoginResponse>> kakaoCallback(
            @RequestParam String code) {
        try {
            AuthDto.LoginResponse response = authService.kakaoLogin(code);
            return ResponseEntity.ok(ApiResponse.success(response, "로그인 성공"));
        } catch (Exception e) {
            log.error("카카오 로그인 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("로그인에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 토큰 갱신
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthDto.LoginResponse>> refreshToken(
            @RequestBody AuthDto.RefreshTokenRequest request) {
        try {
            AuthDto.LoginResponse response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success(response, "토큰 갱신 성공"));
        } catch (IllegalArgumentException e) {
            log.warn("토큰 갱신 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("토큰 갱신 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("토큰 갱신에 실패했습니다."));
        }
    }
    
    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String token) {
        try {
            authService.logout(token);
            return ResponseEntity.ok(ApiResponse.success(null, "로그아웃 성공"));
        } catch (Exception e) {
            log.error("로그아웃 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("로그아웃에 실패했습니다."));
        }
    }
    
    /**
     * 토큰 유효성 검증
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(
            @RequestHeader("Authorization") String bearerToken) {
        try {
            // Bearer 토큰에서 실제 토큰 추출
            String token = bearerToken.startsWith("Bearer ") ? 
                    bearerToken.substring(7) : bearerToken;
            
            // 토큰 유효성 검증 로직은 JWT 필터에서 처리
            // 여기서는 단순히 성공 응답 반환
            return ResponseEntity.ok(ApiResponse.success(true, "유효한 토큰입니다."));
        } catch (Exception e) {
            log.error("토큰 검증 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("유효하지 않은 토큰입니다."));
        }
    }

    @PostMapping("/dev-login")
    public ResponseEntity<ApiResponse<AuthDto.LoginResponse>> devLogin() {

        UserDto.Response testUser;
        try {
            testUser = userService.getUserById(1L);
        } catch (IllegalArgumentException e){
            testUser = userService.createUser(UserDto.CreateRequest.builder()
                    .kakaoId("99999999")
                    .nickname("test_nickname")
                    .profileImage("test_profile_image")
                    .build()
            );
        }

        String accessToken = jwtUtil.generateAccessToken(testUser.getId(), testUser.getKakaoId());
        String refreshToken = jwtUtil.generateRefreshToken(testUser.getId());
        Date expirationDate = jwtUtil.getExpirationFromToken(accessToken);
        LocalDateTime expiresAt = expirationDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        AuthDto.LoginResponse response = AuthDto.LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(testUser)
                .expiresAt(expiresAt)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "개발용 로그인 성공"));
    }
}
