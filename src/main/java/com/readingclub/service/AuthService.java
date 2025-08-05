package com.readingclub.service;

import com.readingclub.dto.AuthDto;
import com.readingclub.dto.UserDto;
import com.readingclub.entity.RefreshToken;
import com.readingclub.entity.User;
import com.readingclub.repository.RefreshTokenRepository;
import com.readingclub.repository.UserRepository;
import com.readingclub.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {
    
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final WebClient webClient = WebClient.builder().build();
    
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;
    
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;
    
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;
    
    /**
     * 카카오 로그인 처리
     */
    @Transactional
    public AuthDto.LoginResponse kakaoLogin(String code) {
        try {
            // 1. 카카오 액세스 토큰 획득
            String kakaoAccessToken = getKakaoAccessToken(code);
            
            // 2. 카카오 사용자 정보 조회
            AuthDto.KakaoUserInfo kakaoUserInfo = getKakaoUserInfo(kakaoAccessToken);
            
            // 3. 사용자 조회 또는 생성
            User user = findOrCreateUser(kakaoUserInfo);
            
            // 4. JWT 토큰 생성
            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getKakaoId());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId());
            
            // 5. Refresh Token 저장
            saveRefreshToken(user, refreshToken);
            
            // 6. 응답 생성
            UserDto.Response userDto = convertToUserDto(user);
            Date expirationDate = jwtUtil.getExpirationFromToken(accessToken);
            LocalDateTime expiresAt = expirationDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            
            log.info("카카오 로그인 성공: {}", user.getKakaoId());
            
            return AuthDto.LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(userDto)
                    .expiresAt(expiresAt)
                    .build();
                    
        } catch (Exception e) {
            log.error("카카오 로그인 실패", e);
            throw new RuntimeException("카카오 로그인에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 토큰 갱신
     */
    @Transactional
    public AuthDto.LoginResponse refreshToken(String refreshTokenValue) {
        // 1. Refresh Token 유효성 검증
        if (!jwtUtil.validateToken(refreshTokenValue)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }
        
        // 2. DB에서 Refresh Token 조회
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new IllegalArgumentException("리프레시 토큰을 찾을 수 없습니다."));
        
        // 3. 토큰 만료 확인
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("만료된 리프레시 토큰입니다.");
        }
        
        // 4. 새로운 토큰 생성
        User user = refreshToken.getUser();
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getKakaoId());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());
        
        // 5. 기존 Refresh Token 삭제 후 새로운 토큰 저장
        refreshTokenRepository.delete(refreshToken);
        saveRefreshToken(user, newRefreshToken);
        
        // 6. 응답 생성
        UserDto.Response userDto = convertToUserDto(user);
        Date expirationDate = jwtUtil.getExpirationFromToken(newAccessToken);
        LocalDateTime expiresAt = expirationDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        
        log.info("토큰 갱신 성공: {}", user.getKakaoId());
        
        return AuthDto.LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(userDto)
                .expiresAt(expiresAt)
                .build();
    }
    
    /**
     * 로그아웃
     */
    @Transactional
    public void logout(String bearerToken) {
        try {
            String token = jwtUtil.extractTokenFromHeader(bearerToken);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 해당 사용자의 모든 Refresh Token 삭제
            refreshTokenRepository.deleteByUserId(userId);
            
            log.info("로그아웃 성공: 사용자 ID {}", userId);
        } catch (Exception e) {
            log.error("로그아웃 실패", e);
            throw new RuntimeException("로그아웃에 실패했습니다.");
        }
    }
    
    /**
     * 카카오 액세스 토큰 획득
     */
    private String getKakaoAccessToken(String code) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        log.info("카카오 토큰 요청 시작 - URL: {}, code: {}", tokenUrl, code);
        log.info("client_id: {}, redirect_uri: {}", kakaoClientId, kakaoRedirectUri);

        AuthDto.KakaoTokenResponse response = webClient.post()
                .uri(tokenUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id", kakaoClientId)
                        .with("client_secret", kakaoClientSecret)
                        .with("redirect_uri", kakaoRedirectUri)
                        .with("code", code))
                .retrieve()
                .bodyToMono(AuthDto.KakaoTokenResponse.class)
                .doOnError(error -> log.error("카카오 토큰 요청 중 에러 발생", error))
                .block();

        if (response == null || response.getAccess_token() == null) {
            log.error("카카오 액세스 토큰 획득 실패 - 응답이 null이거나 access_token 없음");
            throw new RuntimeException("카카오 액세스 토큰 획득에 실패했습니다.");
        }

        return response.getAccess_token();
    }
    
    /**
     * 카카오 사용자 정보 조회
     */
    private AuthDto.KakaoUserInfo getKakaoUserInfo(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
        
        AuthDto.KakaoUserInfo userInfo = webClient.get()
                .uri(userInfoUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(AuthDto.KakaoUserInfo.class)
                .block();
        
        if (userInfo == null) {
            throw new RuntimeException("카카오 사용자 정보 조회에 실패했습니다.");
        }
        
        return userInfo;
    }
    
    /**
     * 사용자 조회 또는 생성
     */
    @Transactional
    public User findOrCreateUser(AuthDto.KakaoUserInfo kakaoUserInfo) {
        String kakaoId = kakaoUserInfo.getId().toString();
        
        return userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> {
                    // 새 사용자 생성
                    String nickname = kakaoUserInfo.getKakao_account().getProfile().getNickname();
                    String profileImage = kakaoUserInfo.getKakao_account().getProfile().getProfile_image_url();
                    
                    // 닉네임 중복 처리
                    String uniqueNickname = generateUniqueNickname(nickname);
                    
                    User newUser = User.builder()
                            .kakaoId(kakaoId)
                            .nickname(uniqueNickname)
                            .profileImage(profileImage)
                            .build();
                    
                    User savedUser = userRepository.save(newUser);
                    log.info("새 사용자 생성: {} ({})", savedUser.getNickname(), savedUser.getKakaoId());
                    
                    return savedUser;
                });
    }
    
    /**
     * 중복되지 않는 닉네임 생성
     */
    private String generateUniqueNickname(String baseNickname) {
        String nickname = baseNickname;
        int counter = 1;
        
        while (userRepository.existsByNickname(nickname)) {
            nickname = baseNickname + counter;
            counter++;
        }
        
        return nickname;
    }
    
    /**
     * Refresh Token 저장
     */
    private void saveRefreshToken(User user, String tokenValue) {
        Date expirationDate = jwtUtil.getExpirationFromToken(tokenValue);
        LocalDateTime expiresAt = expirationDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiresAt(expiresAt)
                .build();
        
        refreshTokenRepository.save(refreshToken);
    }
    
    /**
     * User Entity를 UserDto로 변환
     */
    private UserDto.Response convertToUserDto(User user) {
        return UserDto.Response.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
